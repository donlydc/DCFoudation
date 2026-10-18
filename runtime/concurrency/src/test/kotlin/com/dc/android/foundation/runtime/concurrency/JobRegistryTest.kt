package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

/**
 * [JobRegistry] runs its jobs on [backgroundScope], so these tests advance virtual time with
 * [advanceTimeBy] rather than `advanceUntilIdle()` — see the equivalent note on
 * [SingleFlightTest] for why `advanceUntilIdle()` alone would never run this work.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class JobRegistryTest {

    @Test
    fun `concurrent launchIfIdle calls register only one job`() = runBlocking {
        val release = CompletableDeferred<Unit>()
        val registry = JobRegistry(CoroutineScope(SupervisorJob() + Dispatchers.Default))

        val launched = (0 until 32).map {
            async(Dispatchers.Default) {
                registry.launchIfIdle("refresh") { release.await() }
            }
        }.awaitAll()

        assertEquals(1, launched.count { it != null })
        release.complete(Unit)
        launched.filterNotNull().forEach { it.join() }
    }

    @Test
    fun `launchLatest cancels the previous job under the same key`() = runTest {
        val jobs = JobRegistry(backgroundScope)
        var firstCompleted = false
        var secondCompleted = false

        jobs.launchLatest("search") { delay(100.milliseconds); firstCompleted = true }
        runCurrent()
        jobs.launchLatest("search") { delay(100.milliseconds); secondCompleted = true }
        advanceTimeBy(150.milliseconds)
        runCurrent()

        assertFalse(firstCompleted)
        assertTrue(secondCompleted)
    }

    @Test
    fun `launchLatest under different keys does not cancel unrelated jobs`() = runTest {
        val jobs = JobRegistry(backgroundScope)
        var searchCompleted = false
        var refreshCompleted = false

        jobs.launchLatest("search") { delay(50.milliseconds); searchCompleted = true }
        jobs.launchLatest("refresh") { delay(50.milliseconds); refreshCompleted = true }
        advanceTimeBy(100.milliseconds)
        runCurrent()

        assertTrue(searchCompleted)
        assertTrue(refreshCompleted)
    }

    @Test
    fun `launchIfIdle skips while a job under the same key is already running`() = runTest {
        val jobs = JobRegistry(backgroundScope)
        var runs = 0

        val first = jobs.launchIfIdle("refresh") { runs++; delay(50.milliseconds) }
        runCurrent()
        val skipped = jobs.launchIfIdle("refresh") { runs++ }

        assertNotNull(first)
        assertNull(skipped)
        assertTrue(jobs.isRunning("refresh"))

        advanceTimeBy(100.milliseconds)
        runCurrent()

        assertEquals(1, runs)
        assertFalse(jobs.isRunning("refresh"))
    }

    @Test
    fun `launchIfIdle allows a new job once the previous one under the same key finished`() = runTest {
        val jobs = JobRegistry(backgroundScope)
        var runs = 0

        jobs.launchIfIdle("refresh") { runs++ }
        runCurrent()

        val second = jobs.launchIfIdle("refresh") { runs++ }
        runCurrent()

        assertNotNull(second)
        assertEquals(2, runs)
    }

    @Test
    fun `cancel stops the job registered under a key`() = runTest {
        val jobs = JobRegistry(backgroundScope)
        var completed = false

        jobs.launchLatest("sync") { delay(100.milliseconds); completed = true }
        runCurrent()
        jobs.cancel("sync")
        advanceTimeBy(150.milliseconds)
        runCurrent()

        assertFalse(completed)
        assertFalse(jobs.isRunning("sync"))
    }
}
