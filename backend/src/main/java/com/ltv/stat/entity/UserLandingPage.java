package com.ltv.stat.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_landing_page", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_user_landing_page", columnList = "user_id, landing_page_id", unique = true)
})
public class UserLandingPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "landing_page_id", nullable = false, length = 64)
    private String landingPageId;

    @Column(name = "timezone", length = 32, nullable = false)
    private String timezone = "BJ"; // "BJ" (北京时区) or "ET" (美东时区)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getLandingPageId() { return landingPageId; }
    public void setLandingPageId(String landingPageId) { this.landingPageId = landingPageId; }

    public String getTimezone() { return timezone != null ? timezone : "BJ"; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
