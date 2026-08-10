package com.ltv.stat.dto;

import com.ltv.stat.entity.DailyRechargeDistribution;

import java.util.List;

/**
 * /api/ltv/daily-distribution 接口强类型响应 DTO
 */
public class DailyDistributionResponseDto {
    private int code;
    private String msg;
    private List<DailyRechargeDistribution> data;
    private DailyDistributionSummaryDto summary;
    private Integer total;
    private Long userId;

    public DailyDistributionResponseDto() {
        this.code = 0;
        this.msg = "success";
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public List<DailyRechargeDistribution> getData() { return data; }
    public void setData(List<DailyRechargeDistribution> data) { this.data = data; }

    public DailyDistributionSummaryDto getSummary() { return summary; }
    public void setSummary(DailyDistributionSummaryDto summary) { this.summary = summary; }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
