package com.ltv.stat.service.engine;

import com.ltv.stat.dto.PredictionResult;
import com.ltv.stat.entity.LtvDailyStat;
import com.ltv.stat.util.CohortStatHelper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 充值曲线数学外推与贝叶斯放缩因子推导纯算子引擎
 */
@Component
public class CohortCurveExtrapolator {

    /**
     * 算法 1~3 综合放缩因子计算：
     * 1. 极早期 (maxDays < 7) 贝叶斯先验收缩 (Empirical Bayes Shrinkage)
     * 2. 分段阶跃 Sigmoid 贝叶斯权重函数 w(t)
     * 3. 离群点 Cohort 动态平滑与熔断 (Outlier Clamping)
     */
    public static double computeOptimalScaleFactor(double actualRoi, double baseRoiSum, int maxDays, BigDecimal spend) {
        baseRoiSum = Math.max(0.0001, baseRoiSum);
        double rawAlpha = actualRoi / baseRoiSum;

        double alpha;
        double w;

        double spendVal = spend != null ? spend.doubleValue() : 1000.0;
        if (maxDays < 7) {
            // 算法 1：样本消耗加权贝叶斯先验收缩 (Spend-Weighted Prior Shrinkage)
            double priorWeight = Math.min(0.20, Math.max(0.05, 1000.0 / Math.max(100.0, spendVal) * 0.05));
            alpha = (actualRoi + priorWeight) / (baseRoiSum + priorWeight);

            // 算法 3：极早期离群点截断
            alpha = Math.max(PredictAlgorithmConstants.EARLY_STAGE_MIN_ALPHA,
                    Math.min(PredictAlgorithmConstants.EARLY_STAGE_MAX_ALPHA, alpha));

            // 算法 2：未发生周划扣期平滑权重 (w: 0.15 ~ 0.25)
            w = 0.15 + 0.10 * ((double) maxDays / 7.0);
        } else {
            // 算法 3：跨过划扣节点后的离群点熔断 (保持回本模型精度)
            alpha = Math.max(PredictAlgorithmConstants.MATURE_STAGE_MIN_ALPHA,
                    Math.min(PredictAlgorithmConstants.MATURE_STAGE_MAX_ALPHA, rawAlpha));

            // 算法 2：跨过划扣节点后 Sigmoid 强信任真实划扣数据 (w: 0.85 ~ 0.95)
            w = 0.85 + 0.10 * ((double) Math.min(53, maxDays - 7) / 53.0);
        }

        return w * alpha + (1.0 - w) * 1.0;
    }

    /**
     * 兜底线性对数拟合方法 (OLS)
     */
    public PredictionResult predictCohortOlsFallback(LtvDailyStat stat, int daysElapsed) {
        int maxDays = Math.min(daysElapsed, 60);
        List<Double> xList = new ArrayList<>();
        List<Double> yList = new ArrayList<>();

        for (int d = 1; d <= maxDays; d++) {
            BigDecimal roi = CohortStatHelper.getRoiForDay(stat, d);
            if (roi != null && roi.compareTo(BigDecimal.ZERO) > 0) {
                xList.add(Math.log(d));
                yList.add(roi.doubleValue());
            }
        }

        int n = xList.size();
        if (n < 3) return new PredictionResult(null);

        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < n; i++) {
            double x = xList.get(i);
            double y = yList.get(i);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }

        double denominator = n * sumXX - sumX * sumX;
        double a = (denominator != 0) ? (n * sumXY - sumX * sumY) / denominator : 0;
        double b = (sumY - a * sumX) / n;

        if (a > 0.0001) {
            double tPayback = Math.exp((1.0 - b) / a);
            if (tPayback > 0 && tPayback <= 365) {
                return new PredictionResult((int) Math.round(tPayback));
            } else {
                return new PredictionResult(366);
            }
        } else {
            return new PredictionResult(-1);
        }
    }
}
