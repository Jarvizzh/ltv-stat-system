package com.ltv.stat.service.engine;

import com.ltv.stat.dto.RoiTrendResult;
import com.ltv.stat.entity.LtvDailyStat;
import com.ltv.stat.util.CohortStatHelper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 专职负责 ROI 趋势与里程碑节点 (D30, D60, D90) 外推独立引擎
 */
@Component
public class RoiPredictEngine {

    /**
     * 计算单 Cohort 的 D30, D60, D90 预测 ROI 与预估充值金额
     */
    public RoiTrendResult calculateCohortRoiTrend(LtvDailyStat stat, int daysElapsed, double[] cumRechargeCurve) {
        if (stat == null || stat.getSpend() == null || stat.getSpend().compareTo(BigDecimal.ZERO) <= 0 || cumRechargeCurve == null || cumRechargeCurve.length <= 90) {
            return new RoiTrendResult(null, null, null, null, null, null);
        }

        int maxDays = Math.min(daysElapsed, 60);
        BigDecimal spend = stat.getSpend();

        double actualRechargeVal = CohortStatHelper.getRechargeForDay(stat, maxDays).doubleValue();
        double actualRoiVal = actualRechargeVal / spend.doubleValue();

        // 提取 Day 30、Day 60 与 Day 90 原始外推充值与 ROI
        double rawD30Recharge = cumRechargeCurve[30];
        double rawD60Recharge = cumRechargeCurve[60];
        double rawD90Recharge = cumRechargeCurve[90];

        double rawD30Roi = rawD30Recharge / spend.doubleValue();
        double rawD60Roi = rawD60Recharge / spend.doubleValue();
        double rawD90Roi = rawD90Recharge / spend.doubleValue();

        // 若已判定进入订阅平盘停滞期，未来不再产生任何充值，直接返回当前实际 ROI
        if (CohortStatHelper.isSubscriptionStagnant(stat, maxDays)) {
            BigDecimal predD30Roi = BigDecimal.valueOf(actualRoiVal).setScale(4, RoundingMode.HALF_UP);
            BigDecimal predD60Roi = BigDecimal.valueOf(actualRoiVal).setScale(4, RoundingMode.HALF_UP);
            BigDecimal predD90Roi = BigDecimal.valueOf(actualRoiVal).setScale(4, RoundingMode.HALF_UP);
            BigDecimal predRecharge = BigDecimal.valueOf(actualRechargeVal).setScale(2, RoundingMode.HALF_UP);
            return new RoiTrendResult(predD30Roi, predD60Roi, predD90Roi, predRecharge, predRecharge, predRecharge);
        }

        // 1. D30 ROI: 
        // - 当 daysElapsed >= 30: 直接使用真实 D30 ROI
        // - 当 14 <= daysElapsed < 30: P2 纯外推曲线高度成熟准确，100% 信任 rawD30Roi
        // - 当 daysElapsed < 14: 早期窗口进行历史倍率先验融合与弹性上限保护
        double finalD30Roi;
        if (daysElapsed >= 30) {
            BigDecimal actualD30Roi = CohortStatHelper.getRoiForDay(stat, 30);
            if (actualD30Roi != null && actualD30Roi.compareTo(BigDecimal.ZERO) > 0) {
                finalD30Roi = actualD30Roi.doubleValue();
            } else {
                double actualD30Recharge = CohortStatHelper.getRechargeForDay(stat, 30).doubleValue();
                finalD30Roi = actualD30Recharge / spend.doubleValue();
            }
        } else if (daysElapsed >= 14) {
            finalD30Roi = Math.max(actualRoiVal, rawD30Roi);
        } else {
            // 早期 (D3 ~ D7) 贝叶斯融合与先验收缩
            int subUserCount = stat.getSubUserCount() != null ? stat.getSubUserCount() : 1;
            double userWeight = (double) Math.max(1, subUserCount) / (Math.max(1, subUserCount) + PredictAlgorithmConstants.ROI_SAMPLE_SIZE_K);
            double beta30 = 0.15 + 0.35 * ((double) maxDays / 14.0) * (0.40 + 0.60 * userWeight);

            // 检查 D1 冲动充值与 D2/D3 停滞特征 (Impulse Dropoff Detection)
            double impulseDiscount = 1.0;
            if (maxDays <= 5) {
                BigDecimal r1 = CohortStatHelper.getRawRechargeForDay(stat, 1);
                BigDecimal rNow = CohortStatHelper.getRechargeForDay(stat, maxDays);
                if (r1 != null && rNow != null && r1.compareTo(BigDecimal.ZERO) > 0) {
                    if (rNow.doubleValue() <= r1.doubleValue() * 1.05) {
                        // 首日之后未见新增充值，读者迅速流失
                        impulseDiscount = 0.70;
                    }
                }
            }

            double priorMult30 = PredictAlgorithmConstants.getEmpiricalMultiplierTo30(maxDays) * impulseDiscount;
            double priorD30Roi = actualRoiVal * priorMult30;
            double blendedD30Roi = beta30 * (rawD30Roi * impulseDiscount) + (1.0 - beta30) * priorD30Roi;

            double remainFraction30 = Math.max(0.0, (30.0 - maxDays) / 30.0);
            double maxGrowth30 = 1.0 + 2.2 * Math.sqrt(remainFraction30) * impulseDiscount;
            double maxAllowedRoi30 = actualRoiVal * maxGrowth30 + 0.08 * remainFraction30;

            finalD30Roi = Math.max(actualRoiVal, Math.min(blendedD30Roi, maxAllowedRoi30));
        }

        // 2. D60 ROI:
        double finalD60Roi;
        if (daysElapsed >= 60) {
            BigDecimal actualD60Roi = CohortStatHelper.getRoiForDay(stat, 60);
            if (actualD60Roi != null && actualD60Roi.compareTo(BigDecimal.ZERO) > 0) {
                finalD60Roi = actualD60Roi.doubleValue();
            } else {
                double actualD60Recharge = CohortStatHelper.getRechargeForDay(stat, 60).doubleValue();
                finalD60Roi = actualD60Recharge / spend.doubleValue();
            }
        } else if (daysElapsed >= 30) {
            finalD60Roi = Math.max(actualRoiVal, rawD60Roi);
        } else {
            double beta60 = 0.30 + 0.50 * ((double) maxDays / 30.0);
            double priorD60Roi = finalD30Roi * PredictAlgorithmConstants.ROI_60_CHAIN_MULTIPLIER;
            double blendedD60Roi = beta60 * rawD60Roi + (1.0 - beta60) * priorD60Roi;

            double remainFraction60 = Math.max(0.0, (60.0 - Math.min(maxDays, 30)) / 60.0);
            double maxAllowedRoi60 = finalD30Roi * (1.0 + 1.6 * Math.sqrt(remainFraction60));

            double lowerBound60 = (daysElapsed >= 30) ? actualRoiVal : finalD30Roi;
            finalD60Roi = Math.max(lowerBound60, Math.min(blendedD60Roi, maxAllowedRoi60));
        }

        // 3. D90 ROI:
        double finalD90Roi;
        if (daysElapsed >= 90) {
            BigDecimal actualD90Roi = CohortStatHelper.getRoiForDay(stat, 90);
            if (actualD90Roi != null && actualD90Roi.compareTo(BigDecimal.ZERO) > 0) {
                finalD90Roi = actualD90Roi.doubleValue();
            } else {
                double actualD90Recharge = CohortStatHelper.getRechargeForDay(stat, 90).doubleValue();
                finalD90Roi = actualD90Recharge / spend.doubleValue();
            }
        } else if (daysElapsed >= 60) {
            finalD90Roi = Math.max(actualRoiVal, rawD90Roi);
        } else {
            double beta90 = 0.30 + 0.50 * ((double) maxDays / 60.0);
            double priorD90Roi = finalD60Roi * PredictAlgorithmConstants.ROI_90_CHAIN_MULTIPLIER;
            double blendedD90Roi = beta90 * rawD90Roi + (1.0 - beta90) * priorD90Roi;

            double remainFraction90 = Math.max(0.0, (90.0 - Math.min(maxDays, 60)) / 90.0);
            double maxAllowedRoi90 = finalD60Roi * (1.0 + 1.4 * Math.sqrt(remainFraction90));

            double lowerBound90 = (daysElapsed >= 60) ? actualRoiVal : finalD60Roi;
            finalD90Roi = Math.max(lowerBound90, Math.min(blendedD90Roi, maxAllowedRoi90));
        }

        BigDecimal predD30Roi = BigDecimal.valueOf(finalD30Roi).setScale(4, RoundingMode.HALF_UP);
        BigDecimal predD60Roi = BigDecimal.valueOf(finalD60Roi).setScale(4, RoundingMode.HALF_UP);
        BigDecimal predD90Roi = BigDecimal.valueOf(finalD90Roi).setScale(4, RoundingMode.HALF_UP);

        BigDecimal predD30Recharge = (daysElapsed >= 30)
                ? CohortStatHelper.getRechargeForDay(stat, 30).setScale(2, RoundingMode.HALF_UP)
                : predD30Roi.multiply(spend).setScale(2, RoundingMode.HALF_UP);

        BigDecimal predD60Recharge = (daysElapsed >= 60)
                ? CohortStatHelper.getRechargeForDay(stat, 60).setScale(2, RoundingMode.HALF_UP)
                : predD60Roi.multiply(spend).setScale(2, RoundingMode.HALF_UP);

        BigDecimal predD90Recharge = (daysElapsed >= 90)
                ? CohortStatHelper.getRechargeForDay(stat, 90).setScale(2, RoundingMode.HALF_UP)
                : predD90Roi.multiply(spend).setScale(2, RoundingMode.HALF_UP);

        return new RoiTrendResult(predD30Roi, predD60Roi, predD90Roi, predD30Recharge, predD60Recharge, predD90Recharge);
    }
}
