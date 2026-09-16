package com.ltv.stat.adapter;

import com.ltv.stat.entity.PlatformConfig;
import com.ltv.stat.enums.PlatformEnum;
import com.ltv.stat.service.OrderSyncService;
import com.ltv.stat.service.SubscribeConfigSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ROCNOVEL (中文在线 / Florastory) 专属适配器实现
 */
@Component
public class RocnovelPlatformAdapter implements PlatformSyncAdapter {

    private static final Logger log = LoggerFactory.getLogger(RocnovelPlatformAdapter.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final OrderSyncService orderSyncService;
    private final SubscribeConfigSyncService subscribeConfigSyncService;

    public RocnovelPlatformAdapter(@Lazy OrderSyncService orderSyncService,
                                   @Lazy SubscribeConfigSyncService subscribeConfigSyncService) {
        this.orderSyncService = orderSyncService;
        this.subscribeConfigSyncService = subscribeConfigSyncService;
    }

    @Override
    public PlatformEnum getPlatform() {
        return PlatformEnum.ROCNOVEL;
    }

    @Override
    public int syncOrders(LocalDate startDate, LocalDate endDate, PlatformConfig config) {
        String startStr = startDate != null ? startDate.format(DATE_FORMATTER) : "2026-07-10";
        String endStr = endDate != null ? endDate.format(DATE_FORMATTER) : LocalDate.now().format(DATE_FORMATTER);
        log.info("[RocnovelPlatformAdapter] Triggering syncOrders from {} to {}", startStr, endStr);
        return orderSyncService.syncOrdersAll(startStr, endStr);
    }

    @Override
    public int syncOrdersForLandingPage(String landingPageId, PlatformConfig config) {
        log.info("[RocnovelPlatformAdapter] Triggering syncOrdersForLandingPage: {}", landingPageId);
        return orderSyncService.syncOrdersForLandingPage(landingPageId);
    }

    @Override
    public int syncConfigs(PlatformConfig config) {
        log.info("[RocnovelPlatformAdapter] Triggering syncAllSubscribeConfigs...");
        return subscribeConfigSyncService.syncAllSubscribeConfigs();
    }
}
