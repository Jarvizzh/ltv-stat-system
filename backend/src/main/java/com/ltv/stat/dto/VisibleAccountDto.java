package com.ltv.stat.dto;

public class VisibleAccountDto {
    private Long id;
    private String username;
    private String role;
    private boolean isSelf;

    public VisibleAccountDto() {}

    public VisibleAccountDto(Long id, String username, String role, boolean isSelf) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.isSelf = isSelf;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isSelf() { return isSelf; }
    public void setSelf(boolean self) { isSelf = self; }
}
