package com.ltv.stat.service.engine;

/**
 * 预测算法超参数与熔断常量集中管理类
 */
public final class PredictAlgorithmConstants {

    private PredictAlgorithmConstants() {}

    // 均值回归与续扣脉冲自然衰减指数
    public static final double SCALE_DECAY_EXPONENT = 0.35; // 均值回归放缩因子衰减指数
    public static final double CYCLE_DECAY_EXPONENT = 0.06; // 续扣脉冲自然衰减指数

    // 极早期 (maxDays < 7) 贝叶斯先验与离群点截断
    public static final double EARLY_STAGE_MIN_ALPHA = 0.80;
    public static final double EARLY_STAGE_MAX_ALPHA = 1.25;

    // 成熟期 (maxDays >= 7) 离群点熔断区间
    public static final double MATURE_STAGE_MIN_ALPHA = 0.60;
    public static final double MATURE_STAGE_MAX_ALPHA = 2.00;

    // 平盘防守窗口配置 (requiredFlatDays = max(MIN_FLAT_DAYS, maxPeriod * PERIOD_FLAT_MULTIPLIER))
    public static final int MIN_FLAT_DAYS = 6;
    public static final int PERIOD_FLAT_MULTIPLIER = 2;

    // ROI 里程碑 (D30, D60, D90) 动态上限保护系数
    public static final double D30_BASE_MAX_ROI = 1.50;
    public static final double D30_MAX_ROI_MULT = 1.25;
    public static final double D30_MAX_ROI_ADD = 0.28;

    public static final double D60_BASE_MAX_ROI = 1.80;
    public static final double D60_MAX_ROI_MULT = 1.45;
    public static final double D60_MAX_ROI_ADD = 0.50;

    public static final double D90_BASE_MAX_ROI = 2.20;
    public static final double D90_MAX_ROI_MULT = 1.70;
    public static final double D90_MAX_ROI_ADD = 0.75;
}
