package com.ltv.stat.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_recharge_distribution")
@IdClass(DailyRechargeDistributionId.class)
public class DailyRechargeDistribution {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total_recharge", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalRecharge = BigDecimal.ZERO;

    @Column(name = "single_recharge", nullable = false, precision = 10, scale = 2)
    private BigDecimal singleRecharge = BigDecimal.ZERO;

    @Column(name = "subs_recharge", nullable = false, precision = 10, scale = 2)
    private BigDecimal subsRecharge = BigDecimal.ZERO;

    @Column(name = "total_paid_users", nullable = false)
    private Integer totalPaidUsers = 0;

    @Column(name = "single_paid_users", nullable = false)
    private Integer singlePaidUsers = 0;

    @Column(name = "subs_paid_users", nullable = false)
    private Integer subsPaidUsers = 0;

    // 新用户指标
    @Column(name = "new_recharge", nullable = false, precision = 10, scale = 2)
    private BigDecimal newRecharge = BigDecimal.ZERO;

    @Column(name = "new_recharge_ratio", nullable = false, precision = 10, scale = 4)
    private BigDecimal newRechargeRatio = BigDecimal.ZERO;

    @Column(name = "new_arpu", nullable = false, precision = 10, scale = 2)
    private BigDecimal newArpu = BigDecimal.ZERO;

    @Column(name = "new_paid_users", nullable = false)
    private Integer newPaidUsers = 0;

    @Column(name = "new_single_paid_users", nullable = false)
    private Integer newSinglePaidUsers = 0;

    @Column(name = "new_subs_paid_users", nullable = false)
    private Integer newSubsPaidUsers = 0;

    // 老用户指标
    @Column(name = "old_recharge", nullable = false, precision = 10, scale = 2)
    private BigDecimal oldRecharge = BigDecimal.ZERO;

    @Column(name = "old_recharge_ratio", nullable = false, precision = 10, scale = 4)
    private BigDecimal oldRechargeRatio = BigDecimal.ZERO;

    @Column(name = "old_arpu", nullable = false, precision = 10, scale = 2)
    private BigDecimal oldArpu = BigDecimal.ZERO;

    @Column(name = "old_paid_users", nullable = false)
    private Integer oldPaidUsers = 0;

    @Column(name = "old_single_paid_users", nullable = false)
    private Integer oldSinglePaidUsers = 0;

    @Column(name = "old_subs_paid_users", nullable = false)
    private Integer oldSubsPaidUsers = 0;

    // 整体复充指标 (无需区分新老用户)
    @Column(name = "repeat_paid_users", nullable = false)
    private Integer repeatPaidUsers = 0;

    @Column(name = "repeat_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal repeatRate = BigDecimal.ZERO;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

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

    public BigDecimal getNewRechargeRatio() { return newRechargeRatio; }
    public void setNewRechargeRatio(BigDecimal newRechargeRatio) { this.newRechargeRatio = newRechargeRatio; }

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

    public BigDecimal getOldRechargeRatio() { return oldRechargeRatio; }
    public void setOldRechargeRatio(BigDecimal oldRechargeRatio) { this.oldRechargeRatio = oldRechargeRatio; }

    public BigDecimal getOldArpu() { return oldArpu; }
    public void setOldArpu(BigDecimal oldArpu) { this.oldArpu = oldArpu; }

    public Integer getOldPaidUsers() { return oldPaidUsers; }
    public void setOldPaidUsers(Integer oldPaidUsers) { this.oldPaidUsers = oldPaidUsers; }

    public Integer getOldSinglePaidUsers() { return oldSinglePaidUsers; }
    public void setOldSinglePaidUsers(Integer oldSinglePaidUsers) { this.oldSinglePaidUsers = oldSinglePaidUsers; }

    public Integer getOldSubsPaidUsers() { return oldSubsPaidUsers; }
    public void setOldSubsPaidUsers(Integer oldSubsPaidUsers) { this.oldSubsPaidUsers = oldSubsPaidUsers; }

    public Integer getRepeatPaidUsers() { return repeatPaidUsers; }
    public void setRepeatPaidUsers(Integer repeatPaidUsers) { this.repeatPaidUsers = repeatPaidUsers; }

    public BigDecimal getRepeatRate() { return repeatRate; }
    public void setRepeatRate(BigDecimal repeatRate) { this.repeatRate = repeatRate; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
