package com.ltv.stat.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    public static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");
    public static final ZoneId EASTERN_ZONE = ZoneId.of("America/New_York");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 将北京时间字符串 ("yyyy-MM-dd HH:mm:ss") 转换为美东 ZonedDateTime
     */
    public static ZonedDateTime parseBjToEt(String bjTimeStr) {
        if (bjTimeStr == null || bjTimeStr.trim().isEmpty()) {
            return null;
        }
        LocalDateTime ldt = LocalDateTime.parse(bjTimeStr.trim(), DATETIME_FORMATTER);
        ZonedDateTime bjZdt = ldt.atZone(BEIJING_ZONE);
        return bjZdt.withZoneSameInstant(EASTERN_ZONE);
    }

    /**
     * 提取美东时间的 LocalDate (yyyy-MM-dd)
     */
    public static LocalDate parseBjToEtDate(String bjTimeStr) {
        ZonedDateTime etZdt = parseBjToEt(bjTimeStr);
        return etZdt != null ? etZdt.toLocalDate() : null;
    }

    /**
     * 将 LocalDateTime (假设是北京时间) 转换为美东 LocalDateTime
     */
    public static LocalDateTime convertBjToEt(LocalDateTime bjLdt) {
        if (bjLdt == null) return null;
        return bjLdt.atZone(BEIJING_ZONE).withZoneSameInstant(EASTERN_ZONE).toLocalDateTime();
    }

    public static LocalDate getTodayEt() {
        return LocalDate.now(EASTERN_ZONE);
    }
}
