package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.ContinuationInterceptor
import kotlin.time.Duration.Companion.milliseconds

/**
 * MP:CON-07: ownership and shutdown contract of [AppCoroutineScope]. The scope is process-lifetime
 * in production (never cancelled by its single owner, the application); tests stop it with
 * `cancel()`, which is the only shutdown surface and is exercised here.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppCoroutineScopeTest {

    private class Slots(
        override val main: CoroutineDispatcher,
        override val mainImmediate: CoroutineDispatcher,
        override val io: CoroutineDispatcher,
        override val default: CoroutineDispatcher,
    ) : DispatcherProvider

    private fun TestScope.slots(): Slots = Slots(
        main = StandardTestDispatcher(testScheduler, "main"),
        mainImmediate = StandardTestDispatcher(testScheduler, "mainImmediate"),
        io = StandardTestDispatcher(testScheduler, "io"),
        default = StandardTestDispatcher(testScheduler, "default"),
    )

    @Test(timeout = 20_000)
    fun `work runs on the default slot of the provider`() = runTest {
        val dispatchers = slots()
        val scope = AppCoroutineScope(dispatchers) { }
        try {
            var seen: Any? = null
            scope.launch { seen = currentCoroutineContext()[ContinuationInterceptor] }
            runCurrent()

            assertSame(dispatchers.default, seen)
        } finally {
            scope.cancel()
        }
    }

    @Test(timeout = 20_000)
    fun `an uncaught launch failure reaches the callback and siblings keep running`() = runTest {
        val uncaught = mutableListOf<Throwable>()
        val scope = AppCoroutineScope(slots()) { uncaught += it }
        try {
            var siblingCompleted = false
            scope.launch { throw IllegalStateException("boom") }
            scope.launch { delay(10.milliseconds); siblingCompleted = true }
            runCurrent()
            advanceTimeBy(20.milliseconds)
            runCurrent()

            assertEquals(listOf("boom"), uncaught.map { it.message })
            assertTrue(siblingCompleted)
            assertTrue("the scope outlives a failed child (SupervisorJob)", scope.coroutineContext[Job]!!.isActive)
        } finally {
            scope.cancel()
        }
    }

    @Test(timeout = 20_000)
    fun `an async failure is delivered through await and not to the callback`() = runTest {
        val uncaught = mutableListOf<Throwable>()
        val scope = AppCoroutineScope(slots()) { uncaught += it }
        try {
            val deferred = scope.async<Int> { throw IllegalStateException("boom") }
            runCurrent()

            val failure = runCatching { deferred.await() }.exceptionOrNull()

            assertEquals("boom", failure?.message)
            assertTrue(uncaught.isEmpty())
        } finally {
            scope.cancel()
        }
    }

    @Test(timeout = 20_000)
    fun `cancellation is not reported as an uncaught failure`() = runTest {
        val uncaught = mutableListOf<Throwable>()
        val scope = AppCoroutineScope(slots()) { uncaught += it }
        try {
            scope.launch { throw CancellationException("not a failure") }
            runCurrent()

            assertTrue(uncaught.isEmpty())
            assertTrue(scope.coroutineContext[Job]!!.isActive)
        } finally {
            scope.cancel()
        }
    }

    @Test(timeout = 20_000)
    fun `cancel stops running children and drops later launches`() = runTest {
        val scope = AppCoroutineScope(slots()) { }
        var finished = false
        var launchedAfterCancel = false

        val running = scope.launch { delay(100.milliseconds); finished = true }
        runCurrent()
        scope.cancel()
        scope.launch { launchedAfterCancel = true }
        advanceTimeBy(500.milliseconds)
        runCurrent()

        assertTrue(running.isCancelled)
        assertFalse(finished)
        assertFalse(launchedAfterCancel)
        assertFalse(scope.coroutineContext[Job]!!.isActive)
    }
}
