package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class RetryPolicyTest {

    @Test
    fun `retrying succeeds without delay when the first attempt works`() = runTest {
        var attempts = 0
        val result = retrying { attempts++ }

        assertEquals(1, attempts)
        assertEquals(0, result)
        assertEquals(0, currentTime)
    }

    @Test
    fun `retrying retries eligible failures with exponential virtual delay`() = runTest {
        var attempts = 0
        val policy = RetryPolicy(
            maxAttempts = 3,
            initialDelay = 100.milliseconds,
            factor = 2.0,
            jitter = 0.0,
            retryIf = { it.message == "temporary" }
        )

        val result = retrying(policy) {
            attempts += 1
            if (attempts < 3) error("temporary") else "ok"
        }

        assertEquals("ok", result)
        assertEquals(3, attempts)
        assertEquals(300, currentTime) // 100ms + 200ms
    }

    @Test
    fun `retrying rethrows once maxAttempts is exhausted`() = runTest {
        var attempts = 0
        val policy = RetryPolicy(maxAttempts = 2, initialDelay = 10.milliseconds, jitter = 0.0, retryIf = { true })

        val failure = runCatching {
            retrying(policy) { attempts += 1; error("permanent") }
        }.exceptionOrNull()

        assertEquals(2, attempts)
        assertTrue(failure is IllegalStateException)
    }

    @Test
    fun `retrying never retries an ineligible failure`() = runTest {
        var attempts = 0
        val policy = RetryPolicy(retryIf = { false })

        runCatching { retrying(policy) { attempts += 1; error("not retryable") } }

        assertEquals(1, attempts)
        assertEquals(0, currentTime)
    }

    @Test
    fun `retrying never swallows cancellation`() = runTest {
        var attempts = 0
        val thrown = try {
            retrying { attempts += 1; throw CancellationException("cancelled") }
            null
        } catch (cause: CancellationException) {
            cause
        }

        assertTrue(thrown is CancellationException)
        assertEquals(1, attempts) // rethrown immediately, no retry attempted
    }

    @Test
    fun `Flow retrying applies the same policy to a cold stream`() = runTest {
        var attempts = 0
        val policy = RetryPolicy(
            maxAttempts = 3,
            initialDelay = 50.milliseconds,
            jitter = 0.0,
            retryIf = { it.message == "temporary" }
        )

        val values = flow {
            attempts += 1
            if (attempts < 3) error("temporary")
            emit("ok")
        }.retrying(policy).toList()

        assertEquals(listOf("ok"), values)
        assertEquals(3, attempts)
        assertEquals(150, currentTime) // 50ms + 100ms
    }
}
