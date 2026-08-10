package com.ltv.stat.service.engine;

import com.ltv.stat.entity.LtvDailyStat;
import com.ltv.stat.util.CohortStatHelper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 专职负责回本天数 (Payback Days) 预测与判定独立引擎
 */
@Component
public class PaybackPredictEngine {

    /**
     * 计算单 Cohort 的回本天数与状态
     * @return null (不可预测/首3天内), -1 (停滞), 366 (超出365天), 1~365 (已回本或预测回本天数)
     */
    public Integer calculateCohortPaybackDays(LtvDailyStat stat, int daysElapsed, double[] cumRechargeCurve) {
        if (stat == null || stat.getSpend() == null || stat.getSpend().compareTo(BigDecimal.ZERO) <= 0 || cumRechargeCurve == null || cumRechargeCurve.length < 366) {
            return null;
        }

        int maxDays = Math.min(daysElapsed, 60);
        double actualRechargeVal = getRechargeForDay(stat, maxDays).doubleValue();

        // 1. 判断已知历史数据是否已实现回本 (ROI >= 1.0)
        for (int d = 1; d <= maxDays; d++) {
            BigDecimal r = getRoiForDay(stat, d);
            if (r != null && r.compareTo(BigDecimal.ONE) >= 0) {
                return d;
            }
        }

        // 2. 观察窗口不足 3 天，不具备预测条件
        if (maxDays < 3) {
            return null;
        }

        // 3. 无订阅用户且充值为 0，判为停滞
        int subUserCount = stat.getSubUserCount() != null ? stat.getSubUserCount() : 0;
        if (subUserCount <= 0 && actualRechargeVal <= 0) {
            return -1;
        }

        // 3.5 通用订阅周期平盘停滞判定：日订连续 6 天、周订连续 14 天 (2 周)、月订连续 60 天 (2 月) 无充值增长，判为回本停滞 (-1)
        if (CohortStatHelper.isSubscriptionStagnant(stat, maxDays)) {
            return -1;
        }

        // 4. 从未来天数检索交叉回本点
        double spendGoal = stat.getSpend().doubleValue();
        for (int t = maxDays + 1; t <= 365; t++) {
            if (cumRechargeCurve[t] >= spendGoal - 0.01) {
                return t;
            }
        }

        // 5. 若 365 天内未能回本，判断是否充值仍在微幅增加
        if (cumRechargeCurve[365] > actualRechargeVal + 0.01) {
            return 366; // >365 天回本
        } else {
            return -1;  // 回本停滞
        }
    }

    /**
     * 计算大盘整体 (Overall Cohort) 预测回本天数 (按自然日历真实 LaunchDate 动态交叠求和)
     */
    public Integer calculateOverallPaybackDays(BigDecimal totalSpendAll, BigDecimal totalRechargeAll, List<LtvDailyStat> validStats,
                                               Map<LtvDailyStat, double[]> cohortCurves, LocalDate minLaunchDate, LocalDate today) {
        if (totalSpendAll == null || totalSpendAll.compareTo(BigDecimal.ZERO) <= 0 || cohortCurves == null || cohortCurves.isEmpty()) {
            return null;
        }

        double spendGoal = totalSpendAll.doubleValue();

        // 若当前大盘已知充值已实现回本
        if (totalRechargeAll != null && totalRechargeAll.compareTo(totalSpendAll) >= 0) {
            return 0;
        }

        double lastOverallRecharge = 0.0;
        for (int g = 1; g <= 365; g++) {
            LocalDate currentDate = minLaunchDate.plusDays(g - 1);
            double overallCumRechargeAtDate = 0.0;

            for (LtvDailyStat s : validStats) {
                LocalDate lDate = s.getLaunchDate() != null ? s.getLaunchDate() : minLaunchDate;
                if (!currentDate.isBefore(lDate)) {
                    int cohortDay = (int) java.time.temporal.ChronoUnit.DAYS.between(lDate, currentDate) + 1;
                    cohortDay = Math.min(cohortDay, 365);

                    double[] curve = cohortCurves.get(s);
                    if (curve != null && cohortDay >= 1 && cohortDay < curve.length) {
                        overallCumRechargeAtDate += curve[cohortDay];
                    }
                }
            }

            if (overallCumRechargeAtDate >= spendGoal - 0.01) {
                if (!currentDate.isAfter(today)) {
                    return 0; // 历史已回本
                }
                int remainingDays = (int) java.time.temporal.ChronoUnit.DAYS.between(today, currentDate);
                return Math.max(1, remainingDays);
            }

            lastOverallRecharge = overallCumRechargeAtDate;
        }

        double actualRechargeVal = totalRechargeAll != null ? totalRechargeAll.doubleValue() : 0.0;
        if (lastOverallRecharge > actualRechargeVal + 0.01) {
            return 366; // >365 天回本
        } else {
            return -1;  // 回本停滞
        }
    }

    private BigDecimal getRechargeForDay(LtvDailyStat stat, int day) {
        return CohortStatHelper.getRechargeForDay(stat, day);
    }

    private BigDecimal getRoiForDay(LtvDailyStat stat, int day) {
        return CohortStatHelper.getRoiForDay(stat, day);
    }
}
