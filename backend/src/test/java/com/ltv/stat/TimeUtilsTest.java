package com.ltv.stat;

import com.ltv.stat.util.TimeUtils;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TimeUtilsTest {

    @Test
    public void testBeijingToEasternConversion() {
        // 北京时间 2026-07-28 23:11:32
        String bjTime = "2026-07-28 23:11:32";
        ZonedDateTime etZdt = TimeUtils.parseBjToEt(bjTime);
        assertNotNull(etZdt);

        // 美东时间 2026-07-28 11:11:32 (夏令时 UTC-4)
        assertEquals(2026, etZdt.getYear());
        assertEquals(7, etZdt.getMonthValue());
        assertEquals(28, etZdt.getDayOfMonth());
        assertEquals(11, etZdt.getHour());
        assertEquals(11, etZdt.getMinute());

        LocalDate etDate = TimeUtils.parseBjToEtDate(bjTime);
        assertEquals(LocalDate.of(2026, 7, 28), etDate);
    }

    @Test
    public void testBeijingToUtcConversion() {
        // 北京时间 2026-07-28 02:30:00 (UTC+8) -> UTC 2026-07-27 18:30:00 (跨天)
        String bjTime = "2026-07-28 02:30:00";
        java.time.LocalDateTime utcLdt = TimeUtils.parseBjToUtc(bjTime);
        assertNotNull(utcLdt);
        assertEquals(2026, utcLdt.getYear());
        assertEquals(7, utcLdt.getMonthValue());
        assertEquals(27, utcLdt.getDayOfMonth());
        assertEquals(18, utcLdt.getHour());
        assertEquals(30, utcLdt.getMinute());

        assertEquals(TimeUtils.BEIJING_ZONE, TimeUtils.CST_ZONE);
    }
}
