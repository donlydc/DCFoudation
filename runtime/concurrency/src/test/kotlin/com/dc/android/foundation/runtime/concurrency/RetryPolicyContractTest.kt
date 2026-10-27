package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** MP:CON-03: retry/backoff semantics, all in virtual time with injectable jitter. */
@OptIn(ExperimentalCoroutinesApi::class)
class RetryPolicyContractTest {

    private fun alwaysRetry(
        maxAttempts: Int,
        initialDelay: kotlin.time.Duration = 100.milliseconds,
        maxDelay: kotlin.time.Duration = 8.seconds,
        factor: Double = 2.0,
        jitter: Double = 0.0,
    ) = RetryPolicy(
        maxAttempts = maxAttempts,
        initialDelay = initialDelay,
        maxDelay = maxDelay,
        factor = factor,
        jitter = jitter,
        retryIf = { true },
    )

    @Test(timeout = 20_000)
    fun `backoff grows exponentially and is capped at maxDelay`() = runTest {
        val policy = alwaysRetry(maxAttempts = 6, initialDelay = 100.milliseconds, maxDelay = 250.milliseconds)

        runCatching { retrying(policy) { error("always") } }

        // Delays before retries 1..5: 100, 200, 250 (capped), 250, 250.
        assertEquals(1_050, currentTime)
    }

    @Test(timeout = 20_000)
    fun `block receives zero-based attempt numbers and maxAttempts counts the first execution`() = runTest {
        val seen = mutableListOf<Int>()

        runCatching { retrying(alwaysRetry(maxAttempts = 3)) { attempt -> seen += attempt; error("fail") } }

        assertEquals(listOf(0, 1, 2), seen)
    }

    @Test(timeout = 20_000)
    fun `exhaustion rethrows the last failure`() = runTest {
        val failure = runCatching {
            retrying(alwaysRetry(maxAttempts = 3)) { attempt -> error("attempt $attempt") }
        }.exceptionOrNull()

        assertEquals("attempt 2", failure?.message)
    }

    @Test(timeout = 20_000)
    fun `None policy executes once and never delays`() = runTest {
        var attempts = 0

        val failure = runCatching {
            retrying(RetryPolicy.None) { attempts += 1; throw IOException("offline") }
        }.exceptionOrNull()

        assertTrue(failure is IOException)
        assertEquals(1, attempts)
        assertEquals(0, currentTime)
    }

    @Test(timeout = 20_000)
    fun `default policy retries IOException but not other failures`() = runTest {
        var ioAttempts = 0
        val value = retrying {
            ioAttempts += 1
            if (ioAttempts < 3) throw IOException("offline") else "ok"
        }
        // Delays are 500 ms * (1 +/- 0.2) then 1000 ms * (1 +/- 0.2).
        assertEquals("ok", value)
        assertEquals(3, ioAttempts)
        assertTrue("virtual time was $currentTime", currentTime in 1_200..1_800)

        var otherAttempts = 0
        runCatching { retrying { otherAttempts += 1; error("bug") } }
        assertEquals(1, otherAttempts)
    }

    @Test(timeout = 20_000)
    fun `a timeout raised inside the block is cancellation and is not retried`() = runTest {
        var attempts = 0

        val failure = runCatching {
            retrying(alwaysRetry(maxAttempts = 5)) {
                attempts += 1
                withTimeout(10.milliseconds) { delay(1_000.milliseconds) }
            }
        }.exceptionOrNull()

        assertTrue("expected TimeoutCancellationException but was $failure", failure is TimeoutCancellationException)
        assertEquals(1, attempts)
    }

    @Test(timeout = 20_000)
    fun `cancelling the caller during backoff stops retrying`() = runTest {
        var attempts = 0
        val caller = launch {
            retrying(alwaysRetry(maxAttempts = 5, initialDelay = 100.milliseconds)) {
                attempts += 1
                throw IOException("offline")
            }
        }
        runCurrent()
        assertEquals(1, attempts)

        caller.cancel()
        advanceTimeBy(5.seconds)
        runCurrent()

        assertEquals(1, attempts)
        assertTrue(caller.isCancelled)
    }

    @Test(timeout = 20_000)
    fun `jitter is injectable deterministic and bounded`() {
        val policy = RetryPolicy(initialDelay = 100.milliseconds, factor = 2.0, jitter = 0.2, retryIf = { true })

        assertEquals(policy.delayFor(1, Random(5)), policy.delayFor(1, Random(5)))
        repeat(100) { seed ->
            val delay = policy.delayFor(attempt = 1, random = Random(seed)) // base 200 ms
            assertTrue("seed $seed: $delay", delay >= 160.milliseconds && delay <= 240.milliseconds)
        }
    }

    @Test(timeout = 20_000)
    fun `zero jitter ignores the random source`() {
        val policy = RetryPolicy(initialDelay = 100.milliseconds, factor = 2.0, jitter = 0.0)

        assertEquals(400.milliseconds, policy.delayFor(attempt = 2, random = Random(1)))
        assertEquals(400.milliseconds, policy.delayFor(attempt = 2, random = Random(2)))
    }

    @Test(timeout = 20_000)
    fun `cap applies before jitter so a delay may exceed maxDelay by at most the jitter`() {
        val policy = RetryPolicy.Default // maxDelay 8 s, jitter 0.2

        repeat(100) { seed ->
            val delay = policy.delayFor(attempt = 10, random = Random(seed))
            assertTrue("seed $seed: $delay", delay >= 6_400.milliseconds && delay <= 9_600.milliseconds)
        }
    }

    @Test(timeout = 20_000)
    fun `presets expose the documented shape`() {
        assertEquals(1, RetryPolicy.None.maxAttempts)
        assertEquals(3, RetryPolicy.Default.maxAttempts)
        assertEquals(500.milliseconds, RetryPolicy.Default.initialDelay)
        assertEquals(8.seconds, RetryPolicy.Default.maxDelay)
        assertEquals(5, RetryPolicy.Background.maxAttempts)
        assertEquals(60.seconds, RetryPolicy.Background.maxDelay)
    }

    @Test(timeout = 20_000)
    fun `invalid configuration is rejected at construction`() {
        assertThrows(IllegalArgumentException::class.java) { RetryPolicy(maxAttempts = 0) }
        assertThrows(IllegalArgumentException::class.java) { RetryPolicy(initialDelay = (-1).milliseconds) }
        assertThrows(IllegalArgumentException::class.java) { RetryPolicy(factor = 0.5) }
        assertThrows(IllegalArgumentException::class.java) { RetryPolicy(jitter = 1.5) }
        assertThrows(IllegalArgumentException::class.java) { RetryPolicy(jitter = -0.1) }
    }

    @Test(timeout = 20_000)
    fun `Flow retrying re-collects upstream from the start`() = runTest {
        var runs = 0
        val policy = alwaysRetry(maxAttempts = 3, initialDelay = 10.milliseconds)

        val values = flow {
            runs += 1
            emit(runs)
            if (runs < 3) error("temporary")
        }.retrying(policy).toList()

        assertEquals(listOf(1, 2, 3), values)
        assertEquals(30, currentTime) // 10 ms + 20 ms
    }

    @Test(timeout = 20_000)
    fun `Flow retrying rethrows once attempts are exhausted`() = runTest {
        var runs = 0
        val policy = alwaysRetry(maxAttempts = 2, initialDelay = 10.milliseconds)

        val failure = runCatching {
            flow<Int> { runs += 1; error("permanent $runs") }.retrying(policy).toList()
        }.exceptionOrNull()

        assertEquals(2, runs)
        assertEquals("permanent 2", failure?.message)
    }

    @Test(timeout = 20_000)
    fun `Flow retrying does not retry cancellation`() = runTest {
        var runs = 0

        val failure = runCatching {
            flow<Int> { runs += 1; throw CancellationException("cancelled upstream") }
                .retrying(alwaysRetry(maxAttempts = 5))
                .toList()
        }.exceptionOrNull()

        assertTrue(failure is CancellationException)
        assertEquals(1, runs)
        assertEquals(0, currentTime)
    }
}
