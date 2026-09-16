package com.ltv.stat.adapter;

import com.ltv.stat.enums.PlatformEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 平台适配器注册中心
 * 采用 EnumMap<PlatformEnum, PlatformSyncAdapter> 实现高性能且类型安全的 O(1) 路由分发
 */
@Component
public class PlatformAdapterRegistry {

    private static final Logger log = LoggerFactory.getLogger(PlatformAdapterRegistry.class);

    private final Map<PlatformEnum, PlatformSyncAdapter> adapterMap = new EnumMap<>(PlatformEnum.class);

    public PlatformAdapterRegistry(List<PlatformSyncAdapter> adapters) {
        if (adapters != null) {
            for (PlatformSyncAdapter adapter : adapters) {
                if (adapter.getPlatform() != null) {
                    adapterMap.put(adapter.getPlatform(), adapter);
                    log.info("Registered PlatformSyncAdapter: {} -> {}", adapter.getPlatform(), adapter.getClass().getSimpleName());
                }
            }
        }
    }

    /**
     * 根据枚举获取对应的平台适配器
     */
    public Optional<PlatformSyncAdapter> getAdapter(PlatformEnum platform) {
        if (platform == null) return Optional.empty();
        return Optional.ofNullable(adapterMap.get(platform));
    }

    /**
     * 根据字符串代码安全获取适配器
     */
    public Optional<PlatformSyncAdapter> getAdapterByCode(String code) {
        return PlatformEnum.fromCode(code).flatMap(this::getAdapter);
    }

    /**
     * 获取所有已注册的适配器
     */
    public Collection<PlatformSyncAdapter> getAllAdapters() {
        return Collections.unmodifiableCollection(adapterMap.values());
    }
}
