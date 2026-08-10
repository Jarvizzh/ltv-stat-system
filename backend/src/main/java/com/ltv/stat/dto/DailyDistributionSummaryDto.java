package com.ltv.stat.dto;

import java.math.BigDecimal;

/**
 * 每日充值分布汇总 VO/DTO
 */
public class DailyDistributionSummaryDto {
    private BigDecimal totalRecharge;
    private BigDecimal newRecharge;
    private BigDecimal oldRecharge;
    private BigDecimal thisMonthRecharge;
    private BigDecimal thisMonthRefund;
    private BigDecimal lastMonthRecharge;
    private BigDecimal lastMonthRefund;
    private String thisMonthStr;
    private String lastMonthStr;
    private BigDecimal newRechargeRatio;
    private BigDecimal oldRechargeRatio;
    private Integer totalPaidUsers;
    private Integer newPaidUsers;
    private Integer oldPaidUsers;
    private BigDecimal newArpu;
    private BigDecimal oldArpu;
    private Integer repeatPaidUsers;
    private BigDecimal repeatRate;

    public BigDecimal getTotalRecharge() { return totalRecharge; }
    public void setTotalRecharge(BigDecimal totalRecharge) { this.totalRecharge = totalRecharge; }

    public BigDecimal getNewRecharge() { return newRecharge; }
    public void setNewRecharge(BigDecimal newRecharge) { this.newRecharge = newRecharge; }

    public BigDecimal getOldRecharge() { return oldRecharge; }
    public void setOldRecharge(BigDecimal oldRecharge) { this.oldRecharge = oldRecharge; }

    public BigDecimal getThisMonthRecharge() { return thisMonthRecharge; }
    public void setThisMonthRecharge(BigDecimal thisMonthRecharge) { this.thisMonthRecharge = thisMonthRecharge; }

    public BigDecimal getThisMonthRefund() { return thisMonthRefund; }
    public void setThisMonthRefund(BigDecimal thisMonthRefund) { this.thisMonthRefund = thisMonthRefund; }

    public BigDecimal getLastMonthRecharge() { return lastMonthRecharge; }
    public void setLastMonthRecharge(BigDecimal lastMonthRecharge) { this.lastMonthRecharge = lastMonthRecharge; }

    public BigDecimal getLastMonthRefund() { return lastMonthRefund; }
    public void setLastMonthRefund(BigDecimal lastMonthRefund) { this.lastMonthRefund = lastMonthRefund; }

    public String getThisMonthStr() { return thisMonthStr; }
    public void setThisMonthStr(String thisMonthStr) { this.thisMonthStr = thisMonthStr; }

    public String getLastMonthStr() { return lastMonthStr; }
    public void setLastMonthStr(String lastMonthStr) { this.lastMonthStr = lastMonthStr; }

    public BigDecimal getNewRechargeRatio() { return newRechargeRatio; }
    public void setNewRechargeRatio(BigDecimal newRechargeRatio) { this.newRechargeRatio = newRechargeRatio; }

    public BigDecimal getOldRechargeRatio() { return oldRechargeRatio; }
    public void setOldRechargeRatio(BigDecimal oldRechargeRatio) { this.oldRechargeRatio = oldRechargeRatio; }

    public Integer getTotalPaidUsers() { return totalPaidUsers; }
    public void setTotalPaidUsers(Integer totalPaidUsers) { this.totalPaidUsers = totalPaidUsers; }

    public Integer getNewPaidUsers() { return newPaidUsers; }
    public void setNewPaidUsers(Integer newPaidUsers) { this.newPaidUsers = newPaidUsers; }

    public Integer getOldPaidUsers() { return oldPaidUsers; }
    public void setOldPaidUsers(Integer oldPaidUsers) { this.oldPaidUsers = oldPaidUsers; }

    public BigDecimal getNewArpu() { return newArpu; }
    public void setNewArpu(BigDecimal newArpu) { this.newArpu = newArpu; }

    public BigDecimal getOldArpu() { return oldArpu; }
    public void setOldArpu(BigDecimal oldArpu) { this.oldArpu = oldArpu; }

    public Integer getRepeatPaidUsers() { return repeatPaidUsers; }
    public void setRepeatPaidUsers(Integer repeatPaidUsers) { this.repeatPaidUsers = repeatPaidUsers; }

    public BigDecimal getRepeatRate() { return repeatRate; }
    public void setRepeatRate(BigDecimal repeatRate) { this.repeatRate = repeatRate; }
}
