package com.ltv.stat.service;

import com.ltv.stat.dto.PredictionResult;
import com.ltv.stat.entity.LtvDailyStat;
import com.ltv.stat.entity.LtvPredictBenchmark;
import com.ltv.stat.entity.SubscriptionConfigVersion;
import com.ltv.stat.repository.SubscriptionConfigVersionRepository;
import com.ltv.stat.service.engine.*;
import com.ltv.stat.util.CohortStatHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LtvPredictService {

    private static final Logger log = LoggerFactory.getLogger(LtvPredictService.class);

    private final LtvBenchmarkService benchmarkService;
    private final SubscriptionConfigVersionRepository versionRepository;
    private final LtvPredictFacade ltvPredictFacade;
    private final CohortCurveExtrapolator extrapolator;

    @Autowired
    public LtvPredictService(LtvBenchmarkService benchmarkService,
                             @Autowired(required = false) SubscriptionConfigVersionRepository versionRepository,
                             LtvPredictFacade ltvPredictFacade,
                             CohortCurveExtrapolator extrapolator) {
        this.benchmarkService = benchmarkService;
        this.versionRepository = versionRepository;
        this.ltvPredictFacade = ltvPredictFacade != null ? ltvPredictFacade : new LtvPredictFacade(new PaybackPredictEngine(), new RoiPredictEngine());
        this.extrapolator = extrapolator != null ? extrapolator : new CohortCurveExtrapolator();
    }

    public LtvPredictService(LtvBenchmarkService benchmarkService) {
        this(benchmarkService, null, new LtvPredictFacade(new PaybackPredictEngine(), new RoiPredictEngine()), new CohortCurveExtrapolator());
    }

    /**
     * 单个 Cohort 回本与 ROI 预测入口 (委托给 LtvPredictFacade 引擎组装)
     */
    public PredictionResult predictCohort(LtvDailyStat stat, int daysElapsed) {
        if (stat == null || stat.getSpend() == null || stat.getSpend().compareTo(BigDecimal.ZERO) <= 0) {
            return new PredictionResult(null);
        }

        double[] cumRecharge = predictCohortDailyRechargeCurve(stat, daysElapsed);
        return ltvPredictFacade.assembleCohortPrediction(stat, daysElapsed, cumRecharge);
    }

    /**
     * 自下而上（Bottom-Up）独立 Cohort 预测叠加方案 (委托给 LtvPredictFacade 引擎组装)
     */
    public PredictionResult predictOverallCohort(List<LtvDailyStat> statList) {
        if (statList == null || statList.isEmpty()) {
            return new PredictionResult(null);
        }

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));

        List<LtvDailyStat> validStats = statList.stream()
                .filter(s -> s != null && s.getSpend() != null && s.getSpend().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        if (validStats.isEmpty()) {
            return new PredictionResult(null);
        }

        BigDecimal totalSpendAll = validStats.stream().map(LtvDailyStat::getSpend).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRechargeAll = validStats.stream().map(LtvDailyStat::getTotalRecharge).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalSpendAll.compareTo(BigDecimal.ZERO) <= 0) {
            return new PredictionResult(null);
        }

        LocalDate minLaunchDate = validStats.stream()
                .map(LtvDailyStat::getLaunchDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(today);

        Map<LtvDailyStat, double[]> cohortCurves = new HashMap<>();
        for (LtvDailyStat s : validStats) {
            LocalDate lDate = s.getLaunchDate() != null ? s.getLaunchDate() : minLaunchDate;
            int age = (int) ChronoUnit.DAYS.between(lDate, today) + 1;
            age = Math.max(1, Math.min(age, 60));

            double[] curve = predictCohortDailyRechargeCurve(s, age);
            cohortCurves.put(s, curve);
        }

        return ltvPredictFacade.assembleOverallPrediction(totalSpendAll, totalRechargeAll, validStats, cohortCurves, minLaunchDate, today);
    }

    /**
     * 生成单个 Cohort 从第 1 天到第 365 天的累计预测充值曲线 double[367]
     */
    public double[] predictCohortDailyRechargeCurve(LtvDailyStat stat, int daysElapsed) {
        double[] cumRecharge = new double[367];
        if (stat == null || stat.getSpend() == null || stat.getSpend().compareTo(BigDecimal.ZERO) <= 0) {
            return cumRecharge;
        }
        int maxDays = Math.min(daysElapsed, 60);
        BigDecimal spend = stat.getSpend();

        // 填充 1 到 maxDays 的真实历史充值
        for (int d = 1; d <= maxDays; d++) {
            BigDecimal r = getRechargeForDay(stat, d);
            if (r != null) {
                cumRecharge[d] = r.doubleValue();
            } else {
                cumRecharge[d] = cumRecharge[d - 1];
            }
        }

        BigDecimal actualRecharge = BigDecimal.valueOf(cumRecharge[maxDays]);
        Integer subPeriodDays = stat.getSubPeriodDays() != null ? stat.getSubPeriodDays() : 1;
        int subUserCount = stat.getSubUserCount() != null ? stat.getSubUserCount() : 0;

        if (subUserCount <= 0 && actualRecharge.compareTo(BigDecimal.ZERO) <= 0) {
            for (int d = maxDays + 1; d <= 365; d++) cumRecharge[d] = cumRecharge[maxDays];
            return cumRecharge;
        }

        int effectiveSubUserCount = Math.max(1, subUserCount);
        if (isSubscriptionStagnant(stat, maxDays)) {
            for (int d = maxDays + 1; d <= 365; d++) cumRecharge[d] = cumRecharge[maxDays];
            return cumRecharge;
        }
        LocalDateTime launchTime = stat.getLaunchDate() != null ? stat.getLaunchDate().atStartOfDay() : LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        String dimType = "ALL";
        String dimValue = "DEFAULT";
        if (stat.getUserId() != null) {
            dimType = "USER";
            dimValue = String.valueOf(stat.getUserId());
        }

        Map<Integer, Integer> periodDistMap = parsePeriodDistribution(stat.getSubPeriodDistribution(), subPeriodDays, effectiveSubUserCount);
        List<PeriodContext> contexts = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : periodDistMap.entrySet()) {
            int period = entry.getKey();
            int count = entry.getValue();

            PeriodContext ctx = new PeriodContext();
            ctx.periodDays = period;
            ctx.userCount = count;

            if (versionRepository != null) {
                List<SubscriptionConfigVersion> matchedVersions = versionRepository.findMatchingPeriodVersions(period, launchTime);
                if (!matchedVersions.isEmpty()) {
                    SubscriptionConfigVersion ver = matchedVersions.get(0);
                    if (ver.getRenewPriceCent() != null && ver.getRenewPriceCent() > 0) {
                        ctx.configRenewUsd = ver.getRenewPriceCent() / 100.0;
                    }
                    if (ver.getFirstPriceCent() != null && ver.getFirstPriceCent() > 0) {
                        ctx.configFirstUsd = ver.getFirstPriceCent() / 100.0;
                    }
                }
            }

            List<LtvPredictBenchmark> benchmarkList = benchmarkService.getBenchmarkCurve(dimType, dimValue, period);
            if ((benchmarkList == null || benchmarkList.isEmpty()) && !"ALL".equals(dimType)) {
                benchmarkList = benchmarkService.getBenchmarkCurve("ALL", "DEFAULT", period);
            }

            if (benchmarkList != null && !benchmarkList.isEmpty()) {
                for (LtvPredictBenchmark b : benchmarkList) {
                    int idx = b.getDayIndex();
                    if (idx >= 1 && idx <= 365) {
                        ctx.baseRet[idx] = b.getBaseRetentionRate() != null ? b.getBaseRetentionRate().doubleValue() : 0.0;
                        ctx.baseArpu[idx] = b.getBaseArpu() != null ? b.getBaseArpu().doubleValue() : 0.0;
                    }
                }
            } else {
                // 标准合成基准线兜底 (Synthetic Standard Benchmark Fallback)
                double unitPrice = ctx.configRenewUsd != null ? ctx.configRenewUsd : (ctx.configFirstUsd != null ? ctx.configFirstUsd : 6.99);
                for (int d = 1; d <= 90; d++) {
                    if (period > 1) {
                        if ((d - 1) % period == 0) {
                            int cycleIndex = (d - 1) / period + 1;
                            ctx.baseRet[d] = Math.pow(0.55, cycleIndex - 1);
                        } else {
                            ctx.baseRet[d] = 0.0;
                        }
                    } else {
                        ctx.baseRet[d] = 1.0 / Math.pow(d, 0.75);
                    }
                    ctx.baseArpu[d] = unitPrice;
                }
            }

            int anchorDay = 90;
            if (period > 1) {
                while (anchorDay >= 1 && (anchorDay - 1) % period != 0) {
                    anchorDay--;
                }
            }
            if (anchorDay < 1) anchorDay = 1;

            double lastRet = ctx.baseRet[anchorDay];
            double lastArpu = ctx.baseArpu[anchorDay];

            for (int d = 91; d <= 365; d++) {
                if (ctx.baseRet[d] == 0.0) {
                    if (period > 1 && (d - 1) % period != 0) {
                        ctx.baseRet[d] = 0.0;
                    } else {
                        ctx.baseRet[d] = lastRet * Math.pow((double) anchorDay / d, 1.2);
                    }
                    ctx.baseArpu[d] = lastArpu;
                }
            }
            contexts.add(ctx);
        }

        boolean hasValidBenchmark = false;
        for (PeriodContext ctx : contexts) {
            for (int d = 1; d <= 365; d++) {
                if (ctx.baseRet[d] > 0) {
                    hasValidBenchmark = true;
                    break;
                }
            }
            if (hasValidBenchmark) break;
        }

        if (!hasValidBenchmark) {
            for (int d = maxDays + 1; d <= 365; d++) {
                cumRecharge[d] = actualRecharge.doubleValue();
            }
            return cumRecharge;
        }

        double actualRoi = actualRecharge.divide(spend, 4, RoundingMode.HALF_UP).doubleValue();
        double baseRoiSum = 0;
        for (int d = 1; d <= maxDays; d++) {
            for (PeriodContext ctx : contexts) {
                if (ctx.periodDays == 1 || (d - 1) % ctx.periodDays == 0) {
                    double unitPrice = ctx.baseArpu[d];
                    if (d == 1 && ctx.configFirstUsd != null) {
                        unitPrice = ctx.configFirstUsd;
                    } else if (d > 1 && ctx.configRenewUsd != null) {
                        unitPrice = ctx.configRenewUsd;
                    }
                    baseRoiSum += (ctx.baseRet[d] * unitPrice * ctx.userCount) / spend.doubleValue();
                }
            }
        }
        baseRoiSum = Math.max(0.0001, baseRoiSum);
        double scaleFactor = computeOptimalScaleFactor(actualRoi, baseRoiSum, maxDays, spend);

        double currentCumRecharge = actualRecharge.doubleValue();
        for (int t = maxDays + 1; t <= 365; t++) {
            // 1. 均值回归机制：放缩因子向 1.0 平滑回归 (使用常量 SCALE_DECAY_EXPONENT = 0.35)
            double scaleDecay = Math.pow((double) maxDays / t, PredictAlgorithmConstants.SCALE_DECAY_EXPONENT);
            double effectiveScaleFactor = 1.0 + (scaleFactor - 1.0) * scaleDecay;

            // 2. 周期续订自然衰减校准 (使用常量 CYCLE_DECAY_EXPONENT = 0.06)
            double cycleDecay = (t > 7) ? Math.pow(7.0 / t, PredictAlgorithmConstants.CYCLE_DECAY_EXPONENT) : 1.0;

            double dailyDeltaIncome = 0;
            for (PeriodContext ctx : contexts) {
                if (ctx.periodDays == 1 || (t - 1) % ctx.periodDays == 0) {
                    double predRet = ctx.baseRet[t] * effectiveScaleFactor * cycleDecay;
                    double predArpu = (ctx.configRenewUsd != null) ? ctx.configRenewUsd : ctx.baseArpu[t];
                    dailyDeltaIncome += predRet * predArpu * ctx.userCount;
                }
            }
            currentCumRecharge += dailyDeltaIncome;
            cumRecharge[t] = currentCumRecharge;
        }

        return cumRecharge;
    }

    /**
     * 委托给 CohortCurveExtrapolator 算法算子
     */
    public PredictionResult predictCohortOlsFallback(LtvDailyStat stat, int daysElapsed) {
        return extrapolator.predictCohortOlsFallback(stat, daysElapsed);
    }

    public static BigDecimal getRechargeForDay(LtvDailyStat stat, int day) {
        return CohortStatHelper.getRechargeForDay(stat, day);
    }

    public static BigDecimal getRoiForDay(LtvDailyStat stat, int day) {
        return CohortStatHelper.getRoiForDay(stat, day);
    }

    public static double computeOptimalScaleFactor(double actualRoi, double baseRoiSum, int maxDays) {
        return CohortCurveExtrapolator.computeOptimalScaleFactor(actualRoi, baseRoiSum, maxDays, null);
    }

    public static double computeOptimalScaleFactor(double actualRoi, double baseRoiSum, int maxDays, BigDecimal spend) {
        return CohortCurveExtrapolator.computeOptimalScaleFactor(actualRoi, baseRoiSum, maxDays, spend);
    }

    public static int getMaxPeriodInDistribution(LtvDailyStat stat) {
        return CohortStatHelper.getMaxPeriodInDistribution(stat);
    }

    public static boolean isSubscriptionStagnant(LtvDailyStat stat, int maxDays) {
        return CohortStatHelper.isSubscriptionStagnant(stat, maxDays);
    }

    /**
     * 解析 Cohort 订阅分布 JSON 字符串，例如 "{"1":3,"7":12}" -> Map<Period, Count>
     */
    private Map<Integer, Integer> parsePeriodDistribution(String json, int fallbackPeriod, int fallbackCount) {
        Map<Integer, Integer> map = new LinkedHashMap<>();
        if (json != null && !json.trim().isEmpty()) {
            try {
                String clean = json.trim().replaceAll("[{}\"]", "");
                if (!clean.isEmpty()) {
                    String[] pairs = clean.split(",");
                    for (String p : pairs) {
                        String[] kv = p.split(":");
                        if (kv.length == 2) {
                            int period = Integer.parseInt(kv[0].trim());
                            int count = Integer.parseInt(kv[1].trim());
                            if (count > 0) {
                                map.put(Math.max(1, period), count);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        if (map.isEmpty()) {
            map.put(Math.max(1, fallbackPeriod), Math.max(1, fallbackCount));
        } else {
            int sumCount = map.values().stream().mapToInt(Integer::intValue).sum();
            if (sumCount < fallbackCount) {
                int diff = fallbackCount - sumCount;
                map.merge(Math.max(1, fallbackPeriod), diff, Integer::sum);
            }
        }
        return map;
    }
}
