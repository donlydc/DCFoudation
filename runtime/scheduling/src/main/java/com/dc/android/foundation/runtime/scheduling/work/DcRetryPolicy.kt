package com.dc.android.foundation.runtime.scheduling.work

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Explicit retry ceiling; WorkManager otherwise retries `Result.retry()` indefinitely. */
data class DcRetryPolicy(
    val maxAttempts: Int = 5,
    val initialBackoff: Duration = 30.seconds,
    val exponential: Boolean = true,
) {
    init {
        require(maxAttempts >= 1) { "maxAttempts must be at least 1." }
        require(initialBackoff >= 10.seconds) {
            "initialBackoff must meet WorkManager's 10-second minimum."
        }
    }

    companion object {
        val Default = DcRetryPolicy()
        val NoRetry = DcRetryPolicy(maxAttempts = 1)
    }
}

/** `attempt` is one-based, matching WorkManager's `runAttemptCount + 1` convention. */
internal fun DcRetryPolicy.canRetry(attempt: Int): Boolean = attempt < maxAttempts
