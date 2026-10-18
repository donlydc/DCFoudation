package com.dc.android.foundation.runtime.scheduling

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/** DST-safe calculations for wall-clock schedules. */
fun nextDaily(after: Instant, time: LocalTime, zone: ZoneId): Instant {
    val currentDate = after.atZone(zone).toLocalDate()
    val candidate = occurrenceAt(currentDate, time, zone)
    return if (candidate.toInstant() > after) candidate.toInstant()
    else occurrenceAt(currentDate.plusDays(1), time, zone).toInstant()
}

/** DST-safe calculation for the next occurrence on one of [days]. */
fun nextWeekly(
    after: Instant,
    time: LocalTime,
    days: Set<DayOfWeek>,
    zone: ZoneId,
): Instant {
    require(days.isNotEmpty()) { "A weekly schedule needs at least one day." }
    val start = after.atZone(zone).toLocalDate()
    for (offset in 0L..7L) {
        val date = start.plusDays(offset)
        if (date.dayOfWeek !in days) continue
        val candidate = occurrenceAt(date, time, zone).toInstant()
        if (candidate > after) return candidate
    }
    error("Unable to find the next weekly occurrence.")
}

/**
 * `ofLocal(..., null)` chooses the earlier offset in an overlap and moves a gap forward to the
 * first valid local time. Those are the explicit scheduling policies for autumn and spring DST.
 */
private fun occurrenceAt(date: LocalDate, time: LocalTime, zone: ZoneId): ZonedDateTime =
    ZonedDateTime.ofLocal(LocalDateTime.of(date, time), zone, null)
