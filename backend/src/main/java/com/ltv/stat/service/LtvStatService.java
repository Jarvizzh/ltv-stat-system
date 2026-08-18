package com.ltv.stat.service;

import com.ltv.stat.dto.LandingPageConfigItem;
import com.ltv.stat.dto.LtvListResponseDto;
import com.ltv.stat.dto.MonthlySummaryDto;
import com.ltv.stat.dto.PredictionResult;
import com.ltv.stat.dto.RetainedSubscribersDto;
import com.ltv.stat.dto.SingleMonthSummaryDto;
import com.ltv.stat.entity.*;
import com.ltv.stat.repository.*;
import com.ltv.stat.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class LtvStatService {

    private static final Logger log = LoggerFactory.getLogger(LtvStatService.class);
    public static final LocalDate START_DATE = LocalDate.of(2026, 7, 10);

    // =========================================================================
    // 方案一：高性能内存缓存结构与主动失效机制 (Memory Cache & Active Invalidation)
    // =========================================================================
    private static class CachedLtvResponse {
        final LtvListResponseDto data;
        final long timestamp;

        CachedLtvResponse(LtvListResponseDto data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired(long ttlMs) {
            return System.currentTimeMillis() - timestamp > ttlMs;
        }
    }

    private final RawOrderRepository rawOrderRepository;
    private final LtvLaunchConfigRepository ltvLaunchConfigRepository;
    private final LtvDailyStatRepository ltvDailyStatRepository;
    private final DailyRechargeDistributionRepository dailyRechargeDistributionRepository;
    private final UserSubscriptionPeriodRepository userSubscriptionPeriodRepository;
    private final UserService userService;
    private final LtvPredictService ltvPredictService;
    private final OrderSyncService orderSyncService;
    private final LtvBenchmarkService ltvBenchmarkService;

    public LtvStatService(RawOrderRepository rawOrderRepository,
                          LtvLaunchConfigRepository ltvLaunchConfigRepository,
                          LtvDailyStatRepository ltvDailyStatRepository,
                          DailyRechargeDistributionRepository dailyRechargeDistributionRepository,
                          UserSubscriptionPeriodRepository userSubscriptionPeriodRepository,
                          UserService userService,
                          LtvPredictService ltvPredictService,
                          @org.springframework.context.annotation.Lazy OrderSyncService orderSyncService,
                          @org.springframework.beans.factory.annotation.Autowired(required = false) LtvBenchmarkService ltvBenchmarkService) {
        this.rawOrderRepository = rawOrderRepository;
        this.ltvLaunchConfigRepository = ltvLaunchConfigRepository;
        this.ltvDailyStatRepository = ltvDailyStatRepository;
        this.dailyRechargeDistributionRepository = dailyRechargeDistributionRepository;
        this.userSubscriptionPeriodRepository = userSubscriptionPeriodRepository;
        this.userService = userService;
        this.ltvPredictService = ltvPredictService;
        this.orderSyncService = orderSyncService;
        this.ltvBenchmarkService = ltvBenchmarkService;
    }

    public LtvLaunchConfig saveLaunchConfig(Long userId, LocalDate launchDate, BigDecimal spend, String remark) {
        if (userId == null) userId = 1L;
        if (userService.isMasterAccount(userId)) {
            throw new IllegalArgumentException("主账号为数据汇总账号，消耗由关联子账号自动计算，不可直接编辑！");
        }
        final Long uid = userId;
        LtvLaunchConfig config = ltvLaunchConfigRepository.findByUserIdAndLaunchDate(uid, launchDate).orElseGet(() -> {
            LtvLaunchConfig c = new LtvLaunchConfig();
            c.setUserId(uid);
            c.setLaunchDate(launchDate);
            return c;
        });
        config.setUserId(uid);
        config.setLaunchDate(launchDate);
        if (spend != null) config.setSpend(spend);
        if (remark != null) config.setRemark(remark);
        LtvLaunchConfig saved = ltvLaunchConfigRepository.save(config);
        
        // 触发所属主账号的报表重算
        List<Long> parentMasterIds = userService.getMasterUserIdsForSub(uid);
        for (Long masterId : parentMasterIds) {
            calculateLtvStatsForUser(masterId);
        }
        return saved;
    }

    public LtvLaunchConfig saveLaunchConfig(LocalDate launchDate, BigDecimal spend, String remark) {
        return saveLaunchConfig(1L, launchDate, spend, remark);
    }

    @Transactional
    public int batchSaveLaunchConfig(Long userId, List<Map<String, Object>> items) {
        if (userId == null) userId = 1L;
        if (userService.isMasterAccount(userId)) {
            throw new IllegalArgumentException("主账号为数据汇总账号，消耗由关联子账号自动计算，不可直接导入！");
        }
        int count = 0;
        for (Map<String, Object> item : items) {
            String dateStr = (String) item.get("launchDate");
            Object spendObj = item.get("spend");
            String remark = (String) item.get("remark");

            if (dateStr == null || dateStr.trim().isEmpty()) continue;
            LocalDate launchDate = parseFlexDate(dateStr);
            if (launchDate == null) continue;
            BigDecimal spend = BigDecimal.ZERO;
            if (spendObj != null) {
                try {
                    spend = new BigDecimal(spendObj.toString().trim());
                } catch (Exception ignored) {}
            }

            saveLaunchConfig(userId, launchDate, spend, remark);
            count++;
        }
        ltvLaunchConfigRepository.flush();
        calculateLtvStatsForUser(userId);
        
        // 触发所属主账号的报表重算
        List<Long> parentMasterIds = userService.getMasterUserIdsForSub(userId);
        for (Long masterId : parentMasterIds) {
            calculateLtvStatsForUser(masterId);
        }
        return count;
    }

    /**
     * 提取订单的生效注册日期 (按落地页 ID 对应的时区配置区分美东与北京时间，未标注默认北京时间)
     */
    public static LocalDate getEffectiveRegisterDate(RawOrder order, Map<String, String> tzMap) {
        if (order == null) return null;
        String pid = order.getLandingPageId() != null ? order.getLandingPageId().trim() : "";
        String tz = (tzMap != null && tzMap.containsKey(pid)) ? tzMap.get(pid) : "BJ";
        if ("ET".equalsIgnoreCase(tz)) {
            return order.getRegisterDateEt();
        }
        return order.getRegisterTimeBj() != null ? order.getRegisterTimeBj().toLocalDate() : order.getRegisterDateEt();
    }

    public static LocalDate parseFlexDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            String s = dateStr.trim().replace('.', '-').replace('/', '-');
            String[] parts = s.split("-");
            if (parts.length == 3) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                return LocalDate.of(year, month, day);
            }
            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDate getEffectiveRegisterDate(RawOrder order) {
        return getEffectiveRegisterDate(order, null);
    }

    /**
     * 提取订单的生效支付日期 (按落地页 ID 对应的时区配置区分美东与北京时间，未标注默认北京时间)
     */
    public static LocalDate getEffectivePayDate(RawOrder order, Map<String, String> tzMap) {
        if (order == null) return null;
        String pid = order.getLandingPageId() != null ? order.getLandingPageId().trim() : "";
        String tz = (tzMap != null && tzMap.containsKey(pid)) ? tzMap.get(pid) : "BJ";
        if ("ET".equalsIgnoreCase(tz)) {
            return order.getPayDateEt();
        }
        return order.getPayTimeBj() != null ? order.getPayTimeBj().toLocalDate() : order.getPayDateEt();
    }

    public static LocalDate getEffectivePayDate(RawOrder order) {
        return getEffectivePayDate(order, null);
    }

    /**
     * 提取订单的北京时间注册日期 (每日充值分析全部使用北京时间)
     */
    public static LocalDate getBjRegisterDate(RawOrder order) {
        if (order == null) return null;
        return order.getRegisterTimeBj() != null ? order.getRegisterTimeBj().toLocalDate() : order.getRegisterDateEt();
    }

    /**
     * 提取订单的北京时间支付日期 (每日充值分析全部使用北京时间)
     */
    public static LocalDate getBjPayDate(RawOrder order) {
        if (order == null) return null;
        return order.getPayTimeBj() != null ? order.getPayTimeBj().toLocalDate() : order.getPayDateEt();
    }



    /**
     * 根据特定用户配置的落地页 ID 过滤原始订单 (走数据库 landing_page_id 索引高效查询)
     */
    public List<RawOrder> getOrdersFilteredForUser(Long userId) {
        if (userId == null) userId = 1L;
        List<String> userPIds = userService.getUserLandingPageIds(userId);

        if (userPIds == null || userPIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> trimmedPIds = userPIds.stream()
                .filter(p -> p != null && !p.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        if (trimmedPIds.isEmpty()) {
            return Collections.emptyList();
        }

        return rawOrderRepository.findByLandingPageIdIn(trimmedPIds);
    }

    /**
     * 计算并持久化指定用户的 LTV 统计表
     */
    @Transactional
    public void calculateLtvStatsForUser(Long userId) {
        if (userId == null) userId = 1L;
        invalidateUserCache(userId);
        LocalDate todayBj = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        LocalDate todayEt = ZonedDateTime.now(TimeUtils.EASTERN_ZONE).toLocalDate();
        LocalDate maxToday = todayBj.isAfter(todayEt) ? todayBj : todayEt;

        List<LandingPageConfigItem> userPages = userService.getUserLandingPageConfigs(userId);
        Map<String, String> tzMap = userPages.stream()
                .filter(p -> p.getLandingPageId() != null)
                .collect(Collectors.toMap(p -> p.getLandingPageId().trim(), LandingPageConfigItem::getTimezone, (a, b) -> a));

        List<RawOrder> orders = getOrdersFilteredForUser(userId);

        if (ltvBenchmarkService != null) {
            ltvBenchmarkService.recalculateBenchmarksForUser(userId);
        }

        if (userSubscriptionPeriodRepository.count() == 0 && orderSyncService != null) {
            orderSyncService.backfillUserSubscriptionPeriods();
        }

        Map<LocalDate, List<RawOrder>> ordersByDate = orders.stream()
                .filter(o -> {
                    LocalDate regDate = getEffectiveRegisterDate(o, tzMap);
                    return regDate != null && !regDate.isBefore(START_DATE);
                })
                .collect(Collectors.groupingBy(o -> getEffectiveRegisterDate(o, tzMap)));

        Map<LocalDate, LtvLaunchConfig> configsByDate;
        String masterRemark = "";
        boolean isMasterAcc = userService.isMasterAccount(userId);
        if (isMasterAcc) {
            List<Long> subUserIds = userService.getSubUserIdsForMaster(userId);
            List<String> subUsernames = new ArrayList<>();
            for (Long subId : subUserIds) {
                userService.findById(subId).ifPresent(u -> subUsernames.add(u.getUsername()));
            }
            String subNamesStr = String.join("、", subUsernames);
            masterRemark = subNamesStr.isEmpty() ? "汇总数据" : "汇总数据（子账号：" + subNamesStr + "）";

            Map<LocalDate, BigDecimal> sumSpendMap = new HashMap<>();
            for (Long subId : subUserIds) {
                List<LtvLaunchConfig> subConfigs = ltvLaunchConfigRepository.findByUserId(subId);
                for (LtvLaunchConfig sc : subConfigs) {
                    if (sc.getLaunchDate() != null && sc.getSpend() != null) {
                        sumSpendMap.merge(sc.getLaunchDate(), sc.getSpend(), BigDecimal::add);
                    }
                }
            }
            configsByDate = new HashMap<>();
            for (Map.Entry<LocalDate, BigDecimal> entry : sumSpendMap.entrySet()) {
                LtvLaunchConfig mc = new LtvLaunchConfig();
                mc.setUserId(userId);
                mc.setLaunchDate(entry.getKey());
                mc.setSpend(entry.getValue());
                mc.setRemark(masterRemark);
                configsByDate.put(entry.getKey(), mc);
            }
        } else {
            configsByDate = ltvLaunchConfigRepository.findByUserId(userId).stream()
                    .collect(Collectors.toMap(LtvLaunchConfig::getLaunchDate, c -> c));
        }

        LocalDate currDate = START_DATE;
        List<LtvDailyStat> statList = new ArrayList<>();

        while (!currDate.isAfter(maxToday)) {
            List<RawOrder> cohortOrders = ordersByDate.getOrDefault(currDate, Collections.emptyList());
            LtvLaunchConfig launchConfig = configsByDate.get(currDate);

            BigDecimal spend = launchConfig != null ? launchConfig.getSpend() : BigDecimal.ZERO;
            String remark = isMasterAcc ? masterRemark : (launchConfig != null ? launchConfig.getRemark() : "");

            LtvDailyStat stat = calculateSingleCohort(userId, currDate, cohortOrders, spend, remark, maxToday, tzMap);
            statList.add(stat);
            currDate = currDate.plusDays(1);
        }

        ltvDailyStatRepository.deleteByUserId(userId);
        ltvDailyStatRepository.flush();
        ltvDailyStatRepository.saveAll(statList);
        ltvDailyStatRepository.flush();

        invalidateUserCache(userId);

        // 触发所属主账号的 LTV 报表同步重算
        List<Long> parentMasterIds = userService.getMasterUserIdsForSub(userId);
        for (Long masterId : parentMasterIds) {
            calculateLtvStatsForUser(masterId);
        }
        invalidateUserCache(userId);
    }

    /**
     * 重新计算所有用户的 LTV 统计表 (定时任务调用)
     */
    @Transactional
    public void calculateAllLtvStatsOnly() {
        List<SysUser> users = userService.listAllUsers();
        if (users.isEmpty()) {
            calculateLtvStatsForUser(1L);
        } else {
            for (SysUser user : users) {
                calculateLtvStatsForUser(user.getId());
            }
        }
        log.info("LTV calculation completed for all active users.");
    }

    @Transactional
    public void calculateAllLtvStats() {
        calculateAllLtvStatsOnly();
    }

    private LtvDailyStat calculateSingleCohort(Long userId, LocalDate launchDate, List<RawOrder> cohortOrders, BigDecimal spend, String remark, LocalDate maxToday, Map<String, String> tzMap) {
        LtvDailyStat stat = new LtvDailyStat();
        stat.setUserId(userId);
        stat.setLaunchDate(launchDate);
        stat.setSpend(spend != null ? spend : BigDecimal.ZERO);
        stat.setRemark(remark != null ? remark : "");

        // 1. 总充值
        BigDecimal totalRecharge = cohortOrders.stream()
                .map(o -> o.getOrderAmountUsd() != null ? o.getOrderAmountUsd() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 1.1 已退款 (refundStatus == 2)
        BigDecimal totalRefund = cohortOrders.stream()
                .filter(o -> o.getRefundStatus() != null && o.getRefundStatus() == 2)
                .map(o -> o.getOrderAmountUsd() != null ? o.getOrderAmountUsd() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. 总盈亏 = 累计充值 - 已退款 - 消耗
        BigDecimal totalProfit = totalRecharge.subtract(totalRefund).subtract(stat.getSpend());

        // 2.1 累计 ROI = (累计充值 - 已退款) / 消耗
        BigDecimal totalRoi = BigDecimal.ZERO;
        if (stat.getSpend().compareTo(BigDecimal.ZERO) > 0) {
            totalRoi = totalRecharge.subtract(totalRefund).divide(stat.getSpend(), 4, RoundingMode.HALF_UP);
        }

        // 3. 累计订阅用户数
        List<RawOrder> subOrders = cohortOrders.stream()
                .filter(o -> o.getIsSubs() != null && o.getIsSubs() == 1)
                .collect(Collectors.toList());

        Set<String> subMemberIds = subOrders.stream()
                .map(RawOrder::getMemberId)
                .filter(id -> id != null && !id.trim().isEmpty())
                .collect(Collectors.toSet());

        long subUserCount = subMemberIds.size();

        // 3.0 从 user_subscription_period 表按 subMemberIds 批量关联查询该 Cohort 订阅用户的订阅周期天数
        Integer detectedPeriod = 1;
        if (!subMemberIds.isEmpty()) {
            List<UserSubscriptionPeriod> userSubPeriods = userSubscriptionPeriodRepository.findByMemberIdIn(subMemberIds);
            if (!userSubPeriods.isEmpty()) {
                Map<Integer, Long> periodCountMap = userSubPeriods.stream()
                        .filter(p -> p.getSubPeriodDays() != null)
                        .collect(Collectors.groupingBy(UserSubscriptionPeriod::getSubPeriodDays, Collectors.counting()));
                detectedPeriod = periodCountMap.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(1);

                StringBuilder sb = new StringBuilder("{");
                boolean first = true;
                for (Map.Entry<Integer, Long> entry : periodCountMap.entrySet()) {
                    if (!first) sb.append(",");
                    sb.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
                    first = false;
                }
                sb.append("}");
                stat.setSubPeriodDistribution(sb.toString());
            } else {
                stat.setSubPeriodDistribution(null);
            }
        } else {
            stat.setSubPeriodDistribution(null);
        }
        stat.setSubPeriodDays(detectedPeriod);

        // 3.1 订阅用户 7 日留存（> 7天后仍有充值的去重人数）
        LocalDate day8Date = launchDate.plusDays(7);
        if (!day8Date.isAfter(maxToday)) {
            Set<String> retainedMemberIds = cohortOrders.stream()
                    .filter(o -> o.getMemberId() != null && subMemberIds.contains(o.getMemberId()))
                    .filter(o -> {
                        LocalDate payDate = getEffectivePayDate(o, tzMap);
                        if (payDate == null) return false;
                        long diff = ChronoUnit.DAYS.between(launchDate, payDate);
                        return diff >= 7;
                    })
                    .map(RawOrder::getMemberId)
                    .collect(Collectors.toSet());

            long day7SubCount = retainedMemberIds.size();
            BigDecimal retention = BigDecimal.ZERO;
            if (subUserCount > 0) {
                retention = BigDecimal.valueOf(day7SubCount)
                        .divide(BigDecimal.valueOf(subUserCount), 4, RoundingMode.HALF_UP);
            }
            stat.setDay7SubUserCount((int) day7SubCount);
            stat.setDay7SubUserRetention(retention);
        } else {
            stat.setDay7SubUserCount(null);
            stat.setDay7SubUserRetention(null);
        }

        // 3.2 订阅用户 15 日留存（> 15天后仍有充值的去重人数）
        LocalDate day16Date = launchDate.plusDays(15);
        if (!day16Date.isAfter(maxToday)) {
            Set<String> retainedMemberIds15 = cohortOrders.stream()
                    .filter(o -> o.getMemberId() != null && subMemberIds.contains(o.getMemberId()))
                    .filter(o -> {
                        LocalDate payDate = getEffectivePayDate(o, tzMap);
                        if (payDate == null) return false;
                        long diff = ChronoUnit.DAYS.between(launchDate, payDate);
                        return diff >= 15;
                    })
                    .map(RawOrder::getMemberId)
                    .collect(Collectors.toSet());

            long day15SubCount = retainedMemberIds15.size();
            BigDecimal retention15 = BigDecimal.ZERO;
            if (subUserCount > 0) {
                retention15 = BigDecimal.valueOf(day15SubCount)
                        .divide(BigDecimal.valueOf(subUserCount), 4, RoundingMode.HALF_UP);
            }
            stat.setDay15SubUserCount((int) day15SubCount);
            stat.setDay15SubUserRetention(retention15);
        } else {
            stat.setDay15SubUserCount(null);
            stat.setDay15SubUserRetention(null);
        }

        // 4. 订阅用户成本
        BigDecimal subUserCost = BigDecimal.ZERO;
        if (subUserCount > 0 && stat.getSpend().compareTo(BigDecimal.ZERO) > 0) {
            subUserCost = stat.getSpend().divide(BigDecimal.valueOf(subUserCount), 2, RoundingMode.HALF_UP);
        }

        stat.setTotalRecharge(totalRecharge);
        stat.setTotalRefund(totalRefund);
        stat.setTotalProfit(totalProfit);
        stat.setTotalRoi(totalRoi);
        stat.setSubUserCount((int) subUserCount);
        stat.setSubUserCost(subUserCost);

        // 5. Day 1 ~ Day 60 充值与 ROI 计算
        for (int day = 1; day <= 60; day++) {
            LocalDate dayTargetDate = launchDate.plusDays(day - 1);
            if (dayTargetDate.isAfter(maxToday)) {
                stat.setDayData(day, null, null);
                continue;
            }

            BigDecimal dayCumRecharge = cohortOrders.stream()
                    .filter(o -> {
                        LocalDate payDate = getEffectivePayDate(o, tzMap);
                        return payDate != null && !payDate.isAfter(dayTargetDate);
                    })
                    .map(o -> o.getOrderAmountUsd() != null ? o.getOrderAmountUsd() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal dayCumRoi = null;
            if (stat.getSpend().compareTo(BigDecimal.ZERO) > 0) {
                dayCumRoi = dayCumRecharge.divide(stat.getSpend(), 4, RoundingMode.HALF_UP);
            }

            stat.setDayData(day, dayCumRecharge, dayCumRoi);
        }

        // 6. 回本预测计算 (最小二乘法对数拟合)
        long daysElapsed = ChronoUnit.DAYS.between(launchDate, maxToday) + 1;
        PredictionResult pred = ltvPredictService.predictCohort(stat, (int) daysElapsed);
        stat.setPredictedPaybackDays(pred.getPredictedPaybackDays());
        stat.setPredictedDay30Roi(pred.getPredictedDay30Roi());
        stat.setPredictedDay90Roi(pred.getPredictedDay90Roi());
        stat.setPredictedDay60Roi(pred.getPredictedDay60Roi());
        stat.setPredictedDay30Recharge(pred.getPredictedDay30Recharge());
        stat.setPredictedDay60Recharge(pred.getPredictedDay60Recharge());
        stat.setPredictedDay90Recharge(pred.getPredictedDay90Recharge());

        return stat;
    }

    @Transactional
    public List<LtvDailyStat> getLtvDailyStats(Long userId) {
        if (userId == null) userId = 1L;
        List<LtvDailyStat> list = ltvDailyStatRepository.findByUserIdAndLaunchDateGreaterThanEqualOrderByLaunchDateAsc(userId, START_DATE);
        if (list.isEmpty()) {
            calculateLtvStatsForUser(userId);
            list = ltvDailyStatRepository.findByUserIdAndLaunchDateGreaterThanEqualOrderByLaunchDateAsc(userId, START_DATE);
        }
        return list;
    }

    public List<LtvDailyStat> getLtvDailyStats() {
        return getLtvDailyStats(1L);
    }

    /**
     * 计算指定用户的整体全盘预测回本天数
     */
    public Integer getOverallPredictedPaybackDays(Long userId) {
        List<LtvDailyStat> list = getLtvDailyStats(userId);
        PredictionResult pred = ltvPredictService.predictOverallCohort(list);
        return pred != null ? pred.getPredictedPaybackDays() : null;
    }

    public PredictionResult getOverallPredictionResult(Long userId) {
        List<LtvDailyStat> list = getLtvDailyStats(userId);
        return ltvPredictService.predictOverallCohort(list);
    }

    private final Map<Long, CachedLtvResponse> ltvListResponseCache = new ConcurrentHashMap<>();

    public void invalidateUserCache(Long userId) {
        if (userId != null) {
            ltvListResponseCache.remove(userId);
        } else {
            ltvListResponseCache.clear();
        }
    }

    /**
     * 方案一 + 方案二入口：带内存缓存与 Context 共享的极速 LTV 响应获取方法
     */
    public LtvListResponseDto getLtvListResponse(Long userId) {
        if (userId == null) userId = 1L;
        CachedLtvResponse cached = ltvListResponseCache.get(userId);
        if (cached != null && !cached.isExpired(10 * 60 * 1000L)) {
            return cached.data;
        }

        LtvListResponseDto freshResponse = computeLtvListResponse(userId);
        ltvListResponseCache.put(userId, new CachedLtvResponse(freshResponse));
        return freshResponse;
    }

    public LtvListResponseDto computeLtvListResponse(Long userId) {
        if (userId == null) userId = 1L;
        List<LtvDailyStat> list = getLtvDailyStats(userId);
        if (list.isEmpty()) {
            calculateLtvStatsForUser(userId);
            list = getLtvDailyStats(userId);
        }

        List<RawOrder> userOrders = getOrdersFilteredForUser(userId);
        List<LandingPageConfigItem> userPages = userService.getUserLandingPageConfigs(userId);
        Map<String, String> tzMap = (userPages != null) ? userPages.stream()
                .filter(p -> p.getLandingPageId() != null)
                .collect(Collectors.toMap(p -> p.getLandingPageId().trim(), LandingPageConfigItem::getTimezone, (a, b) -> a)) : Collections.emptyMap();

        UserCalculationContext ctx = new UserCalculationContext(userId, list, userOrders, tzMap);

        PredictionResult overallPred = ltvPredictService.predictOverallCohort(list);
        Integer overallPayback = overallPred != null ? overallPred.getPredictedPaybackDays() : null;
        Integer overallPaybackCycle = overallPred != null ? overallPred.getPaybackCycleDays() : null;

        MonthlySummaryDto monthlySummary = getMonthlySummaryForUser(userId, ctx);
        RetainedSubscribersDto overallRetention = calculateRetainedSubscribers(userId, userOrders, ctx);

        LtvListResponseDto response = new LtvListResponseDto();
        response.setCode(0);
        response.setMsg("success");
        response.setData(list);
        response.setOverallPredictedPaybackDays(overallPayback);
        response.setOverallPaybackCycleDays(overallPaybackCycle);
        if (overallPred != null) {
            response.setOverallPredictedDay30Roi(overallPred.getPredictedDay30Roi());
            response.setOverallPredictedDay60Roi(overallPred.getPredictedDay60Roi());
            response.setOverallPredictedDay90Roi(overallPred.getPredictedDay90Roi());
            response.setOverallPredictedDay30Recharge(overallPred.getPredictedDay30Recharge());
            response.setOverallPredictedDay60Recharge(overallPred.getPredictedDay60Recharge());
            response.setOverallPredictedDay90Recharge(overallPred.getPredictedDay90Recharge());
        }
        response.setMonthlySummary(monthlySummary);
        response.setOverallRetainedSubUsers(overallRetention != null ? overallRetention.getRetainedSubUsers() : 0);
        response.setOverallRetainedRate(overallRetention != null ? overallRetention.getRetainedRate() : "0.00%");
        response.setTotal(list.size());
        response.setUserId(userId);
        return response;
    }

    public MonthlySummaryDto getMonthlySummaryForUser(Long userId) {
        if (userId == null) userId = 1L;
        List<LtvDailyStat> allStats = ltvDailyStatRepository.findByUserIdOrderByLaunchDateAsc(userId);
        List<RawOrder> userOrders = getOrdersFilteredForUser(userId);
        List<LandingPageConfigItem> userPages = userService.getUserLandingPageConfigs(userId);
        Map<String, String> tzMap = (userPages != null) ? userPages.stream()
                .filter(p -> p.getLandingPageId() != null)
                .collect(Collectors.toMap(p -> p.getLandingPageId().trim(), LandingPageConfigItem::getTimezone, (a, b) -> a)) : Collections.emptyMap();

        UserCalculationContext ctx = new UserCalculationContext(userId, allStats, userOrders, tzMap);
        return getMonthlySummaryForUser(userId, ctx);
    }

    public MonthlySummaryDto getMonthlySummaryForUser(Long userId, UserCalculationContext ctx) {
        LocalDate todayBj = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        YearMonth thisMonth = YearMonth.from(todayBj);
        YearMonth lastMonth = thisMonth.minusMonths(1);

        SingleMonthSummaryDto thisMonthSummary = getSingleMonthSummary(userId, thisMonth, false, ctx);
        SingleMonthSummaryDto lastMonthSummary = getSingleMonthSummary(userId, lastMonth, true, ctx);

        return new MonthlySummaryDto(thisMonthSummary, lastMonthSummary);
    }

    public RetainedSubscribersDto calculateRetainedSubscribers(Long userId, List<RawOrder> orders, UserCalculationContext ctx) {
        return calculateRetainedSubscribers(userId, orders, ctx != null ? ctx.tzMap : null);
    }

    public RetainedSubscribersDto calculateRetainedSubscribers(Long userId, List<RawOrder> orders) {
        return calculateRetainedSubscribers(userId, orders, (Map<String, String>) null);
    }

    public RetainedSubscribersDto calculateRetainedSubscribers(Long userId, List<RawOrder> orders, Map<String, String> passedTzMap) {
        if (orders == null || orders.isEmpty()) {
            return new RetainedSubscribersDto(0, 0, "0.00%");
        }

        List<RawOrder> subOrders = orders.stream()
                .filter(o -> o.getIsSubs() != null && o.getIsSubs() == 1)
                .collect(Collectors.toList());

        Set<String> subMemberIds = subOrders.stream()
                .map(RawOrder::getMemberId)
                .filter(id -> id != null && !id.trim().isEmpty())
                .collect(Collectors.toSet());

        int totalSubCount = subMemberIds.size();
        if (totalSubCount == 0) {
            return new RetainedSubscribersDto(0, 0, "0.00%");
        }

        List<UserSubscriptionPeriod> subPeriods = userSubscriptionPeriodRepository.findByMemberIdIn(subMemberIds);
        Map<String, Integer> periodMap = subPeriods.stream()
                .filter(p -> p.getMemberId() != null && p.getSubPeriodDays() != null)
                .collect(Collectors.toMap(p -> p.getMemberId().trim(), UserSubscriptionPeriod::getSubPeriodDays, (a, b) -> a));

        Map<String, String> tzMap = passedTzMap;
        if (tzMap == null) {
            List<LandingPageConfigItem> userPages = userService.getUserLandingPageConfigs(userId);
            tzMap = (userPages != null) ? userPages.stream()
                    .filter(p -> p.getLandingPageId() != null)
                    .collect(Collectors.toMap(p -> p.getLandingPageId().trim(), LandingPageConfigItem::getTimezone, (a, b) -> a)) : Collections.emptyMap();
        }

        LocalDate todayBj = LocalDate.now(ZoneId.of("Asia/Shanghai"));

        Map<String, LocalDate> lastPayDateMap = new HashMap<>();
        for (RawOrder o : orders) {
            if (o.getMemberId() == null || !subMemberIds.contains(o.getMemberId())) continue;
            LocalDate payDate = getEffectivePayDate(o, tzMap);
            if (payDate == null) continue;

            LocalDate prev = lastPayDateMap.get(o.getMemberId());
            if (prev == null || payDate.isAfter(prev)) {
                lastPayDateMap.put(o.getMemberId(), payDate);
            }
        }

        int retainedCount = 0;
        for (String memberId : subMemberIds) {
            LocalDate lastPay = lastPayDateMap.get(memberId);
            if (lastPay == null) continue;

            int periodDays = periodMap.getOrDefault(memberId, 1);
            LocalDate expireDate = lastPay.plusDays(periodDays);
            if (!todayBj.isAfter(expireDate)) {
                retainedCount++;
            }
        }

        BigDecimal rate = totalSubCount > 0
                ? BigDecimal.valueOf(retainedCount).divide(BigDecimal.valueOf(totalSubCount), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return new RetainedSubscribersDto(totalSubCount, retainedCount, rate.setScale(2, RoundingMode.HALF_UP).toString() + "%");
    }

    public SingleMonthSummaryDto getSingleMonthSummary(Long userId, YearMonth yearMonth, boolean isLastMonth, List<LtvDailyStat> allStats, List<RawOrder> userOrders) {
        List<LandingPageConfigItem> userPages = userService.getUserLandingPageConfigs(userId);
        Map<String, String> tzMap = (userPages != null) ? userPages.stream()
                .filter(p -> p.getLandingPageId() != null)
                .collect(Collectors.toMap(p -> p.getLandingPageId().trim(), LandingPageConfigItem::getTimezone, (a, b) -> a)) : Collections.emptyMap();
        UserCalculationContext ctx = new UserCalculationContext(userId, allStats, userOrders, tzMap);
        return getSingleMonthSummary(userId, yearMonth, isLastMonth, ctx);
    }

    public SingleMonthSummaryDto getSingleMonthSummary(Long userId, YearMonth yearMonth, boolean isLastMonth, UserCalculationContext ctx) {
        String monthStr = yearMonth.toString();

        List<LtvDailyStat> monthStats = ctx.allStats.stream()
                .filter(s -> s.getLaunchDate() != null && YearMonth.from(s.getLaunchDate()).equals(yearMonth))
                .collect(Collectors.toList());

        BigDecimal totalSpend = monthStats.stream()
                .map(LtvDailyStat::getSpend)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRecharge = monthStats.stream()
                .map(LtvDailyStat::getTotalRecharge)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRefund = monthStats.stream()
                .map(LtvDailyStat::getTotalRefund)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal profit = totalRecharge.subtract(totalRefund).subtract(totalSpend);

        BigDecimal roi = totalSpend.compareTo(BigDecimal.ZERO) > 0
                ? totalRecharge.subtract(totalRefund).divide(totalSpend, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        List<RawOrder> monthOrders = ctx.userOrders.stream()
                .filter(o -> {
                    LocalDate regDate = getEffectiveRegisterDate(o, ctx.tzMap);
                    return regDate != null && YearMonth.from(regDate).equals(yearMonth);
                })
                .collect(Collectors.toList());

        RetainedSubscribersDto retentionInfo = calculateRetainedSubscribers(userId, monthOrders, ctx);

        // 仅上月统计实际回本天数与进行 D30/D60/D90 ROI 预测，本月不作预测
        Integer actualPaybackDays = isLastMonth ? calculateActualPaybackDaysForMonth(userId, yearMonth, monthStats, totalSpend, totalRecharge, ctx) : null;

        BigDecimal predictedDay30Roi = null;
        BigDecimal predictedDay60Roi = null;
        BigDecimal predictedDay90Roi = null;

        if (isLastMonth && totalSpend.compareTo(BigDecimal.ZERO) > 0 && !monthStats.isEmpty()) {
            BigDecimal sumD30Recharge = BigDecimal.ZERO;
            BigDecimal sumD60Recharge = BigDecimal.ZERO;
            BigDecimal sumD90Recharge = BigDecimal.ZERO;

            LocalDate maxToday = LocalDate.now(ZoneId.of("Asia/Shanghai"));

            for (LtvDailyStat s : monthStats) {
                if (s.getSpend() != null && s.getSpend().compareTo(BigDecimal.ZERO) > 0 && s.getLaunchDate() != null) {
                    long daysElapsed = ChronoUnit.DAYS.between(s.getLaunchDate(), maxToday) + 1;
                    PredictionResult pred = ltvPredictService.predictCohort(s, (int) daysElapsed);

                    BigDecimal d30 = pred.getPredictedDay30Recharge() != null ? pred.getPredictedDay30Recharge() : (s.getTotalRecharge() != null ? s.getTotalRecharge() : BigDecimal.ZERO);
                    BigDecimal d60 = pred.getPredictedDay60Recharge() != null ? pred.getPredictedDay60Recharge() : d30;
                    BigDecimal d90 = pred.getPredictedDay90Recharge() != null ? pred.getPredictedDay90Recharge() : d60;

                    sumD30Recharge = sumD30Recharge.add(d30);
                    sumD60Recharge = sumD60Recharge.add(d60);
                    sumD90Recharge = sumD90Recharge.add(d90);
                }
            }

            predictedDay30Roi = sumD30Recharge.divide(totalSpend, 4, RoundingMode.HALF_UP);
            predictedDay60Roi = sumD60Recharge.divide(totalSpend, 4, RoundingMode.HALF_UP);
            predictedDay90Roi = sumD90Recharge.divide(totalSpend, 4, RoundingMode.HALF_UP);
        }

        SingleMonthSummaryDto dto = new SingleMonthSummaryDto();
        dto.setMonth(monthStr);
        dto.setSpend(totalSpend);
        dto.setRecharge(totalRecharge);
        dto.setRefund(totalRefund);
        dto.setProfit(profit);
        dto.setRoi(roi.setScale(2, RoundingMode.HALF_UP));
        dto.setSubUsers(retentionInfo != null ? retentionInfo.getSubUsers() : 0);
        dto.setRetainedSubUsers(retentionInfo != null ? retentionInfo.getRetainedSubUsers() : 0);
        dto.setRetainedRate(retentionInfo != null ? retentionInfo.getRetainedRate() : "0.00%");
        dto.setActualPaybackDays(actualPaybackDays);
        dto.setPredictedDay30Roi(predictedDay30Roi);
        dto.setPredictedDay60Roi(predictedDay60Roi);
        dto.setPredictedDay90Roi(predictedDay90Roi);
        return dto;
    }

    private Integer calculateActualPaybackDaysForMonth(Long userId, YearMonth yearMonth, List<LtvDailyStat> monthStats, BigDecimal totalSpend, BigDecimal totalRecharge, List<RawOrder> filteredOrders) {
        List<LandingPageConfigItem> userPages = userService.getUserLandingPageConfigs(userId);
        Map<String, String> tzMap = (userPages != null) ? userPages.stream()
                .filter(p -> p.getLandingPageId() != null)
                .collect(Collectors.toMap(p -> p.getLandingPageId().trim(), LandingPageConfigItem::getTimezone, (a, b) -> a)) : Collections.emptyMap();
        return calculateActualPaybackDaysForMonth(userId, yearMonth, monthStats, totalSpend, totalRecharge, filteredOrders, tzMap);
    }

    private Integer calculateActualPaybackDaysForMonth(Long userId, YearMonth yearMonth, List<LtvDailyStat> monthStats, BigDecimal totalSpend, BigDecimal totalRecharge, UserCalculationContext ctx) {
        return calculateActualPaybackDaysForMonth(userId, yearMonth, monthStats, totalSpend, totalRecharge, ctx.userOrders, ctx.tzMap);
    }

    private Integer calculateActualPaybackDaysForMonth(Long userId, YearMonth yearMonth, List<LtvDailyStat> monthStats, BigDecimal totalSpend, BigDecimal totalRecharge, List<RawOrder> filteredOrders, Map<String, String> tzMap) {
        LocalDate todayBj = LocalDate.now(ZoneId.of("Asia/Shanghai"));

        // 1. 严格判断：若总消耗 <= 0 或 截至目前的总充值 < 总消耗 (未回本)，绝对不进行计算与推测，直接返回 null
        if (totalSpend == null || totalSpend.compareTo(BigDecimal.ZERO) <= 0 || totalRecharge == null || totalRecharge.compareTo(totalSpend) < 0) {
            return null;
        }

        // 2. 找出上月首个有消耗的自然日 (firstSpendDate) 作为 Day 1 基准
        List<LtvDailyStat> validSpendStats = monthStats.stream()
                .filter(s -> s.getLaunchDate() != null && s.getSpend() != null && s.getSpend().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        if (validSpendStats.isEmpty()) {
            return null;
        }

        LocalDate firstSpendDate = validSpendStats.stream()
                .map(LtvDailyStat::getLaunchDate)
                .min(LocalDate::compareTo)
                .orElse(yearMonth.atDay(1));

        if (firstSpendDate.isAfter(todayBj)) {
            return null;
        }

        // 3. 过滤出上月份 (yearMonth) 注册/投放的用户订单，且支付日期严格截至今天 (绝不上摸未来时间)
        List<RawOrder> monthOrders = filteredOrders.stream()
                .filter(o -> {
                    LocalDate regDate = getEffectiveRegisterDate(o, tzMap);
                    LocalDate payDate = getEffectivePayDate(o, tzMap);
                    return regDate != null && !regDate.isBefore(START_DATE) && YearMonth.from(regDate).equals(yearMonth) 
                            && payDate != null && !payDate.isAfter(todayBj);
                })
                .collect(Collectors.toList());

        if (monthOrders.isEmpty()) {
            return null;
        }

        // 4. 按订单生效支付日期 (payDate) 归集每日充值额 (早于首个消耗日的归计到首个消耗日)
        Map<LocalDate, BigDecimal> rechargeByPayDate = new TreeMap<>();
        for (RawOrder o : monthOrders) {
            LocalDate payDate = getEffectivePayDate(o, tzMap);
            if (payDate == null) continue;

            LocalDate effectivePay = payDate.isBefore(firstSpendDate) ? firstSpendDate : payDate;
            BigDecimal price = o.getOrderAmountUsd() != null ? o.getOrderAmountUsd() : BigDecimal.ZERO;
            rechargeByPayDate.put(effectivePay, rechargeByPayDate.getOrDefault(effectivePay, BigDecimal.ZERO).add(price));
        }

        // 5. 截止计算日期严格限制为今天 (todayBj)
        LocalDate maxOrderPayDate = rechargeByPayDate.keySet().stream().max(LocalDate::compareTo).orElse(firstSpendDate);
        LocalDate limitDate = maxOrderPayDate.isAfter(todayBj) ? todayBj : maxOrderPayDate;

        // 6. 从首个消耗日 firstSpendDate 开始按自然日推进累加充值额，N 必须截至今天
        BigDecimal cumulative = BigDecimal.ZERO;
        BigDecimal targetThreshold = totalSpend.subtract(new BigDecimal("0.01")); // 允许1分钱舍入误差
        LocalDate currDate = firstSpendDate;

        while (!currDate.isAfter(limitDate)) {
            BigDecimal dayAmount = rechargeByPayDate.getOrDefault(currDate, BigDecimal.ZERO);
            cumulative = cumulative.add(dayAmount);

            // 当累计充值达到或超越总消耗时，该自然日即为实际回本日期
            if (cumulative.compareTo(targetThreshold) >= 0) {
                // 回本天数 = 回本日期 - 首个消耗日(firstSpendDate) + 1
                long days = ChronoUnit.DAYS.between(firstSpendDate, currDate) + 1;
                return (int) days;
            }
            currDate = currDate.plusDays(1);
        }

        // 7. 若截至今天逐自然日累加后仍未达到总消耗（极特殊数据微差），防护返回 null，决不上摸或假定未来天数
        return null;
    }
}
