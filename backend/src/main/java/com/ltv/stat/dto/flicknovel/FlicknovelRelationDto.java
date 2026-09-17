package com.ltv.stat.dto.flicknovel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * 染色记录 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlicknovelRelationDto {

    @JsonProperty("relation_id")
    private String relationId;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("promotion_id")
    private String promotionId;

    @JsonProperty("promotion_code")
    private String promotionCode;

    @JsonProperty("ad_id")
    private String adId;

    @JsonProperty("adset_id")
    private String adsetId;

    @JsonProperty("campaign_id")
    private String campaignId;

    @JsonProperty("ad_account_id")
    private String adAccountId;

    @JsonProperty("relation_begin_time")
    private String relationBeginTime; // 秒级时间戳

    @JsonProperty("media_channel")
    private String mediaChannel;

    @JsonProperty("platform")
    private String platform;

    @JsonProperty("app_id")
    private String appId;

    public String getRelationId() { return relationId; }
    public void setRelationId(String relationId) { this.relationId = relationId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getPromotionId() { return promotionId; }
    public void setPromotionId(String promotionId) { this.promotionId = promotionId; }

    public String getPromotionCode() { return promotionCode; }
    public void setPromotionCode(String promotionCode) { this.promotionCode = promotionCode; }

    public String getAdId() { return adId; }
    public void setAdId(String adId) { this.adId = adId; }

    public String getAdsetId() { return adsetId; }
    public void setAdsetId(String adsetId) { this.adsetId = adsetId; }

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public String getAdAccountId() { return adAccountId; }
    public void setAdAccountId(String adAccountId) { this.adAccountId = adAccountId; }

    public String getRelationBeginTime() { return relationBeginTime; }
    public void setRelationBeginTime(String relationBeginTime) { this.relationBeginTime = relationBeginTime; }

    public String getMediaChannel() { return mediaChannel; }
    public void setMediaChannel(String mediaChannel) { this.mediaChannel = mediaChannel; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
}
