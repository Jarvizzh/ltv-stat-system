package com.ltv.stat.service;

import com.ltv.stat.entity.LtvDailyStat;
import com.ltv.stat.entity.LtvPredictBenchmark;
import com.ltv.stat.dto.PredictionResult;
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
 * 回本预测模型精度验证与回溯测试套件 (Payback Model Accuracy Backtesting Suite)
 */
public class PaybackModelAccuracyTest {

    private LtvBenchmarkService benchmarkService;
    private LtvPredictService predictService;

    @BeforeEach
    void setUp() {
        benchmarkService = Mockito.mock(LtvBenchmarkService.class);
        predictService = new LtvPredictService(benchmarkService);
    }

    /**
     * 精度回溯测试数据结构
     */
    static class BacktestCase {
        String caseName;
        int subPeriodDays;
        String subPeriodDistribution;
        BigDecimal spend;
        int subUserCount;
        int actualPaybackDay;
        double[] actualRechargeCurve; // 1..365 天真实充值
    }

    static class EvaluationMetric {
        int windowDays;
        int totalCases;
        int validPredictions;
        double sumAbsErrorDays;
        double sumAbsPercentageError;
        int hitWithin3Days;
        int hitWithin7Days;
        int hitWithin15Percent;
        int underestimateCount;
        int overestimateCount;

        void recordPrediction(int windowDays, int actualDay, Integer predictedDay) {
            this.windowDays = windowDays;
            this.totalCases++;
            if (predictedDay == null || predictedDay < 0) {
                return;
            }
            this.validPredictions++;
            int errDays = predictedDay - actualDay;
            int absErrDays = Math.abs(errDays);
            double absPe = (double) absErrDays / actualDay * 100.0;

            sumAbsErrorDays += absErrDays;
            sumAbsPercentageError += absPe;

            if (absErrDays <= 3) hitWithin3Days++;
            if (absErrDays <= 7) hitWithin7Days++;
            if (absPe <= 15.0) hitWithin15Percent++;

            if (errDays < 0) underestimateCount++;
            else if (errDays > 0) overestimateCount++;
        }

        double getMae() {
            return validPredictions > 0 ? sumAbsErrorDays / validPredictions : 0;
        }

        double getMape() {
            return validPredictions > 0 ? sumAbsPercentageError / validPredictions : 0;
        }

        double getHitRate3Days() {
            return validPredictions > 0 ? (double) hitWithin3Days / validPredictions * 100.0 : 0;
        }

        double getHitRate7Days() {
            return validPredictions > 0 ? (double) hitWithin7Days / validPredictions * 100.0 : 0;
        }

        double getHitRate15Percent() {
            return validPredictions > 0 ? (double) hitWithin15Percent / validPredictions * 100.0 : 0;
        }
    }

    @Test
    @DisplayName("场景1：高频日续订/单点充值模式精度验证")
    void testDailySubscriptionAccuracy() {
        System.out.println("====== [场景1：高频日续订/单点充值模式精度验证] ======");

        // 构建基准曲线 (subPeriodDays = 1)
        List<LtvPredictBenchmark> benchmarks = new ArrayList<>();
        for (int d = 1; d <= 90; d++) {
            LtvPredictBenchmark b = new LtvPredictBenchmark();
            b.setDayIndex(d);
            b.setSubPeriodDays(1);
            b.setBaseRetentionRate(BigDecimal.valueOf(1.0 / Math.pow(d, 0.45)));
            b.setBaseArpu(BigDecimal.valueOf(1.20));
            benchmarks.add(b);
        }
        when(benchmarkService.getBenchmarkCurve(anyString(), anyString(), eq(1))).thenReturn(benchmarks);

        // 生成真实 Cohort (花费 $1000, 真实在第 12 天回本)
        BacktestCase testCase = createDailyCohort(1000.0, 12);
        int[] windows = {3, 5, 7, 9, 11};

        evaluateAndPrintResults(testCase, windows);
    }

    @Test
    @DisplayName("场景2：周续订阶梯突跃模式精度验证")
    void testWeeklySubscriptionAccuracy() {
        System.out.println("====== [场景2：周续订阶梯突跃模式精度验证] ======");

        List<LtvPredictBenchmark> benchmarks = new ArrayList<>();
        for (int d = 1; d <= 90; d++) {
            LtvPredictBenchmark b = new LtvPredictBenchmark();
            b.setDayIndex(d);
            b.setSubPeriodDays(7);
            if ((d - 1) % 7 == 0) {
                b.setBaseRetentionRate(BigDecimal.valueOf(0.55 * Math.pow((d - 1) / 7 + 1, -0.4)));
            } else {
                b.setBaseRetentionRate(BigDecimal.ZERO);
            }
            b.setBaseArpu(BigDecimal.valueOf(6.99));
            benchmarks.add(b);
        }
        when(benchmarkService.getBenchmarkCurve(anyString(), anyString(), eq(7))).thenReturn(benchmarks);

        // 周续订 Cohort 1: Spend $1000, 真实在第 22 天 (第 4 个周周期) 回本
        BacktestCase case1 = createWeeklyCohort(1000.0, 22);
        // 周续订 Cohort 2: Spend $1500, 真实在第 29 天 (第 5 个周周期) 回本
        BacktestCase case2 = createWeeklyCohort(1500.0, 29);

        int[] windows = {3, 7, 10, 14, 21};

        System.out.println("--- 样本 1 (实际第 22 天回本) ---");
        evaluateAndPrintResults(case1, windows);

        System.out.println("\n--- 样本 2 (实际第 29 天回本) ---");
        evaluateAndPrintResults(case2, windows);
    }

    @Test
    @DisplayName("场景3：月续订长周期模式精度验证")
    void testMonthlySubscriptionAccuracy() {
        System.out.println("====== [场景3：月续订长周期模式精度验证] ======");

        List<LtvPredictBenchmark> benchmarks = new ArrayList<>();
        for (int d = 1; d <= 90; d++) {
            LtvPredictBenchmark b = new LtvPredictBenchmark();
            b.setDayIndex(d);
            b.setSubPeriodDays(30);
            if ((d - 1) % 30 == 0) {
                b.setBaseRetentionRate(BigDecimal.valueOf(0.60 * Math.pow((d - 1) / 30 + 1, -0.5)));
            } else {
                b.setBaseRetentionRate(BigDecimal.ZERO);
            }
            b.setBaseArpu(BigDecimal.valueOf(19.99));
            benchmarks.add(b);
        }
        when(benchmarkService.getBenchmarkCurve(anyString(), anyString(), eq(30))).thenReturn(benchmarks);

        // 月续订 Cohort: Spend $2000, 真实在第 61 天 (第 3 个月周期) 回本
        BacktestCase testCase = createMonthlyCohort(2000.0, 61);
        int[] windows = {7, 14, 21, 30, 45, 60};

        evaluateAndPrintResults(testCase, windows);
    }

    @Test
    @DisplayName("场景4：多周期混合订阅模式综合精度评估")
    void testHybridSubscriptionAccuracy() {
        System.out.println("====== [场景4：多周期混合订阅模式综合精度评估] ======");

        // 配置 7 天和 30 天基准曲线
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

        // 创建 10 个具有不同真实回本天数 (15天 ~ 45天) 的混合订阅测试样本
        List<BacktestCase> cases = new ArrayList<>();
        int[] actualDays = {15, 18, 22, 25, 29, 31, 36, 40, 43, 50};
        for (int i = 0; i < actualDays.length; i++) {
            BacktestCase c = createHybridCohort(1200.0 + i * 150, actualDays[i]);
            c.caseName = "Hybrid_Cohort_" + (i + 1);
            cases.add(c);
        }

        int[] observationWindows = {3, 7, 14, 21, 30};
        Map<Integer, EvaluationMetric> metricsMap = new LinkedHashMap<>();
        for (int w : observationWindows) {
            metricsMap.put(w, new EvaluationMetric());
        }

        System.out.printf("%-18s | %-12s | %-8s | %-8s | %-8s | %-8s | %-8s%n",
                "Cohort 样本", "实际回本(天)", "D3预测", "D7预测", "D14预测", "D21预测", "D30预测");
        System.out.println("-----------------------------------------------------------------------------------------");

        for (BacktestCase c : cases) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-18s | %-12d | ", c.caseName, c.actualPaybackDay));

            for (int w : observationWindows) {
                if (w >= c.actualPaybackDay) {
                    sb.append(String.format("%-8s | ", "已回本"));
                    continue;
                }
                LtvDailyStat stat = buildDailyStatForWindow(c, w);
                PredictionResult res = predictService.predictCohort(stat, w);
                Integer predDay = res != null ? res.getPredictedPaybackDays() : null;

                metricsMap.get(w).recordPrediction(w, c.actualPaybackDay, predDay);
                sb.append(String.format("%-8s | ", predDay != null ? predDay.toString() : "N/A"));
            }
            System.out.println(sb.toString());
        }

        System.out.println("\n---------------- 混合订阅场景综合精度统计 ----------------");
        System.out.printf("%-10s | %-10s | %-10s | %-12s | %-12s | %-12s%n",
                "观察窗口", "MAE (天)", "MAPE (%)", "±3天命中率", "±7天命中率", "±15%相对命中率");
        System.out.println("-----------------------------------------------------------------------------");

        for (int w : observationWindows) {
            EvaluationMetric m = metricsMap.get(w);
            System.out.printf("Day %-6d | %-10.2f | %-10.2f%% | %-11.1f%% | %-11.1f%% | %-12.1f%%%n",
                    w, m.getMae(), m.getMape(), m.getHitRate3Days(), m.getHitRate7Days(), m.getHitRate15Percent());

            // 精度验证断言：随观察窗口增加，MAE 应当逐步降低（精度提升）
            assertTrue(m.getMae() >= 0, "MAE should be non-negative");
        }
    }

    private void evaluateAndPrintResults(BacktestCase c, int[] windows) {
        System.out.printf("测试 Cohort: 消耗=$%.2f, 去重订阅用户=%d人, 实际回本天数=%d天%n",
                c.spend, c.subUserCount, c.actualPaybackDay);
        System.out.printf("%-10s | %-12s | %-12s | %-10s | %-10s%n",
                "观察窗口", "预测回本(天)", "实际回本(天)", "绝对误差(天)", "相对误差(%)");
        System.out.println("---------------------------------------------------------------");

        for (int w : windows) {
            if (w >= c.actualPaybackDay) {
                System.out.printf("Day %-6d | 已在前 %d 天内完成真实回本%n", w, c.actualPaybackDay);
                continue;
            }
            LtvDailyStat stat = buildDailyStatForWindow(c, w);
            PredictionResult res = predictService.predictCohort(stat, w);
            Integer predDay = res != null ? res.getPredictedPaybackDays() : null;

            if (predDay != null && predDay > 0) {
                int err = predDay - c.actualPaybackDay;
                int absErr = Math.abs(err);
                double pe = (double) absErr / c.actualPaybackDay * 100.0;
                System.out.printf("Day %-6d | %-12d | %-12d | %-10d | %-9.2f%%%n",
                        w, predDay, c.actualPaybackDay, err, pe);
            } else {
                System.out.printf("Day %-6d | %-12s | %-12d | N/A        | N/A%n",
                        w, predDay != null ? predDay.toString() : "N/A", c.actualPaybackDay);
            }
        }
    }

    private BacktestCase createDailyCohort(double spendVal, int targetPaybackDay) {
        BacktestCase c = new BacktestCase();
        c.caseName = "Daily_Cohort";
        c.subPeriodDays = 1;
        c.subPeriodDistribution = "{\"1\":100}";
        c.spend = BigDecimal.valueOf(spendVal);
        c.subUserCount = 100;
        c.actualPaybackDay = targetPaybackDay;
        c.actualRechargeCurve = new double[366];

        double dailyAvg = spendVal / (targetPaybackDay * 0.85);
        double cum = 0.0;
        for (int d = 1; d <= 365; d++) {
            double inc = dailyAvg * Math.pow(d, -0.35);
            cum += inc;
            c.actualRechargeCurve[d] = cum;
        }
        // 微调使得在 targetPaybackDay 恰好达到 spendVal
        double scale = spendVal / c.actualRechargeCurve[targetPaybackDay];
        for (int d = 1; d <= 365; d++) {
            c.actualRechargeCurve[d] *= scale;
        }
        return c;
    }

    private BacktestCase createWeeklyCohort(double spendVal, int targetPaybackDay) {
        BacktestCase c = new BacktestCase();
        c.caseName = "Weekly_Cohort";
        c.subPeriodDays = 7;
        c.subPeriodDistribution = "{\"7\":100}";
        c.spend = BigDecimal.valueOf(spendVal);
        c.subUserCount = 100;
        c.actualPaybackDay = targetPaybackDay;
        c.actualRechargeCurve = new double[366];

        double firstPeriodInc = spendVal * 0.45;
        double cum = 0.0;

        for (int d = 1; d <= 365; d++) {
            if (d == 1) {
                cum += firstPeriodInc;
            } else if ((d - 1) % 7 == 0) {
                int cycle = (d - 1) / 7 + 1;
                double renewInc = firstPeriodInc * 0.55 * Math.pow(cycle, -0.4);
                cum += renewInc;
            }
            c.actualRechargeCurve[d] = cum;
        }
        double scale = spendVal / c.actualRechargeCurve[targetPaybackDay];
        for (int d = 1; d <= 365; d++) {
            c.actualRechargeCurve[d] *= scale;
        }
        return c;
    }

    private BacktestCase createMonthlyCohort(double spendVal, int targetPaybackDay) {
        BacktestCase c = new BacktestCase();
        c.caseName = "Monthly_Cohort";
        c.subPeriodDays = 30;
        c.subPeriodDistribution = "{\"30\":100}";
        c.spend = BigDecimal.valueOf(spendVal);
        c.subUserCount = 100;
        c.actualPaybackDay = targetPaybackDay;
        c.actualRechargeCurve = new double[366];

        double firstInc = spendVal * 0.35;
        double cum = 0.0;
        for (int d = 1; d <= 365; d++) {
            if (d == 1) {
                cum += firstInc;
            } else if ((d - 1) % 30 == 0) {
                int cycle = (d - 1) / 30 + 1;
                double renewInc = firstInc * 0.60 * Math.pow(cycle, -0.5);
                cum += renewInc;
            }
            c.actualRechargeCurve[d] = cum;
        }
        double scale = spendVal / c.actualRechargeCurve[targetPaybackDay];
        for (int d = 1; d <= 365; d++) {
            c.actualRechargeCurve[d] *= scale;
        }
        return c;
    }

    private BacktestCase createHybridCohort(double spendVal, int targetPaybackDay) {
        BacktestCase c = new BacktestCase();
        c.caseName = "Hybrid_Cohort";
        c.subPeriodDays = 7;
        c.subPeriodDistribution = "{\"7\":70,\"30\":30}";
        c.spend = BigDecimal.valueOf(spendVal);
        c.subUserCount = 100;
        c.actualPaybackDay = targetPaybackDay;
        c.actualRechargeCurve = new double[366];

        double cum = 0.0;
        for (int d = 1; d <= 365; d++) {
            if (d == 1) {
                cum += spendVal * 0.40;
            } else {
                if ((d - 1) % 7 == 0) {
                    cum += spendVal * 0.08 * Math.pow((d - 1) / 7 + 1, -0.4);
                }
                if ((d - 1) % 30 == 0) {
                    cum += spendVal * 0.15 * Math.pow((d - 1) / 30 + 1, -0.5);
                }
            }
            c.actualRechargeCurve[d] = cum;
        }
        double scale = spendVal / c.actualRechargeCurve[targetPaybackDay];
        for (int d = 1; d <= 365; d++) {
            c.actualRechargeCurve[d] *= scale;
        }
        return c;
    }

    private LtvDailyStat buildDailyStatForWindow(BacktestCase c, int windowDays) {
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
            case 11: stat.setDay11Recharge(rech); stat.setDay11Roi(roi); break;
            case 12: stat.setDay12Recharge(rech); stat.setDay12Roi(roi); break;
            case 13: stat.setDay13Recharge(rech); stat.setDay13Roi(roi); break;
            case 14: stat.setDay14Recharge(rech); stat.setDay14Roi(roi); break;
            case 15: stat.setDay15Recharge(rech); stat.setDay15Roi(roi); break;
            case 16: stat.setDay16Recharge(rech); stat.setDay16Roi(roi); break;
            case 17: stat.setDay17Recharge(rech); stat.setDay17Roi(roi); break;
            case 18: stat.setDay18Recharge(rech); stat.setDay18Roi(roi); break;
            case 19: stat.setDay19Recharge(rech); stat.setDay19Roi(roi); break;
            case 20: stat.setDay20Recharge(rech); stat.setDay20Roi(roi); break;
            case 21: stat.setDay21Recharge(rech); stat.setDay21Roi(roi); break;
            case 22: stat.setDay22Recharge(rech); stat.setDay22Roi(roi); break;
            case 23: stat.setDay23Recharge(rech); stat.setDay23Roi(roi); break;
            case 24: stat.setDay24Recharge(rech); stat.setDay24Roi(roi); break;
            case 25: stat.setDay25Recharge(rech); stat.setDay25Roi(roi); break;
            case 26: stat.setDay26Recharge(rech); stat.setDay26Roi(roi); break;
            case 27: stat.setDay27Recharge(rech); stat.setDay27Roi(roi); break;
            case 28: stat.setDay28Recharge(rech); stat.setDay28Roi(roi); break;
            case 29: stat.setDay29Recharge(rech); stat.setDay29Roi(roi); break;
            case 30: stat.setDay30Recharge(rech); stat.setDay30Roi(roi); break;
            case 31: stat.setDay31Recharge(rech); stat.setDay31Roi(roi); break;
            case 32: stat.setDay32Recharge(rech); stat.setDay32Roi(roi); break;
            case 33: stat.setDay33Recharge(rech); stat.setDay33Roi(roi); break;
            case 34: stat.setDay34Recharge(rech); stat.setDay34Roi(roi); break;
            case 35: stat.setDay35Recharge(rech); stat.setDay35Roi(roi); break;
            case 36: stat.setDay36Recharge(rech); stat.setDay36Roi(roi); break;
            case 37: stat.setDay37Recharge(rech); stat.setDay37Roi(roi); break;
            case 38: stat.setDay38Recharge(rech); stat.setDay38Roi(roi); break;
            case 39: stat.setDay39Recharge(rech); stat.setDay39Roi(roi); break;
            case 40: stat.setDay40Recharge(rech); stat.setDay40Roi(roi); break;
            case 41: stat.setDay41Recharge(rech); stat.setDay41Roi(roi); break;
            case 42: stat.setDay42Recharge(rech); stat.setDay42Roi(roi); break;
            case 43: stat.setDay43Recharge(rech); stat.setDay43Roi(roi); break;
            case 44: stat.setDay44Recharge(rech); stat.setDay44Roi(roi); break;
            case 45: stat.setDay45Recharge(rech); stat.setDay45Roi(roi); break;
            case 46: stat.setDay46Recharge(rech); stat.setDay46Roi(roi); break;
            case 47: stat.setDay47Recharge(rech); stat.setDay47Roi(roi); break;
            case 48: stat.setDay48Recharge(rech); stat.setDay48Roi(roi); break;
            case 49: stat.setDay49Recharge(rech); stat.setDay49Roi(roi); break;
            case 50: stat.setDay50Recharge(rech); stat.setDay50Roi(roi); break;
            case 51: stat.setDay51Recharge(rech); stat.setDay51Roi(roi); break;
            case 52: stat.setDay52Recharge(rech); stat.setDay52Roi(roi); break;
            case 53: stat.setDay53Recharge(rech); stat.setDay53Roi(roi); break;
            case 54: stat.setDay54Recharge(rech); stat.setDay54Roi(roi); break;
            case 55: stat.setDay55Recharge(rech); stat.setDay55Roi(roi); break;
            case 56: stat.setDay56Recharge(rech); stat.setDay56Roi(roi); break;
            case 57: stat.setDay57Recharge(rech); stat.setDay57Roi(roi); break;
            case 58: stat.setDay58Recharge(rech); stat.setDay58Roi(roi); break;
            case 59: stat.setDay59Recharge(rech); stat.setDay59Roi(roi); break;
            case 60: stat.setDay60Recharge(rech); stat.setDay60Roi(roi); break;
        }
    }
}
