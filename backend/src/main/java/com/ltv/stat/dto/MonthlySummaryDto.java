package com.ltv.stat.dto;

/**
 * 月度指标汇总数据 VO/DTO (包含当月与上月)
 */
public class MonthlySummaryDto {
    private SingleMonthSummaryDto thisMonth;
    private SingleMonthSummaryDto lastMonth;

    public MonthlySummaryDto() {
    }

    public MonthlySummaryDto(SingleMonthSummaryDto thisMonth, SingleMonthSummaryDto lastMonth) {
        this.thisMonth = thisMonth;
        this.lastMonth = lastMonth;
    }

    public SingleMonthSummaryDto getThisMonth() { return thisMonth; }
    public void setThisMonth(SingleMonthSummaryDto thisMonth) { this.thisMonth = thisMonth; }

    public SingleMonthSummaryDto getLastMonth() { return lastMonth; }
    public void setLastMonth(SingleMonthSummaryDto lastMonth) { this.lastMonth = lastMonth; }
}
