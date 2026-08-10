package com.ltv.stat.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailyDistributionStat {

    private LocalDate date;

    // 总充值与付费人数
    private BigDecimal totalRecharge = BigDecimal.ZERO;
    private BigDecimal singleRecharge = BigDecimal.ZERO;
    private BigDecimal subsRecharge = BigDecimal.ZERO;

    private Integer totalPaidUsers = 0;
    private Integer singlePaidUsers = 0;
    private Integer subsPaidUsers = 0;

    // 新用户指标 (自然日当天注册)
    private BigDecimal newRecharge = BigDecimal.ZERO;
    private BigDecimal newArpu = BigDecimal.ZERO;
    private Integer newPaidUsers = 0;
    private Integer newSinglePaidUsers = 0;
    private Integer newSubsPaidUsers = 0;

    // 老用户指标 (自然日之前注册)
    private BigDecimal oldRecharge = BigDecimal.ZERO;
    private BigDecimal oldArpu = BigDecimal.ZERO;
    private Integer oldPaidUsers = 0;
    private Integer oldSinglePaidUsers = 0;
    private Integer oldSubsPaidUsers = 0;

    // 复充指标
    private Integer newRepeatUsers = 0;
    private BigDecimal newRepeatRate = BigDecimal.ZERO;
    private Integer oldRepeatUsers = 0;
    private BigDecimal repeatRatioNewToOld = BigDecimal.ZERO;

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getTotalRecharge() { return totalRecharge; }
    public void setTotalRecharge(BigDecimal totalRecharge) { this.totalRecharge = totalRecharge; }

    public BigDecimal getSingleRecharge() { return singleRecharge; }
    public void setSingleRecharge(BigDecimal singleRecharge) { this.singleRecharge = singleRecharge; }

    public BigDecimal getSubsRecharge() { return subsRecharge; }
    public void setSubsRecharge(BigDecimal subsRecharge) { this.subsRecharge = subsRecharge; }

    public Integer getTotalPaidUsers() { return totalPaidUsers; }
    public void setTotalPaidUsers(Integer totalPaidUsers) { this.totalPaidUsers = totalPaidUsers; }

    public Integer getSinglePaidUsers() { return singlePaidUsers; }
    public void setSinglePaidUsers(Integer singlePaidUsers) { this.singlePaidUsers = singlePaidUsers; }

    public Integer getSubsPaidUsers() { return subsPaidUsers; }
    public void setSubsPaidUsers(Integer subsPaidUsers) { this.subsPaidUsers = subsPaidUsers; }

    public BigDecimal getNewRecharge() { return newRecharge; }
    public void setNewRecharge(BigDecimal newRecharge) { this.newRecharge = newRecharge; }

    public BigDecimal getNewArpu() { return newArpu; }
    public void setNewArpu(BigDecimal newArpu) { this.newArpu = newArpu; }

    public Integer getNewPaidUsers() { return newPaidUsers; }
    public void setNewPaidUsers(Integer newPaidUsers) { this.newPaidUsers = newPaidUsers; }

    public Integer getNewSinglePaidUsers() { return newSinglePaidUsers; }
    public void setNewSinglePaidUsers(Integer newSinglePaidUsers) { this.newSinglePaidUsers = newSinglePaidUsers; }

    public Integer getNewSubsPaidUsers() { return newSubsPaidUsers; }
    public void setNewSubsPaidUsers(Integer newSubsPaidUsers) { this.newSubsPaidUsers = newSubsPaidUsers; }

    public BigDecimal getOldRecharge() { return oldRecharge; }
    public void setOldRecharge(BigDecimal oldRecharge) { this.oldRecharge = oldRecharge; }

    public BigDecimal getOldArpu() { return oldArpu; }
    public void setOldArpu(BigDecimal oldArpu) { this.oldArpu = oldArpu; }

    public Integer getOldPaidUsers() { return oldPaidUsers; }
    public void setOldPaidUsers(Integer oldPaidUsers) { this.oldPaidUsers = oldPaidUsers; }

    public Integer getOldSinglePaidUsers() { return oldSinglePaidUsers; }
    public void setOldSinglePaidUsers(Integer oldSinglePaidUsers) { this.oldSinglePaidUsers = oldSinglePaidUsers; }

    public Integer getOldSubsPaidUsers() { return oldSubsPaidUsers; }
    public void setOldSubsPaidUsers(Integer oldSubsPaidUsers) { this.oldSubsPaidUsers = oldSubsPaidUsers; }

    public Integer getNewRepeatUsers() { return newRepeatUsers; }
    public void setNewRepeatUsers(Integer newRepeatUsers) { this.newRepeatUsers = newRepeatUsers; }

    public BigDecimal getNewRepeatRate() { return newRepeatRate; }
    public void setNewRepeatRate(BigDecimal newRepeatRate) { this.newRepeatRate = newRepeatRate; }

    public Integer getOldRepeatUsers() { return oldRepeatUsers; }
    public void setOldRepeatUsers(Integer oldRepeatUsers) { this.oldRepeatUsers = oldRepeatUsers; }

    public BigDecimal getRepeatRatioNewToOld() { return repeatRatioNewToOld; }
    public void setRepeatRatioNewToOld(BigDecimal repeatRatioNewToOld) { this.repeatRatioNewToOld = repeatRatioNewToOld; }
}
