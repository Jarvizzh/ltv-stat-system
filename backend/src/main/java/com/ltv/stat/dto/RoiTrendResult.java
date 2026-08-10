package com.ltv.stat.dto;

import java.math.BigDecimal;

/**
 * ROI 趋势预测结果 DTO
 */
public class RoiTrendResult {
    private final BigDecimal predD30Roi;
    private final BigDecimal predD60Roi;
    private final BigDecimal predD90Roi;
    private final BigDecimal predD30Recharge;
    private final BigDecimal predD60Recharge;
    private final BigDecimal predD90Recharge;

    public RoiTrendResult(BigDecimal predD30Roi, BigDecimal predD60Roi, BigDecimal predD90Roi,
                          BigDecimal predD30Recharge, BigDecimal predD60Recharge, BigDecimal predD90Recharge) {
        this.predD30Roi = predD30Roi;
        this.predD60Roi = predD60Roi;
        this.predD90Roi = predD90Roi;
        this.predD30Recharge = predD30Recharge;
        this.predD60Recharge = predD60Recharge;
        this.predD90Recharge = predD90Recharge;
    }

    public BigDecimal getPredD30Roi() { return predD30Roi; }
    public BigDecimal getPredD60Roi() { return predD60Roi; }
    public BigDecimal getPredD90Roi() { return predD90Roi; }
    public BigDecimal getPredD30Recharge() { return predD30Recharge; }
    public BigDecimal getPredD60Recharge() { return predD60Recharge; }
    public BigDecimal getPredD90Recharge() { return predD90Recharge; }
}
