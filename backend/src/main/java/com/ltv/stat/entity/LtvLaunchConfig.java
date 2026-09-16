package com.ltv.stat.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ltv_launch_config")
@IdClass(LtvLaunchConfigId.class)
public class LtvLaunchConfig {

    @Id
    @Column(name = "platform_code", nullable = false, length = 32)
    private String platformCode = "rocnovel";

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "launch_date", nullable = false)
    private LocalDate launchDate;

    @Column(name = "spend", nullable = false, precision = 10, scale = 2)
    private BigDecimal spend = BigDecimal.ZERO;

    @Column(name = "remark", length = 500)
    private String remark = "";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getPlatformCode() { return platformCode != null ? platformCode : "rocnovel"; }
    public void setPlatformCode(String platformCode) { this.platformCode = platformCode; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getLaunchDate() { return launchDate; }
    public void setLaunchDate(LocalDate launchDate) { this.launchDate = launchDate; }

    public BigDecimal getSpend() { return spend; }
    public void setSpend(BigDecimal spend) { this.spend = spend; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
