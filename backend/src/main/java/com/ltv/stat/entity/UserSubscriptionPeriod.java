package com.ltv.stat.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_subscription_period", indexes = {
    @Index(name = "idx_member_id", columnList = "member_id")
})
public class UserSubscriptionPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, unique = true)
    private String memberId;

    @Column(name = "landing_page_id")
    private String landingPageId;

    @Column(name = "subscribe_config_id")
    private String subscribeConfigId;

    @Column(name = "sub_period_days", nullable = false)
    private Integer subPeriodDays = 1;

    @Column(name = "first_price_cent")
    private Integer firstPriceCent;

    @Column(name = "renew_price_cent")
    private Integer renewPriceCent;

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getLandingPageId() {
        return landingPageId;
    }

    public void setLandingPageId(String landingPageId) {
        this.landingPageId = landingPageId;
    }

    public String getSubscribeConfigId() {
        return subscribeConfigId;
    }

    public void setSubscribeConfigId(String subscribeConfigId) {
        this.subscribeConfigId = subscribeConfigId;
    }

    public Integer getSubPeriodDays() {
        return subPeriodDays;
    }

    public void setSubPeriodDays(Integer subPeriodDays) {
        this.subPeriodDays = subPeriodDays;
    }

    public Integer getFirstPriceCent() {
        return firstPriceCent;
    }

    public void setFirstPriceCent(Integer firstPriceCent) {
        this.firstPriceCent = firstPriceCent;
    }

    public Integer getRenewPriceCent() {
        return renewPriceCent;
    }

    public void setRenewPriceCent(Integer renewPriceCent) {
        this.renewPriceCent = renewPriceCent;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
