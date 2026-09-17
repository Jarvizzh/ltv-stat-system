package com.ltv.stat.dto.flicknovel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 充值模板查询请求
 */
public class FlicknovelRechargeTemplateQueryRequest {

    @JsonProperty("dis_app_id")
    private Long disAppId;

    @JsonProperty("email")
    private String email;

    @JsonProperty("page")
    private Long page = 1L;

    @JsonProperty("page_size")
    private Long pageSize = 50L;

    public FlicknovelRechargeTemplateQueryRequest() {}

    public FlicknovelRechargeTemplateQueryRequest(Long disAppId, String email, Long page, Long pageSize) {
        this.disAppId = disAppId;
        this.email = email;
        this.page = page != null ? page : 1L;
        this.pageSize = pageSize != null ? pageSize : 50L;
    }

    public Long getDisAppId() { return disAppId; }
    public void setDisAppId(Long disAppId) { this.disAppId = disAppId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getPage() { return page; }
    public void setPage(Long page) { this.page = page; }

    public Long getPageSize() { return pageSize; }
    public void setPageSize(Long pageSize) { this.pageSize = pageSize; }
}
