package com.ltv.stat.dto;

import java.util.List;

/**
 * 创建用户请求 DTO
 */
public class CreateUserRequestDto {
    private String username;
    private String password;
    private String role;
    private Integer isMaster;
    private Integer isSettlement;
    private List<Long> visibleUserIds;
    private List<Long> subUserIds;
    private Integer permPredictPayback;
    private Integer permRoiPredict;
    private Integer permGlobalDistribution;
    private Integer permExport;
    private Integer permSettlement;
    private Integer permVideoGen;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getIsMaster() { return isMaster; }
    public void setIsMaster(Integer isMaster) { this.isMaster = isMaster; }

    public Integer getIsSettlement() { return isSettlement != null ? isSettlement : 0; }
    public void setIsSettlement(Integer isSettlement) { this.isSettlement = isSettlement; }

    public List<Long> getVisibleUserIds() { return visibleUserIds; }
    public void setVisibleUserIds(List<Long> visibleUserIds) { this.visibleUserIds = visibleUserIds; }

    public List<Long> getSubUserIds() { return subUserIds; }
    public void setSubUserIds(List<Long> subUserIds) { this.subUserIds = subUserIds; }

    public Integer getPermPredictPayback() { return permPredictPayback; }
    public void setPermPredictPayback(Integer permPredictPayback) { this.permPredictPayback = permPredictPayback; }

    public Integer getPermRoiPredict() { return permRoiPredict; }
    public void setPermRoiPredict(Integer permRoiPredict) { this.permRoiPredict = permRoiPredict; }

    public Integer getPermGlobalDistribution() { return permGlobalDistribution; }
    public void setPermGlobalDistribution(Integer permGlobalDistribution) { this.permGlobalDistribution = permGlobalDistribution; }

    public Integer getPermExport() { return permExport; }
    public void setPermExport(Integer permExport) { this.permExport = permExport; }

    public Integer getPermSettlement() { return permSettlement; }
    public void setPermSettlement(Integer permSettlement) { this.permSettlement = permSettlement; }

    public Integer getPermVideoGen() { return permVideoGen; }
    public void setPermVideoGen(Integer permVideoGen) { this.permVideoGen = permVideoGen; }
}
