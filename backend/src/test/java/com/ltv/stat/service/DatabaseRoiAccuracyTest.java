package com.ltv.stat.service;

import com.ltv.stat.LtvApplication;
import com.ltv.stat.entity.LtvDailyStat;
import com.ltv.stat.repository.LtvDailyStatRepository;
import com.ltv.stat.util.CohortStatHelper;
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
    private LtvBenchmarkService ltvBenchmarkService;

    @Autowired
    private LtvPredictService ltvPredictService;

    @Autowired
    private LtvStatService ltvStatService;

    @Test
    @DisplayName("验证月度 ROI 预测 (Monthly Summary) 同步优化效果")
    void testMonthlySummaryRoiPrediction() {
        System.out.println("\n=========================================================================================");
        System.out.println("            用户月度指标汇总 (Monthly Summary) ROI 预测同步优化验证");
        System.out.println("=========================================================================================\n");

        ltvBenchmarkService.recalculateAllBenchmarks();
        com.ltv.stat.dto.MonthlySummaryDto summary = ltvStatService.getMonthlySummaryForUser(3L);

        assertNotNull(summary, "无法生成月度汇总数据");

        if (summary.getLastMonth() != null) {
            com.ltv.stat.dto.SingleMonthSummaryDto lm = summary.getLastMonth();
            System.out.println("【上月指标汇总 (" + lm.getMonth() + ")】:");
            System.out.printf("  总消耗: $%s | 总充值: $%s | 累计ROI: %s%%%n", lm.getSpend(), lm.getRecharge(), lm.getRoi());
            System.out.printf("  预测回本天数: %s 天%n", lm.getActualPaybackDays() != null ? lm.getActualPaybackDays() : "未回本/计算中");
            System.out.printf("  D30 预测 ROI: %s%%%n", lm.getPredictedDay30Roi() != null ? lm.getPredictedDay30Roi().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) : "N/A");
            System.out.printf("  D60 预测 ROI: %s%%%n", lm.getPredictedDay60Roi() != null ? lm.getPredictedDay60Roi().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) : "N/A");
            System.out.printf("  D90 预测 ROI: %s%%%n", lm.getPredictedDay90Roi() != null ? lm.getPredictedDay90Roi().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP) : "N/A");
        }

        if (summary.getThisMonth() != null) {
            com.ltv.stat.dto.SingleMonthSummaryDto tm = summary.getThisMonth();
            System.out.println("\n【本月指标汇总 (" + tm.getMonth() + ")】:");
            System.out.printf("  总消耗: $%s | 总充值: $%s | 累计ROI: %s%%%n", tm.getSpend(), tm.getRecharge(), tm.getRoi());
        }
        System.out.println("=========================================================================================\n");
    }

    @Test
    @DisplayName("基于数据库真实 Cohort 验证 Day 30 / Day 60 / Day 90 ROI 预测精度")
    void testDatabaseD30D60D90RoiPredictionAccuracy() {
        System.out.println("\n=========================================================================================");
        System.out.println("       数据库真实 Cohort：D30 / D60 / D90 ROI 预测精度全景回溯测试 (jarvis 用户视角)");
        System.out.println("=========================================================================================\n");

        ltvBenchmarkService.recalculateAllBenchmarks();

        List<LtvDailyStat> allStats = ltvDailyStatRepository.findByUserIdAndLaunchDateGreaterThanEqualOrderByLaunchDateAsc(
                3L, LocalDate.of(2026, 7, 10));

        assertNotNull(allStats, "无法找到测试 Cohort 记录");

        List<LtvDailyStat> d30Cohorts = new ArrayList<>();
        for (LtvDailyStat s : allStats) {
            if (s.getSpend() != null && s.getSpend().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal actualD30 = CohortStatHelper.getRoiForDay(s, 30);
                if (actualD30 == null || actualD30.compareTo(BigDecimal.ZERO) <= 0) {
                    BigDecimal rech30 = CohortStatHelper.getRechargeForDay(s, 30);
                    if (rech30 != null && rech30.compareTo(BigDecimal.ZERO) > 0) {
                        actualD30 = rech30.divide(s.getSpend(), 4, RoundingMode.HALF_UP);
                    }
                }
                if (actualD30 != null && actualD30.compareTo(BigDecimal.ZERO) > 0) {
                    d30Cohorts.add(s);
                }
            }
        }

        System.out.printf("找到 %d 个已产生真实 D30 数据的数据库 Cohort 批次进行回溯验证：%n%n", d30Cohorts.size());
        System.out.printf("%-12s | %-9s | %-9s | %-9s | %-9s | %-9s | %-9s | %-9s%n",
                "上线日期", "消耗 ($)", "真实D30", "D3预测D30", "D7预测D30", "D14预测D30", "D21预测D30", "D30修正值");
        System.out.println("-----------------------------------------------------------------------------------------------------------------");

        int[] cutoffs = {3, 7, 14, 21, 30};
        Map<Integer, List<Double>> d30ErrorMap = new HashMap<>();
        Map<Integer, List<Double>> d30AbsDiffMap = new HashMap<>();
        for (int c : cutoffs) {
            d30ErrorMap.put(c, new ArrayList<>());
            d30AbsDiffMap.put(c, new ArrayList<>());
        }

        for (LtvDailyStat stat : d30Cohorts) {
            BigDecimal spend = stat.getSpend();
            BigDecimal actualD30Roi = CohortStatHelper.getRoiForDay(stat, 30);
            if (actualD30Roi == null || actualD30Roi.compareTo(BigDecimal.ZERO) <= 0) {
                actualD30Roi = CohortStatHelper.getRechargeForDay(stat, 30).divide(spend, 4, RoundingMode.HALF_UP);
            }
            double actual30Val = actualD30Roi.doubleValue();

            String launchStr = stat.getLaunchDate() != null ? stat.getLaunchDate().toString() : "N/A";
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-12s | %-9.2f | %-8.2f%% | ", launchStr, spend.doubleValue(), actual30Val * 100));

            for (int c : cutoffs) {
                LtvDailyStat sliced = createSlicedStat(stat, c);
                double[] curve = ltvPredictService.predictCohortDailyRechargeCurve(sliced, c);
                double rawD30 = curve[30] / spend.doubleValue();
                com.ltv.stat.dto.PredictionResult res = ltvPredictService.predictCohort(sliced, c);
                BigDecimal predD30 = res.getPredictedDay30Roi();
                if (predD30 != null) {
                    double p30 = predD30.doubleValue();
                    double ape = Math.abs(p30 - actual30Val) / actual30Val * 100.0;
                    double absDiff = Math.abs(p30 - actual30Val);
                    d30ErrorMap.get(c).add(ape);
                    d30AbsDiffMap.get(c).add(absDiff);
                    sb.append(String.format("%-8.2f%%(raw:%-6.2f%%) | ", p30 * 100, rawD30 * 100));
                } else {
                    sb.append(String.format("%-9s | ", "N/A"));
                }
            }
            System.out.println(sb.toString());
        }

        System.out.println("\n-----------------------------------------------------------------------------------------------------------------");
        System.out.println("                          数据库真实 Cohort：D30 ROI 预测精度汇总 (Accuracy Summary)");
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-10s | %-12s | %-16s | %-16s | %-14s | %-14s%n",
                "观察窗口", "样本数", "平均相对误差MAPE", "平均绝对误差MAE", "误差<=10%占比", "误差<=20%占比");
        System.out.println("-----------------------------------------------------------------------------------------------------------------");

        for (int c : cutoffs) {
            List<Double> apeList = d30ErrorMap.get(c);
            List<Double> absList = d30AbsDiffMap.get(c);
            double mape = apeList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double mae = absList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            long hit10 = apeList.stream().filter(e -> e <= 10.0).count();
            long hit20 = apeList.stream().filter(e -> e <= 20.0).count();
            double hit10Pct = (double) hit10 / apeList.size() * 100.0;
            double hit20Pct = (double) hit20 / apeList.size() * 100.0;

            System.out.printf("Day %-6d | %-12d | %-15.2f%% | %-16.4f | %-13.1f%% | %-13.1f%%%n",
                    c, apeList.size(), mape, mae, hit10Pct, hit20Pct);
        }
        System.out.println("=========================================================================================\n");
    }

    @Test
    @DisplayName("基于数据库真实 Cohort 验证 Day 7, Day 14, Day 21 ROI 预测精度")
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
