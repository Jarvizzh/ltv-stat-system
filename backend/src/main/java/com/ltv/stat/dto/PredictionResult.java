package com.ltv.stat.dto;

import java.math.BigDecimal;

/**
 * LTV 预测结果 DTO
 */
public class PredictionResult {
    private final Integer predictedPaybackDays;
    private final Integer paybackCycleDays;
    private final BigDecimal predictedDay30Roi;
    private final BigDecimal predictedDay60Roi;
    private final BigDecimal predictedDay90Roi;
    private final BigDecimal predictedDay30Recharge;
    private final BigDecimal predictedDay60Recharge;
    private final BigDecimal predictedDay90Recharge;

    public PredictionResult(Integer predictedPaybackDays) {
        this(predictedPaybackDays, null, null, null, null, null, null, null);
    }

    public PredictionResult(Integer predictedPaybackDays, Integer paybackCycleDays) {
        this(predictedPaybackDays, paybackCycleDays, null, null, null, null, null, null);
    }

    public PredictionResult(Integer predictedPaybackDays, Integer paybackCycleDays,
                            BigDecimal predictedDay60Roi, BigDecimal predictedDay90Roi,
                            BigDecimal predictedDay60Recharge, BigDecimal predictedDay90Recharge) {
        this(predictedPaybackDays, paybackCycleDays, null, predictedDay60Roi, predictedDay90Roi, null, predictedDay60Recharge, predictedDay90Recharge);
    }

    public PredictionResult(Integer predictedPaybackDays, Integer paybackCycleDays,
                            BigDecimal predictedDay30Roi, BigDecimal predictedDay60Roi, BigDecimal predictedDay90Roi,
                            BigDecimal predictedDay30Recharge, BigDecimal predictedDay60Recharge, BigDecimal predictedDay90Recharge) {
        this.predictedPaybackDays = predictedPaybackDays;
        this.paybackCycleDays = paybackCycleDays;
        this.predictedDay30Roi = predictedDay30Roi;
        this.predictedDay60Roi = predictedDay60Roi;
        this.predictedDay90Roi = predictedDay90Roi;
        this.predictedDay30Recharge = predictedDay30Recharge;
        this.predictedDay60Recharge = predictedDay60Recharge;
        this.predictedDay90Recharge = predictedDay90Recharge;
    }

    public Integer getPredictedPaybackDays() { return predictedPaybackDays; }
    public Integer getPaybackCycleDays() { return paybackCycleDays; }
    public BigDecimal getPredictedDay30Roi() { return predictedDay30Roi; }
    public BigDecimal getPredictedDay60Roi() { return predictedDay60Roi; }
    public BigDecimal getPredictedDay90Roi() { return predictedDay90Roi; }
    public BigDecimal getPredictedDay30Recharge() { return predictedDay30Recharge; }
    public BigDecimal getPredictedDay60Recharge() { return predictedDay60Recharge; }
    public BigDecimal getPredictedDay90Recharge() { return predictedDay90Recharge; }
}
