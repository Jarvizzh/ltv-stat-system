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

    // P2 模块 1：贝叶斯单客充值力 (Realized ARPU) 萃取超参数
    public static final double ARPU_SHRINKAGE_K_USER = 5.0; // 用户量先验收缩常数 K_user

    // P2 模块 2：成熟期 (D14+) 双轨 OLS 动量系综超参数
    public static final int OLS_ENSEMBLE_MIN_DAYS = 14;      // 激活 OLS 动量融合的最小观察天数
    public static final double OLS_ENSEMBLE_MIN_R2 = 0.85;   // 激活 OLS 融合的拟合优度 R^2 门槛
    public static final double OLS_ENSEMBLE_MIN_SLOPE = 0.03;// 激活 OLS 融合的最小增长斜率 a
    public static final double OLS_ENSEMBLE_MAX_WEIGHT = 0.45;// OLS 动量最大融合权重 lambda

    // P2 模块 3：小样本 (N <= 5) 活跃大户自适应松绑超参数
    public static final int SMALL_COHORT_MAX_USERS = 5;
    public static final double SMALL_COHORT_CONTINUITY_THRESHOLD = 0.70; // 近7天有>=70%天数连续产生充值
    public static final double SMALL_COHORT_MIN_ROI = 0.40;              // 激活松绑的最小实际达成 ROI
    public static final double SMALL_COHORT_MATURE_MAX_ALPHA = 3.50;     // 小样本松绑后的最大放缩上限
    public static final double SMALL_COHORT_SCALE_DECAY_EXPONENT = 0.15; // 小样本松绑后的远期衰减指数

    // 默认兜底价格常量：周订 19.99，日订 9.99
    public static final double DEFAULT_DAILY_SUB_PRICE = 9.99;
    public static final double DEFAULT_WEEKLY_SUB_PRICE = 19.99;
}
