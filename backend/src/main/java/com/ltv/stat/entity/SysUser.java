package com.ltv.stat.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_user")
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "role", nullable = false, length = 20)
    private String role; // "ADMIN" or "USER"

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 1: Active, 0: Disabled

    @Column(name = "is_master", nullable = false)
    private Integer isMaster = 0; // 0: Normal/Sub, 1: Master Account

    @Column(name = "is_settlement", nullable = false)
    private Integer isSettlement = 0; // 0: 不参与结算, 1: 参与结算账号 (由超级管理员独立勾选配置)

    // 细粒度功能权限 (0: 无权限, 1: 拥有权限，超级管理员默认拥有所有权限)
    @Column(name = "perm_predict_payback", nullable = false)
    private Integer permPredictPayback = 0; // 预测回本（包括LTV表格预测回本列）

    @Column(name = "perm_roi_predict", nullable = false)
    private Integer permRoiPredict = 0; // （D30~D90）ROI预测

    @Column(name = "perm_global_distribution", nullable = false)
    private Integer permGlobalDistribution = 0; // 平台汇总

    @Column(name = "perm_export", nullable = false)
    private Integer permExport = 0; // 数据导出

    @Column(name = "perm_settlement", nullable = false)
    private Integer permSettlement = 0; // 月份结算

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getIsMaster() { return isMaster != null ? isMaster : 0; }
    public void setIsMaster(Integer isMaster) { this.isMaster = isMaster; }
    public boolean isMasterAccount() { return Integer.valueOf(1).equals(this.isMaster); }

    public Integer getIsSettlement() { return isSettlement != null ? isSettlement : 0; }
    public void setIsSettlement(Integer isSettlement) { this.isSettlement = isSettlement; }
    public boolean isSettlementAccount() { return Integer.valueOf(1).equals(this.isSettlement); }

    public boolean isSuperAdmin() { return "SUPER_ADMIN".equalsIgnoreCase(this.role); }

    public Integer getPermPredictPayback() { return permPredictPayback != null ? permPredictPayback : 0; }
    public void setPermPredictPayback(Integer permPredictPayback) { this.permPredictPayback = permPredictPayback; }
    public boolean hasPermPredictPayback() { return isSuperAdmin() || Integer.valueOf(1).equals(this.permPredictPayback); }

    public Integer getPermRoiPredict() { return permRoiPredict != null ? permRoiPredict : 0; }
    public void setPermRoiPredict(Integer permRoiPredict) { this.permRoiPredict = permRoiPredict; }
    public boolean hasPermRoiPredict() { return isSuperAdmin() || Integer.valueOf(1).equals(this.permRoiPredict); }

    public Integer getPermGlobalDistribution() { return permGlobalDistribution != null ? permGlobalDistribution : 0; }
    public void setPermGlobalDistribution(Integer permGlobalDistribution) { this.permGlobalDistribution = permGlobalDistribution; }
    public boolean hasPermGlobalDistribution() { return isSuperAdmin() || Integer.valueOf(1).equals(this.permGlobalDistribution); }

    public Integer getPermExport() { return permExport != null ? permExport : 0; }
    public void setPermExport(Integer permExport) { this.permExport = permExport; }
    public boolean hasPermExport() { return isSuperAdmin() || Integer.valueOf(1).equals(this.permExport); }

    public Integer getPermSettlement() { return permSettlement != null ? permSettlement : 0; }
    public void setPermSettlement(Integer permSettlement) { this.permSettlement = permSettlement; }
    public boolean hasPermSettlement() { return isSuperAdmin() || Integer.valueOf(1).equals(this.permSettlement); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
