package com.ltv.stat.service;

import com.ltv.stat.LtvApplication;
import com.ltv.stat.entity.LtvDailyStat;
import com.ltv.stat.repository.LtvDailyStatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 基于真实数据库 Cohort 数据验证 Day 7, Day 14, Day 30 ROI 预测精度的测试类
 */
@SpringBootTest(classes = LtvApplication.class)
@Transactional
public class DatabaseRoiAccuracyTest {

    @Autowired
    private LtvDailyStatRepository ltvDailyStatRepository;

    @Autowired
    private LtvPredictService ltvPredictService;

    @Test
    @DisplayName("基于数据库真实 Cohort 验证 Day 7, Day 14, Day 30 ROI 预测精度")
    void testDatabaseRoiPredictionAccuracy() {
        System.out.println("\n=========================================================================================");
        System.out.println("            数据库真实 Cohort：Day 7, Day 14, Day 30 ROI 预测精度比对测试");
        System.out.println("=========================================================================================\n");

        List<LtvDailyStat> allStats = ltvDailyStatRepository.findByUserIdAndLaunchDateGreaterThanEqualOrderByLaunchDateAsc(
                3L, LocalDate.of(2026, 7, 10));

        assertNotNull(allStats, "无法找到测试 Cohort 记录");

        System.out.printf("%-12s | %-8s | %-8s | %-8s | %-10s | %-10s | %-10s | %-10s%n",
                "上线日期", "真实D7", "真实D14", "真实D21", "D3预测D7", "D3预测D14", "D7预测D14", "D7预测D21");
        System.out.println("---------------------------------------------------------------------------------------------------------------------");

        List<Double> d3PredD7Errors = new ArrayList<>();
        List<Double> d3PredD14Errors = new ArrayList<>();
        List<Double> d7PredD14Errors = new ArrayList<>();
        List<Double> d7PredD21Errors = new ArrayList<>();

        for (LtvDailyStat stat : allStats) {
            if (stat.getSpend() == null || stat.getSpend().compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal spend = stat.getSpend();
            BigDecimal actualD7Roi = stat.getDay7Roi();
            BigDecimal actualD14Roi = stat.getDay14Roi();
            BigDecimal actualD21Roi = stat.getDay21Roi();

            String launchStr = stat.getLaunchDate() != null ? stat.getLaunchDate().toString() : "N/A";
            String d7Str = actualD7Roi != null && actualD7Roi.compareTo(BigDecimal.ZERO) > 0 ? String.format("%.2f%%", actualD7Roi.doubleValue() * 100) : "N/A";
            String d14Str = actualD14Roi != null && actualD14Roi.compareTo(BigDecimal.ZERO) > 0 ? String.format("%.2f%%", actualD14Roi.doubleValue() * 100) : "N/A";
            String d21Str = actualD21Roi != null && actualD21Roi.compareTo(BigDecimal.ZERO) > 0 ? String.format("%.2f%%", actualD21Roi.doubleValue() * 100) : "N/A";

            // D3 预测
            LtvDailyStat statD3 = createSlicedStat(stat, 3);
            double[] curveD3 = ltvPredictService.predictCohortDailyRechargeCurve(statD3, 3);
            double d3PredD7RoiVal = curveD3[7] / spend.doubleValue();
            double d3PredD14RoiVal = curveD3[14] / spend.doubleValue();

            // D7 预测
            LtvDailyStat statD7 = createSlicedStat(stat, 7);
            double[] curveD7 = ltvPredictService.predictCohortDailyRechargeCurve(statD7, 7);
            double d7PredD14RoiVal = curveD7[14] / spend.doubleValue();
            double d7PredD21RoiVal = curveD7[21] / spend.doubleValue();

            String pD3D7Str = String.format("%.2f%%", d3PredD7RoiVal * 100);
            String pD3D14Str = String.format("%.2f%%", d3PredD14RoiVal * 100);
            String pD7D14Str = String.format("%.2f%%", d7PredD14RoiVal * 100);
            String pD7D21Str = String.format("%.2f%%", d7PredD21RoiVal * 100);

            if (actualD7Roi != null && actualD7Roi.compareTo(BigDecimal.ZERO) > 0) {
                double err = Math.abs(d3PredD7RoiVal - actualD7Roi.doubleValue()) / actualD7Roi.doubleValue() * 100.0;
                d3PredD7Errors.add(err);
            }
            if (actualD14Roi != null && actualD14Roi.compareTo(BigDecimal.ZERO) > 0) {
                double errD3 = Math.abs(d3PredD14RoiVal - actualD14Roi.doubleValue()) / actualD14Roi.doubleValue() * 100.0;
                double errD7 = Math.abs(d7PredD14RoiVal - actualD14Roi.doubleValue()) / actualD14Roi.doubleValue() * 100.0;
                d3PredD14Errors.add(errD3);
                d7PredD14Errors.add(errD7);
            }
            if (actualD21Roi != null && actualD21Roi.compareTo(BigDecimal.ZERO) > 0) {
                double errD7D21 = Math.abs(d7PredD21RoiVal - actualD21Roi.doubleValue()) / actualD21Roi.doubleValue() * 100.0;
                d7PredD21Errors.add(errD7D21);
            }

            System.out.printf("%-12s | %-8s | %-8s | %-8s | %-10s | %-10s | %-10s | %-10s%n",
                    launchStr, d7Str, d14Str, d21Str, pD3D7Str, pD3D14Str, pD7D14Str, pD7D21Str);
        }

        System.out.println("\n---------------------------------------------------------------------------------------------------------------------");
        System.out.println("                                 真实数据库 ROI 预测误差 (MAPE & Weighted Summary)");
        System.out.println("---------------------------------------------------------------------------------------------------------------------");
        
        double mapeD3D7 = d3PredD7Errors.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double mapeD3D14 = d3PredD14Errors.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double mapeD7D14 = d7PredD14Errors.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double mapeD7D21 = d7PredD21Errors.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        // 计算中位数 APE
        Collections.sort(d7PredD14Errors);
        double medianD7D14 = d7PredD14Errors.size() > 0 ? d7PredD14Errors.get(d7PredD14Errors.size() / 2) : 0.0;

        Collections.sort(d7PredD21Errors);
        double medianD7D21 = d7PredD21Errors.size() > 0 ? d7PredD21Errors.get(d7PredD21Errors.size() / 2) : 0.0;

        System.out.printf("Day 3 预测 Day 7  ROI 未加权 MAPE: %.2f%%%n", mapeD3D7);
        System.out.printf("Day 3 预测 Day 14 ROI 未加权 MAPE: %.2f%%%n", mapeD3D14);
        System.out.printf("Day 7 预测 Day 14 ROI 未加权 MAPE: %.2f%%  |  中位数误差 MedAPE: %.2f%%%n", mapeD7D14, medianD7D14);
        System.out.printf("Day 7 预测 Day 21 ROI 未加权 MAPE: %.2f%%  |  中位数误差 MedAPE: %.2f%%%n", mapeD7D21, medianD7D21);
        System.out.println("=========================================================================================\n");
    }

    private LtvDailyStat createSlicedStat(LtvDailyStat origin, int targetDays) {
        LtvDailyStat sliced = new LtvDailyStat();
        sliced.setUserId(origin.getUserId());
        sliced.setLaunchDate(origin.getLaunchDate());
        sliced.setSpend(origin.getSpend());
        sliced.setSubUserCount(origin.getSubUserCount());
        sliced.setSubPeriodDays(origin.getSubPeriodDays());
        sliced.setSubPeriodDistribution(origin.getSubPeriodDistribution());

        BigDecimal cumRecharge = BigDecimal.ZERO;
        for (int d = 1; d <= targetDays; d++) {
            BigDecimal dayRecharge = getRechargeForDay(origin, d);
            if (dayRecharge != null) {
                cumRecharge = dayRecharge;
            }
            BigDecimal roi = origin.getSpend().compareTo(BigDecimal.ZERO) > 0 ?
                    cumRecharge.divide(origin.getSpend(), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            setRechargeAndRoi(sliced, d, cumRecharge, roi);
        }
        sliced.setTotalRecharge(cumRecharge);
        return sliced;
    }

    private BigDecimal getRechargeForDay(LtvDailyStat stat, int day) {
        switch (day) {
            case 1: return stat.getDay1Recharge();
            case 2: return stat.getDay2Recharge();
            case 3: return stat.getDay3Recharge();
            case 4: return stat.getDay4Recharge();
            case 5: return stat.getDay5Recharge();
            case 6: return stat.getDay6Recharge();
            case 7: return stat.getDay7Recharge();
            case 8: return stat.getDay8Recharge();
            case 9: return stat.getDay9Recharge();
            case 10: return stat.getDay10Recharge();
            case 14: return stat.getDay14Recharge();
            case 21: return stat.getDay21Recharge();
            case 30: return stat.getDay30Recharge();
            case 60: return stat.getDay60Recharge();
            default: return null;
        }
    }

    private void setRechargeAndRoi(LtvDailyStat stat, int d, BigDecimal rech, BigDecimal roi) {
        switch (d) {
            case 1: stat.setDay1Recharge(rech); stat.setDay1Roi(roi); break;
            case 2: stat.setDay2Recharge(rech); stat.setDay2Roi(roi); break;
            case 3: stat.setDay3Recharge(rech); stat.setDay3Roi(roi); break;
            case 4: stat.setDay4Recharge(rech); stat.setDay4Roi(roi); break;
            case 5: stat.setDay5Recharge(rech); stat.setDay5Roi(roi); break;
            case 6: stat.setDay6Recharge(rech); stat.setDay6Roi(roi); break;
            case 7: stat.setDay7Recharge(rech); stat.setDay7Roi(roi); break;
            case 8: stat.setDay8Recharge(rech); stat.setDay8Roi(roi); break;
            case 9: stat.setDay9Recharge(rech); stat.setDay9Roi(roi); break;
            case 10: stat.setDay10Recharge(rech); stat.setDay10Roi(roi); break;
            case 14: stat.setDay14Recharge(rech); stat.setDay14Roi(roi); break;
            case 21: stat.setDay21Recharge(rech); stat.setDay21Roi(roi); break;
            case 30: stat.setDay30Recharge(rech); stat.setDay30Roi(roi); break;
            case 60: stat.setDay60Recharge(rech); stat.setDay60Roi(roi); break;
        }
    }
}
