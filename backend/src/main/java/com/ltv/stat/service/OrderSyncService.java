package com.ltv.stat.service;

import com.ltv.stat.dto.OrderReportRecordDto;
import com.ltv.stat.dto.OrderReportResponseDto;
import com.ltv.stat.entity.RawOrder;
import com.ltv.stat.entity.SubscriptionConfigVersion;
import com.ltv.stat.entity.SystemConfig;
import com.ltv.stat.repository.RawOrderRepository;
import com.ltv.stat.repository.SubscriptionConfigVersionRepository;
import com.ltv.stat.repository.SystemConfigRepository;
import com.ltv.stat.repository.UserSubscriptionPeriodRepository;
import com.ltv.stat.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OrderSyncService {

    private static final Logger log = LoggerFactory.getLogger(OrderSyncService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RestTemplate restTemplate;
    private final RawOrderRepository rawOrderRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final SubscriptionConfigVersionRepository versionRepository;
    private final com.ltv.stat.repository.UserSubscriptionPeriodRepository userSubscriptionPeriodRepository;
    private final Executor syncExecutor;

    @Value("${order.api.url}")
    private String apiUrl;

    @Value("${order.api.authorization}")
    private String defaultAuthorization;

    @Value("${order.api.cookie:JSESSIONID=b959ba11-507c-4f63-8a01-b16ab37f96f4}")
    private String defaultCookie;

    @Value("${order.api.client-group-id}")
    private String clientGroupId;

    public OrderSyncService(RestTemplate restTemplate,
                            RawOrderRepository rawOrderRepository,
                            SystemConfigRepository systemConfigRepository,
                            SubscriptionConfigVersionRepository versionRepository,
                            UserSubscriptionPeriodRepository userSubscriptionPeriodRepository,
                            @Qualifier("orderSyncExecutor") Executor syncExecutor) {
        this.restTemplate = restTemplate;
        this.rawOrderRepository = rawOrderRepository;
        this.systemConfigRepository = systemConfigRepository;
        this.versionRepository = versionRepository;
        this.userSubscriptionPeriodRepository = userSubscriptionPeriodRepository;
        this.syncExecutor = syncExecutor;
    }

    public String getActiveAuthorization() {
        return systemConfigRepository.findById("API_AUTHORIZATION")
                .map(SystemConfig::getConfigValue)
                .filter(val -> !val.trim().isEmpty())
                .orElse(defaultAuthorization);
    }

    public String getActiveCookie() {
        return systemConfigRepository.findById("API_COOKIE")
                .map(SystemConfig::getConfigValue)
                .filter(val -> !val.trim().isEmpty())
                .orElse(defaultCookie);
    }

    public void updateApiToken(String authorization, String cookie) {
        if (authorization != null) {
            SystemConfig config = new SystemConfig();
            config.setConfigKey("API_AUTHORIZATION");
            config.setConfigValue(authorization.trim());
            systemConfigRepository.save(config);
        }
        if (cookie != null) {
            SystemConfig config = new SystemConfig();
            config.setConfigKey("API_COOKIE");
            config.setConfigValue(cookie.trim());
            systemConfigRepository.save(config);
        }
    }

    public Map<String, Object> testFetchTodayOrdersNoPid(String targetDayStr) {
        String currentAuth = getActiveAuthorization();
        String currentCookie = getActiveCookie();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", currentAuth);
        if (!currentCookie.isEmpty()) {
            headers.set("Cookie", currentCookie);
        }
        headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36");

        Map<String, Object> payload = new HashMap<>();
        payload.put("contentType", 4);
        payload.put("clientGroupId", clientGroupId);
        payload.put("pageIndex", 1);
        payload.put("pageSize", 100);
        payload.put("startTime", targetDayStr + " 00:00:00");
        payload.put("endTime", targetDayStr + " 23:59:59");
        payload.put("pId", "");
        payload.put("_", System.currentTimeMillis());

        Map<String, Object> result = new HashMap<>();
        result.put("date", targetDayStr);
        result.put("pIdSent", "");

        try {
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
            ResponseEntity<OrderReportResponseDto> response = restTemplate.postForEntity(apiUrl, requestEntity, OrderReportResponseDto.class);

            result.put("httpStatusCode", response.getStatusCodeValue());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                OrderReportResponseDto body = response.getBody();
                result.put("code", body.getCode());
                result.put("msg", body.getMsg());

                if (body.getData() != null) {
                    result.put("totalRecords", body.getData().getTotal());
                    result.put("totalPages", body.getData().getPages());
                    List<OrderReportRecordDto> records = body.getData().getRecords();
                    result.put("fetchedRecordsCount", records != null ? records.size() : 0);
                    if (records != null && !records.isEmpty()) {
                        result.put("firstRecordSample", records.get(0));
                    }
                }
            } else {
                result.put("error", "Non-2xx status code or null body");
            }
        } catch (Exception e) {
            log.error("Error in testFetchTodayOrdersNoPid for date: {}", targetDayStr, e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 按落地页 ID 维度同步 2026-07-10 至今的所有订单 (多线程按天并发)
     */
    public int syncOrdersForLandingPage(String landingPageId) {
        log.info("Starting order sync for landingPageId: {}", landingPageId);

        LocalDate startDate = LocalDate.of(2026, 7, 10);
        LocalDate endDate = TimeUtils.getTodayEt();

        List<String> targetDays = new ArrayList<>();
        LocalDate curr = startDate;
        while (!curr.isAfter(endDate)) {
            targetDays.add(curr.format(DATE_FORMATTER));
            curr = curr.plusDays(1);
        }

        AtomicInteger totalSavedCount = new AtomicInteger(0);
        AtomicBoolean tokenExpiredFlag = new AtomicBoolean(false);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String dayStr : targetDays) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                if (tokenExpiredFlag.get()) {
                    return;
                }
                try {
                    int count = syncOrdersForSingleDay(landingPageId, dayStr);
                    totalSavedCount.addAndGet(count);
                } catch (RuntimeException re) {
                    if (re.getMessage() != null && re.getMessage().contains("TOKEN_EXPIRED")) {
                        tokenExpiredFlag.set(true);
                        throw re;
                    }
                }
            }, syncExecutor);

            futures.add(future);
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException ce) {
            if (tokenExpiredFlag.get() || (ce.getCause() != null && ce.getCause().getMessage() != null && ce.getCause().getMessage().contains("TOKEN_EXPIRED"))) {
                throw new RuntimeException("TOKEN_EXPIRED: 登录 Token 已过期，请在页面更新最新 Token");
            }
            log.error("Concurrent sync task encountered error for PId: {}", landingPageId, ce);
        }

        log.info("Finished concurrent order sync for PId: {}, total new/updated orders saved: {}", landingPageId, totalSavedCount.get());
        return totalSavedCount.get();
    }

    /**
     * 全量按日期并发同步订单 (指定 [startTimeStr, endTimeStr])
     */
    public int syncOrdersAll(String startTimeStr, String endTimeStr) {
        LocalDate start = parseFlexDate(startTimeStr);
        LocalDate end = parseFlexDate(endTimeStr);
        if (start == null) start = LocalDate.of(2026, 7, 10);
        if (end == null) end = TimeUtils.getTodayEt();

        List<String> targetDays = new ArrayList<>();
        LocalDate curr = start;
        while (!curr.isAfter(end)) {
            targetDays.add(curr.format(DATE_FORMATTER));
            curr = curr.plusDays(1);
        }

        AtomicInteger totalSavedCount = new AtomicInteger(0);
        AtomicBoolean tokenExpiredFlag = new AtomicBoolean(false);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String dayStr : targetDays) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                if (tokenExpiredFlag.get()) return;
                try {
                    int count = syncOrdersForSingleDay("", dayStr);
                    totalSavedCount.addAndGet(count);
                } catch (RuntimeException re) {
                    if (re.getMessage() != null && re.getMessage().contains("TOKEN_EXPIRED")) {
                        tokenExpiredFlag.set(true);
                        throw re;
                    }
                }
            }, syncExecutor);
            futures.add(future);
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException ce) {
            if (tokenExpiredFlag.get() || (ce.getCause() != null && ce.getCause().getMessage() != null && ce.getCause().getMessage().contains("TOKEN_EXPIRED"))) {
                throw new RuntimeException("TOKEN_EXPIRED: 登录 Token 已过期，请在页面更新最新 Token");
            }
            log.error("Concurrent sync task encountered error", ce);
        }

        log.info("Finished syncOrdersAll from {} to {}, saved: {}", startTimeStr, endTimeStr, totalSavedCount.get());
        return totalSavedCount.get();
    }

    private LocalDate parseFlexDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            String[] parts = dateStr.trim().split("[/\\-]");
            if (parts.length == 3) {
                return LocalDate.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            }
            return LocalDate.parse(dateStr.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 单日订单同步 (startTime == endTime == targetDayStr)
     */
    private int syncOrdersForSingleDay(String landingPageId, String targetDayStr) {
        String currentAuth = getActiveAuthorization();
        String currentCookie = getActiveCookie();

        int savedCount = 0;
        int pageIndex = 1;
        int pageSize = 50;
        int totalPages = 1;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", currentAuth);
        if (!currentCookie.isEmpty()) {
            headers.set("Cookie", currentCookie);
        }
        headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36");
        headers.set("Accept", "*/*");

        do {
            Map<String, Object> payload = new HashMap<>();
            payload.put("contentType", 4);
            payload.put("clientGroupId", clientGroupId);
            payload.put("pageIndex", pageIndex);
            payload.put("pageSize", pageSize);
            payload.put("startTime", targetDayStr + " 00:00:00");
            payload.put("endTime", targetDayStr + " 23:59:59");
            payload.put("pId", landingPageId);
            payload.put("_", System.currentTimeMillis());

            try {
                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
                ResponseEntity<OrderReportResponseDto> response = restTemplate.postForEntity(apiUrl, requestEntity, OrderReportResponseDto.class);

                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    log.error("Failed to fetch orders for PId: {}, date: {}, status: {}", landingPageId, targetDayStr, response.getStatusCode());
                    break;
                }

                OrderReportResponseDto root = response.getBody();
                int code = root.getCode() != null ? root.getCode() : -1;
                if (code != 0) {
                    String msg = root.getMsg();
                    if (code == 4002) {
                        log.error("Order API returned token expired (code 4002) for date: {}", targetDayStr);
                        throw new RuntimeException("TOKEN_EXPIRED: 登录 Token 已过期，请在页面更新最新 Token");
                    }
                    log.warn("Order API returned non-zero code: {}, msg: {} for date: {}", code, msg, targetDayStr);
                    break;
                }

                if (root.getData() != null) {
                    totalPages = root.getData().getPages() != null ? root.getData().getPages() : 1;
                    List<OrderReportRecordDto> records = root.getData().getRecords();

                    if (records != null) {
                        for (OrderReportRecordDto record : records) {
                            boolean saved = processAndSaveOrder(record, landingPageId);
                            if (saved) savedCount++;
                        }
                    }
                }
                pageIndex++;

            } catch (RuntimeException re) {
                throw re;
            } catch (Exception e) {
                log.error("Exception occurred while fetching orders for PId: {}, date: {}, page: {}", landingPageId, targetDayStr, pageIndex, e);
                break;
            }
        } while (pageIndex <= totalPages);

        if (savedCount > 0) {
            log.info("Synced day {} for PId: {}, saved orders: {}", targetDayStr, landingPageId, savedCount);
        }

        return savedCount;
    }

    private boolean processAndSaveOrder(OrderReportRecordDto record, String fallbackLandingPageId) {
        String orderId = record.getOrderId();
        if (orderId == null || orderId.trim().isEmpty()) {
            return false;
        }

        String memberId = record.getMemberId();
        String rawPId = record.getLandingPageId();
        String pId = (rawPId != null && !rawPId.trim().isEmpty() && !rawPId.trim().equalsIgnoreCase("null"))
                ? rawPId.trim()
                : (fallbackLandingPageId != null ? fallbackLandingPageId.trim() : "");

        String userCreateTimeStr = record.getUserCreateTime();
        String payDateStr = record.getPayDate();
        int orderAmountCent = record.getOrderAmount() != null ? record.getOrderAmount() : 0;
        int isSubs = record.getIsSubs() != null ? record.getIsSubs() : 0;
        int renewType = record.getRenewType() != null ? record.getRenewType() : 1;
        int payState = record.getPayState() != null ? record.getPayState() : 1;
        int refundStatus = record.getRefundStatus() != null ? record.getRefundStatus() : 0;

        if (payState != 1) {
            return false;
        }

        LocalDateTime regTimeBj = null;
        LocalDateTime payTimeBj = null;
        try {
            if (userCreateTimeStr != null && !userCreateTimeStr.trim().isEmpty()) {
                regTimeBj = LocalDateTime.parse(userCreateTimeStr.trim(), TimeUtils.DATETIME_FORMATTER);
            }
            if (payDateStr != null && !payDateStr.trim().isEmpty()) {
                payTimeBj = LocalDateTime.parse(payDateStr.trim(), TimeUtils.DATETIME_FORMATTER);
            }
        } catch (Exception e) {
            log.warn("Failed to parse date for orderId: {}, userCreateTime: {}, payDate: {}", orderId, userCreateTimeStr, payDateStr);
            return false;
        }

        if (regTimeBj == null || payTimeBj == null) {
            return false;
        }

        ZonedDateTime regEtZdt = TimeUtils.parseBjToEt(userCreateTimeStr);
        ZonedDateTime payEtZdt = TimeUtils.parseBjToEt(payDateStr);

        LocalDateTime regTimeEt = regEtZdt.toLocalDateTime();
        LocalDate regDateEt = regEtZdt.toLocalDate();

        LocalDateTime payTimeEt = payEtZdt.toLocalDateTime();
        LocalDate payDateEt = payEtZdt.toLocalDate();

        BigDecimal orderAmountUsd = BigDecimal.valueOf(orderAmountCent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        Optional<RawOrder> existingOpt = rawOrderRepository.findByOrderId(orderId);
        RawOrder order = existingOpt.orElseGet(RawOrder::new);

        // 保存 raw_order (作为纯交易明细，不保存依赖反演的 subPeriodDays)
        order.setOrderId(orderId);
        order.setMemberId(memberId);
        order.setLandingPageId(pId);
        order.setRegisterTimeBj(regTimeBj);
        order.setRegisterTimeEt(regTimeEt);
        order.setRegisterDateEt(regDateEt);
        order.setPayTimeBj(payTimeBj);
        order.setPayTimeEt(payTimeEt);
        order.setPayDateEt(payDateEt);
        order.setOrderAmountCent(orderAmountCent);
        order.setOrderAmountUsd(orderAmountUsd);
        order.setIsSubs(isSubs);
        order.setRenewType(renewType);
        order.setPayState(payState);
        order.setRefundStatus(refundStatus);

        rawOrderRepository.save(order);

        // 仅当为首次订阅订单 (isSubs == 1 且 renewType == 1) 时，触发保存/更新【用户-订阅周期关联表】(user_subscription_period)
        if (isSubs == 1 && renewType == 1 && memberId != null && !memberId.trim().isEmpty()) {
            saveOrUpdateUserSubscriptionPeriod(memberId, pId, orderAmountCent, regTimeBj);
        }

        return true;
    }

    /**
     * 仅首次订阅调用：根据首次订阅价格反推订阅套餐，维护【用户-订阅周期关联表 (user_subscription_period)】
     * 若未精准匹配，自动选择价格最接近的套餐。
     */
    public void saveOrUpdateUserSubscriptionPeriod(String memberId, String landingPageId, int priceCent, LocalDateTime regTime) {
        if (memberId == null || memberId.trim().isEmpty()) return;

        com.ltv.stat.entity.UserSubscriptionPeriod userSub = userSubscriptionPeriodRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    com.ltv.stat.entity.UserSubscriptionPeriod p = new com.ltv.stat.entity.UserSubscriptionPeriod();
                    p.setMemberId(memberId);
                    return p;
                });

        SubscriptionConfigVersion matchedVer = null;

        // 1. 优先精准匹配 landingPageId + first_price_cent + 生效时间窗
        List<SubscriptionConfigVersion> versions = versionRepository.findMatchingFirstPriceVersions(landingPageId, priceCent, regTime);
        if (!versions.isEmpty()) {
            matchedVer = versions.get(0);
        } else {
            // 2. 落地页内兜底：若未精准匹配，在该落地页生效版本中，选择【首订价格最接近】的套餐
            List<SubscriptionConfigVersion> pageVersions = versionRepository.findMatchingPageVersions(landingPageId, regTime);
            if (!pageVersions.isEmpty()) {
                matchedVer = pageVersions.stream()
                        .min(Comparator.comparingInt(v -> Math.abs((v.getFirstPriceCent() != null ? v.getFirstPriceCent() : 0) - priceCent)))
                        .orElse(null);
            }
        }

        // 3. 全局大盘兜底：查找所有配置版本中首订价格最接近的套餐
        if (matchedVer == null) {
            List<SubscriptionConfigVersion> allVersions = versionRepository.findAll();
            if (!allVersions.isEmpty()) {
                matchedVer = allVersions.stream()
                        .min(Comparator.comparingInt(v -> Math.abs((v.getFirstPriceCent() != null ? v.getFirstPriceCent() : 0) - priceCent)))
                        .orElse(null);
            }
        }

        userSub.setLandingPageId(landingPageId);
        if (matchedVer != null) {
            userSub.setSubscribeConfigId(matchedVer.getSubscribeConfigId());
            userSub.setSubPeriodDays(matchedVer.getSubPeriodDays() != null ? matchedVer.getSubPeriodDays() : 1);
            userSub.setFirstPriceCent(matchedVer.getFirstPriceCent());
            userSub.setRenewPriceCent(matchedVer.getRenewPriceCent());
        } else {
            userSub.setSubPeriodDays(1);
            userSub.setFirstPriceCent(priceCent);
        }
        userSub.setUpdatedAt(LocalDateTime.now());
        userSubscriptionPeriodRepository.save(userSub);
    }

    /**
     * 对全量历史 raw_order 表进行扫描，将所有首次订阅订单 (isSubs = 1 且 renewType = 1) 补全反演并存储至 user_subscription_period 表
     */
    public int backfillUserSubscriptionPeriods() {
        List<RawOrder> firstSubOrders = rawOrderRepository.findAll().stream()
                .filter(o -> o.getIsSubs() != null && o.getIsSubs() == 1 && (o.getRenewType() == null || o.getRenewType() == 1))
                .collect(Collectors.toList());

        int count = 0;
        for (RawOrder o : firstSubOrders) {
            if (o.getMemberId() != null && !o.getMemberId().trim().isEmpty()) {
                saveOrUpdateUserSubscriptionPeriod(o.getMemberId(), o.getLandingPageId(), o.getOrderAmountCent(), o.getRegisterTimeBj());
                count++;
            }
        }
        log.info("Backfilled user_subscription_period table for {} initial subscription users.", count);
        return count;
    }
}
