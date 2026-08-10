package com.ltv.stat.service;

/**
 * 预测曲线推演中针对特定订阅周期的基准与指标上下文对象
 */
public class PeriodContext {
    public int periodDays;
    public int userCount;
    public double[] baseRet = new double[366];
    public double[] baseArpu = new double[366];
    public Double configRenewUsd;
    public Double configFirstUsd;
}
