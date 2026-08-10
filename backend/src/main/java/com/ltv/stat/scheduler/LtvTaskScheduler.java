package com.ltv.stat.scheduler;

import com.ltv.stat.service.DailyRechargeStatService;
import com.ltv.stat.service.LtvStatService;
import com.ltv.stat.service.OrderSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@EnableScheduling
public class LtvTaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(LtvTaskScheduler.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final OrderSyncService orderSyncService;
    private final LtvStatService ltvStatService;
    private final DailyRechargeStatService dailyRechargeStatService;

    public LtvTaskScheduler(OrderSyncService orderSyncService,
                            LtvStatService ltvStatService,
                            DailyRechargeStatService dailyRechargeStatService) {
        this.orderSyncService = orderSyncService;
        this.ltvStatService = ltvStatService;
        this.dailyRechargeStatService = dailyRechargeStatService;
    }

    /**
     * 北京时间每小时 05 分 (例如 00:05, 01:05, ..., 23:05): 定时拉取过去 3 天的全量增量订单
     */
    @Scheduled(cron = "0 5 * * * ?", zone = "Asia/Shanghai")
    public void scheduledOrderFetch() {
        log.info("Starting scheduled order fetch at xx:05 BJ Time (past 3 days)");
        LocalDate todayBj = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        LocalDate startBj = todayBj.minusDays(3);

        String startTimeStr = startBj.format(DATE_FORMATTER);
        String endTimeStr = todayBj.format(DATE_FORMATTER);

        try {
            orderSyncService.syncOrdersAll(startTimeStr, endTimeStr);
        } catch (Exception e) {
            log.error("Scheduled ALL order fetch failed", e);
        }
        log.info("Scheduled order fetch finished.");
    }

    /**
     * 北京时间每 6 小时 (00:30, 06:30, 12:30, 18:30): 定时全量统计 LTV 数据
     */
    @Scheduled(cron = "0 30 0,6,12,18 * * ?", zone = "Asia/Shanghai")
    public void scheduledLtvCalculation() {
        log.info("Starting scheduled LTV calculation at 00:30/06:30/12:30/18:30 BJ Time");
        try {
            ltvStatService.calculateAllLtvStats();
        } catch (Exception e) {
            log.error("Scheduled LTV calculation failed", e);
        }
        log.info("Scheduled LTV calculation finished.");
    }

    /**
     * 北京时间每小时 30 分 (例如 00:30, 01:30, ..., 23:30): 定时统计【每日充值分布】数据并落库
     */
    @Scheduled(cron = "0 30 * * * ?", zone = "Asia/Shanghai")
    public void scheduledDailyDistributionCalculation() {
        log.info("Starting hourly scheduled daily distribution calculation at xx:30 BJ Time");
        try {
            dailyRechargeStatService.calculateAllDailyDistributionStats();
        } catch (Exception e) {
            log.error("Scheduled daily distribution calculation failed", e);
        }
        log.info("Scheduled daily distribution calculation finished.");
    }
}
