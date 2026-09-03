package com.ltv.stat.dto;

import java.math.BigDecimal;

public class MonthlySettlementSaveRequestDto {

    private String settlementType; // "PLATFORM_ALL", "USER_ACCOUNT", "UNLINKED_PID"
    private Long targetUserId;
    private String monthStr; // e.g. "2026-07"
    private BigDecimal settledRefundAmount; // 已结算退款金额
    private BigDecimal monthSettledRefundAmount; // 当月结算退款金额
    private BigDecimal crossPeriodRefundAmount; // 跨周期退款金额
    private BigDecimal shareRatio; // 分成比例 (e.g. 0.95)
    private BigDecimal channelFeeRate; // 渠道费率 (e.g. 0.07)
    private String remark;

    public MonthlySettlementSaveRequestDto() {}

    public String getSettlementType() { return settlementType; }
    public void setSettlementType(String settlementType) { this.settlementType = settlementType; }

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public String getMonthStr() { return monthStr; }
    public void setMonthStr(String monthStr) { this.monthStr = monthStr; }

    public BigDecimal getSettledRefundAmount() { return settledRefundAmount; }
    public void setSettledRefundAmount(BigDecimal settledRefundAmount) { this.settledRefundAmount = settledRefundAmount; }

    public BigDecimal getMonthSettledRefundAmount() { return monthSettledRefundAmount; }
    public void setMonthSettledRefundAmount(BigDecimal monthSettledRefundAmount) { this.monthSettledRefundAmount = monthSettledRefundAmount; }

    public BigDecimal getCrossPeriodRefundAmount() { return crossPeriodRefundAmount; }
    public void setCrossPeriodRefundAmount(BigDecimal crossPeriodRefundAmount) { this.crossPeriodRefundAmount = crossPeriodRefundAmount; }

    public BigDecimal getShareRatio() { return shareRatio; }
    public void setShareRatio(BigDecimal shareRatio) { this.shareRatio = shareRatio; }

    public BigDecimal getChannelFeeRate() { return channelFeeRate; }
    public void setChannelFeeRate(BigDecimal channelFeeRate) { this.channelFeeRate = channelFeeRate; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
