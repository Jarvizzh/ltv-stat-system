package com.ltv.stat.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class DailyRechargeDistributionId implements Serializable {

    private Long userId;
    private LocalDate date;

    public DailyRechargeDistributionId() {}

    public DailyRechargeDistributionId(Long userId, LocalDate date) {
        this.userId = userId;
        this.date = date;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyRechargeDistributionId that = (DailyRechargeDistributionId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, date);
    }
}
