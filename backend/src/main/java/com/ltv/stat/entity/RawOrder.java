package com.ltv.stat.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "raw_order", indexes = {
    @Index(name = "idx_reg_date", columnList = "register_date_et"),
    @Index(name = "idx_landing_page", columnList = "landing_page_id")
})
public class RawOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "landing_page_id")
    private String landingPageId;

    @Column(name = "register_time_bj", nullable = false)
    private LocalDateTime registerTimeBj;

    @Column(name = "register_time_et", nullable = false)
    private LocalDateTime registerTimeEt;

    @Column(name = "register_date_et", nullable = false)
    private LocalDate registerDateEt;

    @Column(name = "pay_time_bj", nullable = false)
    private LocalDateTime payTimeBj;

    @Column(name = "pay_time_et", nullable = false)
    private LocalDateTime payTimeEt;

    @Column(name = "pay_date_et", nullable = false)
    private LocalDate payDateEt;

    @Column(name = "order_amount_cent", nullable = false)
    private Integer orderAmountCent;

    @Column(name = "order_amount_usd", nullable = false, precision = 10, scale = 2)
    private BigDecimal orderAmountUsd;

    @Column(name = "is_subs")
    private Integer isSubs;

    @Column(name = "renew_type")
    private Integer renewType = 1;

    @Column(name = "pay_state")
    private Integer payState;

    @Column(name = "refund_status")
    private Integer refundStatus = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getLandingPageId() { return landingPageId; }
    public void setLandingPageId(String landingPageId) { this.landingPageId = landingPageId; }

    public LocalDateTime getRegisterTimeBj() { return registerTimeBj; }
    public void setRegisterTimeBj(LocalDateTime registerTimeBj) { this.registerTimeBj = registerTimeBj; }

    public LocalDateTime getRegisterTimeEt() { return registerTimeEt; }
    public void setRegisterTimeEt(LocalDateTime registerTimeEt) { this.registerTimeEt = registerTimeEt; }

    public LocalDate getRegisterDateEt() { return registerDateEt; }
    public void setRegisterDateEt(LocalDate registerDateEt) { this.registerDateEt = registerDateEt; }

    public LocalDateTime getPayTimeBj() { return payTimeBj; }
    public void setPayTimeBj(LocalDateTime payTimeBj) { this.payTimeBj = payTimeBj; }

    public LocalDateTime getPayTimeEt() { return payTimeEt; }
    public void setPayTimeEt(LocalDateTime payTimeEt) { this.payTimeEt = payTimeEt; }

    public LocalDate getPayDateEt() { return payDateEt; }
    public void setPayDateEt(LocalDate payDateEt) { this.payDateEt = payDateEt; }

    public Integer getOrderAmountCent() { return orderAmountCent; }
    public void setOrderAmountCent(Integer orderAmountCent) { this.orderAmountCent = orderAmountCent; }

    public BigDecimal getOrderAmountUsd() { return orderAmountUsd; }
    public void setOrderAmountUsd(BigDecimal orderAmountUsd) { this.orderAmountUsd = orderAmountUsd; }

    public Integer getIsSubs() { return isSubs; }
    public void setIsSubs(Integer isSubs) { this.isSubs = isSubs; }

    public Integer getRenewType() { return renewType != null ? renewType : 1; }
    public void setRenewType(Integer renewType) { this.renewType = renewType; }

    public Integer getPayState() { return payState; }
    public void setPayState(Integer payState) { this.payState = payState; }

    public Integer getRefundStatus() { return refundStatus != null ? refundStatus : 0; }
    public void setRefundStatus(Integer refundStatus) { this.refundStatus = refundStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
