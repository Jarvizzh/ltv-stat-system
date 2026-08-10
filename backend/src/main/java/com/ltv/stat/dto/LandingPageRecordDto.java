package com.ltv.stat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LandingPageRecordDto {

    @JsonProperty("id")
    private String id; // 落地页 ID (PId)

    @JsonProperty("name")
    private String name;

    @JsonProperty("contentId")
    private String contentId;

    @JsonProperty("contentName")
    private String contentName;

    @JsonProperty("saleComboId")
    private String saleComboId; // 销售组合 ID

    @JsonProperty("saleComboName")
    private String saleComboName; // 销售组合名称

    @JsonProperty("subscribeConfigId")
    private String subscribeConfigId; // 订阅配置 ID

    @JsonProperty("subscribeConfigName")
    private String subscribeConfigName; // 订阅配置名称

    @JsonProperty("createDateTime")
    private String createDateTime;

    @JsonProperty("updateDateTime")
    private String updateDateTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public String getContentName() { return contentName; }
    public void setContentName(String contentName) { this.contentName = contentName; }

    public String getSaleComboId() { return saleComboId; }
    public void setSaleComboId(String saleComboId) { this.saleComboId = saleComboId; }

    public String getSaleComboName() { return saleComboName; }
    public void setSaleComboName(String saleComboName) { this.saleComboName = saleComboName; }

    public String getSubscribeConfigId() { return subscribeConfigId; }
    public void setSubscribeConfigId(String subscribeConfigId) { this.subscribeConfigId = subscribeConfigId; }

    public String getSubscribeConfigName() { return subscribeConfigName; }
    public void setSubscribeConfigName(String subscribeConfigName) { this.subscribeConfigName = subscribeConfigName; }

    public String getCreateDateTime() { return createDateTime; }
    public void setCreateDateTime(String createDateTime) { this.createDateTime = createDateTime; }

    public String getUpdateDateTime() { return updateDateTime; }
    public void setUpdateDateTime(String updateDateTime) { this.updateDateTime = updateDateTime; }
}
