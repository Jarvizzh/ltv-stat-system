package com.ltv.stat.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class LtvDailyStatId implements Serializable {

    private String platformCode = "rocnovel";
    private Long userId;
    private LocalDate launchDate;

    public LtvDailyStatId() {}

    public LtvDailyStatId(Long userId, LocalDate launchDate) {
        this("ALL", userId, launchDate);
    }

    public LtvDailyStatId(String platformCode, Long userId, LocalDate launchDate) {
        this.platformCode = (platformCode != null && !platformCode.trim().isEmpty()) ? platformCode.trim() : "ALL";
        this.userId = userId;
        this.launchDate = launchDate;
    }

    public String getPlatformCode() { return platformCode; }
    public void setPlatformCode(String platformCode) { this.platformCode = platformCode; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getLaunchDate() { return launchDate; }
    public void setLaunchDate(LocalDate launchDate) { this.launchDate = launchDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LtvDailyStatId that = (LtvDailyStatId) o;
        return Objects.equals(platformCode, that.platformCode) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(launchDate, that.launchDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platformCode, userId, launchDate);
    }
}
