package com.ltv.stat.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MonthlySettlementItemDto {

    private String monthStr; // e.g. "2026-07"
    private String settlementType; // "PLATFORM_ALL", "USER_ACCOUNT", "UNLINKED_PID"
    private Long targetUserId;
    private String targetUsername;

    private BigDecimal totalRecharge = BigDecimal.ZERO; // 当月总充值金额
    private BigDecimal totalRefund = BigDecimal.ZERO; // 当月总退款金额
    private BigDecimal settledRefundAmount = BigDecimal.ZERO; // 已结算退款金额 (用户填写)
    private BigDecimal monthSettledRefundAmount = BigDecimal.ZERO; // 当月结算退款金额 (用户填写)
    private BigDecimal unsettledRefundAmount = BigDecimal.ZERO; // 未结算退款差额 (totalRefund - settledRefundAmount)
    private BigDecimal crossPeriodRefundAmount = BigDecimal.ZERO; // 跨周期退款金额 (用户填写)

    private BigDecimal shareRatio = new BigDecimal("0.9500"); // 分成比例，默认 95%
    private BigDecimal channelFeeRate = new BigDecimal("0.0700"); // 渠道费率，默认 7%

    private BigDecimal effectiveBaseAmount = BigDecimal.ZERO; // 有效结算基数
    private BigDecimal finalSettlementAmount = BigDecimal.ZERO; // 最终结算金额

    private String refundRate = "0.00%"; // 退款率
    private Integer totalOrders = 0; // 总充值订单笔数
    private Integer refundOrders = 0; // 退款成功订单笔数

    private String remark = "";
    private LocalDateTime updatedAt;

    public MonthlySettlementItemDto() {}

    public String getMonthStr() { return monthStr; }
    public void setMonthStr(String monthStr) { this.monthStr = monthStr; }

    public String getSettlementType() { return settlementType; }
    public void setSettlementType(String settlementType) { this.settlementType = settlementType; }

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }

    public BigDecimal getTotalRecharge() { return totalRecharge; }
    public void setTotalRecharge(BigDecimal totalRecharge) { this.totalRecharge = totalRecharge; }

    public BigDecimal getTotalRefund() { return totalRefund; }
    public void setTotalRefund(BigDecimal totalRefund) { this.totalRefund = totalRefund; }

    public BigDecimal getSettledRefundAmount() { return settledRefundAmount; }
    public void setSettledRefundAmount(BigDecimal settledRefundAmount) { this.settledRefundAmount = settledRefundAmount; }

    public BigDecimal getMonthSettledRefundAmount() { return monthSettledRefundAmount; }
    public void setMonthSettledRefundAmount(BigDecimal monthSettledRefundAmount) { this.monthSettledRefundAmount = monthSettledRefundAmount; }

    public BigDecimal getUnsettledRefundAmount() { return unsettledRefundAmount; }
    public void setUnsettledRefundAmount(BigDecimal unsettledRefundAmount) { this.unsettledRefundAmount = unsettledRefundAmount; }

    public BigDecimal getCrossPeriodRefundAmount() { return crossPeriodRefundAmount; }
    public void setCrossPeriodRefundAmount(BigDecimal crossPeriodRefundAmount) { this.crossPeriodRefundAmount = crossPeriodRefundAmount; }

    public BigDecimal getShareRatio() { return shareRatio; }
    public void setShareRatio(BigDecimal shareRatio) { this.shareRatio = shareRatio; }

    public BigDecimal getChannelFeeRate() { return channelFeeRate; }
    public void setChannelFeeRate(BigDecimal channelFeeRate) { this.channelFeeRate = channelFeeRate; }

    public BigDecimal getEffectiveBaseAmount() { return effectiveBaseAmount; }
    public void setEffectiveBaseAmount(BigDecimal effectiveBaseAmount) { this.effectiveBaseAmount = effectiveBaseAmount; }

    public BigDecimal getFinalSettlementAmount() { return finalSettlementAmount; }
    public void setFinalSettlementAmount(BigDecimal finalSettlementAmount) { this.finalSettlementAmount = finalSettlementAmount; }

    public String getRefundRate() { return refundRate; }
    public void setRefundRate(String refundRate) { this.refundRate = refundRate; }

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }

    public Integer getRefundOrders() { return refundOrders; }
    public void setRefundOrders(Integer refundOrders) { this.refundOrders = refundOrders; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
