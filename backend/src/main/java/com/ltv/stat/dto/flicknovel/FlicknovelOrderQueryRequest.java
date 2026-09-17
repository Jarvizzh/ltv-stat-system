package com.ltv.stat.dto.flicknovel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 获取订单列表请求参数
 */
public class FlicknovelOrderQueryRequest {

    /**
     * 订单创建时间戳下界 (秒级时间戳，闭区间)
     */
    @JsonProperty("begin_ts")
    private Long beginTs;

    /**
     * 订单创建时间戳上界 (秒级时间戳，开区间，时间跨度不能超过30天)
     */
    @JsonProperty("end_ts")
    private Long endTs;

    /**
     * 页码，从1开始
     */
    @JsonProperty("page")
    private Long page = 1L;

    /**
     * 单页数据条数 [100, 5000]
     */
    @JsonProperty("page_size")
    private Long pageSize = 100L;

    public FlicknovelOrderQueryRequest() {}

    public FlicknovelOrderQueryRequest(Long beginTs, Long endTs, Long page, Long pageSize) {
        this.beginTs = beginTs;
        this.endTs = endTs;
        this.page = page != null ? page : 1L;
        this.pageSize = pageSize != null ? pageSize : 100L;
    }

    public Long getBeginTs() {
        return beginTs;
    }

    public void setBeginTs(Long beginTs) {
        this.beginTs = beginTs;
    }

    public Long getEndTs() {
        return endTs;
    }

    public void setEndTs(Long endTs) {
        this.endTs = endTs;
    }

    public Long getPage() {
        return page;
    }

    public void setPage(Long page) {
        this.page = page;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }
}
