package com.ltv.stat.dto;

/**
 * 登录/ Token 校验响应 DTO
 */
public class LoginResponseDto {
    private int code;
    private String msg;
    private String token;
    private Long userId;
    private String username;
    private String role;
    private Integer expireDays;

    public LoginResponseDto() {}

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getExpireDays() { return expireDays; }
    public void setExpireDays(Integer expireDays) { this.expireDays = expireDays; }
}
