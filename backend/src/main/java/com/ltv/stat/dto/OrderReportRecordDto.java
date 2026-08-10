package com.ltv.stat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderReportRecordDto {

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("memberId")
    private String memberId;

    @JsonProperty("PId")
    private String landingPageId;

    @JsonProperty("isSubs")
    private Integer isSubs;

    @JsonProperty("renewType")
    private Integer renewType; // 1: 手动/首次订阅, 2: 自动续费

    @JsonProperty("orderAmount")
    private Integer orderAmount; // 单位：分

    @JsonProperty("payState")
    private Integer payState; // 1: 支付成功

    @JsonProperty("refundStatus")
    private Integer refundStatus; // 2: 已退款

    @JsonProperty("userCreateTime")
    private String userCreateTime;

    @JsonProperty("payDate")
    private String payDate;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getLandingPageId() { return landingPageId; }
    public void setLandingPageId(String landingPageId) { this.landingPageId = landingPageId; }

    public Integer getIsSubs() { return isSubs; }
    public void setIsSubs(Integer isSubs) { this.isSubs = isSubs; }

    public Integer getRenewType() { return renewType; }
    public void setRenewType(Integer renewType) { this.renewType = renewType; }

    public Integer getOrderAmount() { return orderAmount; }
    public void setOrderAmount(Integer orderAmount) { this.orderAmount = orderAmount; }

    public Integer getPayState() { return payState; }
    public void setPayState(Integer payState) { this.payState = payState; }

    public Integer getRefundStatus() { return refundStatus; }
    public void setRefundStatus(Integer refundStatus) { this.refundStatus = refundStatus; }

    public String getUserCreateTime() { return userCreateTime; }
    public void setUserCreateTime(String userCreateTime) { this.userCreateTime = userCreateTime; }

    public String getPayDate() { return payDate; }
    public void setPayDate(String payDate) { this.payDate = payDate; }
}
