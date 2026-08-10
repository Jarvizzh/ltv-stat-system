package com.ltv.stat.dto;

/**
 * 重置密码请求 DTO
 */
public class ResetPasswordRequestDto {
    private String newPassword;

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
