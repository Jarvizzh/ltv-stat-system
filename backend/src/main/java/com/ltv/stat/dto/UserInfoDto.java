package com.ltv.stat.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息表项 VO/DTO
 */
public class UserInfoDto {
    private Long id;
    private String username;
    private String role;
    private Integer status;
    private LocalDateTime createdAt;
    private List<String> landingPageIds;
    private Integer landingPageCount;
    private List<Long> visibleUserIds;
    private Integer isMaster;
    private List<Long> subUserIds;
    private Integer permPredictPayback;
    private Integer permRoiPredict;
    private Integer permGlobalDistribution;
    private Integer permExport;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<String> getLandingPageIds() { return landingPageIds; }
    public void setLandingPageIds(List<String> landingPageIds) { this.landingPageIds = landingPageIds; }

    public Integer getLandingPageCount() { return landingPageCount; }
    public void setLandingPageCount(Integer landingPageCount) { this.landingPageCount = landingPageCount; }

    public List<Long> getVisibleUserIds() { return visibleUserIds; }
    public void setVisibleUserIds(List<Long> visibleUserIds) { this.visibleUserIds = visibleUserIds; }

    public Integer getIsMaster() { return isMaster; }
    public void setIsMaster(Integer isMaster) { this.isMaster = isMaster; }

    public List<Long> getSubUserIds() { return subUserIds; }
    public void setSubUserIds(List<Long> subUserIds) { this.subUserIds = subUserIds; }

    public Integer getPermPredictPayback() { return permPredictPayback != null ? permPredictPayback : 0; }
    public void setPermPredictPayback(Integer permPredictPayback) { this.permPredictPayback = permPredictPayback; }

    public Integer getPermRoiPredict() { return permRoiPredict != null ? permRoiPredict : 0; }
    public void setPermRoiPredict(Integer permRoiPredict) { this.permRoiPredict = permRoiPredict; }

    public Integer getPermGlobalDistribution() { return permGlobalDistribution != null ? permGlobalDistribution : 0; }
    public void setPermGlobalDistribution(Integer permGlobalDistribution) { this.permGlobalDistribution = permGlobalDistribution; }

    public Integer getPermExport() { return permExport != null ? permExport : 0; }
    public void setPermExport(Integer permExport) { this.permExport = permExport; }
}
