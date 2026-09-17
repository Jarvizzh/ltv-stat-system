package com.ltv.stat.dto.flicknovel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 充值模板 v2 查询请求 DTO
 * 接口: /open/recharge_template/query/v2
 */
public class FlicknovelRechargeTemplateV2QueryRequest {

    @JsonProperty("email")
    private String email;

    @JsonProperty("dis_app_id")
    private Long disAppId;

    @JsonProperty("page")
    private Long page = 1L;

    @JsonProperty("page_size")
    private Long pageSize = 50L;

    public FlicknovelRechargeTemplateV2QueryRequest() {}

    public FlicknovelRechargeTemplateV2QueryRequest(String email, Long disAppId, Long page, Long pageSize) {
        this.email = email;
        this.disAppId = disAppId;
        this.page = page;
        this.pageSize = pageSize;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getDisAppId() { return disAppId; }
    public void setDisAppId(Long disAppId) { this.disAppId = disAppId; }

    public Long getPage() { return page; }
    public void setPage(Long page) { this.page = page; }

    public Long getPageSize() { return pageSize; }
    public void setPageSize(Long pageSize) { this.pageSize = pageSize; }
}
