package com.ltv.stat.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_landing_page", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_platform_user", columnList = "platform_code, user_id"),
    @Index(name = "idx_user_landing_page", columnList = "platform_code, user_id, landing_page_id", unique = true)
})
public class UserLandingPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform_code", nullable = false, length = 32)
    private String platformCode = "rocnovel";

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "landing_page_id", nullable = false, length = 64)
    private String landingPageId;

    @Column(name = "timezone", length = 32, nullable = false)
    private String timezone = "CST"; // "CST" (北京时间/中国标准时间), "UTC" (世界协调时), "ET" (美东时区)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlatformCode() { return platformCode != null ? platformCode : "rocnovel"; }
    public void setPlatformCode(String platformCode) { this.platformCode = platformCode; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getLandingPageId() { return landingPageId; }
    public void setLandingPageId(String landingPageId) { this.landingPageId = landingPageId; }

    public String getTimezone() { 
        if (timezone == null || timezone.trim().isEmpty() || "BJ".equalsIgnoreCase(timezone.trim())) {
            return "flicknovel".equalsIgnoreCase(platformCode) ? "UTC" : "CST";
        }
        return timezone.toUpperCase(); 
    }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
