package com.ltv.stat.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 番茄司南推广链接实体
 */
@Entity
@Table(name = "flicknovel_promotion", indexes = {
    @Index(name = "idx_fn_prmt_recharge_tpl_id", columnList = "recharge_tpl_id"),
    @Index(name = "idx_fn_prmt_dist_app_id", columnList = "dist_app_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_fn_prmt_promotion_id", columnNames = {"promotion_id"})
})
public class FlicknovelPromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "promotion_id", nullable = false, length = 64)
    private String promotionId;

    @Column(name = "promotion_name", length = 255)
    private String promotionName;

    @Column(name = "recharge_tpl_id", length = 64)
    private String rechargeTplId;

    @Column(name = "recharge_tpl_name", length = 255)
    private String rechargeTplName;

    @Column(name = "dist_app_id")
    private Long distAppId;

    @Column(name = "drama_id", length = 64)
    private String dramaId;

    @Column(name = "drama_title", length = 255)
    private String dramaTitle;

    @Column(name = "chapter_id", length = 64)
    private String chapterId;

    @Column(name = "chapter_title", length = 255)
    private String chapterTitle;

    @Column(name = "media_channel")
    private Integer mediaChannel;

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

    public String getPromotionId() { return promotionId; }
    public void setPromotionId(String promotionId) { this.promotionId = promotionId; }

    public String getPromotionName() { return promotionName; }
    public void setPromotionName(String promotionName) { this.promotionName = promotionName; }

    public String getRechargeTplId() { return rechargeTplId; }
    public void setRechargeTplId(String rechargeTplId) { this.rechargeTplId = rechargeTplId; }

    public String getRechargeTplName() { return rechargeTplName; }
    public void setRechargeTplName(String rechargeTplName) { this.rechargeTplName = rechargeTplName; }

    public Long getDistAppId() { return distAppId; }
    public void setDistAppId(Long distAppId) { this.distAppId = distAppId; }

    public String getDramaId() { return dramaId; }
    public void setDramaId(String dramaId) { this.dramaId = dramaId; }

    public String getDramaTitle() { return dramaTitle; }
    public void setDramaTitle(String dramaTitle) { this.dramaTitle = dramaTitle; }

    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }

    public String getChapterTitle() { return chapterTitle; }
    public void setChapterTitle(String chapterTitle) { this.chapterTitle = chapterTitle; }

    public Integer getMediaChannel() { return mediaChannel; }
    public void setMediaChannel(Integer mediaChannel) { this.mediaChannel = mediaChannel; }

    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
