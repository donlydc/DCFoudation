package com.dc.android.foundation.runtime.scheduling

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class NextOccurrenceTest {
    private val newYork = ZoneId.of("America/New_York")

    @Test
    fun `daily schedule uses the same day when its time is still ahead`() {
        val next = nextDaily(
            after = Instant.parse("2026-01-10T10:00:00Z"),
            time = LocalTime.of(7, 0),
            zone = newYork,
        )

        assertEquals(Instant.parse("2026-01-10T12:00:00Z"), next)
    }

    @Test
    fun `daily schedule advances across midnight`() {
        val next = nextDaily(
            after = Instant.parse("2026-01-10T13:00:00Z"),
            time = LocalTime.of(7, 0),
            zone = newYork,
        )

        assertEquals(Instant.parse("2026-01-11T12:00:00Z"), next)
    }

    @Test
    fun `spring DST gap moves a nonexistent wall time forward`() {
        val next = nextDaily(
            after = Instant.parse("2026-03-08T05:00:00Z"),
            time = LocalTime.of(2, 30),
            zone = newYork,
        )

        assertEquals(Instant.parse("2026-03-08T07:30:00Z"), next)
    }

    @Test
    fun `autumn DST overlap chooses the earlier offset`() {
        val next = nextDaily(
            after = Instant.parse("2026-11-01T04:00:00Z"),
            time = LocalTime.of(1, 30),
            zone = newYork,
        )

        assertEquals(Instant.parse("2026-11-01T05:30:00Z"), next)
    }

    @Test
    fun `weekly schedule selects the next requested day`() {
        val next = nextWeekly(
            after = Instant.parse("2026-01-11T13:00:00Z"), // Sunday 08:00 in New York
            time = LocalTime.of(7, 0),
            days = setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY),
            zone = newYork,
        )

        assertEquals(Instant.parse("2026-01-12T12:00:00Z"), next)
    }

    @Test
    fun `same wall clock time resolves to a different instant after zone change`() {
        val after = Instant.parse("2026-01-10T00:00:00Z")

        assertEquals(
            Instant.parse("2026-01-10T07:00:00Z"),
            nextDaily(after, LocalTime.of(7, 0), ZoneId.of("UTC")),
        )
        assertEquals(
            Instant.parse("2026-01-11T00:00:00Z"),
            nextDaily(after, LocalTime.of(7, 0), ZoneId.of("Asia/Bangkok")),
        )
    }
}
