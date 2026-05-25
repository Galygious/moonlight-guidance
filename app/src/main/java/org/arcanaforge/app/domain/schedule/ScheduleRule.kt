package org.arcanaforge.app.domain.schedule

import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object ScheduleRuleCodec {
    const val Daily = "daily"
    const val Weekly = "weekly"
    const val Monthly = "monthly"

    fun label(rule: String): String =
        when (rule) {
            Daily -> "Daily"
            Weekly -> "Weekly"
            Monthly -> "Monthly"
            else -> "Daily"
        }

    fun nextDelayMillis(
        rule: String,
        reminderTime: String,
        now: LocalDateTime = LocalDateTime.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Long {
        val time = parseReminderTime(reminderTime)
        var next = now.toLocalDate().atTime(time)
        if (!next.isAfter(now)) {
            next = when (rule) {
                Weekly -> next.plusWeeks(1)
                Monthly -> next.plusMonths(1)
                else -> next.plusDays(1)
            }
        }
        return Duration.between(
            now.atZone(zoneId).toInstant(),
            next.atZone(zoneId).toInstant(),
        ).toMillis().coerceAtLeast(1_000L)
    }

    fun parseReminderTime(value: String): LocalTime =
        runCatching { LocalTime.parse(value) }.getOrDefault(LocalTime.of(9, 0))
}
