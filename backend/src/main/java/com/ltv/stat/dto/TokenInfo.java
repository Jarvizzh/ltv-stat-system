package com.ltv.stat.dto;

public class TokenInfo {
    private Long userId;
    private String username;
    private String role; // "ADMIN" or "USER"
    private boolean valid;

    public TokenInfo() {}

    public TokenInfo(Long userId, String username, String role, boolean valid) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.valid = valid;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role) || "SUPER_ADMIN".equalsIgnoreCase(role);
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equalsIgnoreCase(role);
    }
}
