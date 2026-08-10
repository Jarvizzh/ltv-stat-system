package com.ltv.stat.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ltv_daily_stat")
@IdClass(LtvDailyStatId.class)
public class LtvDailyStat {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "launch_date", nullable = false)
    private LocalDate launchDate;

    @Column(name = "spend", nullable = false, precision = 10, scale = 2)
    private BigDecimal spend = BigDecimal.ZERO;

    @Column(name = "remark", length = 500)
    private String remark = "";

    @Column(name = "total_recharge", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalRecharge = BigDecimal.ZERO;

    @Column(name = "total_refund", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalRefund = BigDecimal.ZERO;

    @Column(name = "total_profit", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalProfit = BigDecimal.ZERO;

    @Column(name = "total_roi", nullable = false, precision = 10, scale = 4)
    private BigDecimal totalRoi = BigDecimal.ZERO;

    @Column(name = "sub_user_count", nullable = false)
    private Integer subUserCount = 0;

    @Column(name = "sub_user_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal subUserCost = BigDecimal.ZERO;

    @Column(name = "day7_sub_user_count")
    private Integer day7SubUserCount;

    @Column(name = "day7_sub_user_retention", precision = 10, scale = 4)
    private BigDecimal day7SubUserRetention;

    @Column(name = "day15_sub_user_count")
    private Integer day15SubUserCount;

    @Column(name = "day15_sub_user_retention", precision = 10, scale = 4)
    private BigDecimal day15SubUserRetention;

    @Column(name = "sub_period_days")
    private Integer subPeriodDays = 1;

    @Column(name = "sub_period_distribution", length = 500)
    private String subPeriodDistribution;

    // Day 1 ~ Day 30 Recharges & ROIs
    @Column(name = "day1_recharge", precision = 10, scale = 2)
    private BigDecimal day1Recharge = BigDecimal.ZERO;
    @Column(name = "day1_roi", precision = 10, scale = 4)
    private BigDecimal day1Roi = BigDecimal.ZERO;

    @Column(name = "day2_recharge", precision = 10, scale = 2)
    private BigDecimal day2Recharge = BigDecimal.ZERO;
    @Column(name = "day2_roi", precision = 10, scale = 4)
    private BigDecimal day2Roi = BigDecimal.ZERO;

    @Column(name = "day3_recharge", precision = 10, scale = 2)
    private BigDecimal day3Recharge = BigDecimal.ZERO;
    @Column(name = "day3_roi", precision = 10, scale = 4)
    private BigDecimal day3Roi = BigDecimal.ZERO;

    @Column(name = "day4_recharge", precision = 10, scale = 2)
    private BigDecimal day4Recharge = BigDecimal.ZERO;
    @Column(name = "day4_roi", precision = 10, scale = 4)
    private BigDecimal day4Roi = BigDecimal.ZERO;

    @Column(name = "day5_recharge", precision = 10, scale = 2)
    private BigDecimal day5Recharge = BigDecimal.ZERO;
    @Column(name = "day5_roi", precision = 10, scale = 4)
    private BigDecimal day5Roi = BigDecimal.ZERO;

    @Column(name = "day6_recharge", precision = 10, scale = 2)
    private BigDecimal day6Recharge = BigDecimal.ZERO;
    @Column(name = "day6_roi", precision = 10, scale = 4)
    private BigDecimal day6Roi = BigDecimal.ZERO;

    @Column(name = "day7_recharge", precision = 10, scale = 2)
    private BigDecimal day7Recharge = BigDecimal.ZERO;
    @Column(name = "day7_roi", precision = 10, scale = 4)
    private BigDecimal day7Roi = BigDecimal.ZERO;

    @Column(name = "day8_recharge", precision = 10, scale = 2)
    private BigDecimal day8Recharge = BigDecimal.ZERO;
    @Column(name = "day8_roi", precision = 10, scale = 4)
    private BigDecimal day8Roi = BigDecimal.ZERO;

    @Column(name = "day9_recharge", precision = 10, scale = 2)
    private BigDecimal day9Recharge = BigDecimal.ZERO;
    @Column(name = "day9_roi", precision = 10, scale = 4)
    private BigDecimal day9Roi = BigDecimal.ZERO;

    @Column(name = "day10_recharge", precision = 10, scale = 2)
    private BigDecimal day10Recharge = BigDecimal.ZERO;
    @Column(name = "day10_roi", precision = 10, scale = 4)
    private BigDecimal day10Roi = BigDecimal.ZERO;

    @Column(name = "day11_recharge", precision = 10, scale = 2)
    private BigDecimal day11Recharge = BigDecimal.ZERO;
    @Column(name = "day11_roi", precision = 10, scale = 4)
    private BigDecimal day11Roi = BigDecimal.ZERO;

    @Column(name = "day12_recharge", precision = 10, scale = 2)
    private BigDecimal day12Recharge = BigDecimal.ZERO;
    @Column(name = "day12_roi", precision = 10, scale = 4)
    private BigDecimal day12Roi = BigDecimal.ZERO;

    @Column(name = "day13_recharge", precision = 10, scale = 2)
    private BigDecimal day13Recharge = BigDecimal.ZERO;
    @Column(name = "day13_roi", precision = 10, scale = 4)
    private BigDecimal day13Roi = BigDecimal.ZERO;

    @Column(name = "day14_recharge", precision = 10, scale = 2)
    private BigDecimal day14Recharge = BigDecimal.ZERO;
    @Column(name = "day14_roi", precision = 10, scale = 4)
    private BigDecimal day14Roi = BigDecimal.ZERO;

    @Column(name = "day15_recharge", precision = 10, scale = 2)
    private BigDecimal day15Recharge = BigDecimal.ZERO;
    @Column(name = "day15_roi", precision = 10, scale = 4)
    private BigDecimal day15Roi = BigDecimal.ZERO;

    @Column(name = "day16_recharge", precision = 10, scale = 2)
    private BigDecimal day16Recharge = BigDecimal.ZERO;
    @Column(name = "day16_roi", precision = 10, scale = 4)
    private BigDecimal day16Roi = BigDecimal.ZERO;

    @Column(name = "day17_recharge", precision = 10, scale = 2)
    private BigDecimal day17Recharge = BigDecimal.ZERO;
    @Column(name = "day17_roi", precision = 10, scale = 4)
    private BigDecimal day17Roi = BigDecimal.ZERO;

    @Column(name = "day18_recharge", precision = 10, scale = 2)
    private BigDecimal day18Recharge = BigDecimal.ZERO;
    @Column(name = "day18_roi", precision = 10, scale = 4)
    private BigDecimal day18Roi = BigDecimal.ZERO;

    @Column(name = "day19_recharge", precision = 10, scale = 2)
    private BigDecimal day19Recharge = BigDecimal.ZERO;
    @Column(name = "day19_roi", precision = 10, scale = 4)
    private BigDecimal day19Roi = BigDecimal.ZERO;

    @Column(name = "day20_recharge", precision = 10, scale = 2)
    private BigDecimal day20Recharge = BigDecimal.ZERO;
    @Column(name = "day20_roi", precision = 10, scale = 4)
    private BigDecimal day20Roi = BigDecimal.ZERO;

    @Column(name = "day21_recharge", precision = 10, scale = 2)
    private BigDecimal day21Recharge = BigDecimal.ZERO;
    @Column(name = "day21_roi", precision = 10, scale = 4)
    private BigDecimal day21Roi = BigDecimal.ZERO;

    @Column(name = "day22_recharge", precision = 10, scale = 2)
    private BigDecimal day22Recharge = BigDecimal.ZERO;
    @Column(name = "day22_roi", precision = 10, scale = 4)
    private BigDecimal day22Roi = BigDecimal.ZERO;

    @Column(name = "day23_recharge", precision = 10, scale = 2)
    private BigDecimal day23Recharge = BigDecimal.ZERO;
    @Column(name = "day23_roi", precision = 10, scale = 4)
    private BigDecimal day23Roi = BigDecimal.ZERO;

    @Column(name = "day24_recharge", precision = 10, scale = 2)
    private BigDecimal day24Recharge = BigDecimal.ZERO;
    @Column(name = "day24_roi", precision = 10, scale = 4)
    private BigDecimal day24Roi = BigDecimal.ZERO;

    @Column(name = "day25_recharge", precision = 10, scale = 2)
    private BigDecimal day25Recharge = BigDecimal.ZERO;
    @Column(name = "day25_roi", precision = 10, scale = 4)
    private BigDecimal day25Roi = BigDecimal.ZERO;

    @Column(name = "day26_recharge", precision = 10, scale = 2)
    private BigDecimal day26Recharge = BigDecimal.ZERO;
    @Column(name = "day26_roi", precision = 10, scale = 4)
    private BigDecimal day26Roi = BigDecimal.ZERO;

    @Column(name = "day27_recharge", precision = 10, scale = 2)
    private BigDecimal day27Recharge = BigDecimal.ZERO;
    @Column(name = "day27_roi", precision = 10, scale = 4)
    private BigDecimal day27Roi = BigDecimal.ZERO;

    @Column(name = "day28_recharge", precision = 10, scale = 2)
    private BigDecimal day28Recharge = BigDecimal.ZERO;
    @Column(name = "day28_roi", precision = 10, scale = 4)
    private BigDecimal day28Roi = BigDecimal.ZERO;

    @Column(name = "day29_recharge", precision = 10, scale = 2)
    private BigDecimal day29Recharge = BigDecimal.ZERO;
    @Column(name = "day29_roi", precision = 10, scale = 4)
    private BigDecimal day29Roi = BigDecimal.ZERO;

    @Column(name = "day30_recharge", precision = 10, scale = 2)
    private BigDecimal day30Recharge = BigDecimal.ZERO;
    @Column(name = "day30_roi", precision = 10, scale = 4)
    private BigDecimal day30Roi = BigDecimal.ZERO;

    @Column(name = "day31_recharge", precision = 10, scale = 2)
    private BigDecimal day31Recharge = BigDecimal.ZERO;
    @Column(name = "day31_roi", precision = 10, scale = 4)
    private BigDecimal day31Roi = BigDecimal.ZERO;

    @Column(name = "day32_recharge", precision = 10, scale = 2)
    private BigDecimal day32Recharge = BigDecimal.ZERO;
    @Column(name = "day32_roi", precision = 10, scale = 4)
    private BigDecimal day32Roi = BigDecimal.ZERO;

    @Column(name = "day33_recharge", precision = 10, scale = 2)
    private BigDecimal day33Recharge = BigDecimal.ZERO;
    @Column(name = "day33_roi", precision = 10, scale = 4)
    private BigDecimal day33Roi = BigDecimal.ZERO;

    @Column(name = "day34_recharge", precision = 10, scale = 2)
    private BigDecimal day34Recharge = BigDecimal.ZERO;
    @Column(name = "day34_roi", precision = 10, scale = 4)
    private BigDecimal day34Roi = BigDecimal.ZERO;

    @Column(name = "day35_recharge", precision = 10, scale = 2)
    private BigDecimal day35Recharge = BigDecimal.ZERO;
    @Column(name = "day35_roi", precision = 10, scale = 4)
    private BigDecimal day35Roi = BigDecimal.ZERO;

    @Column(name = "day36_recharge", precision = 10, scale = 2)
    private BigDecimal day36Recharge = BigDecimal.ZERO;
    @Column(name = "day36_roi", precision = 10, scale = 4)
    private BigDecimal day36Roi = BigDecimal.ZERO;

    @Column(name = "day37_recharge", precision = 10, scale = 2)
    private BigDecimal day37Recharge = BigDecimal.ZERO;
    @Column(name = "day37_roi", precision = 10, scale = 4)
    private BigDecimal day37Roi = BigDecimal.ZERO;

    @Column(name = "day38_recharge", precision = 10, scale = 2)
    private BigDecimal day38Recharge = BigDecimal.ZERO;
    @Column(name = "day38_roi", precision = 10, scale = 4)
    private BigDecimal day38Roi = BigDecimal.ZERO;

    @Column(name = "day39_recharge", precision = 10, scale = 2)
    private BigDecimal day39Recharge = BigDecimal.ZERO;
    @Column(name = "day39_roi", precision = 10, scale = 4)
    private BigDecimal day39Roi = BigDecimal.ZERO;

    @Column(name = "day40_recharge", precision = 10, scale = 2)
    private BigDecimal day40Recharge = BigDecimal.ZERO;
    @Column(name = "day40_roi", precision = 10, scale = 4)
    private BigDecimal day40Roi = BigDecimal.ZERO;

    @Column(name = "day41_recharge", precision = 10, scale = 2)
    private BigDecimal day41Recharge = BigDecimal.ZERO;
    @Column(name = "day41_roi", precision = 10, scale = 4)
    private BigDecimal day41Roi = BigDecimal.ZERO;

    @Column(name = "day42_recharge", precision = 10, scale = 2)
    private BigDecimal day42Recharge = BigDecimal.ZERO;
    @Column(name = "day42_roi", precision = 10, scale = 4)
    private BigDecimal day42Roi = BigDecimal.ZERO;

    @Column(name = "day43_recharge", precision = 10, scale = 2)
    private BigDecimal day43Recharge = BigDecimal.ZERO;
    @Column(name = "day43_roi", precision = 10, scale = 4)
    private BigDecimal day43Roi = BigDecimal.ZERO;

    @Column(name = "day44_recharge", precision = 10, scale = 2)
    private BigDecimal day44Recharge = BigDecimal.ZERO;
    @Column(name = "day44_roi", precision = 10, scale = 4)
    private BigDecimal day44Roi = BigDecimal.ZERO;

    @Column(name = "day45_recharge", precision = 10, scale = 2)
    private BigDecimal day45Recharge = BigDecimal.ZERO;
    @Column(name = "day45_roi", precision = 10, scale = 4)
    private BigDecimal day45Roi = BigDecimal.ZERO;

    @Column(name = "day46_recharge", precision = 10, scale = 2)
    private BigDecimal day46Recharge = BigDecimal.ZERO;
    @Column(name = "day46_roi", precision = 10, scale = 4)
    private BigDecimal day46Roi = BigDecimal.ZERO;

    @Column(name = "day47_recharge", precision = 10, scale = 2)
    private BigDecimal day47Recharge = BigDecimal.ZERO;
    @Column(name = "day47_roi", precision = 10, scale = 4)
    private BigDecimal day47Roi = BigDecimal.ZERO;

    @Column(name = "day48_recharge", precision = 10, scale = 2)
    private BigDecimal day48Recharge = BigDecimal.ZERO;
    @Column(name = "day48_roi", precision = 10, scale = 4)
    private BigDecimal day48Roi = BigDecimal.ZERO;

    @Column(name = "day49_recharge", precision = 10, scale = 2)
    private BigDecimal day49Recharge = BigDecimal.ZERO;
    @Column(name = "day49_roi", precision = 10, scale = 4)
    private BigDecimal day49Roi = BigDecimal.ZERO;

    @Column(name = "day50_recharge", precision = 10, scale = 2)
    private BigDecimal day50Recharge = BigDecimal.ZERO;
    @Column(name = "day50_roi", precision = 10, scale = 4)
    private BigDecimal day50Roi = BigDecimal.ZERO;

    @Column(name = "day51_recharge", precision = 10, scale = 2)
    private BigDecimal day51Recharge = BigDecimal.ZERO;
    @Column(name = "day51_roi", precision = 10, scale = 4)
    private BigDecimal day51Roi = BigDecimal.ZERO;

    @Column(name = "day52_recharge", precision = 10, scale = 2)
    private BigDecimal day52Recharge = BigDecimal.ZERO;
    @Column(name = "day52_roi", precision = 10, scale = 4)
    private BigDecimal day52Roi = BigDecimal.ZERO;

    @Column(name = "day53_recharge", precision = 10, scale = 2)
    private BigDecimal day53Recharge = BigDecimal.ZERO;
    @Column(name = "day53_roi", precision = 10, scale = 4)
    private BigDecimal day53Roi = BigDecimal.ZERO;

    @Column(name = "day54_recharge", precision = 10, scale = 2)
    private BigDecimal day54Recharge = BigDecimal.ZERO;
    @Column(name = "day54_roi", precision = 10, scale = 4)
    private BigDecimal day54Roi = BigDecimal.ZERO;

    @Column(name = "day55_recharge", precision = 10, scale = 2)
    private BigDecimal day55Recharge = BigDecimal.ZERO;
    @Column(name = "day55_roi", precision = 10, scale = 4)
    private BigDecimal day55Roi = BigDecimal.ZERO;

    @Column(name = "day56_recharge", precision = 10, scale = 2)
    private BigDecimal day56Recharge = BigDecimal.ZERO;
    @Column(name = "day56_roi", precision = 10, scale = 4)
    private BigDecimal day56Roi = BigDecimal.ZERO;

    @Column(name = "day57_recharge", precision = 10, scale = 2)
    private BigDecimal day57Recharge = BigDecimal.ZERO;
    @Column(name = "day57_roi", precision = 10, scale = 4)
    private BigDecimal day57Roi = BigDecimal.ZERO;

    @Column(name = "day58_recharge", precision = 10, scale = 2)
    private BigDecimal day58Recharge = BigDecimal.ZERO;
    @Column(name = "day58_roi", precision = 10, scale = 4)
    private BigDecimal day58Roi = BigDecimal.ZERO;

    @Column(name = "day59_recharge", precision = 10, scale = 2)
    private BigDecimal day59Recharge = BigDecimal.ZERO;
    @Column(name = "day59_roi", precision = 10, scale = 4)
    private BigDecimal day59Roi = BigDecimal.ZERO;

    @Column(name = "day60_recharge", precision = 10, scale = 2)
    private BigDecimal day60Recharge = BigDecimal.ZERO;
    @Column(name = "day60_roi", precision = 10, scale = 4)
    private BigDecimal day60Roi = BigDecimal.ZERO;

    @Column(name = "predicted_payback_days")
    private Integer predictedPaybackDays;

    @Column(name = "predicted_day30_recharge", precision = 10, scale = 2)
    private BigDecimal predictedDay30Recharge;
    @Column(name = "predicted_day30_roi", precision = 10, scale = 4)
    private BigDecimal predictedDay30Roi;

    @Column(name = "predicted_day60_recharge", precision = 10, scale = 2)
    private BigDecimal predictedDay60Recharge;
    @Column(name = "predicted_day60_roi", precision = 10, scale = 4)
    private BigDecimal predictedDay60Roi;
    @Column(name = "predicted_day90_recharge", precision = 10, scale = 2)
    private BigDecimal predictedDay90Recharge;
    @Column(name = "predicted_day90_roi", precision = 10, scale = 4)
    private BigDecimal predictedDay90Roi;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper setter by day index (1-60)
    public void setDayData(int day, BigDecimal recharge, BigDecimal roi) {
        switch (day) {
            case 1: this.day1Recharge = recharge; this.day1Roi = roi; break;
            case 2: this.day2Recharge = recharge; this.day2Roi = roi; break;
            case 3: this.day3Recharge = recharge; this.day3Roi = roi; break;
            case 4: this.day4Recharge = recharge; this.day4Roi = roi; break;
            case 5: this.day5Recharge = recharge; this.day5Roi = roi; break;
            case 6: this.day6Recharge = recharge; this.day6Roi = roi; break;
            case 7: this.day7Recharge = recharge; this.day7Roi = roi; break;
            case 8: this.day8Recharge = recharge; this.day8Roi = roi; break;
            case 9: this.day9Recharge = recharge; this.day9Roi = roi; break;
            case 10: this.day10Recharge = recharge; this.day10Roi = roi; break;
            case 11: this.day11Recharge = recharge; this.day11Roi = roi; break;
            case 12: this.day12Recharge = recharge; this.day12Roi = roi; break;
            case 13: this.day13Recharge = recharge; this.day13Roi = roi; break;
            case 14: this.day14Recharge = recharge; this.day14Roi = roi; break;
            case 15: this.day15Recharge = recharge; this.day15Roi = roi; break;
            case 16: this.day16Recharge = recharge; this.day16Roi = roi; break;
            case 17: this.day17Recharge = recharge; this.day17Roi = roi; break;
            case 18: this.day18Recharge = recharge; this.day18Roi = roi; break;
            case 19: this.day19Recharge = recharge; this.day19Roi = roi; break;
            case 20: this.day20Recharge = recharge; this.day20Roi = roi; break;
            case 21: this.day21Recharge = recharge; this.day21Roi = roi; break;
            case 22: this.day22Recharge = recharge; this.day22Roi = roi; break;
            case 23: this.day23Recharge = recharge; this.day23Roi = roi; break;
            case 24: this.day24Recharge = recharge; this.day24Roi = roi; break;
            case 25: this.day25Recharge = recharge; this.day25Roi = roi; break;
            case 26: this.day26Recharge = recharge; this.day26Roi = roi; break;
            case 27: this.day27Recharge = recharge; this.day27Roi = roi; break;
            case 28: this.day28Recharge = recharge; this.day28Roi = roi; break;
            case 29: this.day29Recharge = recharge; this.day29Roi = roi; break;
            case 30: this.day30Recharge = recharge; this.day30Roi = roi; break;
            case 31: this.day31Recharge = recharge; this.day31Roi = roi; break;
            case 32: this.day32Recharge = recharge; this.day32Roi = roi; break;
            case 33: this.day33Recharge = recharge; this.day33Roi = roi; break;
            case 34: this.day34Recharge = recharge; this.day34Roi = roi; break;
            case 35: this.day35Recharge = recharge; this.day35Roi = roi; break;
            case 36: this.day36Recharge = recharge; this.day36Roi = roi; break;
            case 37: this.day37Recharge = recharge; this.day37Roi = roi; break;
            case 38: this.day38Recharge = recharge; this.day38Roi = roi; break;
            case 39: this.day39Recharge = recharge; this.day39Roi = roi; break;
            case 40: this.day40Recharge = recharge; this.day40Roi = roi; break;
            case 41: this.day41Recharge = recharge; this.day41Roi = roi; break;
            case 42: this.day42Recharge = recharge; this.day42Roi = roi; break;
            case 43: this.day43Recharge = recharge; this.day43Roi = roi; break;
            case 44: this.day44Recharge = recharge; this.day44Roi = roi; break;
            case 45: this.day45Recharge = recharge; this.day45Roi = roi; break;
            case 46: this.day46Recharge = recharge; this.day46Roi = roi; break;
            case 47: this.day47Recharge = recharge; this.day47Roi = roi; break;
            case 48: this.day48Recharge = recharge; this.day48Roi = roi; break;
            case 49: this.day49Recharge = recharge; this.day49Roi = roi; break;
            case 50: this.day50Recharge = recharge; this.day50Roi = roi; break;
            case 51: this.day51Recharge = recharge; this.day51Roi = roi; break;
            case 52: this.day52Recharge = recharge; this.day52Roi = roi; break;
            case 53: this.day53Recharge = recharge; this.day53Roi = roi; break;
            case 54: this.day54Recharge = recharge; this.day54Roi = roi; break;
            case 55: this.day55Recharge = recharge; this.day55Roi = roi; break;
            case 56: this.day56Recharge = recharge; this.day56Roi = roi; break;
            case 57: this.day57Recharge = recharge; this.day57Roi = roi; break;
            case 58: this.day58Recharge = recharge; this.day58Roi = roi; break;
            case 59: this.day59Recharge = recharge; this.day59Roi = roi; break;
            case 60: this.day60Recharge = recharge; this.day60Roi = roi; break;
        }
    }

    // Standard Getters & Setters
    public LocalDate getLaunchDate() { return launchDate; }
    public void setLaunchDate(LocalDate launchDate) { this.launchDate = launchDate; }

    public BigDecimal getSpend() { return spend; }
    public void setSpend(BigDecimal spend) { this.spend = spend; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public BigDecimal getTotalRecharge() { return totalRecharge; }
    public void setTotalRecharge(BigDecimal totalRecharge) { this.totalRecharge = totalRecharge; }

    public BigDecimal getTotalRefund() { return totalRefund != null ? totalRefund : BigDecimal.ZERO; }
    public void setTotalRefund(BigDecimal totalRefund) { this.totalRefund = totalRefund; }

    public BigDecimal getTotalProfit() { return totalProfit; }
    public void setTotalProfit(BigDecimal totalProfit) { this.totalProfit = totalProfit; }

    public BigDecimal getTotalRoi() { return totalRoi; }
    public void setTotalRoi(BigDecimal totalRoi) { this.totalRoi = totalRoi; }

    public Integer getSubUserCount() { return subUserCount; }
    public void setSubUserCount(Integer subUserCount) { this.subUserCount = subUserCount; }

    public BigDecimal getSubUserCost() { return subUserCost; }
    public void setSubUserCost(BigDecimal subUserCost) { this.subUserCost = subUserCost; }

    public Integer getDay7SubUserCount() { return day7SubUserCount; }
    public void setDay7SubUserCount(Integer day7SubUserCount) { this.day7SubUserCount = day7SubUserCount; }

    public BigDecimal getDay7SubUserRetention() { return day7SubUserRetention; }
    public void setDay7SubUserRetention(BigDecimal day7SubUserRetention) { this.day7SubUserRetention = day7SubUserRetention; }

    public Integer getDay15SubUserCount() { return day15SubUserCount; }
    public void setDay15SubUserCount(Integer day15SubUserCount) { this.day15SubUserCount = day15SubUserCount; }

    public BigDecimal getDay15SubUserRetention() { return day15SubUserRetention; }
    public void setDay15SubUserRetention(BigDecimal day15SubUserRetention) { this.day15SubUserRetention = day15SubUserRetention; }

    public BigDecimal getDay1Recharge() { return day1Recharge; }
    public void setDay1Recharge(BigDecimal day1Recharge) { this.day1Recharge = day1Recharge; }
    public BigDecimal getDay1Roi() { return day1Roi; }
    public void setDay1Roi(BigDecimal day1Roi) { this.day1Roi = day1Roi; }

    public BigDecimal getDay2Recharge() { return day2Recharge; }
    public void setDay2Recharge(BigDecimal day2Recharge) { this.day2Recharge = day2Recharge; }
    public BigDecimal getDay2Roi() { return day2Roi; }
    public void setDay2Roi(BigDecimal day2Roi) { this.day2Roi = day2Roi; }

    public BigDecimal getDay3Recharge() { return day3Recharge; }
    public void setDay3Recharge(BigDecimal day3Recharge) { this.day3Recharge = day3Recharge; }
    public BigDecimal getDay3Roi() { return day3Roi; }
    public void setDay3Roi(BigDecimal day3Roi) { this.day3Roi = day3Roi; }

    public BigDecimal getDay4Recharge() { return day4Recharge; }
    public void setDay4Recharge(BigDecimal day4Recharge) { this.day4Recharge = day4Recharge; }
    public BigDecimal getDay4Roi() { return day4Roi; }
    public void setDay4Roi(BigDecimal day4Roi) { this.day4Roi = day4Roi; }

    public BigDecimal getDay5Recharge() { return day5Recharge; }
    public void setDay5Recharge(BigDecimal day5Recharge) { this.day5Recharge = day5Recharge; }
    public BigDecimal getDay5Roi() { return day5Roi; }
    public void setDay5Roi(BigDecimal day5Roi) { this.day5Roi = day5Roi; }

    public BigDecimal getDay6Recharge() { return day6Recharge; }
    public void setDay6Recharge(BigDecimal day6Recharge) { this.day6Recharge = day6Recharge; }
    public BigDecimal getDay6Roi() { return day6Roi; }
    public void setDay6Roi(BigDecimal day6Roi) { this.day6Roi = day6Roi; }

    public BigDecimal getDay7Recharge() { return day7Recharge; }
    public void setDay7Recharge(BigDecimal day7Recharge) { this.day7Recharge = day7Recharge; }
    public BigDecimal getDay7Roi() { return day7Roi; }
    public void setDay7Roi(BigDecimal day7Roi) { this.day7Roi = day7Roi; }

    public BigDecimal getDay8Recharge() { return day8Recharge; }
    public void setDay8Recharge(BigDecimal day8Recharge) { this.day8Recharge = day8Recharge; }
    public BigDecimal getDay8Roi() { return day8Roi; }
    public void setDay8Roi(BigDecimal day8Roi) { this.day8Roi = day8Roi; }

    public BigDecimal getDay9Recharge() { return day9Recharge; }
    public void setDay9Recharge(BigDecimal day9Recharge) { this.day9Recharge = day9Recharge; }
    public BigDecimal getDay9Roi() { return day9Roi; }
    public void setDay9Roi(BigDecimal day9Roi) { this.day9Roi = day9Roi; }

    public BigDecimal getDay10Recharge() { return day10Recharge; }
    public void setDay10Recharge(BigDecimal day10Recharge) { this.day10Recharge = day10Recharge; }
    public BigDecimal getDay10Roi() { return day10Roi; }
    public void setDay10Roi(BigDecimal day10Roi) { this.day10Roi = day10Roi; }

    public BigDecimal getDay11Recharge() { return day11Recharge; }
    public void setDay11Recharge(BigDecimal day11Recharge) { this.day11Recharge = day11Recharge; }
    public BigDecimal getDay11Roi() { return day11Roi; }
    public void setDay11Roi(BigDecimal day11Roi) { this.day11Roi = day11Roi; }

    public BigDecimal getDay12Recharge() { return day12Recharge; }
    public void setDay12Recharge(BigDecimal day12Recharge) { this.day12Recharge = day12Recharge; }
    public BigDecimal getDay12Roi() { return day12Roi; }
    public void setDay12Roi(BigDecimal day12Roi) { this.day12Roi = day12Roi; }

    public BigDecimal getDay13Recharge() { return day13Recharge; }
    public void setDay13Recharge(BigDecimal day13Recharge) { this.day13Recharge = day13Recharge; }
    public BigDecimal getDay13Roi() { return day13Roi; }
    public void setDay13Roi(BigDecimal day13Roi) { this.day13Roi = day13Roi; }

    public BigDecimal getDay14Recharge() { return day14Recharge; }
    public void setDay14Recharge(BigDecimal day14Recharge) { this.day14Recharge = day14Recharge; }
    public BigDecimal getDay14Roi() { return day14Roi; }
    public void setDay14Roi(BigDecimal day14Roi) { this.day14Roi = day14Roi; }

    public BigDecimal getDay15Recharge() { return day15Recharge; }
    public void setDay15Recharge(BigDecimal day15Recharge) { this.day15Recharge = day15Recharge; }
    public BigDecimal getDay15Roi() { return day15Roi; }
    public void setDay15Roi(BigDecimal day15Roi) { this.day15Roi = day15Roi; }

    public BigDecimal getDay16Recharge() { return day16Recharge; }
    public void setDay16Recharge(BigDecimal day16Recharge) { this.day16Recharge = day16Recharge; }
    public BigDecimal getDay16Roi() { return day16Roi; }
    public void setDay16Roi(BigDecimal day16Roi) { this.day16Roi = day16Roi; }

    public BigDecimal getDay17Recharge() { return day17Recharge; }
    public void setDay17Recharge(BigDecimal day17Recharge) { this.day17Recharge = day17Recharge; }
    public BigDecimal getDay17Roi() { return day17Roi; }
    public void setDay17Roi(BigDecimal day17Roi) { this.day17Roi = day17Roi; }

    public BigDecimal getDay18Recharge() { return day18Recharge; }
    public void setDay18Recharge(BigDecimal day18Recharge) { this.day18Recharge = day18Recharge; }
    public BigDecimal getDay18Roi() { return day18Roi; }
    public void setDay18Roi(BigDecimal day18Roi) { this.day18Roi = day18Roi; }

    public BigDecimal getDay19Recharge() { return day19Recharge; }
    public void setDay19Recharge(BigDecimal day19Recharge) { this.day19Recharge = day19Recharge; }
    public BigDecimal getDay19Roi() { return day19Roi; }
    public void setDay19Roi(BigDecimal day19Roi) { this.day19Roi = day19Roi; }

    public BigDecimal getDay20Recharge() { return day20Recharge; }
    public void setDay20Recharge(BigDecimal day20Recharge) { this.day20Recharge = day20Recharge; }
    public BigDecimal getDay20Roi() { return day20Roi; }
    public void setDay20Roi(BigDecimal day20Roi) { this.day20Roi = day20Roi; }

    public BigDecimal getDay21Recharge() { return day21Recharge; }
    public void setDay21Recharge(BigDecimal day21Recharge) { this.day21Recharge = day21Recharge; }
    public BigDecimal getDay21Roi() { return day21Roi; }
    public void setDay21Roi(BigDecimal day21Roi) { this.day21Roi = day21Roi; }

    public BigDecimal getDay22Recharge() { return day22Recharge; }
    public void setDay22Recharge(BigDecimal day22Recharge) { this.day22Recharge = day22Recharge; }
    public BigDecimal getDay22Roi() { return day22Roi; }
    public void setDay22Roi(BigDecimal day22Roi) { this.day22Roi = day22Roi; }

    public BigDecimal getDay23Recharge() { return day23Recharge; }
    public void setDay23Recharge(BigDecimal day23Recharge) { this.day23Recharge = day23Recharge; }
    public BigDecimal getDay23Roi() { return day23Roi; }
    public void setDay23Roi(BigDecimal day23Roi) { this.day23Roi = day23Roi; }

    public BigDecimal getDay24Recharge() { return day24Recharge; }
    public void setDay24Recharge(BigDecimal day24Recharge) { this.day24Recharge = day24Recharge; }
    public BigDecimal getDay24Roi() { return day24Roi; }
    public void setDay24Roi(BigDecimal day24Roi) { this.day24Roi = day24Roi; }

    public BigDecimal getDay25Recharge() { return day25Recharge; }
    public void setDay25Recharge(BigDecimal day25Recharge) { this.day25Recharge = day25Recharge; }
    public BigDecimal getDay25Roi() { return day25Roi; }
    public void setDay25Roi(BigDecimal day25Roi) { this.day25Roi = day25Roi; }

    public BigDecimal getDay26Recharge() { return day26Recharge; }
    public void setDay26Recharge(BigDecimal day26Recharge) { this.day26Recharge = day26Recharge; }
    public BigDecimal getDay26Roi() { return day26Roi; }
    public void setDay26Roi(BigDecimal day26Roi) { this.day26Roi = day26Roi; }

    public BigDecimal getDay27Recharge() { return day27Recharge; }
    public void setDay27Recharge(BigDecimal day27Recharge) { this.day27Recharge = day27Recharge; }
    public BigDecimal getDay27Roi() { return day27Roi; }
    public void setDay27Roi(BigDecimal day27Roi) { this.day27Roi = day27Roi; }

    public BigDecimal getDay28Recharge() { return day28Recharge; }
    public void setDay28Recharge(BigDecimal day28Recharge) { this.day28Recharge = day28Recharge; }
    public BigDecimal getDay28Roi() { return day28Roi; }
    public void setDay28Roi(BigDecimal day28Roi) { this.day28Roi = day28Roi; }

    public BigDecimal getDay29Recharge() { return day29Recharge; }
    public void setDay29Recharge(BigDecimal day29Recharge) { this.day29Recharge = day29Recharge; }
    public BigDecimal getDay29Roi() { return day29Roi; }
    public void setDay29Roi(BigDecimal day29Roi) { this.day29Roi = day29Roi; }

    public BigDecimal getDay30Recharge() { return day30Recharge; }
    public void setDay30Recharge(BigDecimal day30Recharge) { this.day30Recharge = day30Recharge; }
    public BigDecimal getDay30Roi() { return day30Roi; }
    public void setDay30Roi(BigDecimal day30Roi) { this.day30Roi = day30Roi; }

    public BigDecimal getDay31Recharge() { return day31Recharge; }
    public void setDay31Recharge(BigDecimal day31Recharge) { this.day31Recharge = day31Recharge; }
    public BigDecimal getDay31Roi() { return day31Roi; }
    public void setDay31Roi(BigDecimal day31Roi) { this.day31Roi = day31Roi; }

    public BigDecimal getDay32Recharge() { return day32Recharge; }
    public void setDay32Recharge(BigDecimal day32Recharge) { this.day32Recharge = day32Recharge; }
    public BigDecimal getDay32Roi() { return day32Roi; }
    public void setDay32Roi(BigDecimal day32Roi) { this.day32Roi = day32Roi; }

    public BigDecimal getDay33Recharge() { return day33Recharge; }
    public void setDay33Recharge(BigDecimal day33Recharge) { this.day33Recharge = day33Recharge; }
    public BigDecimal getDay33Roi() { return day33Roi; }
    public void setDay33Roi(BigDecimal day33Roi) { this.day33Roi = day33Roi; }

    public BigDecimal getDay34Recharge() { return day34Recharge; }
    public void setDay34Recharge(BigDecimal day34Recharge) { this.day34Recharge = day34Recharge; }
    public BigDecimal getDay34Roi() { return day34Roi; }
    public void setDay34Roi(BigDecimal day34Roi) { this.day34Roi = day34Roi; }

    public BigDecimal getDay35Recharge() { return day35Recharge; }
    public void setDay35Recharge(BigDecimal day35Recharge) { this.day35Recharge = day35Recharge; }
    public BigDecimal getDay35Roi() { return day35Roi; }
    public void setDay35Roi(BigDecimal day35Roi) { this.day35Roi = day35Roi; }

    public BigDecimal getDay36Recharge() { return day36Recharge; }
    public void setDay36Recharge(BigDecimal day36Recharge) { this.day36Recharge = day36Recharge; }
    public BigDecimal getDay36Roi() { return day36Roi; }
    public void setDay36Roi(BigDecimal day36Roi) { this.day36Roi = day36Roi; }

    public BigDecimal getDay37Recharge() { return day37Recharge; }
    public void setDay37Recharge(BigDecimal day37Recharge) { this.day37Recharge = day37Recharge; }
    public BigDecimal getDay37Roi() { return day37Roi; }
    public void setDay37Roi(BigDecimal day37Roi) { this.day37Roi = day37Roi; }

    public BigDecimal getDay38Recharge() { return day38Recharge; }
    public void setDay38Recharge(BigDecimal day38Recharge) { this.day38Recharge = day38Recharge; }
    public BigDecimal getDay38Roi() { return day38Roi; }
    public void setDay38Roi(BigDecimal day38Roi) { this.day38Roi = day38Roi; }

    public BigDecimal getDay39Recharge() { return day39Recharge; }
    public void setDay39Recharge(BigDecimal day39Recharge) { this.day39Recharge = day39Recharge; }
    public BigDecimal getDay39Roi() { return day39Roi; }
    public void setDay39Roi(BigDecimal day39Roi) { this.day39Roi = day39Roi; }

    public BigDecimal getDay40Recharge() { return day40Recharge; }
    public void setDay40Recharge(BigDecimal day40Recharge) { this.day40Recharge = day40Recharge; }
    public BigDecimal getDay40Roi() { return day40Roi; }
    public void setDay40Roi(BigDecimal day40Roi) { this.day40Roi = day40Roi; }

    public BigDecimal getDay41Recharge() { return day41Recharge; }
    public void setDay41Recharge(BigDecimal day41Recharge) { this.day41Recharge = day41Recharge; }
    public BigDecimal getDay41Roi() { return day41Roi; }
    public void setDay41Roi(BigDecimal day41Roi) { this.day41Roi = day41Roi; }

    public BigDecimal getDay42Recharge() { return day42Recharge; }
    public void setDay42Recharge(BigDecimal day42Recharge) { this.day42Recharge = day42Recharge; }
    public BigDecimal getDay42Roi() { return day42Roi; }
    public void setDay42Roi(BigDecimal day42Roi) { this.day42Roi = day42Roi; }

    public BigDecimal getDay43Recharge() { return day43Recharge; }
    public void setDay43Recharge(BigDecimal day43Recharge) { this.day43Recharge = day43Recharge; }
    public BigDecimal getDay43Roi() { return day43Roi; }
    public void setDay43Roi(BigDecimal day43Roi) { this.day43Roi = day43Roi; }

    public BigDecimal getDay44Recharge() { return day44Recharge; }
    public void setDay44Recharge(BigDecimal day44Recharge) { this.day44Recharge = day44Recharge; }
    public BigDecimal getDay44Roi() { return day44Roi; }
    public void setDay44Roi(BigDecimal day44Roi) { this.day44Roi = day44Roi; }

    public BigDecimal getDay45Recharge() { return day45Recharge; }
    public void setDay45Recharge(BigDecimal day45Recharge) { this.day45Recharge = day45Recharge; }
    public BigDecimal getDay45Roi() { return day45Roi; }
    public void setDay45Roi(BigDecimal day45Roi) { this.day45Roi = day45Roi; }

    public BigDecimal getDay46Recharge() { return day46Recharge; }
    public void setDay46Recharge(BigDecimal day46Recharge) { this.day46Recharge = day46Recharge; }
    public BigDecimal getDay46Roi() { return day46Roi; }
    public void setDay46Roi(BigDecimal day46Roi) { this.day46Roi = day46Roi; }

    public BigDecimal getDay47Recharge() { return day47Recharge; }
    public void setDay47Recharge(BigDecimal day47Recharge) { this.day47Recharge = day47Recharge; }
    public BigDecimal getDay47Roi() { return day47Roi; }
    public void setDay47Roi(BigDecimal day47Roi) { this.day47Roi = day47Roi; }

    public BigDecimal getDay48Recharge() { return day48Recharge; }
    public void setDay48Recharge(BigDecimal day48Recharge) { this.day48Recharge = day48Recharge; }
    public BigDecimal getDay48Roi() { return day48Roi; }
    public void setDay48Roi(BigDecimal day48Roi) { this.day48Roi = day48Roi; }

    public BigDecimal getDay49Recharge() { return day49Recharge; }
    public void setDay49Recharge(BigDecimal day49Recharge) { this.day49Recharge = day49Recharge; }
    public BigDecimal getDay49Roi() { return day49Roi; }
    public void setDay49Roi(BigDecimal day49Roi) { this.day49Roi = day49Roi; }

    public BigDecimal getDay50Recharge() { return day50Recharge; }
    public void setDay50Recharge(BigDecimal day50Recharge) { this.day50Recharge = day50Recharge; }
    public BigDecimal getDay50Roi() { return day50Roi; }
    public void setDay50Roi(BigDecimal day50Roi) { this.day50Roi = day50Roi; }

    public BigDecimal getDay51Recharge() { return day51Recharge; }
    public void setDay51Recharge(BigDecimal day51Recharge) { this.day51Recharge = day51Recharge; }
    public BigDecimal getDay51Roi() { return day51Roi; }
    public void setDay51Roi(BigDecimal day51Roi) { this.day51Roi = day51Roi; }

    public BigDecimal getDay52Recharge() { return day52Recharge; }
    public void setDay52Recharge(BigDecimal day52Recharge) { this.day52Recharge = day52Recharge; }
    public BigDecimal getDay52Roi() { return day52Roi; }
    public void setDay52Roi(BigDecimal day52Roi) { this.day52Roi = day52Roi; }

    public BigDecimal getDay53Recharge() { return day53Recharge; }
    public void setDay53Recharge(BigDecimal day53Recharge) { this.day53Recharge = day53Recharge; }
    public BigDecimal getDay53Roi() { return day53Roi; }
    public void setDay53Roi(BigDecimal day53Roi) { this.day53Roi = day53Roi; }

    public BigDecimal getDay54Recharge() { return day54Recharge; }
    public void setDay54Recharge(BigDecimal day54Recharge) { this.day54Recharge = day54Recharge; }
    public BigDecimal getDay54Roi() { return day54Roi; }
    public void setDay54Roi(BigDecimal day54Roi) { this.day54Roi = day54Roi; }

    public BigDecimal getDay55Recharge() { return day55Recharge; }
    public void setDay55Recharge(BigDecimal day55Recharge) { this.day55Recharge = day55Recharge; }
    public BigDecimal getDay55Roi() { return day55Roi; }
    public void setDay55Roi(BigDecimal day55Roi) { this.day55Roi = day55Roi; }

    public BigDecimal getDay56Recharge() { return day56Recharge; }
    public void setDay56Recharge(BigDecimal day56Recharge) { this.day56Recharge = day56Recharge; }
    public BigDecimal getDay56Roi() { return day56Roi; }
    public void setDay56Roi(BigDecimal day56Roi) { this.day56Roi = day56Roi; }

    public BigDecimal getDay57Recharge() { return day57Recharge; }
    public void setDay57Recharge(BigDecimal day57Recharge) { this.day57Recharge = day57Recharge; }
    public BigDecimal getDay57Roi() { return day57Roi; }
    public void setDay57Roi(BigDecimal day57Roi) { this.day57Roi = day57Roi; }

    public BigDecimal getDay58Recharge() { return day58Recharge; }
    public void setDay58Recharge(BigDecimal day58Recharge) { this.day58Recharge = day58Recharge; }
    public BigDecimal getDay58Roi() { return day58Roi; }
    public void setDay58Roi(BigDecimal day58Roi) { this.day58Roi = day58Roi; }

    public BigDecimal getDay59Recharge() { return day59Recharge; }
    public void setDay59Recharge(BigDecimal day59Recharge) { this.day59Recharge = day59Recharge; }
    public BigDecimal getDay59Roi() { return day59Roi; }
    public void setDay59Roi(BigDecimal day59Roi) { this.day59Roi = day59Roi; }

    public BigDecimal getDay60Recharge() { return day60Recharge; }
    public void setDay60Recharge(BigDecimal day60Recharge) { this.day60Recharge = day60Recharge; }
    public BigDecimal getDay60Roi() { return day60Roi; }
    public void setDay60Roi(BigDecimal day60Roi) { this.day60Roi = day60Roi; }

    public Integer getPredictedPaybackDays() { return predictedPaybackDays; }
    public void setPredictedPaybackDays(Integer predictedPaybackDays) { this.predictedPaybackDays = predictedPaybackDays; }

    public BigDecimal getPredictedDay30Recharge() { return predictedDay30Recharge; }
    public void setPredictedDay30Recharge(BigDecimal predictedDay30Recharge) { this.predictedDay30Recharge = predictedDay30Recharge; }
    public BigDecimal getPredictedDay30Roi() { return predictedDay30Roi; }
    public void setPredictedDay30Roi(BigDecimal predictedDay30Roi) { this.predictedDay30Roi = predictedDay30Roi; }

    public BigDecimal getPredictedDay60Recharge() { return predictedDay60Recharge; }
    public void setPredictedDay60Recharge(BigDecimal predictedDay60Recharge) { this.predictedDay60Recharge = predictedDay60Recharge; }
    public BigDecimal getPredictedDay60Roi() { return predictedDay60Roi; }
    public void setPredictedDay60Roi(BigDecimal predictedDay60Roi) { this.predictedDay60Roi = predictedDay60Roi; }

    public BigDecimal getPredictedDay90Recharge() { return predictedDay90Recharge; }
    public void setPredictedDay90Recharge(BigDecimal predictedDay90Recharge) { this.predictedDay90Recharge = predictedDay90Recharge; }
    public BigDecimal getPredictedDay90Roi() { return predictedDay90Roi; }
    public void setPredictedDay90Roi(BigDecimal predictedDay90Roi) { this.predictedDay90Roi = predictedDay90Roi; }

    public Integer getSubPeriodDays() { return subPeriodDays != null ? subPeriodDays : 1; }
    public void setSubPeriodDays(Integer subPeriodDays) { this.subPeriodDays = subPeriodDays; }

    public String getSubPeriodDistribution() { return subPeriodDistribution; }
    public void setSubPeriodDistribution(String subPeriodDistribution) { this.subPeriodDistribution = subPeriodDistribution; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
