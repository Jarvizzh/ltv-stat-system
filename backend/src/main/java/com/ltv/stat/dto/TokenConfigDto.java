package com.ltv.stat.dto;

/**
 * API Token & Cookie 配置 DTO
 */
public class TokenConfigDto {
    private String authorization;
    private String cookie;

    public TokenConfigDto() {}

    public TokenConfigDto(String authorization, String cookie) {
        this.authorization = authorization;
        this.cookie = cookie;
    }

    public String getAuthorization() { return authorization; }
    public void setAuthorization(String authorization) { this.authorization = authorization; }

    public String getCookie() { return cookie; }
    public void setCookie(String cookie) { this.cookie = cookie; }
}
