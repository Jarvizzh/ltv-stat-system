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
        return computeOptimalScaleFactor(actualRoi, baseRoiSum, maxDays, spend, false);
    }

    /**
     * 算法 1~3 综合放缩因子计算（含 P2 模块 3 小样本强活跃动态松绑）：
     * 1. 极早期 (maxDays <= 7) 贝叶斯先验收缩 (Empirical Bayes Shrinkage)
     * 2. 分段阶跃 Sigmoid 贝叶斯权重函数 w(t)
     * 3. 离群点 Cohort 动态平滑与熔断 (Outlier Clamping)
     */
    public static double computeOptimalScaleFactor(double actualRoi, double baseRoiSum, int maxDays, BigDecimal spend, boolean isSmallCohortActive) {
        baseRoiSum = Math.max(0.0001, baseRoiSum);
        double rawAlpha = actualRoi / baseRoiSum;

        double alpha;
        double w;

        double spendVal = spend != null ? spend.doubleValue() : 1000.0;
        double maxAlpha = isSmallCohortActive ? PredictAlgorithmConstants.SMALL_COHORT_MATURE_MAX_ALPHA : PredictAlgorithmConstants.MATURE_STAGE_MAX_ALPHA;

        if (maxDays <= 7) {
            // 算法 1：样本消耗加权贝叶斯先验收缩 (Spend-Weighted Prior Shrinkage)
            double priorWeight = Math.min(0.20, Math.max(0.05, 1000.0 / Math.max(100.0, spendVal) * 0.05));
            alpha = (actualRoi + priorWeight) / (baseRoiSum + priorWeight);

            // 算法 3：极早期离群点截断
            alpha = Math.max(PredictAlgorithmConstants.EARLY_STAGE_MIN_ALPHA,
                    Math.min(PredictAlgorithmConstants.EARLY_STAGE_MAX_ALPHA, alpha));

            // 算法 2：未发生周划扣期平滑权重 (w: 0.15 ~ 0.25)
            w = 0.15 + 0.10 * ((double) maxDays / 7.0);
        } else if (maxDays <= 14) {
            // 算法 2：首个周划扣周期内 (7~14天) 平滑渐进权重 (w: 0.25 ~ 0.85)
            alpha = Math.max(PredictAlgorithmConstants.MATURE_STAGE_MIN_ALPHA,
                    Math.min(maxAlpha, rawAlpha));

            w = 0.25 + 0.60 * ((double) (maxDays - 7) / 7.0);
        } else {
            // 算法 3：跨过双周划扣节点后的离群点熔断 (保持回本模型精度)
            alpha = Math.max(PredictAlgorithmConstants.MATURE_STAGE_MIN_ALPHA,
                    Math.min(maxAlpha, rawAlpha));

            // 算法 2：成熟期高信任真实划扣数据 (w: 0.85 ~ 0.95)
            w = 0.85 + 0.10 * ((double) Math.min(46, maxDays - 14) / 46.0);
        }

        return w * alpha + (1.0 - w) * 1.0;
    }

    /**
     * OLS 对数回归拟合结果结构体
     */
    public static class OlsFitResult {
        public final double a;
        public final double b;
        public final double r2;
        public final boolean valid;

        public OlsFitResult(double a, double b, double r2, boolean valid) {
            this.a = a;
            this.b = b;
            this.r2 = r2;
            this.valid = valid;
        }
    }

    /**
     * 计算历史实际数据的对数拟合 ROI(t) = a * ln(t) + b 以及判定系数 R^2
     */
    public static OlsFitResult computeOlsFit(LtvDailyStat stat, int maxDays) {
        int limit = Math.min(maxDays, 60);
        List<Double> xList = new ArrayList<>();
        List<Double> yList = new ArrayList<>();

        for (int d = 1; d <= limit; d++) {
            BigDecimal roi = CohortStatHelper.getRoiForDay(stat, d);
            if (roi != null && roi.compareTo(BigDecimal.ZERO) > 0) {
                xList.add(Math.log(d));
                yList.add(roi.doubleValue());
            }
        }

        int n = xList.size();
        if (n < 5) {
            return new OlsFitResult(0, 0, 0, false);
        }

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

        double meanY = sumY / n;
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < n; i++) {
            double y = yList.get(i);
            double yPred = a * xList.get(i) + b;
            ssTot += (y - meanY) * (y - meanY);
            ssRes += (y - yPred) * (y - yPred);
        }

        double r2 = (ssTot > 0.0001) ? Math.max(0.0, 1.0 - ssRes / ssTot) : 0.0;
        boolean valid = (a >= PredictAlgorithmConstants.OLS_ENSEMBLE_MIN_SLOPE && r2 >= PredictAlgorithmConstants.OLS_ENSEMBLE_MIN_R2);

        return new OlsFitResult(a, b, r2, valid);
    }

    /**
     * 计算成熟期 (D14+) 双轨 OLS 动量动态系综融合权重 lambda
     */
    public static double computeOlsEnsembleWeight(double r2, int maxDays) {
        if (maxDays < PredictAlgorithmConstants.OLS_ENSEMBLE_MIN_DAYS || r2 < PredictAlgorithmConstants.OLS_ENSEMBLE_MIN_R2) {
            return 0.0;
        }
        double r2Bonus = Math.min(1.0, (r2 - PredictAlgorithmConstants.OLS_ENSEMBLE_MIN_R2) / (1.0 - PredictAlgorithmConstants.OLS_ENSEMBLE_MIN_R2));
        double timeBonus = Math.min(1.0, (double) (maxDays - PredictAlgorithmConstants.OLS_ENSEMBLE_MIN_DAYS) / 20.0);
        double weight = PredictAlgorithmConstants.OLS_ENSEMBLE_MAX_WEIGHT * (0.50 + 0.50 * r2Bonus) * (0.30 + 0.70 * timeBonus);
        return Math.max(0.0, Math.min(PredictAlgorithmConstants.OLS_ENSEMBLE_MAX_WEIGHT, weight));
    }

    /**
     * 兜底线性对数拟合方法 (OLS)
     */
    public PredictionResult predictCohortOlsFallback(LtvDailyStat stat, int daysElapsed) {
        int maxDays = Math.min(daysElapsed, 60);
        OlsFitResult fit = computeOlsFit(stat, maxDays);

        if (fit.a > 0.0001) {
            double tPayback = Math.exp((1.0 - fit.b) / fit.a);
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
