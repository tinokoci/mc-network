package net.exemine.api.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static net.exemine.api.util.TimeUtil.isDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeUtilTest {

    @Test
    void getDateWithMillis() {
        long givenValue = 100000000000L;
        String expectedValue = "03/03/1973 09:46 UTC";
        assertEquals(expectedValue, TimeUtil.getDate(givenValue));
    }

    @Test
    void getDateWithLocalDateTime() {
        LocalDateTime givenValue = LocalDateTime.of(2022, 10, 10, 10, 10, 10);
        String expectedValue = "10/10/2022 10:10 UTC";
        assertEquals(expectedValue, TimeUtil.getDate(givenValue));
    }

    void getFullDuration() {
        System.out.println(TimeUtil.getFullDuration(TimeUtil.DAY * 2 + TimeUtil.HOUR * 10 + TimeUtil.MINUTE * 8));
    }

    @Test
    void getDuration() {
    }

    @Test
    void getShortDuration() {
    }

    @Test
    void getCharDurationFromMillis() {
    }

    @Test
    void getMillisFromInput() {
    }

    @Test
    void formatPlaytime() {
        System.out.println(TimeUtil.getMillisFromInput(""));
        System.out.println(TimeUtil.getMillisFromInput("kurac"));
        System.out.println(TimeUtil.getMillisFromInput("kurac1d"));
        System.out.println(TimeUtil.getMillisFromInput("perm"));
        System.out.println(TimeUtil.getMillisFromInput("3h"));
        System.out.println(TimeUtil.getMillisFromInput("1d2h5m20s"));
        System.out.println(TimeUtil.getMillisFromInput("a1d2h5m20s"));
        System.out.println(TimeUtil.getMillisFromInput("1dla2h5m20s"));
        System.out.println(TimeUtil.getMillisFromInput("1d2h5m20s22"));
        System.out.println(TimeUtil.getMillisFromInput("1d2h5m20s2p"));
    }

    @Test
    void testIsDate() {
        assertTrue(isDate("01/01/2023 10:00"));
        assertFalse(isDate("01/25/2023 10:00"));
        assertFalse(isDate("01/01/202 8:00"));
        assertTrue(isDate("21/12/2023 17:35"));
    }

    @Test
    void test() {
    }

    @Test
    void formatTimeClock() {
        System.out.println(TimeUtil.getClockTime(1000L));
        System.out.println(TimeUtil.getClockTime(2000L));
        System.out.println(TimeUtil.getClockTime(2));
    }
}