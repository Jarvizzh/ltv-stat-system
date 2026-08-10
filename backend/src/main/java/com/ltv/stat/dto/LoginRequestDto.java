package com.ltv.stat.dto;

/**
 * 登录请求 DTO
 */
public class LoginRequestDto {
    private String username;
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
