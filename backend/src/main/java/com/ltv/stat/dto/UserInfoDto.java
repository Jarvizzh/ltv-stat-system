package com.ltv.stat.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息表项 VO/DTO
 */
public class UserInfoDto {
    private Long id;
    private String username;
    private String role;
    private Integer status;
    private LocalDateTime createdAt;
    private List<String> landingPageIds;
    private Integer landingPageCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<String> getLandingPageIds() { return landingPageIds; }
    public void setLandingPageIds(List<String> landingPageIds) { this.landingPageIds = landingPageIds; }

    public Integer getLandingPageCount() { return landingPageCount; }
    public void setLandingPageCount(Integer landingPageCount) { this.landingPageCount = landingPageCount; }
}
