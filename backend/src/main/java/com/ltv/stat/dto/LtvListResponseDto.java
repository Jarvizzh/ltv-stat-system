package com.ltv.stat.dto;

import com.ltv.stat.entity.LtvDailyStat;

import java.math.BigDecimal;
import java.util.List;

/**
 * /api/ltv/list 接口强类型响应 DTO
 */
public class LtvListResponseDto {
    private int code;
    private String msg;
    private List<LtvDailyStat> data;
    private Integer overallPredictedPaybackDays;
    private Integer overallPaybackCycleDays;
    private BigDecimal overallPredictedDay30Roi;
    private BigDecimal overallPredictedDay60Roi;
    private BigDecimal overallPredictedDay90Roi;
    private BigDecimal overallPredictedDay30Recharge;
    private BigDecimal overallPredictedDay60Recharge;
    private BigDecimal overallPredictedDay90Recharge;
    private MonthlySummaryDto monthlySummary;
    private Integer overallRetainedSubUsers;
    private String overallRetainedRate;
    private Integer total;
    private Long userId;

    public LtvListResponseDto() {
        this.code = 0;
        this.msg = "success";
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public List<LtvDailyStat> getData() { return data; }
    public void setData(List<LtvDailyStat> data) { this.data = data; }

    public Integer getOverallPredictedPaybackDays() { return overallPredictedPaybackDays; }
    public void setOverallPredictedPaybackDays(Integer overallPredictedPaybackDays) { this.overallPredictedPaybackDays = overallPredictedPaybackDays; }

    public Integer getOverallPaybackCycleDays() { return overallPaybackCycleDays; }
    public void setOverallPaybackCycleDays(Integer overallPaybackCycleDays) { this.overallPaybackCycleDays = overallPaybackCycleDays; }

    public BigDecimal getOverallPredictedDay30Roi() { return overallPredictedDay30Roi; }
    public void setOverallPredictedDay30Roi(BigDecimal overallPredictedDay30Roi) { this.overallPredictedDay30Roi = overallPredictedDay30Roi; }

    public BigDecimal getOverallPredictedDay60Roi() { return overallPredictedDay60Roi; }
    public void setOverallPredictedDay60Roi(BigDecimal overallPredictedDay60Roi) { this.overallPredictedDay60Roi = overallPredictedDay60Roi; }

    public BigDecimal getOverallPredictedDay90Roi() { return overallPredictedDay90Roi; }
    public void setOverallPredictedDay90Roi(BigDecimal overallPredictedDay90Roi) { this.overallPredictedDay90Roi = overallPredictedDay90Roi; }

    public BigDecimal getOverallPredictedDay30Recharge() { return overallPredictedDay30Recharge; }
    public void setOverallPredictedDay30Recharge(BigDecimal overallPredictedDay30Recharge) { this.overallPredictedDay30Recharge = overallPredictedDay30Recharge; }

    public BigDecimal getOverallPredictedDay60Recharge() { return overallPredictedDay60Recharge; }
    public void setOverallPredictedDay60Recharge(BigDecimal overallPredictedDay60Recharge) { this.overallPredictedDay60Recharge = overallPredictedDay60Recharge; }

    public BigDecimal getOverallPredictedDay90Recharge() { return overallPredictedDay90Recharge; }
    public void setOverallPredictedDay90Recharge(BigDecimal overallPredictedDay90Recharge) { this.overallPredictedDay90Recharge = overallPredictedDay90Recharge; }

    public MonthlySummaryDto getMonthlySummary() { return monthlySummary; }
    public void setMonthlySummary(MonthlySummaryDto monthlySummary) { this.monthlySummary = monthlySummary; }

    public Integer getOverallRetainedSubUsers() { return overallRetainedSubUsers; }
    public void setOverallRetainedSubUsers(Integer overallRetainedSubUsers) { this.overallRetainedSubUsers = overallRetainedSubUsers; }

    public String getOverallRetainedRate() { return overallRetainedRate; }
    public void setOverallRetainedRate(String overallRetainedRate) { this.overallRetainedRate = overallRetainedRate; }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
