package com.ltv.stat.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class LtvLaunchConfigId implements Serializable {
    private Long userId;
    private LocalDate launchDate;

    public LtvLaunchConfigId() {}

    public LtvLaunchConfigId(Long userId, LocalDate launchDate) {
        this.userId = userId;
        this.launchDate = launchDate;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getLaunchDate() { return launchDate; }
    public void setLaunchDate(LocalDate launchDate) { this.launchDate = launchDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LtvLaunchConfigId that = (LtvLaunchConfigId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(launchDate, that.launchDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, launchDate);
    }
}
