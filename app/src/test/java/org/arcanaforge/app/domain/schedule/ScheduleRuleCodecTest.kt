package org.arcanaforge.app.domain.schedule

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleRuleCodecTest {
    @Test
    fun parsesInvalidReminderTimeAsNineAm() {
        assertEquals(LocalTime.of(9, 0), ScheduleRuleCodec.parseReminderTime("bad"))
    }

    @Test
    fun dailyDelayUsesTodayWhenReminderIsStillAhead() {
        val delay = ScheduleRuleCodec.nextDelayMillis(
            rule = ScheduleRuleCodec.Daily,
            reminderTime = "09:30",
            now = LocalDateTime.of(2026, 5, 24, 9, 0),
            zoneId = ZoneId.of("UTC"),
        )

        assertEquals(30 * 60 * 1000L, delay)
    }

    @Test
    fun weeklyDelayMovesToNextWeekWhenReminderPassed() {
        val delay = ScheduleRuleCodec.nextDelayMillis(
            rule = ScheduleRuleCodec.Weekly,
            reminderTime = "09:00",
            now = LocalDateTime.of(2026, 5, 24, 10, 0),
            zoneId = ZoneId.of("UTC"),
        )

        assertTrue(delay > 6L * 24L * 60L * 60L * 1000L)
    }
}
