package com.ltv.stat.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 平台枚举定义 (强类型)
 * 严禁在业务逻辑与数据接入层使用魔术字符串
 */
public enum PlatformEnum {

    ALL("ALL", "全平台综合大盘", true),
    ROCNOVEL("rocnovel", "中文在线", true),
    FLICKNOVEL("flicknovel", "番茄海外", true);

    private final String code;
    private final String displayName;
    private final boolean enabled;

    PlatformEnum(String code, String displayName, boolean enabled) {
        this.code = code;
        this.displayName = displayName;
        this.enabled = enabled;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAll() {
        return this == ALL;
    }

    /**
     * 根据 code 安全解析枚举，若为空默认返回 ALL，若无法识别返回 empty
     */
    public static Optional<PlatformEnum> fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.of(ALL);
        }
        for (PlatformEnum p : values()) {
            if (p.code.equalsIgnoreCase(code.trim())) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    /**
     * 获取所有可独立接入业务的实际平台（排除 ALL 综合大盘）
     */
    public static List<PlatformEnum> getActualPlatforms() {
        return Arrays.stream(values())
                .filter(p -> p != ALL && p.isEnabled())
                .collect(Collectors.toList());
    }
}
