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

        // 动态熔断上限策略 (仅作用于 D30/D60/D90 里程碑 ROI 预测，完全解耦不影响回本模型 Daily 曲线与精度)
        double maxAllowedRoi30 = Math.max(PredictAlgorithmConstants.D30_BASE_MAX_ROI,
                Math.min(actualRoiVal * PredictAlgorithmConstants.D30_MAX_ROI_MULT, actualRoiVal + PredictAlgorithmConstants.D30_MAX_ROI_ADD));
        double maxAllowedRoi60 = Math.max(PredictAlgorithmConstants.D60_BASE_MAX_ROI,
                Math.min(actualRoiVal * PredictAlgorithmConstants.D60_MAX_ROI_MULT, actualRoiVal + PredictAlgorithmConstants.D60_MAX_ROI_ADD));
        double maxAllowedRoi90 = Math.max(PredictAlgorithmConstants.D90_BASE_MAX_ROI,
                Math.min(actualRoiVal * PredictAlgorithmConstants.D90_MAX_ROI_MULT, actualRoiVal + PredictAlgorithmConstants.D90_MAX_ROI_ADD));

        // 保证单调非递减约束：Actual ROI <= D30 ROI <= D60 ROI <= D90 ROI
        double finalD30Roi = Math.max(actualRoiVal, Math.min(rawD30Roi, maxAllowedRoi30));
        double finalD60Roi = Math.max(finalD30Roi, Math.min(rawD60Roi, maxAllowedRoi60));
        double finalD90Roi = Math.max(finalD60Roi, Math.min(rawD90Roi, maxAllowedRoi90));

        BigDecimal predD30Roi = BigDecimal.valueOf(finalD30Roi).setScale(4, RoundingMode.HALF_UP);
        BigDecimal predD60Roi = BigDecimal.valueOf(finalD60Roi).setScale(4, RoundingMode.HALF_UP);
        BigDecimal predD90Roi = BigDecimal.valueOf(finalD90Roi).setScale(4, RoundingMode.HALF_UP);

        BigDecimal predD30Recharge = predD30Roi.multiply(spend).setScale(2, RoundingMode.HALF_UP);
        BigDecimal predD60Recharge = predD60Roi.multiply(spend).setScale(2, RoundingMode.HALF_UP);
        BigDecimal predD90Recharge = predD90Roi.multiply(spend).setScale(2, RoundingMode.HALF_UP);

        return new RoiTrendResult(predD30Roi, predD60Roi, predD90Roi, predD30Recharge, predD60Recharge, predD90Recharge);
    }
}
