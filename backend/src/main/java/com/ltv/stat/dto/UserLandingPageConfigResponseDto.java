package com.ltv.stat.dto;

import java.util.List;

/**
 * 用户落地页配置查询响应 DTO
 */
public class UserLandingPageConfigResponseDto {
    private int code;
    private List<LandingPageConfigItem> data;
    private List<String> landingPageIds;

    public UserLandingPageConfigResponseDto() {
        this.code = 0;
    }

    public UserLandingPageConfigResponseDto(List<LandingPageConfigItem> data, List<String> landingPageIds) {
        this.code = 0;
        this.data = data;
        this.landingPageIds = landingPageIds;
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public List<LandingPageConfigItem> getData() { return data; }
    public void setData(List<LandingPageConfigItem> data) { this.data = data; }

    public List<String> getLandingPageIds() { return landingPageIds; }
    public void setLandingPageIds(List<String> landingPageIds) { this.landingPageIds = landingPageIds; }
}
