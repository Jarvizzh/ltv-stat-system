package com.ltv.stat.dto;

import java.util.List;

/**
 * 用户落地页配置更新请求 DTO
 */
public class UserLandingPageUpdateRequestDto {
    private Long targetUserId;
    private List<LandingPageConfigItem> landingPages;
    private List<String> landingPageIds;

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public List<LandingPageConfigItem> getLandingPages() { return landingPages; }
    public void setLandingPages(List<LandingPageConfigItem> landingPages) { this.landingPages = landingPages; }

    public List<String> getLandingPageIds() { return landingPageIds; }
    public void setLandingPageIds(List<String> landingPageIds) { this.landingPageIds = landingPageIds; }
}
