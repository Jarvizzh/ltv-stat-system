package com.ltv.stat.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_settlement_config", uniqueConstraints = {
    @UniqueConstraint(name = "uk_settle_type_user_month", columnNames = {"settlement_type", "target_user_id", "month_str"})
})
public class MonthlySettlementConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "settlement_type", nullable = false, length = 32)
    private String settlementType; // "PLATFORM_ALL", "USER_ACCOUNT", "UNLINKED_PID"

    @Column(name = "target_user_id")
    private Long targetUserId; // null for PLATFORM_ALL and UNLINKED_PID

    @Column(name = "month_str", nullable = false, length = 16)
    private String monthStr; // e.g. "2026-07"

    @Column(name = "settled_refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal settledRefundAmount = BigDecimal.ZERO; // 已结算退款金额

    @Column(name = "month_settled_refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthSettledRefundAmount = BigDecimal.ZERO; // 当月结算退款金额

    @Column(name = "cross_period_refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal crossPeriodRefundAmount = BigDecimal.ZERO; // 跨周期退款金额

    @Column(name = "share_ratio", nullable = false, precision = 6, scale = 4)
    private BigDecimal shareRatio = new BigDecimal("0.9500"); // 分成比例，默认 95%

    @Column(name = "channel_fee_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal channelFeeRate = new BigDecimal("0.0700"); // 渠道费率，默认 7%

    @Column(name = "remark", length = 500)
    private String remark = "";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSettlementType() { return settlementType; }
    public void setSettlementType(String settlementType) { this.settlementType = settlementType; }

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public String getMonthStr() { return monthStr; }
    public void setMonthStr(String monthStr) { this.monthStr = monthStr; }

    public BigDecimal getSettledRefundAmount() { return settledRefundAmount != null ? settledRefundAmount : BigDecimal.ZERO; }
    public void setSettledRefundAmount(BigDecimal settledRefundAmount) { this.settledRefundAmount = settledRefundAmount; }

    public BigDecimal getMonthSettledRefundAmount() { return monthSettledRefundAmount != null ? monthSettledRefundAmount : BigDecimal.ZERO; }
    public void setMonthSettledRefundAmount(BigDecimal monthSettledRefundAmount) { this.monthSettledRefundAmount = monthSettledRefundAmount; }

    public BigDecimal getCrossPeriodRefundAmount() { return crossPeriodRefundAmount != null ? crossPeriodRefundAmount : BigDecimal.ZERO; }
    public void setCrossPeriodRefundAmount(BigDecimal crossPeriodRefundAmount) { this.crossPeriodRefundAmount = crossPeriodRefundAmount; }

    public BigDecimal getShareRatio() { return shareRatio != null ? shareRatio : new BigDecimal("0.9500"); }
    public void setShareRatio(BigDecimal shareRatio) { this.shareRatio = shareRatio; }

    public BigDecimal getChannelFeeRate() { return channelFeeRate != null ? channelFeeRate : new BigDecimal("0.0700"); }
    public void setChannelFeeRate(BigDecimal channelFeeRate) { this.channelFeeRate = channelFeeRate; }

    public String getRemark() { return remark != null ? remark : ""; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
