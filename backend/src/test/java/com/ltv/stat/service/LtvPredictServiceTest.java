package com.ltv.stat.service;

import com.ltv.stat.entity.LtvDailyStat;
import com.ltv.stat.entity.LtvPredictBenchmark;
import com.ltv.stat.dto.PredictionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class LtvPredictServiceTest {

    private LtvBenchmarkService benchmarkService;
    private LtvPredictService predictService;

    @BeforeEach
    void setUp() {
        benchmarkService = Mockito.mock(LtvBenchmarkService.class);
        predictService = new LtvPredictService(benchmarkService);
    }

    @Test
    void testDailySubPrediction() {
        List<LtvPredictBenchmark> benchmarks = new ArrayList<>();
        for (int d = 1; d <= 90; d++) {
            LtvPredictBenchmark b = new LtvPredictBenchmark();
            b.setDayIndex(d);
            b.setSubPeriodDays(1);
            b.setBaseRetentionRate(BigDecimal.valueOf(1.0 / Math.pow(d, 0.5)));
            b.setBaseArpu(BigDecimal.valueOf(0.99));
            benchmarks.add(b);
        }
        when(benchmarkService.getBenchmarkCurve(anyString(), anyString(), eq(1))).thenReturn(benchmarks);

        LtvDailyStat stat = new LtvDailyStat();
        stat.setSpend(BigDecimal.valueOf(100.00));
        stat.setSubUserCount(100);
        stat.setSubPeriodDays(1);
        stat.setDay1Recharge(BigDecimal.valueOf(99.00));
        stat.setDay1Roi(BigDecimal.valueOf(0.99));
        stat.setDay2Recharge(BigDecimal.valueOf(120.00));
        stat.setDay2Roi(BigDecimal.valueOf(1.20));

        PredictionResult result = predictService.predictCohort(stat, 2);
        assertNotNull(result);
        assertEquals(2, result.getPredictedPaybackDays(), "Already payed back on day 2");
    }

    @Test
    void testWeeklySubPrediction() {
        List<LtvPredictBenchmark> benchmarks = new ArrayList<>();
        for (int d = 1; d <= 90; d++) {
            LtvPredictBenchmark b = new LtvPredictBenchmark();
            b.setDayIndex(d);
            b.setSubPeriodDays(7);
            if ((d - 1) % 7 == 0) {
                b.setBaseRetentionRate(BigDecimal.valueOf(0.5));
            } else {
                b.setBaseRetentionRate(BigDecimal.ZERO);
            }
            b.setBaseArpu(BigDecimal.valueOf(6.99));
            benchmarks.add(b);
        }
        when(benchmarkService.getBenchmarkCurve(anyString(), anyString(), eq(7))).thenReturn(benchmarks);

        LtvDailyStat stat = new LtvDailyStat();
        stat.setSpend(BigDecimal.valueOf(1000.00));
        stat.setSubUserCount(100);
        stat.setSubPeriodDays(7);
        stat.setDay1Recharge(BigDecimal.valueOf(699.00));
        stat.setDay1Roi(BigDecimal.valueOf(0.699));

        PredictionResult result = predictService.predictCohort(stat, 5);
        assertNotNull(result);
        assertEquals(8, result.getPredictedPaybackDays(), "Should pay back on Day 8 weekly renewal step");
    }

    @Test
    void testWeeklySubLongTermPrediction() {
        List<LtvPredictBenchmark> benchmarks = new ArrayList<>();
        for (int d = 1; d <= 90; d++) {
            LtvPredictBenchmark b = new LtvPredictBenchmark();
            b.setDayIndex(d);
            b.setSubPeriodDays(7);
            if ((d - 1) % 7 == 0) {
                b.setBaseRetentionRate(BigDecimal.valueOf(0.15));
            } else {
                b.setBaseRetentionRate(BigDecimal.ZERO);
            }
            b.setBaseArpu(BigDecimal.valueOf(6.99));
            benchmarks.add(b);
        }
        when(benchmarkService.getBenchmarkCurve(anyString(), anyString(), eq(7))).thenReturn(benchmarks);

        LtvDailyStat stat = new LtvDailyStat();
        stat.setSpend(BigDecimal.valueOf(5000.00));
        stat.setSubUserCount(100);
        stat.setSubPeriodDays(7);
        stat.setDay1Recharge(BigDecimal.valueOf(699.00));
        stat.setDay1Roi(BigDecimal.valueOf(0.1398));

        PredictionResult result = predictService.predictCohort(stat, 10);
        assertNotNull(result);
        assertTrue(result.getPredictedPaybackDays() > 90, "Should pay back after day 90 on weekly renewal days");
    }
}
