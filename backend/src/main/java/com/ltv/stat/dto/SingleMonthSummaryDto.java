package com.ltv.stat.dto;

import java.math.BigDecimal;

/**
 * 单月（本月/上月）指标汇总 DTO/VO
 */
public class SingleMonthSummaryDto {
    private String month;
    private BigDecimal spend;
    private BigDecimal recharge;
    private BigDecimal refund;
    private BigDecimal profit;
    private BigDecimal roi;
    private Integer subUsers;
    private Integer retainedSubUsers;
    private String retainedRate;
    private Integer actualPaybackDays;
    private BigDecimal predictedDay30Roi;
    private BigDecimal predictedDay60Roi;
    private BigDecimal predictedDay90Roi;

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public BigDecimal getSpend() { return spend; }
    public void setSpend(BigDecimal spend) { this.spend = spend; }

    public BigDecimal getRecharge() { return recharge; }
    public void setRecharge(BigDecimal recharge) { this.recharge = recharge; }

    public BigDecimal getRefund() { return refund; }
    public void setRefund(BigDecimal refund) { this.refund = refund; }

    public BigDecimal getProfit() { return profit; }
    public void setProfit(BigDecimal profit) { this.profit = profit; }

    public BigDecimal getRoi() { return roi; }
    public void setRoi(BigDecimal roi) { this.roi = roi; }

    public Integer getSubUsers() { return subUsers; }
    public void setSubUsers(Integer subUsers) { this.subUsers = subUsers; }

    public Integer getRetainedSubUsers() { return retainedSubUsers; }
    public void setRetainedSubUsers(Integer retainedSubUsers) { this.retainedSubUsers = retainedSubUsers; }

    public String getRetainedRate() { return retainedRate; }
    public void setRetainedRate(String retainedRate) { this.retainedRate = retainedRate; }

    public Integer getActualPaybackDays() { return actualPaybackDays; }
    public void setActualPaybackDays(Integer actualPaybackDays) { this.actualPaybackDays = actualPaybackDays; }

    public BigDecimal getPredictedDay30Roi() { return predictedDay30Roi; }
    public void setPredictedDay30Roi(BigDecimal predictedDay30Roi) { this.predictedDay30Roi = predictedDay30Roi; }

    public BigDecimal getPredictedDay60Roi() { return predictedDay60Roi; }
    public void setPredictedDay60Roi(BigDecimal predictedDay60Roi) { this.predictedDay60Roi = predictedDay60Roi; }

    public BigDecimal getPredictedDay90Roi() { return predictedDay90Roi; }
    public void setPredictedDay90Roi(BigDecimal predictedDay90Roi) { this.predictedDay90Roi = predictedDay90Roi; }
}
