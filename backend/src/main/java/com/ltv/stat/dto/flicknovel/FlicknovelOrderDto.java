package com.ltv.stat.dto.flicknovel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 番茄海外订单数据实体
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlicknovelOrderDto {

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("ad_id")
    private String adId;

    @JsonProperty("adset_id")
    private String adsetId;

    @JsonProperty("campaign_id")
    private String campaignId;

    @JsonProperty("ad_account_id")
    private String adAccountId;

    @JsonProperty("promotion_id")
    private String promotionId;

    @JsonProperty("promotion_code")
    private String promotionCode;

    @JsonProperty("distributor_id")
    private String distributorId;

    @JsonProperty("content_id")
    private String contentId;

    @JsonProperty("language")
    private String language;

    @JsonProperty("media_channel")
    private String mediaChannel;

    /**
     * 秒级时间戳字符串
     */
    @JsonProperty("created_at")
    private String createdAt;

    /**
     * 秒级时间戳字符串
     */
    @JsonProperty("completed_at")
    private String completedAt;

    /**
     * 美元金额字符串，如 "39.99"
     */
    @JsonProperty("us_price")
    private String usPrice;

    @JsonProperty("relation_id")
    private String relationId;

    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("app_name")
    private String appName;

    /**
     * 充值商品类型 (兼容扩展: 1-代币充值, 2-时长订阅)
     */
    @JsonProperty("benefit_type")
    private Integer benefitType;

    @JsonProperty("recharge_type")
    private Integer rechargeType;

    @JsonProperty("product_id")
    private String productId;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getAdId() { return adId; }
    public void setAdId(String adId) { this.adId = adId; }

    public String getAdsetId() { return adsetId; }
    public void setAdsetId(String adsetId) { this.adsetId = adsetId; }

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public String getAdAccountId() { return adAccountId; }
    public void setAdAccountId(String adAccountId) { this.adAccountId = adAccountId; }

    public String getPromotionId() { return promotionId; }
    public void setPromotionId(String promotionId) { this.promotionId = promotionId; }

    public String getPromotionCode() { return promotionCode; }
    public void setPromotionCode(String promotionCode) { this.promotionCode = promotionCode; }

    public String getDistributorId() { return distributorId; }
    public void setDistributorId(String distributorId) { this.distributorId = distributorId; }

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getMediaChannel() { return mediaChannel; }
    public void setMediaChannel(String mediaChannel) { this.mediaChannel = mediaChannel; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    public String getUsPrice() { return usPrice; }
    public void setUsPrice(String usPrice) { this.usPrice = usPrice; }

    public String getRelationId() { return relationId; }
    public void setRelationId(String relationId) { this.relationId = relationId; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public Integer getBenefitType() { return benefitType; }
    public void setBenefitType(Integer benefitType) { this.benefitType = benefitType; }

    public Integer getRechargeType() { return rechargeType; }
    public void setRechargeType(Integer rechargeType) { this.rechargeType = rechargeType; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
}
