package com.dc.android.foundation.runtime.scheduling.work

import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DcWorkPolicyTest {
    @Test
    fun `retry ceiling turns the final retry into failure`() {
        val policy = DcRetryPolicy(maxAttempts = 3)

        assertTrue(policy.canRetry(attempt = 1))
        assertTrue(policy.canRetry(attempt = 2))
        assertFalse(policy.canRetry(attempt = 3))
    }

    @Test
    fun `periodic work below fifteen minutes is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            validatePeriodicInterval(14.minutes)
        }
    }

    @Test
    fun `periodic flex must be valid`() {
        assertThrows(IllegalArgumentException::class.java) { validatePeriodicInterval(15.minutes, 4.minutes) }
        assertThrows(IllegalArgumentException::class.java) { validatePeriodicInterval(15.minutes, 15.minutes) }
    }

    @Test
    fun `retry backoff below WorkManager minimum is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            DcRetryPolicy(initialBackoff = 9.seconds)
        }
    }
}
