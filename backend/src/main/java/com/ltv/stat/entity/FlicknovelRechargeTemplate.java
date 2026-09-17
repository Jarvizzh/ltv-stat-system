package com.ltv.stat.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 番茄海外充值模板实体 (v2)
 */
@Entity
@Table(name = "flicknovel_recharge_template", indexes = {
    @Index(name = "idx_fn_tpl_dist_app_id", columnList = "dist_app_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_fn_tpl_template_id", columnNames = {"template_id"})
})
public class FlicknovelRechargeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id", nullable = false, length = 64)
    private String templateId;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "dist_app_id")
    private Long distAppId;

    /**
     * 存储解析好的价格字典 JSON，格式形如：{"1199": 0, "1999": 1, "2999": 1, "3999": 0}
     */
    @Column(name = "price_config_json", columnDefinition = "TEXT")
    private String priceConfigJson;

    @Column(name = "raw_payload", columnDefinition = "MEDIUMTEXT")
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

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getDistAppId() { return distAppId; }
    public void setDistAppId(Long distAppId) { this.distAppId = distAppId; }

    public String getPriceConfigJson() { return priceConfigJson; }
    public void setPriceConfigJson(String priceConfigJson) { this.priceConfigJson = priceConfigJson; }

    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
