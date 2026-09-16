package com.ltv.stat.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class DailyRechargeDistributionId implements Serializable {

    private String platformCode = "rocnovel";
    private Long userId;
    private LocalDate date;

    public DailyRechargeDistributionId() {}

    public DailyRechargeDistributionId(Long userId, LocalDate date) {
        this("ALL", userId, date);
    }

    public DailyRechargeDistributionId(String platformCode, Long userId, LocalDate date) {
        this.platformCode = (platformCode != null && !platformCode.trim().isEmpty()) ? platformCode.trim() : "ALL";
        this.userId = userId;
        this.date = date;
    }

    public String getPlatformCode() { return platformCode; }
    public void setPlatformCode(String platformCode) { this.platformCode = platformCode; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyRechargeDistributionId that = (DailyRechargeDistributionId) o;
        return Objects.equals(platformCode, that.platformCode) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platformCode, userId, date);
    }
}
