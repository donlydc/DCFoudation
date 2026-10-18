package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

/**
 * [SingleFlight]'s shared execution runs on [backgroundScope], so these tests advance virtual
 * time with [advanceTimeBy] rather than `advanceUntilIdle()`: `advanceUntilIdle()` deliberately
 * stops once only background-scoped work remains queued (to avoid hanging on a never-ending
 * background job), which would never run the delay inside [SingleFlight.run]'s shared block.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SingleFlightTest {

    @Test
    fun `completed immediate execution is removed before the next call`() = runBlocking {
        val singleFlight = SingleFlight<String, Int>(CoroutineScope(Dispatchers.Unconfined))
        var executions = 0

        assertEquals(1, singleFlight.run("token") { ++executions })
        assertEquals(2, singleFlight.run("token") { ++executions })
        assertEquals(2, executions)
    }

    @Test
    fun `concurrent calls with the same key collapse into one execution`() = runTest {
        val singleFlight = SingleFlight<String, Int>(backgroundScope)
        var executions = 0

        val a = async { singleFlight.run("articles") { executions++; delay(50.milliseconds); 42 } }
        val b = async { singleFlight.run("articles") { executions++; delay(50.milliseconds); 42 } }
        advanceTimeBy(100.milliseconds)
        runCurrent()

        assertEquals(1, executions)
        assertEquals(42, a.await())
        assertEquals(42, b.await())
    }

    @Test
    fun `different keys run independently`() = runTest {
        val singleFlight = SingleFlight<String, Int>(backgroundScope)
        var executions = 0

        val a = async { singleFlight.run("articles") { executions++; delay(10.milliseconds); 1 } }
        val b = async { singleFlight.run("profile") { executions++; delay(10.milliseconds); 2 } }
        advanceTimeBy(50.milliseconds)
        runCurrent()

        assertEquals(2, executions)
        assertEquals(1, a.await())
        assertEquals(2, b.await())
    }

    @Test
    fun `canceling one caller does not cancel the shared execution or other waiters`() = runTest {
        val singleFlight = SingleFlight<String, Int>(backgroundScope)
        var executions = 0

        val a = launch { singleFlight.run("articles") { executions++; delay(50.milliseconds); 1 } }
        val b = async { singleFlight.run("articles") { executions++; delay(50.milliseconds); 1 } }
        runCurrent()

        a.cancel()
        advanceTimeBy(100.milliseconds)
        runCurrent()

        assertEquals(1, executions)
        assertEquals(1, b.await())
    }
}
