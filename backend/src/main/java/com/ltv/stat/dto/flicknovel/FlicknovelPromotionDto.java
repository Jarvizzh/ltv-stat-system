package com.ltv.stat.dto.flicknovel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 推广链接实体
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlicknovelPromotionDto {

    @JsonProperty("promotion_id")
    private String promotionId;

    @JsonProperty("promotion_name")
    private String promotionName;

    @JsonProperty("drama_id")
    private String dramaId;

    @JsonProperty("drama_title")
    private String dramaTitle;

    @JsonProperty("chapter_id")
    private String chapterId;

    @JsonProperty("chapter_title")
    private String chapterTitle;

    @JsonProperty("sort_no")
    private Integer sortNo;

    @JsonProperty("client_os")
    private Integer clientOs;

    @JsonProperty("media_channel")
    private Integer mediaChannel;

    @JsonProperty("landing_type")
    private Integer landingType;

    @JsonProperty("pixel_id")
    private String pixelId;

    @JsonProperty("pixel_name")
    private String pixelName;

    @JsonProperty("recharge_tpl_id")
    private String rechargeTplId;

    @JsonProperty("recharge_tpl_name")
    private String rechargeTplName;

    @JsonProperty("created_by")
    private Long createdBy;

    @JsonProperty("creator_name")
    private String creatorName;

    @JsonProperty("created_at")
    private Long createdAt;

    @JsonProperty("updated_at")
    private Long updatedAt;

    @JsonProperty("ad_convt_config_id")
    private String adConvtConfigId;

    @JsonProperty("ad_convt_config_name")
    private String adConvtConfigName;

    @JsonProperty("turn_tag")
    private Integer turnTag;

    @JsonProperty("app_id")
    private Long appId;

    @JsonProperty("dist_app_id")
    private Long distAppId;

    @JsonProperty("genre")
    private Integer genre;

    @JsonProperty("delivery_goal")
    private Integer deliveryGoal;

    @JsonProperty("land_info_detail")
    private LandInfoDetail landInfoDetail;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LandInfoDetail {
        @JsonProperty("deeplink")
        private String deeplink;

        @JsonProperty("landing_url")
        private String landingUrl;

        @JsonProperty("ad_group")
        private String adGroup;

        @JsonProperty("minis_link")
        private String minisLink;

        public String getDeeplink() { return deeplink; }
        public void setDeeplink(String deeplink) { this.deeplink = deeplink; }

        public String getLandingUrl() { return landingUrl; }
        public void setLandingUrl(String landingUrl) { this.landingUrl = landingUrl; }

        public String getAdGroup() { return adGroup; }
        public void setAdGroup(String adGroup) { this.adGroup = adGroup; }

        public String getMinisLink() { return minisLink; }
        public void setMinisLink(String minisLink) { this.minisLink = minisLink; }
    }

    public String getPromotionId() { return promotionId; }
    public void setPromotionId(String promotionId) { this.promotionId = promotionId; }

    public String getPromotionName() { return promotionName; }
    public void setPromotionName(String promotionName) { this.promotionName = promotionName; }

    public String getDramaId() { return dramaId; }
    public void setDramaId(String dramaId) { this.dramaId = dramaId; }

    public String getDramaTitle() { return dramaTitle; }
    public void setDramaTitle(String dramaTitle) { this.dramaTitle = dramaTitle; }

    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }

    public String getChapterTitle() { return chapterTitle; }
    public void setChapterTitle(String chapterTitle) { this.chapterTitle = chapterTitle; }

    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }

    public Integer getClientOs() { return clientOs; }
    public void setClientOs(Integer clientOs) { this.clientOs = clientOs; }

    public Integer getMediaChannel() { return mediaChannel; }
    public void setMediaChannel(Integer mediaChannel) { this.mediaChannel = mediaChannel; }

    public Integer getLandingType() { return landingType; }
    public void setLandingType(Integer landingType) { this.landingType = landingType; }

    public String getPixelId() { return pixelId; }
    public void setPixelId(String pixelId) { this.pixelId = pixelId; }

    public String getPixelName() { return pixelName; }
    public void setPixelName(String pixelName) { this.pixelName = pixelName; }

    public String getRechargeTplId() { return rechargeTplId; }
    public void setRechargeTplId(String rechargeTplId) { this.rechargeTplId = rechargeTplId; }

    public String getRechargeTplName() { return rechargeTplName; }
    public void setRechargeTplName(String rechargeTplName) { this.rechargeTplName = rechargeTplName; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }

    public String getAdConvtConfigId() { return adConvtConfigId; }
    public void setAdConvtConfigId(String adConvtConfigId) { this.adConvtConfigId = adConvtConfigId; }

    public String getAdConvtConfigName() { return adConvtConfigName; }
    public void setAdConvtConfigName(String adConvtConfigName) { this.adConvtConfigName = adConvtConfigName; }

    public Integer getTurnTag() { return turnTag; }
    public void setTurnTag(Integer turnTag) { this.turnTag = turnTag; }

    public Long getAppId() { return appId; }
    public void setAppId(Long appId) { this.appId = appId; }

    public Long getDistAppId() { return distAppId; }
    public void setDistAppId(Long distAppId) { this.distAppId = distAppId; }

    public Integer getGenre() { return genre; }
    public void setGenre(Integer genre) { this.genre = genre; }

    public Integer getDeliveryGoal() { return deliveryGoal; }
    public void setDeliveryGoal(Integer deliveryGoal) { this.deliveryGoal = deliveryGoal; }

    public LandInfoDetail getLandInfoDetail() { return landInfoDetail; }
    public void setLandInfoDetail(LandInfoDetail landInfoDetail) { this.landInfoDetail = landInfoDetail; }
}
