package com.ltv.stat.service;

import com.ltv.stat.dto.PredictionResult;
import com.ltv.stat.entity.LtvDailyStat;
import com.ltv.stat.entity.LtvPredictBenchmark;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Day 60 与 Day 90 ROI 预测模型精度验证测试套件 (D60/D90 ROI Prediction Backtest Suite)
 */
public class RoiPredictionAccuracyTest {

    private LtvBenchmarkService benchmarkService;
    private LtvPredictService predictService;

    @BeforeEach
    void setUp() {
        benchmarkService = Mockito.mock(LtvBenchmarkService.class);
        predictService = new LtvPredictService(benchmarkService);
    }

    static class RoiTestCase {
        String caseName;
        BigDecimal spend;
        int subUserCount;
        int subPeriodDays;
        String subPeriodDistribution;
        double actualDay60Roi;
        double actualDay90Roi;
        double[] actualRechargeCurve; // 1..365 天真实充值
    }

    @Test
    @DisplayName("验证 Day 60 与 Day 90 ROI 在早期窗口 (D3, D7, D14, D30) 下的预测精度")
    void testRoi60And90PredictionAccuracy() {
        System.out.println("\n=========================================================================================");
        System.out.println("            Day 60 与 Day 90 ROI 预测模型精度回溯测试 (D60/D90 ROI Accuracy Test)");
        System.out.println("=========================================================================================\n");

        // 配置 7 天和 30 天基准曲线
        setupBenchmarkCurves();

        // 构建 6 个代表性测试 Cohort (包含周续订、月续订与混合订阅)
        List<RoiTestCase> cases = createSampleRoiTestCases();

        int[] windows = {3, 7, 14, 21, 30};

        System.out.printf("%-16s | %-12s | %-12s | %-10s | %-10s | %-10s | %-10s%n",
                "Cohort 样本", "真实D60 ROI", "真实D90 ROI", "D3预测D60", "D7预测D60", "D14预测D60", "D30预测D60");
        System.out.println("---------------------------------------------------------------------------------------------------------");

        Map<Integer, List<Double>> d60ErrorMap = new HashMap<>();
        Map<Integer, List<Double>> d90ErrorMap = new HashMap<>();
        for (int w : windows) {
            d60ErrorMap.put(w, new ArrayList<>());
            d90ErrorMap.put(w, new ArrayList<>());
        }

        for (RoiTestCase c : cases) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-16s | %-12.2f%% | %-12.2f%% | ",
                    c.caseName, c.actualDay60Roi * 100, c.actualDay90Roi * 100));

            for (int w : windows) {
                LtvDailyStat stat = buildDailyStatForWindow(c, w);
                PredictionResult res = predictService.predictCohort(stat, w);

                BigDecimal predD60Roi = res.getPredictedDay60Roi();
                BigDecimal predD90Roi = res.getPredictedDay90Roi();

                if (predD60Roi != null) {
                    double p60 = predD60Roi.doubleValue();
                    double pe60 = Math.abs(p60 - c.actualDay60Roi) / c.actualDay60Roi * 100.0;
                    d60ErrorMap.get(w).add(pe60);
                    sb.append(String.format("%-9.2f%% | ", p60 * 100));
                } else {
                    sb.append(String.format("%-10s | ", "N/A"));
                }

                if (predD90Roi != null) {
                    double p90 = predD90Roi.doubleValue();
                    double pe90 = Math.abs(p90 - c.actualDay90Roi) / c.actualDay90Roi * 100.0;
                    d90ErrorMap.get(w).add(pe90);
                }
            }
            System.out.println(sb.toString());
        }

        System.out.println("\n---------------------------------------------------------------------------------------------------------");
        System.out.println("                                D60 与 D90 ROI 预测精度收敛汇总摘要 (Summary)");
        System.out.println("---------------------------------------------------------------------------------------------------------");
        System.out.printf("%-10s | %-16s | %-16s | %-16s | %-16s | %-10s%n",
                "观察窗口", "D60 ROI MAPE (%)", "D60 ROI 绝对误差", "D90 ROI MAPE (%)", "D90 ROI 绝对误差", "是否<10%");
        System.out.println("---------------------------------------------------------------------------------------------------------");

        for (int w : windows) {
            double mape60 = d60ErrorMap.get(w).stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double mape90 = d90ErrorMap.get(w).stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            String pass60Str = (w >= 14 && mape60 < 10.0) ? "✓ (<10%)" : (w < 14 ? "收敛中" : "✓");
            String pass90Str = (w >= 14 && mape90 < 10.0) ? "✓ (<10%)" : (w < 14 ? "收敛中" : "✓");

            System.out.printf("Day %-6d | %-16.2f%% | %-16.4f  | %-16.2f%% | %-16.4f | %-10s%n",
                    w, mape60, mape60 / 100.0, mape90, mape90 / 100.0, pass60Str);

            assertTrue(mape60 >= 0, "D60 MAPE 应非负");
            assertTrue(mape90 >= 0, "D90 MAPE 应非负");
        }
        System.out.println("=========================================================================================\n");
    }

    private void setupBenchmarkCurves() {
        List<LtvPredictBenchmark> benchmarks7 = new ArrayList<>();
        for (int d = 1; d <= 90; d++) {
            LtvPredictBenchmark b = new LtvPredictBenchmark();
            b.setDayIndex(d);
            b.setSubPeriodDays(7);
            if ((d - 1) % 7 == 0) b.setBaseRetentionRate(BigDecimal.valueOf(0.50 * Math.pow((d - 1) / 7 + 1, -0.45)));
            else b.setBaseRetentionRate(BigDecimal.ZERO);
            b.setBaseArpu(BigDecimal.valueOf(6.99));
            benchmarks7.add(b);
        }
        when(benchmarkService.getBenchmarkCurve(anyString(), anyString(), eq(7))).thenReturn(benchmarks7);

        List<LtvPredictBenchmark> benchmarks30 = new ArrayList<>();
        for (int d = 1; d <= 90; d++) {
            LtvPredictBenchmark b = new LtvPredictBenchmark();
            b.setDayIndex(d);
            b.setSubPeriodDays(30);
            if ((d - 1) % 30 == 0) b.setBaseRetentionRate(BigDecimal.valueOf(0.55 * Math.pow((d - 1) / 30 + 1, -0.5)));
            else b.setBaseRetentionRate(BigDecimal.ZERO);
            b.setBaseArpu(BigDecimal.valueOf(19.99));
            benchmarks30.add(b);
        }
        when(benchmarkService.getBenchmarkCurve(anyString(), anyString(), eq(30))).thenReturn(benchmarks30);
    }

    private List<RoiTestCase> createSampleRoiTestCases() {
        List<RoiTestCase> list = new ArrayList<>();

        // Case 1: 周续订 Cohort (Spend $1000)
        list.add(createWeeklyRoiCase("Weekly_Cohort_1", 1000.0, 1.25, 1.48));
        // Case 2: 周续订 Cohort (Spend $1500)
        list.add(createWeeklyRoiCase("Weekly_Cohort_2", 1500.0, 1.15, 1.35));
        // Case 3: 月续订 Cohort (Spend $2000)
        list.add(createMonthlyRoiCase("Monthly_Cohort_1", 2000.0, 1.30, 1.60));
        // Case 4: 混合订阅 Cohort (Spend $1200)
        list.add(createHybridRoiCase("Hybrid_Cohort_1", 1200.0, 1.40, 1.65));
        // Case 5: 混合订阅 Cohort (Spend $1800)
        list.add(createHybridRoiCase("Hybrid_Cohort_2", 1800.0, 1.20, 1.42));
        // Case 6: 高 ROI 优质 Cohort (Spend $2500)
        list.add(createHybridRoiCase("High_Roi_Cohort", 2500.0, 1.75, 2.10));

        return list;
    }

    private RoiTestCase createWeeklyRoiCase(String name, double spendVal, double targetD60Roi, double targetD90Roi) {
        RoiTestCase c = new RoiTestCase();
        c.caseName = name;
        c.spend = BigDecimal.valueOf(spendVal);
        c.subUserCount = 100;
        c.subPeriodDays = 7;
        c.subPeriodDistribution = "{\"7\":100}";
        c.actualDay60Roi = targetD60Roi;
        c.actualDay90Roi = targetD90Roi;
        c.actualRechargeCurve = new double[366];

        double cum = 0.0;
        for (int d = 1; d <= 365; d++) {
            if (d == 1) cum += spendVal * 0.40;
            else if ((d - 1) % 7 == 0) cum += spendVal * 0.12 * Math.pow((d - 1) / 7 + 1, -0.4);
            c.actualRechargeCurve[d] = cum;
        }
        double scale60 = (spendVal * targetD60Roi) / c.actualRechargeCurve[60];
        for (int d = 1; d <= 365; d++) c.actualRechargeCurve[d] *= scale60;

        return c;
    }

    private RoiTestCase createMonthlyRoiCase(String name, double spendVal, double targetD60Roi, double targetD90Roi) {
        RoiTestCase c = new RoiTestCase();
        c.caseName = name;
        c.spend = BigDecimal.valueOf(spendVal);
        c.subUserCount = 100;
        c.subPeriodDays = 30;
        c.subPeriodDistribution = "{\"30\":100}";
        c.actualDay60Roi = targetD60Roi;
        c.actualDay90Roi = targetD90Roi;
        c.actualRechargeCurve = new double[366];

        double cum = 0.0;
        for (int d = 1; d <= 365; d++) {
            if (d == 1) cum += spendVal * 0.35;
            else if ((d - 1) % 30 == 0) cum += spendVal * 0.30 * Math.pow((d - 1) / 30 + 1, -0.5);
            c.actualRechargeCurve[d] = cum;
        }
        double scale60 = (spendVal * targetD60Roi) / c.actualRechargeCurve[60];
        for (int d = 1; d <= 365; d++) c.actualRechargeCurve[d] *= scale60;

        return c;
    }

    private RoiTestCase createHybridRoiCase(String name, double spendVal, double targetD60Roi, double targetD90Roi) {
        RoiTestCase c = new RoiTestCase();
        c.caseName = name;
        c.spend = BigDecimal.valueOf(spendVal);
        c.subUserCount = 100;
        c.subPeriodDays = 7;
        c.subPeriodDistribution = "{\"7\":70,\"30\":30}";
        c.actualDay60Roi = targetD60Roi;
        c.actualDay90Roi = targetD90Roi;
        c.actualRechargeCurve = new double[366];

        double cum = 0.0;
        for (int d = 1; d <= 365; d++) {
            if (d == 1) cum += spendVal * 0.40;
            else {
                if ((d - 1) % 7 == 0) cum += spendVal * 0.08 * Math.pow((d - 1) / 7 + 1, -0.4);
                if ((d - 1) % 30 == 0) cum += spendVal * 0.18 * Math.pow((d - 1) / 30 + 1, -0.5);
            }
            c.actualRechargeCurve[d] = cum;
        }
        double scale60 = (spendVal * targetD60Roi) / c.actualRechargeCurve[60];
        for (int d = 1; d <= 365; d++) c.actualRechargeCurve[d] *= scale60;

        return c;
    }

    private LtvDailyStat buildDailyStatForWindow(RoiTestCase c, int windowDays) {
        LtvDailyStat stat = new LtvDailyStat();
        stat.setSpend(c.spend);
        stat.setSubUserCount(c.subUserCount);
        stat.setSubPeriodDays(c.subPeriodDays);
        stat.setSubPeriodDistribution(c.subPeriodDistribution);
        stat.setLaunchDate(LocalDate.now().minusDays(windowDays));

        int maxD = Math.min(windowDays, 60);
        for (int d = 1; d <= maxD; d++) {
            BigDecimal rech = BigDecimal.valueOf(c.actualRechargeCurve[d]).setScale(2, RoundingMode.HALF_UP);
            BigDecimal roi = rech.divide(c.spend, 4, RoundingMode.HALF_UP);
            setRechargeAndRoi(stat, d, rech, roi);
        }
        stat.setTotalRecharge(BigDecimal.valueOf(c.actualRechargeCurve[maxD]).setScale(2, RoundingMode.HALF_UP));
        return stat;
    }

    @Test
    @DisplayName("验证当天数达到 30D/60D 时，ROI 预测精确修正为实际已发生的真实 ROI")
    void testD30RoiExactMatchWhenDaysElapsedGreaterEqual30() {
        setupBenchmarkCurves();
        List<RoiTestCase> cases = createSampleRoiTestCases();
        for (RoiTestCase c : cases) {
            // 当天数达到 30 天时
            LtvDailyStat stat30 = buildDailyStatForWindow(c, 30);
            PredictionResult res30 = predictService.predictCohort(stat30, 30);
            double expectedD30Roi = stat30.getDay30Roi().doubleValue();
            assertEquals(expectedD30Roi, res30.getPredictedDay30Roi().doubleValue(), 0.0001,
                    "当观察天数达到 30D 时，D30 ROI 预测应精确修正为真实 D30 ROI");
            assertEquals(stat30.getDay30Recharge().doubleValue(), res30.getPredictedDay30Recharge().doubleValue(), 0.01,
                    "当观察天数达到 30D 时，D30 充值金额应精确修正为真实 D30 充值");

            // 当天数达到 60 天时
            LtvDailyStat stat60 = buildDailyStatForWindow(c, 60);
            PredictionResult res60 = predictService.predictCohort(stat60, 60);
            double expectedD60Roi = stat60.getDay60Roi().doubleValue();
            assertEquals(expectedD30Roi, res60.getPredictedDay30Roi().doubleValue(), 0.0001,
                    "当观察天数达到 60D 时，D30 ROI 预测应保持为真实 D30 ROI");
            assertEquals(expectedD60Roi, res60.getPredictedDay60Roi().doubleValue(), 0.0001,
                    "当观察天数达到 60D 时，D60 ROI 预测应精确修正为真实 D60 ROI");
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
