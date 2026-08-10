package com.ltv.stat.dto;

/**
 * 留存订阅用户汇总 DTO/VO
 */
public class RetainedSubscribersDto {
    private Integer subUsers;
    private Integer retainedSubUsers;
    private String retainedRate;

    public RetainedSubscribersDto() {
    }

    public RetainedSubscribersDto(Integer subUsers, Integer retainedSubUsers, String retainedRate) {
        this.subUsers = subUsers;
        this.retainedSubUsers = retainedSubUsers;
        this.retainedRate = retainedRate;
    }

    public Integer getSubUsers() { return subUsers; }
    public void setSubUsers(Integer subUsers) { this.subUsers = subUsers; }

    public Integer getRetainedSubUsers() { return retainedSubUsers; }
    public void setRetainedSubUsers(Integer retainedSubUsers) { this.retainedSubUsers = retainedSubUsers; }

    public String getRetainedRate() { return retainedRate; }
    public void setRetainedRate(String retainedRate) { this.retainedRate = retainedRate; }
}
