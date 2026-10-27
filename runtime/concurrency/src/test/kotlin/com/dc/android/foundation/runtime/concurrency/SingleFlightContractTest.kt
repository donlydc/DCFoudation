package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

/**
 * MP:CON-01 contract tests. Everything runs in virtual time on a [VirtualTimeScope]; there is no
 * real sleep and no real thread race.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SingleFlightContractTest {

    @Test(timeout = 10_000)
    fun `using the test scope itself as the flight scope lets runTest finish`() = runTest {
        assertEquals(1, SingleFlight<String, Int>(this).run("k") { 1 })
        assertEquals(2, SingleFlight<String, Int>(this).run("k") { 2 })
    }

    @Test(timeout = 10_000)
    fun `a joined coroutineScope used as the flight scope completes`() = runBlocking {
        val value = coroutineScope { SingleFlight<String, Int>(this).run("k") { 1 } }
        assertEquals(1, value)
        val failure = runCatching {
            coroutineScope { SingleFlight<String, Int>(this).run("k") { error("boom") } }
        }.exceptionOrNull()
        assertEquals("boom", failure?.message)
    }

    @Test(timeout = 10_000)
    fun `failure reaches every awaiter and is not cached`() = runTest {
        VirtualTimeScope(this).use { work ->
            val flight = SingleFlight<String, Int>(work.scope)
            var executions = 0
            val gate = CompletableDeferred<Unit>()

            val a = async { runCatching { flight.run("k") { executions++; gate.await(); error("boom") } } }
            val b = async { runCatching { flight.run("k") { executions++; gate.await(); error("other") } } }
            runCurrent()
            gate.complete(Unit)
            runCurrent()

            assertEquals(1, executions)
            assertEquals("boom", a.await().exceptionOrNull()?.message)
            assertEquals("boom", b.await().exceptionOrNull()?.message)
            // The failed execution released the key, so the next call executes again and succeeds.
            assertEquals(7, flight.run("k") { executions++; 7 })
            assertEquals(2, executions)
            assertTrue("a failed block is delivered to awaiters, not to the scope handler", work.failures.isEmpty())
        }
    }

    @Test(timeout = 20_000)
    fun `a failing block does not cancel a scope that has a plain Job`() = runTest {
        val plainJob = Job()
        VirtualTimeScope(this, job = plainJob).use { work ->
            val flight = SingleFlight<String, Int>(work.scope)

            val failure = runCatching { flight.run("k") { error("boom") } }.exceptionOrNull()

            assertEquals("boom", failure?.message)
            assertTrue("the caller-supplied scope must survive a failed execution", plainJob.isActive)
            assertEquals(1, flight.run("k") { 1 })
        }
    }

    @Test(timeout = 20_000)
    fun `execution continues and the key is released when every caller cancels`() = runTest {
        VirtualTimeScope(this).use { work ->
            val flight = SingleFlight<String, Int>(work.scope)
            var finished = false

            val a = launch { flight.run("k") { delay(50.milliseconds); finished = true; 1 } }
            val b = launch { flight.run("k") { delay(50.milliseconds); finished = true; 1 } }
            runCurrent()
            a.cancel()
            b.cancel()
            runCurrent()
            assertFalse(finished)

            advanceTimeBy(100.milliseconds)
            runCurrent()
            assertTrue("the shared execution is owned by the scope, not by its callers", finished)

            assertEquals(2, flight.run("k") { 2 })
        }
    }

    @Test(timeout = 20_000)
    fun `a late caller joins the execution already in flight`() = runTest {
        VirtualTimeScope(this).use { work ->
            val flight = SingleFlight<String, Int>(work.scope)
            var executions = 0

            val first = async { flight.run("k") { executions++; delay(50.milliseconds); 10 } }
            runCurrent()
            advanceTimeBy(20.milliseconds)
            val late = async { flight.run("k") { executions++; 99 } }
            advanceTimeBy(100.milliseconds)
            runCurrent()

            assertEquals(10, first.await())
            assertEquals(10, late.await())
            assertEquals(1, executions)
        }
    }

    @Test(timeout = 20_000)
    fun `a cancelled scope fails calls with CancellationException and leaves no stale key`() = runTest {
        VirtualTimeScope(this).use { work ->
            val flight = SingleFlight<String, Int>(work.scope)
            work.scope.cancel()

            repeat(2) {
                val failure = runCatching { flight.run("k") { 1 } }.exceptionOrNull()
                assertTrue("expected CancellationException but was $failure", failure is CancellationException)
            }
        }
    }

    @Test(timeout = 20_000)
    fun `cancelling the scope cancels the in-flight execution and its waiters`() = runTest {
        VirtualTimeScope(this).use { work ->
            val flight = SingleFlight<String, Int>(work.scope)
            var finished = false

            val waiter = async { runCatching { flight.run("k") { delay(1_000.milliseconds); finished = true; 1 } } }
            runCurrent()
            work.scope.cancel()
            advanceTimeBy(2_000.milliseconds)
            runCurrent()

            assertTrue(waiter.await().exceptionOrNull() is CancellationException)
            assertFalse(finished)
        }
    }

    @Test(timeout = 20_000)
    fun `128 seeded interleavings keep one live execution per key and join only live executions`() = runTest {
        class Execution(val key: Int, val start: Long) {
            var end: Long = Long.MAX_VALUE
        }

        class Observation(val key: Int, val cancellable: Boolean) {
            var invokedAt = -1L
            var executionId: Int? = null
        }

        repeat(SEEDS) { seed ->
            VirtualTimeScope(this).use { work ->
                val random = Random(seed)
                val flight = SingleFlight<Int, Int>(work.scope)
                val executions = mutableListOf<Execution>()
                val liveByKey = IntArray(KEYS)
                var maxLiveForAKey = 0
                val observations = mutableListOf<Observation>()

                repeat(CALLERS) {
                    val key = random.nextInt(KEYS)
                    val startAt = random.nextLong(0, 40)
                    val runFor = random.nextLong(1, 30)
                    val cancelAfter = if (random.nextInt(4) == 0) random.nextLong(0, 30) else null
                    val observation = Observation(key, cancellable = cancelAfter != null)
                    observations += observation

                    val caller = launch {
                        delay(startAt)
                        observation.invokedAt = testScheduler.currentTime
                        observation.executionId = flight.run(key) {
                            val id = executions.size
                            val execution = Execution(key, testScheduler.currentTime)
                            executions += execution
                            liveByKey[key]++
                            maxLiveForAKey = maxOf(maxLiveForAKey, liveByKey[key])
                            delay(runFor)
                            liveByKey[key]--
                            execution.end = testScheduler.currentTime
                            id
                        }
                    }
                    if (cancelAfter != null) {
                        launch {
                            delay(startAt + cancelAfter)
                            caller.cancel()
                        }
                    }
                }

                advanceTimeBy(1_000.milliseconds)
                runCurrent()

                assertTrue("seed $seed: executions for one key overlapped", maxLiveForAKey <= 1)
                assertTrue("seed $seed: some execution must have run", executions.isNotEmpty())
                observations.forEach { observation ->
                    val id = observation.executionId
                    if (!observation.cancellable) {
                        assertNotNull("seed $seed: an uncancelled caller must receive a result", id)
                    }
                    if (id != null) {
                        val execution = executions[id]
                        assertEquals("seed $seed: joined an execution of another key", observation.key, execution.key)
                        assertTrue(
                            "seed $seed: joined an execution that was not live at ${observation.invokedAt} " +
                                "(${execution.start}..${execution.end})",
                            execution.start <= observation.invokedAt && observation.invokedAt <= execution.end,
                        )
                    }
                }

                // Everything is idle: no key may be stuck, so each used key executes afresh.
                observations.map { it.key }.toSet().forEach { key ->
                    assertEquals(-key, flight.run(key) { -key })
                }
            }
        }
    }

    private companion object {
        const val SEEDS = 128
        const val KEYS = 3
        const val CALLERS = 12
    }
}
