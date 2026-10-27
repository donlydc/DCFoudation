package com.dc.android.foundation.data.database

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class StalenessPolicyTest {

    private val policy = StalenessPolicy(maxAge = 15.minutes)

    @Test
    fun `missing data is stale`() {
        assertTrue(policy.isStale(lastUpdatedAtMillis = null, nowMillis = 1_000_000L))
    }

    @Test
    fun `data at or over the maximum age is stale`() {
        assertTrue(policy.isStale(lastUpdatedAtMillis = 100_000L, nowMillis = 1_000_000L))
    }

    @Test
    fun `future timestamps are treated as fresh`() {
        assertFalse(policy.isStale(lastUpdatedAtMillis = 1_100_000L, nowMillis = 1_000_000L))
    }

    // maxAge = 900_000 ms; updated at 100_000 -> boundary "now" is 1_000_000.

    @Test
    fun `one millisecond before the TTL boundary is fresh`() {
        assertFalse(policy.isStale(lastUpdatedAtMillis = 100_000L, nowMillis = 999_999L))
    }

    @Test
    fun `exactly at the TTL boundary is stale`() {
        assertTrue(policy.isStale(lastUpdatedAtMillis = 100_000L, nowMillis = 1_000_000L))
    }

    @Test
    fun `one millisecond past the TTL boundary is stale`() {
        assertTrue(policy.isStale(lastUpdatedAtMillis = 100_000L, nowMillis = 1_000_001L))
    }

    @Test
    fun `timestamp equal to now has zero age and is fresh`() {
        assertFalse(policy.isStale(lastUpdatedAtMillis = 1_000_000L, nowMillis = 1_000_000L))
    }

    @Test
    fun `negative clock skew of any size is fresh and never stale`() {
        assertFalse(policy.isStale(lastUpdatedAtMillis = 1_000_001L, nowMillis = 1_000_000L))
        assertFalse(policy.isStale(lastUpdatedAtMillis = Long.MAX_VALUE, nowMillis = 0L))
    }

    @Test
    fun `zero maxAge makes every non-future timestamp stale`() {
        val zero = StalenessPolicy(maxAge = Duration.ZERO)
        assertTrue(zero.isStale(lastUpdatedAtMillis = 5L, nowMillis = 5L))
        assertFalse(zero.isStale(lastUpdatedAtMillis = 6L, nowMillis = 5L))
    }

    @Test
    fun `sub-millisecond maxAge truncates to zero milliseconds`() {
        // Documents current behaviour: inWholeMilliseconds truncation, so 0.5 ms behaves like zero.
        val tiny = StalenessPolicy(maxAge = Duration.parse("PT0.0005S"))
        assertTrue(tiny.isStale(lastUpdatedAtMillis = 5L, nowMillis = 5L))
    }

    @Test
    fun `now is caller supplied so the policy reads no clock`() {
        val p = StalenessPolicy(maxAge = 10.milliseconds)
        assertFalse(p.isStale(lastUpdatedAtMillis = 0L, nowMillis = 9L))
        assertTrue(p.isStale(lastUpdatedAtMillis = 0L, nowMillis = 10L))
    }

    @Test
    fun `negative maxAge is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            StalenessPolicy(maxAge = (-1).minutes)
        }
    }
}
