package com.ltv.stat.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 番茄海外染色归因明细实体
 */
@Entity
@Table(name = "flicknovel_relation", indexes = {
    @Index(name = "idx_fn_device_id", columnList = "device_id"),
    @Index(name = "idx_fn_promotion_id", columnList = "promotion_id"),
    @Index(name = "idx_fn_begin_time_bj", columnList = "relation_begin_time_bj"),
    @Index(name = "idx_fn_begin_date_et", columnList = "relation_begin_date_et"),
    @Index(name = "idx_fn_media_channel", columnList = "media_channel")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_fn_relation_id", columnNames = {"relation_id"})
})
public class FlicknovelRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "relation_id", nullable = false, length = 64)
    private String relationId;

    @Column(name = "device_id", nullable = false, length = 128)
    private String deviceId;

    @Column(name = "promotion_id", length = 64)
    private String promotionId;

    @Column(name = "promotion_code", length = 64)
    private String promotionCode;

    @Column(name = "ad_id", length = 64)
    private String adId;

    @Column(name = "adset_id", length = 64)
    private String adsetId;

    @Column(name = "campaign_id", length = 64)
    private String campaignId;

    @Column(name = "ad_account_id", length = 64)
    private String adAccountId;

    @Column(name = "relation_begin_time_bj")
    private LocalDateTime relationBeginTimeBj;

    @Column(name = "relation_begin_time_et")
    private LocalDateTime relationBeginTimeEt;

    @Column(name = "relation_begin_date_et")
    private LocalDate relationBeginDateEt;

    @Column(name = "relation_begin_timestamp")
    private Long relationBeginTimestamp;

    @Column(name = "media_channel", length = 64)
    private String mediaChannel;

    @Column(name = "platform", length = 32)
    private String platform;

    @Column(name = "app_id", length = 32)
    private String appId;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRelationId() { return relationId; }
    public void setRelationId(String relationId) { this.relationId = relationId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getPromotionId() { return promotionId; }
    public void setPromotionId(String promotionId) { this.promotionId = promotionId; }

    public String getPromotionCode() { return promotionCode; }
    public void setPromotionCode(String promotionCode) { this.promotionCode = promotionCode; }

    public String getAdId() { return adId; }
    public void setAdId(String adId) { this.adId = adId; }

    public String getAdsetId() { return adsetId; }
    public void setAdsetId(String adsetId) { this.adsetId = adsetId; }

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public String getAdAccountId() { return adAccountId; }
    public void setAdAccountId(String adAccountId) { this.adAccountId = adAccountId; }

    public LocalDateTime getRelationBeginTimeBj() { return relationBeginTimeBj; }
    public void setRelationBeginTimeBj(LocalDateTime relationBeginTimeBj) { this.relationBeginTimeBj = relationBeginTimeBj; }

    public LocalDateTime getRelationBeginTimeEt() { return relationBeginTimeEt; }
    public void setRelationBeginTimeEt(LocalDateTime relationBeginTimeEt) { this.relationBeginTimeEt = relationBeginTimeEt; }

    public LocalDate getRelationBeginDateEt() { return relationBeginDateEt; }
    public void setRelationBeginDateEt(LocalDate relationBeginDateEt) { this.relationBeginDateEt = relationBeginDateEt; }

    public Long getRelationBeginTimestamp() { return relationBeginTimestamp; }
    public void setRelationBeginTimestamp(Long relationBeginTimestamp) { this.relationBeginTimestamp = relationBeginTimestamp; }

    public String getMediaChannel() { return mediaChannel; }
    public void setMediaChannel(String mediaChannel) { this.mediaChannel = mediaChannel; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
