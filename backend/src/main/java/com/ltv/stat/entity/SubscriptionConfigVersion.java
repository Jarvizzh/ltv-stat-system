package com.ltv.stat.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_config_version", indexes = {
    @Index(name = "idx_pid_first_price", columnList = "landing_page_id, first_price_cent, effective_start_time")
})
public class SubscriptionConfigVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "landing_page_id", nullable = false)
    private String landingPageId;

    @Column(name = "subscribe_config_id", nullable = false)
    private String subscribeConfigId;

    @Column(name = "subscribe_config_name")
    private String subscribeConfigName;

    @Column(name = "sale_combo_id")
    private String saleComboId;

    @Column(name = "sale_combo_name")
    private String saleComboName;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "sub_period_days", nullable = false)
    private Integer subPeriodDays = 1;

    @Column(name = "first_price_cent", nullable = false)
    private Integer firstPriceCent;

    @Column(name = "renew_price_cent", nullable = false)
    private Integer renewPriceCent;

    @Column(name = "version_num", nullable = false)
    private Integer versionNum = 1;

    @Column(name = "effective_start_time", nullable = false)
    private LocalDateTime effectiveStartTime;

    @Column(name = "effective_end_time")
    private LocalDateTime effectiveEndTime;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLandingPageId() { return landingPageId; }
    public void setLandingPageId(String landingPageId) { this.landingPageId = landingPageId; }

    public String getSubscribeConfigId() { return subscribeConfigId; }
    public void setSubscribeConfigId(String subscribeConfigId) { this.subscribeConfigId = subscribeConfigId; }

    public String getSubscribeConfigName() { return subscribeConfigName; }
    public void setSubscribeConfigName(String subscribeConfigName) { this.subscribeConfigName = subscribeConfigName; }

    public String getSaleComboId() { return saleComboId; }
    public void setSaleComboId(String saleComboId) { this.saleComboId = saleComboId; }

    public String getSaleComboName() { return saleComboName; }
    public void setSaleComboName(String saleComboName) { this.saleComboName = saleComboName; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getSubPeriodDays() { return subPeriodDays; }
    public void setSubPeriodDays(Integer subPeriodDays) { this.subPeriodDays = subPeriodDays; }

    public Integer getFirstPriceCent() { return firstPriceCent; }
    public void setFirstPriceCent(Integer firstPriceCent) { this.firstPriceCent = firstPriceCent; }

    public Integer getRenewPriceCent() { return renewPriceCent; }
    public void setRenewPriceCent(Integer renewPriceCent) { this.renewPriceCent = renewPriceCent; }

    public Integer getVersionNum() { return versionNum; }
    public void setVersionNum(Integer versionNum) { this.versionNum = versionNum; }

    public LocalDateTime getEffectiveStartTime() { return effectiveStartTime; }
    public void setEffectiveStartTime(LocalDateTime effectiveStartTime) { this.effectiveStartTime = effectiveStartTime; }

    public LocalDateTime getEffectiveEndTime() { return effectiveEndTime; }
    public void setEffectiveEndTime(LocalDateTime effectiveEndTime) { this.effectiveEndTime = effectiveEndTime; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
