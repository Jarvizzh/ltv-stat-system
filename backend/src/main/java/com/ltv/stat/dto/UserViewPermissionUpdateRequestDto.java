package com.ltv.stat.dto;

import java.util.List;

public class UserViewPermissionUpdateRequestDto {
    private List<Long> targetUserIds;

    public List<Long> getTargetUserIds() { return targetUserIds; }
    public void setTargetUserIds(List<Long> targetUserIds) { this.targetUserIds = targetUserIds; }
}
