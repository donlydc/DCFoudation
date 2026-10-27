package com.dc.android.foundation.runtime.scheduling

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * MP:SCH-02. Complements [NextOccurrenceTest] with boundary, DST-transition, offset and
 * clock-shift cases. New York 2026: DST starts Sun 8 Mar (02:00 EST = 07:00Z) and ends Sun 1 Nov
 * (02:00 EDT = 06:00Z).
 */
class NextOccurrenceContractTest {
    private val newYork = ZoneId.of("America/New_York")
    private val utc = ZoneOffset.UTC

    @Test(timeout = 20_000)
    fun `daily occurrence equal to the reference instant is strictly in the past so the next day wins`() {
        val next = nextDaily(Instant.parse("2026-01-10T12:00:00Z"), LocalTime.of(7, 0), newYork)

        assertEquals(Instant.parse("2026-01-11T12:00:00Z"), next)
    }

    @Test(timeout = 20_000)
    fun `daily schedule keeps the wall time across the spring forward transition`() {
        val next = nextDaily(Instant.parse("2026-03-07T13:00:00Z"), LocalTime.of(7, 0), newYork)

        assertEquals(Instant.parse("2026-03-08T11:00:00Z"), next) // 07:00 EDT
    }

    @Test(timeout = 20_000)
    fun `daily schedule keeps the wall time across the fall back transition`() {
        val next = nextDaily(Instant.parse("2026-10-31T12:00:00Z"), LocalTime.of(7, 0), newYork)

        assertEquals(Instant.parse("2026-11-01T12:00:00Z"), next) // 07:00 EST
    }

    @Test(timeout = 20_000)
    fun `second pass through an autumn overlap does not fire twice`() {
        // 05:30Z is the first (EDT) 01:30; the repeated 01:30 EST (06:30Z) must not be used today.
        val next = nextDaily(Instant.parse("2026-11-01T05:30:00Z"), LocalTime.of(1, 30), newYork)

        assertEquals(Instant.parse("2026-11-02T06:30:00Z"), next) // 01:30 EST the next day
    }

    @Test(timeout = 20_000)
    fun `just before the overlap occurrence still returns it`() {
        val next = nextDaily(Instant.parse("2026-11-01T05:29:59Z"), LocalTime.of(1, 30), newYork)

        assertEquals(Instant.parse("2026-11-01T05:30:00Z"), next)
    }

    @Test(timeout = 20_000)
    fun `day after a spring gap returns to the real wall time`() {
        val next = nextDaily(Instant.parse("2026-03-08T07:30:00Z"), LocalTime.of(2, 30), newYork)

        assertEquals(Instant.parse("2026-03-09T06:30:00Z"), next) // 02:30 EDT
    }

    @Test(timeout = 20_000)
    fun `half hour offset zone resolves to the correct instant`() {
        val kolkata = ZoneId.of("Asia/Kolkata")

        val next = nextDaily(Instant.parse("2026-01-10T00:00:00Z"), LocalTime.of(6, 0), kolkata)

        assertEquals(Instant.parse("2026-01-10T00:30:00Z"), next)
    }

    @Test(timeout = 20_000)
    fun `fixed offset zone at the reference instant advances one day`() {
        val next = nextDaily(Instant.parse("2026-01-10T00:00:00Z"), LocalTime.of(7, 0), ZoneOffset.ofHours(7))

        assertEquals(Instant.parse("2026-01-11T00:00:00Z"), next)
    }

    @Test(timeout = 20_000)
    fun `leap day and year rollover are handled`() {
        assertEquals(
            Instant.parse("2028-02-29T12:00:00Z"),
            nextDaily(Instant.parse("2028-02-28T23:00:00Z"), LocalTime.NOON, utc),
        )
        assertEquals(
            Instant.parse("2027-01-01T07:00:00Z"),
            nextDaily(Instant.parse("2026-12-31T23:00:00Z"), LocalTime.of(7, 0), utc),
        )
    }

    @Test(timeout = 20_000)
    fun `weekly schedule uses today when its time is still ahead`() {
        val next = nextWeekly(
            after = Instant.parse("2026-01-12T10:00:00Z"), // Monday 05:00 EST
            time = LocalTime.of(7, 0),
            days = setOf(DayOfWeek.MONDAY),
            zone = newYork,
        )

        assertEquals(Instant.parse("2026-01-12T12:00:00Z"), next)
    }

    @Test(timeout = 20_000)
    fun `weekly schedule on a single day waits a full week once today has passed`() {
        val next = nextWeekly(
            after = Instant.parse("2026-01-12T13:00:00Z"), // Monday 08:00 EST
            time = LocalTime.of(7, 0),
            days = setOf(DayOfWeek.MONDAY),
            zone = newYork,
        )

        assertEquals(Instant.parse("2026-01-19T12:00:00Z"), next)
    }

    @Test(timeout = 20_000)
    fun `weekly schedule moves to the following requested day`() {
        val next = nextWeekly(
            after = Instant.parse("2026-01-09T23:00:00Z"), // Friday 18:00 EST
            time = LocalTime.of(7, 0),
            days = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
            zone = newYork,
        )

        assertEquals(Instant.parse("2026-01-10T12:00:00Z"), next)
    }

    @Test(timeout = 20_000)
    fun `weekly schedule crossing the spring forward keeps the wall time`() {
        val next = nextWeekly(
            after = Instant.parse("2026-03-02T00:00:00Z"), // Sunday 1 Mar 19:00 EST
            time = LocalTime.of(7, 0),
            days = setOf(DayOfWeek.SUNDAY),
            zone = newYork,
        )

        assertEquals(Instant.parse("2026-03-08T11:00:00Z"), next) // 07:00 EDT
    }

    @Test(timeout = 20_000)
    fun `weekly schedule over every day matches the daily schedule`() {
        var after = Instant.parse("2026-03-06T00:00:00Z")
        repeat(60) {
            assertEquals(
                "after=$after",
                nextDaily(after, LocalTime.of(2, 30), newYork),
                nextWeekly(after, LocalTime.of(2, 30), DayOfWeek.entries.toSet(), newYork),
            )
            after = after.plus(Duration.ofHours(7))
        }
    }

    @Test(timeout = 20_000)
    fun `an empty weekly day set is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            nextWeekly(Instant.EPOCH, LocalTime.NOON, emptySet(), utc)
        }
    }

    @Test(timeout = 20_000)
    fun `daily results are strictly in the future, monotonic in the reference instant and at most 25 hours away`() {
        val zones = listOf("UTC", "America/New_York", "Asia/Kolkata", "Australia/Lord_Howe", "Europe/London")
            .map(ZoneId::of)

        zones.forEach { zone ->
            var after = Instant.parse("2026-03-05T00:00:00Z")
            var previous = Instant.MIN
            repeat(220) {
                val next = nextDaily(after, LocalTime.of(2, 30), zone)
                assertTrue("$zone after=$after next=$next", next > after)
                assertTrue("$zone after=$after next=$next", Duration.between(after, next) <= Duration.ofHours(25))
                assertTrue("$zone not monotonic at $after", next >= previous)
                previous = next
                after = after.plus(Duration.ofHours(6))
            }
        }
    }

    @Test(timeout = 20_000)
    fun `moving the clock backwards yields an earlier or equal next occurrence`() {
        val now = Instant.parse("2026-03-10T15:00:00Z")
        val setBack = now.minus(Duration.ofDays(2))

        val fromNow = nextDaily(now, LocalTime.of(7, 0), newYork)
        val fromSetBack = nextDaily(setBack, LocalTime.of(7, 0), newYork)

        assertTrue(fromSetBack < fromNow)
        assertEquals(Instant.parse("2026-03-09T11:00:00Z"), fromSetBack)
    }
}
