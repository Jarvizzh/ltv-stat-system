package com.ltv.stat.dto.flicknovel;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 获取推广链列表请求参数
 */
public class FlicknovelPromotionQueryRequest {

    @JsonProperty("email")
    private String email;

    @JsonProperty("promotion_id")
    private String promotionId;

    @JsonProperty("created_start_time")
    private Long createdStartTime;

    @JsonProperty("created_end_time")
    private Long createdEndTime;

    @JsonProperty("dist_app_id")
    private List<Long> distAppId;

    @JsonProperty("genres")
    private List<Long> genres;

    @JsonProperty("page")
    private Long page = 1L;

    @JsonProperty("page_size")
    private Long pageSize = 50L;

    public FlicknovelPromotionQueryRequest() {}

    public FlicknovelPromotionQueryRequest(String email, Long page, Long pageSize) {
        this.email = email;
        this.page = page != null ? page : 1L;
        this.pageSize = pageSize != null ? pageSize : 50L;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPromotionId() { return promotionId; }
    public void setPromotionId(String promotionId) { this.promotionId = promotionId; }

    public Long getCreatedStartTime() { return createdStartTime; }
    public void setCreatedStartTime(Long createdStartTime) { this.createdStartTime = createdStartTime; }

    public Long getCreatedEndTime() { return createdEndTime; }
    public void setCreatedEndTime(Long createdEndTime) { this.createdEndTime = createdEndTime; }

    public List<Long> getDistAppId() { return distAppId; }
    public void setDistAppId(List<Long> distAppId) { this.distAppId = distAppId; }

    public List<Long> getGenres() { return genres; }
    public void setGenres(List<Long> genres) { this.genres = genres; }

    public Long getPage() { return page; }
    public void setPage(Long page) { this.page = page; }

    public Long getPageSize() { return pageSize; }
    public void setPageSize(Long pageSize) { this.pageSize = pageSize; }
}
