package com.ltv.stat.service;

import com.ltv.stat.dto.MonthlySettlementItemDto;
import com.ltv.stat.dto.MonthlySettlementSaveRequestDto;
import com.ltv.stat.entity.MonthlySettlementConfig;
import com.ltv.stat.entity.RawOrder;
import com.ltv.stat.entity.SysUser;
import com.ltv.stat.entity.UserLandingPage;
import com.ltv.stat.repository.MonthlySettlementConfigRepository;
import com.ltv.stat.repository.RawOrderRepository;
import com.ltv.stat.repository.UserLandingPageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MonthlySettlementService {

    private static final Logger log = LoggerFactory.getLogger(MonthlySettlementService.class);
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final RawOrderRepository rawOrderRepository;
    private final MonthlySettlementConfigRepository configRepository;
    private final UserLandingPageRepository userLandingPageRepository;
    private final UserService userService;

    public MonthlySettlementService(
            RawOrderRepository rawOrderRepository,
            MonthlySettlementConfigRepository configRepository,
            UserLandingPageRepository userLandingPageRepository,
            UserService userService
    ) {
        this.rawOrderRepository = rawOrderRepository;
        this.configRepository = configRepository;
        this.userLandingPageRepository = userLandingPageRepository;
        this.userService = userService;
    }

    /**
     * 获取月度结算列表
     * @param settlementType "PLATFORM_ALL", "USER_ACCOUNT", "UNLINKED_PID"
     * @param targetUserId 针对 USER_ACCOUNT 类型传入
     */
    public List<MonthlySettlementItemDto> getMonthlySettlementList(String settlementType, Long targetUserId) {
        String type = (settlementType != null && !settlementType.trim().isEmpty()) ? settlementType.trim().toUpperCase() : "PLATFORM_ALL";

        // 获取全部订单数据
        List<RawOrder> allOrders = rawOrderRepository.findAll();

        // 获取系统中所有已配置的落地页 ID 集合
        Set<String> allConfiguredPids = userLandingPageRepository.findAll().stream()
                .map(UserLandingPage::getLandingPageId)
                .filter(pid -> pid != null && !pid.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toSet());

        // 针对 USER_ACCOUNT 获取该用户的落地页集合（主账号自动聚合子账号）
        Set<String> userPids = Collections.emptySet();
        String targetUsername = null;
        if ("USER_ACCOUNT".equals(type) && targetUserId != null) {
            List<String> pids = userService.getUserLandingPageIds(targetUserId);
            userPids = pids.stream().filter(p -> p != null && !p.trim().isEmpty()).map(String::trim).collect(Collectors.toSet());
            targetUsername = userService.findById(targetUserId).map(SysUser::getUsername).orElse("用户#" + targetUserId);
        }

        // 获取现有月份列表（2026-07 至今所有月份）
        TreeSet<String> monthsSet = new TreeSet<>(Comparator.reverseOrder());
        YearMonth startYm = YearMonth.of(2026, 7);
        YearMonth currentYm = YearMonth.now();
        YearMonth maxYm = currentYm.isAfter(startYm) ? currentYm : startYm;

        YearMonth cur = startYm;
        while (!cur.isAfter(maxYm)) {
            monthsSet.add(cur.format(MONTH_FORMATTER));
            cur = cur.plusMonths(1);
        }

        // 提取订单中出现的月份
        for (RawOrder o : allOrders) {
            if (o.getPayTimeBj() != null) {
                monthsSet.add(o.getPayTimeBj().format(MONTH_FORMATTER));
            }
        }

        List<MonthlySettlementItemDto> resultList = new ArrayList<>();

        for (String monthStr : monthsSet) {
            YearMonth ym = YearMonth.parse(monthStr, MONTH_FORMATTER);
            LocalDateTime monthStart = ym.atDay(1).atStartOfDay();
            LocalDateTime monthEnd = ym.atEndOfMonth().atTime(23, 59, 59);

            // 过滤当月订单
            final Set<String> finalUserPids = userPids;
            List<RawOrder> monthOrders = allOrders.stream()
                    .filter(o -> o.getPayTimeBj() != null && !o.getPayTimeBj().isBefore(monthStart) && !o.getPayTimeBj().isAfter(monthEnd))
                    .filter(o -> {
                        String pid = o.getLandingPageId() != null ? o.getLandingPageId().trim() : "";
                        if ("PLATFORM_ALL".equals(type)) {
                            return true;
                        } else if ("USER_ACCOUNT".equals(type)) {
                            return !pid.isEmpty() && finalUserPids.contains(pid);
                        } else if ("UNLINKED_PID".equals(type)) {
                            return pid.isEmpty() || !allConfiguredPids.contains(pid);
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

            // 统计总充值与总退款
            long totalRechargeCents = 0;
            long totalRefundCents = 0;
            int totalOrders = 0;
            int refundOrders = 0;

            for (RawOrder o : monthOrders) {
                // 仅统计支付成功的充值
                if (o.getPayState() != null && o.getPayState() == 1) {
                    long cent = o.getOrderAmountCent() != null ? o.getOrderAmountCent() : 0;
                    totalRechargeCents += cent;
                    totalOrders++;

                    // 退款成功 (refund_status == 2)
                    if (o.getRefundStatus() != null && o.getRefundStatus() == 2) {
                        totalRefundCents += cent;
                        refundOrders++;
                    }
                }
            }

            BigDecimal totalRecharge = BigDecimal.valueOf(totalRechargeCents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal totalRefund = BigDecimal.valueOf(totalRefundCents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            // 加载持久化配置
            Optional<MonthlySettlementConfig> configOpt;
            if ("USER_ACCOUNT".equals(type) && targetUserId != null) {
                configOpt = configRepository.findBySettlementTypeAndTargetUserIdAndMonthStr(type, targetUserId, monthStr);
            } else {
                configOpt = configRepository.findBySettlementTypeAndTargetUserIdIsNullAndMonthStr(type, monthStr);
            }

            BigDecimal settledRefundAmount = BigDecimal.ZERO;
            BigDecimal monthSettledRefundAmount = BigDecimal.ZERO;
            BigDecimal crossPeriodRefundAmount = BigDecimal.ZERO;
            BigDecimal shareRatio = new BigDecimal("0.9500");
            BigDecimal channelFeeRate = new BigDecimal("0.0700");
            String remark = "";
            LocalDateTime updatedAt = null;

            if (configOpt.isPresent()) {
                MonthlySettlementConfig cfg = configOpt.get();
                settledRefundAmount = cfg.getSettledRefundAmount();
                monthSettledRefundAmount = cfg.getMonthSettledRefundAmount();
                crossPeriodRefundAmount = cfg.getCrossPeriodRefundAmount();
                shareRatio = cfg.getShareRatio();
                channelFeeRate = cfg.getChannelFeeRate();
                remark = cfg.getRemark();
                updatedAt = cfg.getUpdatedAt();
            }

            // 计算未结算退款 = 累计退款 - 已结算退款
            BigDecimal unsettledRefundAmount = totalRefund.subtract(settledRefundAmount);

            // 计算有效结算基数 = 累计充值 - 当月结算退款 - 跨周期退款
            BigDecimal effectiveBaseAmount = totalRecharge.subtract(monthSettledRefundAmount).subtract(crossPeriodRefundAmount);

            // 计算最终结算金额 = 有效结算基数 * 分成比例 * (1 - 渠道费率)
            BigDecimal finalSettlementAmount = BigDecimal.ZERO;
            if (effectiveBaseAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal netFactor = BigDecimal.ONE.subtract(channelFeeRate);
                finalSettlementAmount = effectiveBaseAmount.multiply(shareRatio).multiply(netFactor).setScale(2, RoundingMode.HALF_UP);
            } else {
                finalSettlementAmount = effectiveBaseAmount.multiply(shareRatio).setScale(2, RoundingMode.HALF_UP);
            }

            // 退款率
            String refundRate = "0.00%";
            if (totalRecharge.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal rate = totalRefund.divide(totalRecharge, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                refundRate = rate.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
            }

            MonthlySettlementItemDto item = new MonthlySettlementItemDto();
            item.setMonthStr(monthStr);
            item.setSettlementType(type);
            item.setTargetUserId(targetUserId);
            item.setTargetUsername(targetUsername);
            item.setTotalRecharge(totalRecharge);
            item.setTotalRefund(totalRefund);
            item.setSettledRefundAmount(settledRefundAmount);
            item.setMonthSettledRefundAmount(monthSettledRefundAmount);
            item.setUnsettledRefundAmount(unsettledRefundAmount);
            item.setCrossPeriodRefundAmount(crossPeriodRefundAmount);
            item.setShareRatio(shareRatio);
            item.setChannelFeeRate(channelFeeRate);
            item.setEffectiveBaseAmount(effectiveBaseAmount);
            item.setFinalSettlementAmount(finalSettlementAmount);
            item.setRefundRate(refundRate);
            item.setTotalOrders(totalOrders);
            item.setRefundOrders(refundOrders);
            item.setRemark(remark);
            item.setUpdatedAt(updatedAt);

            resultList.add(item);
        }

        return resultList;
    }

    /**
     * 保存或更新月度结算配置
     */
    public MonthlySettlementConfig saveSettlementConfig(MonthlySettlementSaveRequestDto dto) {
        if (dto == null || dto.getSettlementType() == null || dto.getMonthStr() == null) {
            throw new IllegalArgumentException("结算类型与月份不能为空");
        }

        String type = dto.getSettlementType().trim().toUpperCase();
        String monthStr = dto.getMonthStr().trim();
        Long targetUserId = "USER_ACCOUNT".equals(type) ? dto.getTargetUserId() : null;

        MonthlySettlementConfig config;
        Optional<MonthlySettlementConfig> opt;
        if ("USER_ACCOUNT".equals(type) && targetUserId != null) {
            opt = configRepository.findBySettlementTypeAndTargetUserIdAndMonthStr(type, targetUserId, monthStr);
        } else {
            opt = configRepository.findBySettlementTypeAndTargetUserIdIsNullAndMonthStr(type, monthStr);
        }

        if (opt.isPresent()) {
            config = opt.get();
        } else {
            config = new MonthlySettlementConfig();
            config.setSettlementType(type);
            config.setTargetUserId(targetUserId);
            config.setMonthStr(monthStr);
        }

        if (dto.getSettledRefundAmount() != null) {
            config.setSettledRefundAmount(dto.getSettledRefundAmount());
        }
        if (dto.getMonthSettledRefundAmount() != null) {
            config.setMonthSettledRefundAmount(dto.getMonthSettledRefundAmount());
        }
        if (dto.getCrossPeriodRefundAmount() != null) {
            config.setCrossPeriodRefundAmount(dto.getCrossPeriodRefundAmount());
        }
        if (dto.getShareRatio() != null) {
            config.setShareRatio(dto.getShareRatio());
        }
        if (dto.getChannelFeeRate() != null) {
            config.setChannelFeeRate(dto.getChannelFeeRate());
        }
        if (dto.getRemark() != null) {
            config.setRemark(dto.getRemark().trim());
        }

        return configRepository.save(config);
    }
}
