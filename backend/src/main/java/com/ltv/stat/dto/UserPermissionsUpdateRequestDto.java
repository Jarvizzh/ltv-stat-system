package com.ltv.stat.dto;

public class UserPermissionsUpdateRequestDto {

    private Integer permPredictPayback;
    private Integer permRoiPredict;
    private Integer permGlobalDistribution;
    private Integer permExport;
    private Integer permSettlement;

    public UserPermissionsUpdateRequestDto() {}

    public UserPermissionsUpdateRequestDto(Integer permPredictPayback, Integer permRoiPredict, Integer permGlobalDistribution, Integer permExport, Integer permSettlement) {
        this.permPredictPayback = permPredictPayback;
        this.permRoiPredict = permRoiPredict;
        this.permGlobalDistribution = permGlobalDistribution;
        this.permExport = permExport;
        this.permSettlement = permSettlement;
    }

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
}
