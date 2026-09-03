package com.ltv.stat.dto;

public class VisibleAccountDto {
    private Long id;
    private String username;
    private String role;
    private boolean isSelf;
    private Integer isMaster;
    private Integer isSettlement;
    private Integer subAccountCount;

    public VisibleAccountDto() {}

    public VisibleAccountDto(Long id, String username, String role, boolean isSelf) {
        this(id, username, role, isSelf, 0, 0, 0);
    }

    public VisibleAccountDto(Long id, String username, String role, boolean isSelf, Integer isMaster, Integer subAccountCount) {
        this(id, username, role, isSelf, isMaster, 0, subAccountCount);
    }

    public VisibleAccountDto(Long id, String username, String role, boolean isSelf, Integer isMaster, Integer isSettlement, Integer subAccountCount) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.isSelf = isSelf;
        this.isMaster = isMaster;
        this.isSettlement = isSettlement;
        this.subAccountCount = subAccountCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isSelf() { return isSelf; }
    public void setSelf(boolean self) { isSelf = self; }

    public Integer getIsMaster() { return isMaster; }
    public void setIsMaster(Integer isMaster) { this.isMaster = isMaster; }

    public Integer getIsSettlement() { return isSettlement != null ? isSettlement : 0; }
    public void setIsSettlement(Integer isSettlement) { this.isSettlement = isSettlement; }

    public Integer getSubAccountCount() { return subAccountCount; }
    public void setSubAccountCount(Integer subAccountCount) { this.subAccountCount = subAccountCount; }
}
