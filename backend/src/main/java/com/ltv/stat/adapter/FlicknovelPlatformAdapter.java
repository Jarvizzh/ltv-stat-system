package com.ltv.stat.adapter;

import com.ltv.stat.entity.PlatformConfig;
import com.ltv.stat.enums.PlatformEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 番茄海外 (FlickNovel) 适配器实现 (预留扩展骨架)
 */
@Component
public class FlicknovelPlatformAdapter implements PlatformSyncAdapter {

    private static final Logger log = LoggerFactory.getLogger(FlicknovelPlatformAdapter.class);

    @Override
    public PlatformEnum getPlatform() {
        return PlatformEnum.FLICKNOVEL;
    }

    @Override
    public int syncOrders(LocalDate startDate, LocalDate endDate, PlatformConfig config) {
        log.info("[FlicknovelAdapter] Syncing orders from {} to {} (Flicknovel API stub)", startDate, endDate);
        // 此处待番茄海外实际 OpenAPI 联调参数就绪后填充 HTTP 请求与解析逻辑
        return 0;
    }

    @Override
    public int syncOrdersForLandingPage(String landingPageId, PlatformConfig config) {
        log.info("[FlicknovelAdapter] Syncing orders for channel: {} (Flicknovel API stub)", landingPageId);
        return 0;
    }

    @Override
    public int syncConfigs(PlatformConfig config) {
        log.info("[FlicknovelAdapter] Syncing configs for Flicknovel (stub)");
        return 0;
    }
}
