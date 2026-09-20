package com.ltv.stat.adapter;

import com.ltv.stat.entity.PlatformConfig;
import com.ltv.stat.enums.PlatformEnum;
import com.ltv.stat.service.flicknovel.FlicknovelApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 番茄司南 (FlickNovel) 适配器实现
 */
@Component
public class FlicknovelPlatformAdapter implements PlatformSyncAdapter {

    private static final Logger log = LoggerFactory.getLogger(FlicknovelPlatformAdapter.class);

    private final FlicknovelApiService flicknovelApiService;

    public FlicknovelPlatformAdapter(@Lazy FlicknovelApiService flicknovelApiService) {
        this.flicknovelApiService = flicknovelApiService;
    }

    @Override
    public PlatformEnum getPlatform() {
        return PlatformEnum.FLICKNOVEL;
    }

    @Override
    public int syncOrders(LocalDate startDate, LocalDate endDate, PlatformConfig config) {
        log.info("[FlicknovelAdapter] Syncing orders from {} to {}", startDate, endDate);
        return flicknovelApiService.syncOrders(startDate, endDate, config);
    }

    @Override
    public int syncOrdersForLandingPage(String landingPageId, PlatformConfig config) {
        log.info("[FlicknovelAdapter] Syncing orders for channel: {}", landingPageId);
        // 番茄司南按时间区间全量拉取后按落地页关联归集
        return flicknovelApiService.syncOrders(null, null, config);
    }

    @Override
    public int syncConfigs(PlatformConfig config) {
        log.info("[FlicknovelAdapter] Syncing configs for Flicknovel");
        return flicknovelApiService.syncPromotionsAndConfigs(null, null);
    }
}
