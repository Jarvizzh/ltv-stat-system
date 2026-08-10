package com.ltv.stat.service.engine;

import com.ltv.stat.entity.LtvDailyStat;
import com.ltv.stat.dto.PredictionResult;
import com.ltv.stat.dto.RoiTrendResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 预测引擎门面 (Facade) 类：聚合 PaybackPredictEngine 与 RoiPredictEngine 结果
 */
@Component
public class LtvPredictFacade {

    private final PaybackPredictEngine paybackPredictEngine;
    private final RoiPredictEngine roiPredictEngine;

    public LtvPredictFacade(PaybackPredictEngine paybackPredictEngine, RoiPredictEngine roiPredictEngine) {
        this.paybackPredictEngine = paybackPredictEngine;
        this.roiPredictEngine = roiPredictEngine;
    }

    /**
     * 聚合单 Cohort 的回本预测与 ROI 趋势预测
     */
    public PredictionResult assembleCohortPrediction(LtvDailyStat stat, int daysElapsed, double[] cumRechargeCurve) {
        if (stat == null || stat.getSpend() == null || stat.getSpend().compareTo(BigDecimal.ZERO) <= 0) {
            return new PredictionResult(null);
        }

        // 1. 调用独立 ROI 趋势引擎计算 D30, D60, D90 预测
        RoiTrendResult roiResult = roiPredictEngine.calculateCohortRoiTrend(stat, daysElapsed, cumRechargeCurve);

        // 2. 调用独立回本预测引擎计算预计回本天数
        Integer paybackDays = paybackPredictEngine.calculateCohortPaybackDays(stat, daysElapsed, cumRechargeCurve);

        return new PredictionResult(
                paybackDays,
                null,
                roiResult.getPredD30Roi(),
                roiResult.getPredD60Roi(),
                roiResult.getPredD90Roi(),
                roiResult.getPredD30Recharge(),
                roiResult.getPredD60Recharge(),
                roiResult.getPredD90Recharge()
        );
    }

    /**
     * 聚合大盘整体 (Overall Cohort) 的回本预测 (不进行大盘整体 ROI 预测)
     */
    public PredictionResult assembleOverallPrediction(BigDecimal totalSpendAll, BigDecimal totalRechargeAll,
                                                      List<LtvDailyStat> validStats, Map<LtvDailyStat, double[]> cohortCurves,
                                                      LocalDate minLaunchDate, LocalDate today) {
        if (totalSpendAll == null || totalSpendAll.compareTo(BigDecimal.ZERO) <= 0) {
            return new PredictionResult(null);
        }

        // 调用独立回本预测引擎按自然日历交叠求和计算大盘整体回本天数
        Integer overallPaybackDays = paybackPredictEngine.calculateOverallPaybackDays(totalSpendAll, totalRechargeAll, validStats, cohortCurves, minLaunchDate, today);

        return new PredictionResult(
                overallPaybackDays,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
