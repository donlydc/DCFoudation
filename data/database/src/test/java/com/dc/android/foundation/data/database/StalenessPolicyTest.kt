package com.dc.android.foundation.data.database

import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
}
