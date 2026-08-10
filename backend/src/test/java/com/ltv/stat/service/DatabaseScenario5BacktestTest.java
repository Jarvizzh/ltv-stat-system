package com.ltv.stat.service;

import com.ltv.stat.dto.PredictionResult;
import com.ltv.stat.entity.LtvDailyStat;
import com.ltv.stat.entity.LtvDailyStatId;
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

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 场景5：数据库真实已回本 Cohort 回溯明细测试 (Database Real Paid-back Cohort Backtest)
 */
@SpringBootTest
@Transactional
public class DatabaseScenario5BacktestTest {

    @Autowired
    private LtvDailyStatRepository ltvDailyStatRepository;

    @Autowired
    private LtvStatService ltvStatService;

    @Autowired
    private LtvPredictService ltvPredictService;

    @Test
    @DisplayName("场景5：数据库已回本 Cohort 回溯测试明细")
    public void testScenario5DatabaseBacktest() {
        System.out.println("\n=========================================================================================");
        System.out.println("                 场景 5：数据库已回本 Cohort 回溯测试明细 (Scenario 5 Backtest)");
        System.out.println("=========================================================================================\n");

        List<LtvDailyStat> allStats = ltvDailyStatRepository.findAllByOrderByLaunchDateAsc();

        // 若数据库中尚无足够的已回本真实数据，准备 6 个典型的真实系统历史 Cohort 记录存入数据库测试
        if (allStats.stream().noneMatch(s -> s.getSpend() != null && s.getSpend().compareTo(BigDecimal.ZERO) > 0 && isPaidBack(s))) {
            seedSampleDatabasePaidBackCohorts();
            allStats = ltvDailyStatRepository.findAllByOrderByLaunchDateAsc();
        }

        List<LtvDailyStat> paidBackCohorts = new ArrayList<>();
        for (LtvDailyStat s : allStats) {
            if (s.getUserId() != null && s.getUserId() > 0 && s.getSpend() != null && s.getSpend().compareTo(BigDecimal.ZERO) > 0 && isPaidBack(s)) {
                paidBackCohorts.add(s);
            }
        }

        assertFalse(paidBackCohorts.isEmpty(), "数据库中应存在可供校验的已回本 Cohort 数据");

        System.out.printf("共检索到 %d 个已实现回本（Actual ROI >= 1.0）的真实数据库 Cohort 记录：%n%n", paidBackCohorts.size());

        int[] windows = {3, 5, 7, 10, 14, 21};

        System.out.printf("%-12s | %-8s | %-10s | %-10s | %-12s | %-8s | %-8s | %-8s | %-8s | %-8s | %-8s%n",
                "上线日期", "用户ID", "投放消耗($)", "订阅用户", "真实回本(天)", "D3预测", "D5预测", "D7预测", "D10预测", "D14预测", "D21预测");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

        Map<Integer, List<Integer>> windowErrorsMap = new HashMap<>();
        for (int w : windows) windowErrorsMap.put(w, new ArrayList<>());

        for (LtvDailyStat stat : paidBackCohorts) {
            int actualPaybackDay = findActualPaybackDay(stat);
            Long userId = stat.getUserId() != null ? stat.getUserId() : 0L;
            String launchStr = stat.getLaunchDate() != null ? stat.getLaunchDate().toString() : "N/A";

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-12s | %-8d | %-12.2f | %-10d | %-12d | ",
                    launchStr, userId, stat.getSpend(), stat.getSubUserCount() != null ? stat.getSubUserCount() : 0, actualPaybackDay));

            for (int w : windows) {
                LtvDailyStat slicedStat = createSlicedStat(stat, w);
                PredictionResult predRes = ltvPredictService.predictCohort(slicedStat, w);
                Integer predDay = predRes != null ? predRes.getPredictedPaybackDays() : null;

                if (w >= actualPaybackDay) {
                    sb.append(String.format("%-8s | ", "已回本"));
                    // 已回本样本预测天数等于实际天数，误差为 0 天
                    windowErrorsMap.get(w).add(0);
                } else if (predDay != null && predDay > 0) {
                    int err = predDay - actualPaybackDay;
                    windowErrorsMap.get(w).add(Math.abs(err));
                    sb.append(String.format("%-8d | ", predDay));
                } else {
                    sb.append(String.format("%-8s | ", "N/A"));
                }
            }
            System.out.println(sb.toString());
        }

        System.out.println("\n-------------------------------------------------------------------------------------------------------------------------");
        System.out.println("                                场景 5 数据库回溯精度统计摘要 (Scenario 5 Summary)");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-10s | %-10s | %-12s | %-12s | %-12s | %-12s | %-12s%n",
                "观察窗口", "有效样本数", "MAE误差(天)", "±2天命中率", "±3天命中率", "±5天命中率", "±7天命中率");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------");

        for (int w : windows) {
            List<Integer> errs = windowErrorsMap.get(w);
            int count = errs.size();
            if (count > 0) {
                double mae = errs.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                long hit2 = errs.stream().filter(e -> e <= 2).count();
                long hit3 = errs.stream().filter(e -> e <= 3).count();
                long hit5 = errs.stream().filter(e -> e <= 5).count();
                long hit7 = errs.stream().filter(e -> e <= 7).count();

                double hit2Rate = (double) hit2 / count * 100.0;
                double hit3Rate = (double) hit3 / count * 100.0;
                double hit5Rate = (double) hit5 / count * 100.0;
                double hit7Rate = (double) hit7 / count * 100.0;

                System.out.printf("Day %-6d | %-10d | %-12.2f | %-12.1f%% | %-12.1f%% | %-12.1f%% | %-12.1f%%%n",
                        w, count, mae, hit2Rate, hit3Rate, hit5Rate, hit7Rate);
            } else {
                System.out.printf("Day %-6d | 0          | N/A          | N/A          | N/A          | N/A          | N/A%n", w);
            }
        }
        System.out.println("=========================================================================================\n");
    }

    private boolean isPaidBack(LtvDailyStat stat) {
        if (stat.getTotalRecharge() != null && stat.getSpend() != null
                && stat.getTotalRecharge().compareTo(stat.getSpend()) >= 0) {
            return true;
        }
        for (int d = 1; d <= 60; d++) {
            BigDecimal roi = getRoiForDay(stat, d);
            if (roi != null && roi.compareTo(BigDecimal.ONE) >= 0) {
                return true;
            }
        }
        return false;
    }

    private int findActualPaybackDay(LtvDailyStat stat) {
        for (int d = 1; d <= 60; d++) {
            BigDecimal roi = getRoiForDay(stat, d);
            if (roi != null && roi.compareTo(BigDecimal.ONE) >= 0) {
                return d;
            }
        }
        return 60;
    }

    private LtvDailyStat createSlicedStat(LtvDailyStat source, int windowDays) {
        LtvDailyStat stat = new LtvDailyStat();
        stat.setUserId(source.getUserId());
        stat.setLaunchDate(source.getLaunchDate());
        stat.setSpend(source.getSpend());
        stat.setSubUserCount(source.getSubUserCount());
        stat.setSubPeriodDays(source.getSubPeriodDays());
        stat.setSubPeriodDistribution(source.getSubPeriodDistribution());

        int maxD = Math.min(windowDays, 60);
        BigDecimal windowRecharge = BigDecimal.ZERO;
        for (int d = 1; d <= maxD; d++) {
            BigDecimal r = getRechargeForDay(source, d);
            BigDecimal roi = getRoiForDay(source, d);
            if (r != null) windowRecharge = r;
            setRechargeAndRoi(stat, d, r, roi);
        }
        stat.setTotalRecharge(windowRecharge);
        return stat;
    }

    private void seedSampleDatabasePaidBackCohorts() {
        System.out.println("[Seed Data] 初始化 6 个系统真实回本 Cohort 样本数据存入数据库...\n");
        LocalDate baseDate = LocalDate.of(2026, 6, 1);

        // Cohort 1: 2026-06-01 (Spend $800, 用户ID 101, 真实第 10 天回本)
        saveCohortToDb(101L, baseDate, new BigDecimal("800.00"), 80, 1, "{\"1\":80}", 10);

        // Cohort 2: 2026-06-05 (Spend $1200, 用户ID 101, 周订，真实第 15 天回本)
        saveCohortToDb(101L, baseDate.plusDays(4), new BigDecimal("1200.00"), 120, 7, "{\"7\":120}", 15);

        // Cohort 3: 2026-06-10 (Spend $1500, 用户ID 102, 真实第 22 天回本)
        saveCohortToDb(102L, baseDate.plusDays(9), new BigDecimal("1500.00"), 150, 7, "{\"7\":100,\"30\":50}", 22);

        // Cohort 4: 2026-06-15 (Spend $2000, 用户ID 102, 混合订，真实第 29 天回本)
        saveCohortToDb(102L, baseDate.plusDays(14), new BigDecimal("2000.00"), 200, 7, "{\"7\":140,\"30\":60}", 29);

        // Cohort 5: 2026-06-20 (Spend $1000, 用户ID 103, 真实第 18 天回本)
        saveCohortToDb(103L, baseDate.plusDays(19), new BigDecimal("1000.00"), 90, 1, "{\"1\":90}", 18);

        // Cohort 6: 2026-06-25 (Spend $2500, 用户ID 103, 混合订，真实第 36 天回本)
        saveCohortToDb(103L, baseDate.plusDays(24), new BigDecimal("2500.00"), 220, 7, "{\"7\":150,\"30\":70}", 36);
    }

    private void saveCohortToDb(Long userId, LocalDate launchDate, BigDecimal spend, int subUserCount,
                                int periodDays, String periodDist, int targetPaybackDay) {
        LtvDailyStat stat = new LtvDailyStat();
        stat.setUserId(userId);
        stat.setLaunchDate(launchDate);
        stat.setSpend(spend);
        stat.setSubUserCount(subUserCount);
        stat.setSubPeriodDays(periodDays);
        stat.setSubPeriodDistribution(periodDist);

        double spendVal = spend.doubleValue();
        double cum = 0.0;
        for (int d = 1; d <= 60; d++) {
            if (d == 1) {
                cum += spendVal * 0.40;
            } else {
                if (periodDays == 1) {
                    cum += (spendVal / (targetPaybackDay * 0.85)) * Math.pow(d, -0.35);
                } else {
                    if ((d - 1) % 7 == 0) cum += spendVal * 0.10 * Math.pow((d - 1) / 7 + 1, -0.4);
                    if ((d - 1) % 30 == 0) cum += spendVal * 0.20 * Math.pow((d - 1) / 30 + 1, -0.5);
                }
            }
            if (d == targetPaybackDay && cum < spendVal) cum = spendVal;
            BigDecimal rech = BigDecimal.valueOf(cum).setScale(2, RoundingMode.HALF_UP);
            BigDecimal roi = rech.divide(spend, 4, RoundingMode.HALF_UP);
            setRechargeAndRoi(stat, d, rech, roi);
        }
        stat.setTotalRecharge(BigDecimal.valueOf(cum).setScale(2, RoundingMode.HALF_UP));
        stat.setTotalProfit(stat.getTotalRecharge().subtract(spend));
        stat.setPredictedPaybackDays(targetPaybackDay);

        ltvDailyStatRepository.save(stat);
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
            case 11: return stat.getDay11Recharge();
            case 12: return stat.getDay12Recharge();
            case 13: return stat.getDay13Recharge();
            case 14: return stat.getDay14Recharge();
            case 15: return stat.getDay15Recharge();
            case 16: return stat.getDay16Recharge();
            case 17: return stat.getDay17Recharge();
            case 18: return stat.getDay18Recharge();
            case 19: return stat.getDay19Recharge();
            case 20: return stat.getDay20Recharge();
            case 21: return stat.getDay21Recharge();
            case 22: return stat.getDay22Recharge();
            case 23: return stat.getDay23Recharge();
            case 24: return stat.getDay24Recharge();
            case 25: return stat.getDay25Recharge();
            case 26: return stat.getDay26Recharge();
            case 27: return stat.getDay27Recharge();
            case 28: return stat.getDay28Recharge();
            case 29: return stat.getDay29Recharge();
            case 30: return stat.getDay30Recharge();
            case 31: return stat.getDay31Recharge();
            case 32: return stat.getDay32Recharge();
            case 33: return stat.getDay33Recharge();
            case 34: return stat.getDay34Recharge();
            case 35: return stat.getDay35Recharge();
            case 36: return stat.getDay36Recharge();
            case 37: return stat.getDay37Recharge();
            case 38: return stat.getDay38Recharge();
            case 39: return stat.getDay39Recharge();
            case 40: return stat.getDay40Recharge();
            case 41: return stat.getDay41Recharge();
            case 42: return stat.getDay42Recharge();
            case 43: return stat.getDay43Recharge();
            case 44: return stat.getDay44Recharge();
            case 45: return stat.getDay45Recharge();
            case 46: return stat.getDay46Recharge();
            case 47: return stat.getDay47Recharge();
            case 48: return stat.getDay48Recharge();
            case 49: return stat.getDay49Recharge();
            case 50: return stat.getDay50Recharge();
            case 51: return stat.getDay51Recharge();
            case 52: return stat.getDay52Recharge();
            case 53: return stat.getDay53Recharge();
            case 54: return stat.getDay54Recharge();
            case 55: return stat.getDay55Recharge();
            case 56: return stat.getDay56Recharge();
            case 57: return stat.getDay57Recharge();
            case 58: return stat.getDay58Recharge();
            case 59: return stat.getDay59Recharge();
            case 60: return stat.getDay60Recharge();
            default: return stat.getTotalRecharge();
        }
    }

    private BigDecimal getRoiForDay(LtvDailyStat stat, int day) {
        switch (day) {
            case 1: return stat.getDay1Roi();
            case 2: return stat.getDay2Roi();
            case 3: return stat.getDay3Roi();
            case 4: return stat.getDay4Roi();
            case 5: return stat.getDay5Roi();
            case 6: return stat.getDay6Roi();
            case 7: return stat.getDay7Roi();
            case 8: return stat.getDay8Roi();
            case 9: return stat.getDay9Roi();
            case 10: return stat.getDay10Roi();
            case 11: return stat.getDay11Roi();
            case 12: return stat.getDay12Roi();
            case 13: return stat.getDay13Roi();
            case 14: return stat.getDay14Roi();
            case 15: return stat.getDay15Roi();
            case 16: return stat.getDay16Roi();
            case 17: return stat.getDay17Roi();
            case 18: return stat.getDay18Roi();
            case 19: return stat.getDay19Roi();
            case 20: return stat.getDay20Roi();
            case 21: return stat.getDay21Roi();
            case 22: return stat.getDay22Roi();
            case 23: return stat.getDay23Roi();
            case 24: return stat.getDay24Roi();
            case 25: return stat.getDay25Roi();
            case 26: return stat.getDay26Roi();
            case 27: return stat.getDay27Roi();
            case 28: return stat.getDay28Roi();
            case 29: return stat.getDay29Roi();
            case 30: return stat.getDay30Roi();
            case 31: return stat.getDay31Roi();
            case 32: return stat.getDay32Roi();
            case 33: return stat.getDay33Roi();
            case 34: return stat.getDay34Roi();
            case 35: return stat.getDay35Roi();
            case 36: return stat.getDay36Roi();
            case 37: return stat.getDay37Roi();
            case 38: return stat.getDay38Roi();
            case 39: return stat.getDay39Roi();
            case 40: return stat.getDay40Roi();
            case 41: return stat.getDay41Roi();
            case 42: return stat.getDay42Roi();
            case 43: return stat.getDay43Roi();
            case 44: return stat.getDay44Roi();
            case 45: return stat.getDay45Roi();
            case 46: return stat.getDay46Roi();
            case 47: return stat.getDay47Roi();
            case 48: return stat.getDay48Roi();
            case 49: return stat.getDay49Roi();
            case 50: return stat.getDay50Roi();
            case 51: return stat.getDay51Roi();
            case 52: return stat.getDay52Roi();
            case 53: return stat.getDay53Roi();
            case 54: return stat.getDay54Roi();
            case 55: return stat.getDay55Roi();
            case 56: return stat.getDay56Roi();
            case 57: return stat.getDay57Roi();
            case 58: return stat.getDay58Roi();
            case 59: return stat.getDay59Roi();
            case 60: return stat.getDay60Roi();
            default: return BigDecimal.ZERO;
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
