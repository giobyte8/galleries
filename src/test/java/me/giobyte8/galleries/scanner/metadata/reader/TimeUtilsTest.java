package me.giobyte8.galleries.scanner.metadata.reader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimeUtilsTest {

    @Test
    void getTimeZoneFallback() {
        var datetimeRaw = "2025:11:06 13:44:30";
        var tz = TimeUtils.getTimeZone(datetimeRaw);

        assertEquals("UTC", tz.getID());
    }

    @Test
    void getTimeZoneThai() {
        var datetimeRaw = "2025:11:06 13:44:30+07:00";
        var tz = TimeUtils.getTimeZone(datetimeRaw);

        assertEquals("GMT+07:00", tz.getID());
    }

    @Test
    void getTimeZoneYvr() {
        var datetimeRaw = "2025:11:06 13:44:30-07:00";
        var tz = TimeUtils.getTimeZone(datetimeRaw);

        assertEquals("GMT-07:00", tz.getID());
    }

    @Test
    void getTimeZoneCst() {
        var datetimeRaw = "2025:11:06 13:44:30-06:00";
        var tz = TimeUtils.getTimeZone(datetimeRaw);

        assertEquals("GMT-06:00", tz.getID());
    }

    @Test
    void containsTimezoneNoTz() {
        var datetimeRaw = "2025:11:08 12:15:12p.m.";
        var containsTz = TimeUtils.containsTz(datetimeRaw);

        assertFalse(containsTz, "Timezone is not expected");
    }

    @Test
    void containsTimezone() {
        var datetimeRaw = "2025:11:08 12:15:12p.m.+07:00";
        var containsTz = TimeUtils.containsTz(datetimeRaw);

        assertTrue(containsTz, "Timezone should have been detected");
    }
}