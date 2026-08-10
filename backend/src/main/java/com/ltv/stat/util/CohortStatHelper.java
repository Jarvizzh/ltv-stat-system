package com.ltv.stat.util;

import com.ltv.stat.entity.LtvDailyStat;
import com.ltv.stat.service.engine.PredictAlgorithmConstants;

import java.math.BigDecimal;

/**
 * Cohort 数据提取与订阅平盘停滞判定助手类
 */
public final class CohortStatHelper {

    private CohortStatHelper() {}

    /**
     * 获取指定 Cohort 第 day 天的累计充值金额（向上兜底查找最新有效值）
     */
    public static BigDecimal getRechargeForDay(LtvDailyStat stat, int day) {
        if (stat == null || day <= 0) {
            return BigDecimal.ZERO;
        }
        for (int d = day; d >= 1; d--) {
            BigDecimal val = getRawRechargeForDay(stat, d);
            if (val != null && val.compareTo(BigDecimal.ZERO) > 0) {
                return val;
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * 读取指定 Cohort 第 day 天的原始充值属性
     */
    public static BigDecimal getRawRechargeForDay(LtvDailyStat stat, int day) {
        if (stat == null || day <= 0) {
            return BigDecimal.ZERO;
        }
        switch (day) {
            case 1: return stat.getDay1Recharge();
            case 2: return stat.getDay2Recharge();
            case 3: return stat.getDay3Recharge();
            case 4: return stat.getDay4Recharge();
            case 5: return stat.getDay5Recharge();
            case 6: return stat.getDay6Recharge();
            case 7: return stat.getDay7Recharge();
            case 8: return stat.getDay8Recharge();
            case 9: return stat.getDay9Recharge();
            case 10: return stat.getDay10Recharge();
            case 11: return stat.getDay11Recharge();
            case 12: return stat.getDay12Recharge();
            case 13: return stat.getDay13Recharge();
            case 14: return stat.getDay14Recharge();
            case 15: return stat.getDay15Recharge();
            case 16: return stat.getDay16Recharge();
            case 17: return stat.getDay17Recharge();
            case 18: return stat.getDay18Recharge();
            case 19: return stat.getDay19Recharge();
            case 20: return stat.getDay20Recharge();
            case 21: return stat.getDay21Recharge();
            case 22: return stat.getDay22Recharge();
            case 23: return stat.getDay23Recharge();
            case 24: return stat.getDay24Recharge();
            case 25: return stat.getDay25Recharge();
            case 26: return stat.getDay26Recharge();
            case 27: return stat.getDay27Recharge();
            case 28: return stat.getDay28Recharge();
            case 29: return stat.getDay29Recharge();
            case 30: return stat.getDay30Recharge();
            case 31: return stat.getDay31Recharge();
            case 32: return stat.getDay32Recharge();
            case 33: return stat.getDay33Recharge();
            case 34: return stat.getDay34Recharge();
            case 35: return stat.getDay35Recharge();
            case 36: return stat.getDay36Recharge();
            case 37: return stat.getDay37Recharge();
            case 38: return stat.getDay38Recharge();
            case 39: return stat.getDay39Recharge();
            case 40: return stat.getDay40Recharge();
            case 41: return stat.getDay41Recharge();
            case 42: return stat.getDay42Recharge();
            case 43: return stat.getDay43Recharge();
            case 44: return stat.getDay44Recharge();
            case 45: return stat.getDay45Recharge();
            case 46: return stat.getDay46Recharge();
            case 47: return stat.getDay47Recharge();
            case 48: return stat.getDay48Recharge();
            case 49: return stat.getDay49Recharge();
            case 50: return stat.getDay50Recharge();
            case 51: return stat.getDay51Recharge();
            case 52: return stat.getDay52Recharge();
            case 53: return stat.getDay53Recharge();
            case 54: return stat.getDay54Recharge();
            case 55: return stat.getDay55Recharge();
            case 56: return stat.getDay56Recharge();
            case 57: return stat.getDay57Recharge();
            case 58: return stat.getDay58Recharge();
            case 59: return stat.getDay59Recharge();
            case 60: return stat.getDay60Recharge();
            default: return stat.getTotalRecharge();
        }
    }

    /**
     * 读取指定 Cohort 第 day 天的 ROI 属性
     */
    public static BigDecimal getRoiForDay(LtvDailyStat stat, int day) {
        if (stat == null || day <= 0) {
            return BigDecimal.ZERO;
        }
        switch (day) {
            case 1: return stat.getDay1Roi();
            case 2: return stat.getDay2Roi();
            case 3: return stat.getDay3Roi();
            case 4: return stat.getDay4Roi();
            case 5: return stat.getDay5Roi();
            case 6: return stat.getDay6Roi();
            case 7: return stat.getDay7Roi();
            case 8: return stat.getDay8Roi();
            case 9: return stat.getDay9Roi();
            case 10: return stat.getDay10Roi();
            case 11: return stat.getDay11Roi();
            case 12: return stat.getDay12Roi();
            case 13: return stat.getDay13Roi();
            case 14: return stat.getDay14Roi();
            case 15: return stat.getDay15Roi();
            case 16: return stat.getDay16Roi();
            case 17: return stat.getDay17Roi();
            case 18: return stat.getDay18Roi();
            case 19: return stat.getDay19Roi();
            case 20: return stat.getDay20Roi();
            case 21: return stat.getDay21Roi();
            case 22: return stat.getDay22Roi();
            case 23: return stat.getDay23Roi();
            case 24: return stat.getDay24Roi();
            case 25: return stat.getDay25Roi();
            case 26: return stat.getDay26Roi();
            case 27: return stat.getDay27Roi();
            case 28: return stat.getDay28Roi();
            case 29: return stat.getDay29Roi();
            case 30: return stat.getDay30Roi();
            case 31: return stat.getDay31Roi();
            case 32: return stat.getDay32Roi();
            case 33: return stat.getDay33Roi();
            case 34: return stat.getDay34Roi();
            case 35: return stat.getDay35Roi();
            case 36: return stat.getDay36Roi();
            case 37: return stat.getDay37Roi();
            case 38: return stat.getDay38Roi();
            case 39: return stat.getDay39Roi();
            case 40: return stat.getDay40Roi();
            case 41: return stat.getDay41Roi();
            case 42: return stat.getDay42Roi();
            case 43: return stat.getDay43Roi();
            case 44: return stat.getDay44Roi();
            case 45: return stat.getDay45Roi();
            case 46: return stat.getDay46Roi();
            case 47: return stat.getDay47Roi();
            case 48: return stat.getDay48Roi();
            case 49: return stat.getDay49Roi();
            case 50: return stat.getDay50Roi();
            case 51: return stat.getDay51Roi();
            case 52: return stat.getDay52Roi();
            case 53: return stat.getDay53Roi();
            case 54: return stat.getDay54Roi();
            case 55: return stat.getDay55Roi();
            case 56: return stat.getDay56Roi();
            case 57: return stat.getDay57Roi();
            case 58: return stat.getDay58Roi();
            case 59: return stat.getDay59Roi();
            case 60: return stat.getDay60Roi();
            default: return stat.getTotalRoi();
        }
    }

    /**
     * 解析 Cohort 订阅分布 JSON，获取存在的最长订阅周期天数 (日订=1, 周订=7, 月订=30等)
     */
    public static int getMaxPeriodInDistribution(LtvDailyStat stat) {
        if (stat == null) return 1;
        String json = stat.getSubPeriodDistribution();
        if (json != null && !json.trim().isEmpty()) {
            try {
                String clean = json.trim().replaceAll("[{}\"]", "");
                if (!clean.isEmpty()) {
                    String[] pairs = clean.split(",");
                    int maxP = 1;
                    for (String p : pairs) {
                        String[] kv = p.split(":");
                        if (kv.length == 2) {
                            int period = Integer.parseInt(kv[0].trim());
                            int count = Integer.parseInt(kv[1].trim());
                            if (count > 0 && period > maxP) {
                                maxP = period;
                            }
                        }
                    }
                    return maxP;
                }
            } catch (Exception ignored) {}
        }
        return stat.getSubPeriodDays() != null ? Math.max(1, stat.getSubPeriodDays()) : 1;
    }

    /**
     * 根据订阅/追更周期与实际充值增长情况，通用判定 ROI 是否已进入平盘停滞期
     */
    public static boolean isSubscriptionStagnant(LtvDailyStat stat, int maxDays) {
        if (stat == null || maxDays < PredictAlgorithmConstants.MIN_FLAT_DAYS) return false;
        int maxPeriod = getMaxPeriodInDistribution(stat);

        int requiredFlatDays = Math.max(PredictAlgorithmConstants.MIN_FLAT_DAYS, maxPeriod * PredictAlgorithmConstants.PERIOD_FLAT_MULTIPLIER);

        if (maxDays >= requiredFlatDays) {
            BigDecimal rNow = getRechargeForDay(stat, maxDays);
            BigDecimal rPrev = getRechargeForDay(stat, maxDays - requiredFlatDays);
            if (rNow != null && rPrev != null) {
                return rNow.subtract(rPrev).compareTo(new BigDecimal("0.01")) <= 0;
            }
        }
        return false;
    }
}
