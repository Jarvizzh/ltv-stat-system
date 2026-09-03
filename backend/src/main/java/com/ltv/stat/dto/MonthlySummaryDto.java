package com.ltv.stat.dto;

import java.util.List;

/**
 * 月度指标汇总数据 VO/DTO (包含近4个月数据，从 2026-07-10 开始)
 */
public class MonthlySummaryDto {
    private List<SingleMonthSummaryDto> months;
    private SingleMonthSummaryDto thisMonth;
    private SingleMonthSummaryDto lastMonth;

    public MonthlySummaryDto() {
    }

    public MonthlySummaryDto(List<SingleMonthSummaryDto> months) {
        this.months = months;
        if (months != null && !months.isEmpty()) {
            this.thisMonth = months.get(0);
            if (months.size() > 1) {
                this.lastMonth = months.get(1);
            }
        }
    }

    public MonthlySummaryDto(SingleMonthSummaryDto thisMonth, SingleMonthSummaryDto lastMonth) {
        this.thisMonth = thisMonth;
        this.lastMonth = lastMonth;
    }

    public List<SingleMonthSummaryDto> getMonths() { return months; }
    public void setMonths(List<SingleMonthSummaryDto> months) {
        this.months = months;
        if (months != null && !months.isEmpty()) {
            this.thisMonth = months.get(0);
            if (months.size() > 1) {
                this.lastMonth = months.get(1);
            }
        }
    }

    public SingleMonthSummaryDto getThisMonth() { return thisMonth; }
    public void setThisMonth(SingleMonthSummaryDto thisMonth) { this.thisMonth = thisMonth; }

    public SingleMonthSummaryDto getLastMonth() { return lastMonth; }
    public void setLastMonth(SingleMonthSummaryDto lastMonth) { this.lastMonth = lastMonth; }
}
