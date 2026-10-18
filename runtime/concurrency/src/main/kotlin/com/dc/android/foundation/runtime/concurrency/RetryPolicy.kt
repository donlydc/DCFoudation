package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.retryWhen
import java.io.IOException
import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * One retry policy, two surfaces: [retrying] for a single suspend call, [Flow.retrying] for a
 * cold stream — a call site never needs to re-derive backoff math for whichever shape it has.
 *
 * Delay grows exponentially from [initialDelay] by [factor], capped at [maxDelay], with up to
 * ±[jitter] randomization so many callers retrying the same failure don't all wake on one tick
 * (thundering herd). [CancellationException] is never retried regardless of [retryIf].
 */
data class RetryPolicy(
    val maxAttempts: Int = 3,
    val initialDelay: Duration = 500.milliseconds,
    val maxDelay: Duration = 8.seconds,
    val factor: Double = 2.0,
    val jitter: Double = 0.2,
    val retryIf: (Throwable) -> Boolean = { it is IOException }
) {
    init {
        require(maxAttempts >= 1) { "maxAttempts must be at least 1." }
        require(!initialDelay.isNegative()) { "initialDelay must be non-negative." }
        require(factor >= 1.0) { "factor must be at least 1.0." }
        require(jitter in 0.0..1.0) { "jitter must be within [0.0, 1.0]." }
    }

    companion object {
        /** Fails on the first error. For call sites that must not silently retry. */
        val None = RetryPolicy(maxAttempts = 1)
        val Default = RetryPolicy()

        /** Longer ceiling and more attempts for WorkManager-style background jobs. */
        val Background = RetryPolicy(maxAttempts = 5, maxDelay = 60.seconds)
    }

    internal fun shouldRetry(cause: Throwable, attempt: Int): Boolean =
        cause !is CancellationException && attempt < maxAttempts - 1 && retryIf(cause)

    internal fun delayFor(attempt: Int): Duration {
        val exponential = initialDelay * factor.pow(attempt)
        val capped = if (exponential > maxDelay) maxDelay else exponential
        if (jitter == 0.0) return capped
        val jittered = 1.0 + Random.nextDouble(-jitter, jitter)
        return capped * jittered
    }
}

/**
 * Retries a single suspend call under [policy]. [block] receives the zero-based attempt number,
 * useful for logging — ignore it (`retrying { api.fetch() }`) when not needed.
 */
suspend fun <T> retrying(policy: RetryPolicy = RetryPolicy.Default, block: suspend (attempt: Int) -> T): T {
    var attempt = 0
    while (true) {
        try {
            return block(attempt)
        } catch (cause: CancellationException) {
            throw cause
        } catch (cause: Throwable) {
            if (!policy.shouldRetry(cause, attempt)) throw cause
            delay(policy.delayFor(attempt))
            attempt += 1
        }
    }
}

/** Same policy, applied to a cold [Flow]: retries by re-collecting upstream from the start. */
fun <T> Flow<T>.retrying(policy: RetryPolicy = RetryPolicy.Default): Flow<T> = retryWhen { cause, attempt ->
    policy.shouldRetry(cause, attempt.toInt()).also { willRetry ->
        if (willRetry) delay(policy.delayFor(attempt.toInt()))
    }
}
