package com.ltv.stat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscribeConfigProductRecordDto {

    @JsonProperty("id")
    private String id; // 产品项 ID

    @JsonProperty("configId")
    private String configId; // 订阅配置 ID

    @JsonProperty("name")
    private String name;

    @JsonProperty("cycle")
    private Integer cycle;

    @JsonProperty("cycleStr")
    private String cycleStr; // "1 Day", "3 Days", "Weekly", "Monthly", "Annual"

    @JsonProperty("preferentialPrice")
    private String preferentialPrice; // 首次订阅价格 (USD, e.g. "9.99")

    @JsonProperty("price")
    private String price; // 续费价格 (USD, e.g. "29.99")

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("deleted")
    private Integer deleted;

    @JsonProperty("createDateTime")
    private String createDateTime;

    @JsonProperty("updateDateTime")
    private String updateDateTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCycle() { return cycle; }
    public void setCycle(Integer cycle) { this.cycle = cycle; }

    public String getCycleStr() { return cycleStr; }
    public void setCycleStr(String cycleStr) { this.cycleStr = cycleStr; }

    public String getPreferentialPrice() { return preferentialPrice; }
    public void setPreferentialPrice(String preferentialPrice) { this.preferentialPrice = preferentialPrice; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

    public String getCreateDateTime() { return createDateTime; }
    public void setCreateDateTime(String createDateTime) { this.createDateTime = createDateTime; }

    public String getUpdateDateTime() { return updateDateTime; }
    public void setUpdateDateTime(String updateDateTime) { this.updateDateTime = updateDateTime; }
}
