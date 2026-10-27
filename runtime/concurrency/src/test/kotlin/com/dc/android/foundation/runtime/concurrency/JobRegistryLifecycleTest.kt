package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

/** MP:CON-02: registration / cancel / removal lifecycle and "no orphan job" guarantees. */
@OptIn(ExperimentalCoroutinesApi::class)
class JobRegistryLifecycleTest {

    @Test(timeout = 20_000)
    fun `cancelAll cancels every job and frees every key`() = runTest {
        VirtualTimeScope(this).use { work ->
            val jobs = JobRegistry(work.scope)
            var completed = 0

            val a = jobs.launchLatest("a") { delay(100.milliseconds); completed++ }
            val b = jobs.launchLatest("b") { delay(100.milliseconds); completed++ }
            runCurrent()
            assertTrue(jobs.isRunning("a"))
            assertTrue(jobs.isRunning("b"))

            jobs.cancelAll()
            runCurrent()

            assertTrue(a.isCancelled)
            assertTrue(b.isCancelled)
            assertFalse(jobs.isRunning("a"))
            assertFalse(jobs.isRunning("b"))
            advanceTimeBy(300.milliseconds)
            runCurrent()
            assertEquals(0, completed)
            assertNotNull("a cancelled key must be reusable", jobs.launchIfIdle("a") { })
        }
    }

    @Test(timeout = 20_000)
    fun `calls without a key share one default slot`() = runTest {
        VirtualTimeScope(this).use { work ->
            val jobs = JobRegistry(work.scope)

            val first = jobs.launchLatest { delay(100.milliseconds) }
            runCurrent()
            assertTrue(jobs.isRunning())

            val second = jobs.launchLatest { delay(100.milliseconds) }
            runCurrent()

            assertTrue(first.isCancelled)
            assertTrue(second.isActive)
            assertNull(jobs.launchIfIdle { })
            jobs.cancel()
            runCurrent()
            assertFalse(jobs.isRunning())
        }
    }

    @Test(timeout = 20_000)
    fun `a failed job releases its key and reports to the scope`() = runTest {
        VirtualTimeScope(this).use { work ->
            val jobs = JobRegistry(work.scope)

            val failing = jobs.launchLatest("sync") { error("boom") }
            runCurrent()

            assertTrue(failing.isCompleted)
            assertFalse(jobs.isRunning("sync"))
            assertEquals(listOf("boom"), work.failures.map { it.message })
            assertNotNull("a failed key must be reusable", jobs.launchIfIdle("sync") { })
        }
    }

    @Test(timeout = 20_000)
    fun `cancelling the owner scope leaves no registered job behind`() = runTest {
        VirtualTimeScope(this).use { work ->
            val jobs = JobRegistry(work.scope)
            var ran = false

            jobs.launchLatest("a") { delay(100.milliseconds) }
            jobs.launchLatest("b") { delay(100.milliseconds) }
            runCurrent()
            work.scope.cancel()
            runCurrent()

            assertFalse(jobs.isRunning("a"))
            assertFalse(jobs.isRunning("b"))

            // Registering on a dead scope must not wedge the key as busy either.
            val late = jobs.launchIfIdle("a") { ran = true }
            assertNotNull(late)
            runCurrent()
            assertTrue(late!!.isCancelled)
            assertFalse(ran)
            assertNotNull("the key must not stay occupied after the scope died", jobs.launchIfIdle("a") { })
        }
    }

    @Test(timeout = 20_000)
    fun `a predecessor finishing late does not evict its successor`() = runTest {
        VirtualTimeScope(this).use { work ->
            val jobs = JobRegistry(work.scope)

            val first = jobs.launchLatest("k") { withContext(NonCancellable) { delay(50.milliseconds) } }
            runCurrent()
            val second = jobs.launchLatest("k") { delay(200.milliseconds) }
            advanceTimeBy(100.milliseconds)
            runCurrent()

            assertTrue("the predecessor finished its non-cancellable section", first.isCompleted)
            assertTrue(second.isActive)
            assertTrue(jobs.isRunning("k"))
            assertNull("the successor is still registered, so the key is busy", jobs.launchIfIdle("k") { })

            advanceTimeBy(300.milliseconds)
            runCurrent()
            assertFalse(jobs.isRunning("k"))
        }
    }

    @Test(timeout = 20_000)
    fun `128 seeded operation sequences match a simple model and end with no running job`() = runTest {
        class Slot(val id: Int, val endsAt: Long)

        repeat(SEEDS) { seed ->
            VirtualTimeScope(this).use { work ->
                val random = Random(seed)
                val jobs = JobRegistry(work.scope)
                val slots = arrayOfNulls<Slot>(KEYS)
                val expected = mutableSetOf<Int>()
                val completed = mutableListOf<Int>()
                var nextId = 0

                // Operations happen at even virtual times and every job lasts an odd number of
                // milliseconds, so a job end never coincides with an operation.
                fun settle() {
                    for (key in 0 until KEYS) {
                        val slot = slots[key]
                        if (slot != null && slot.endsAt < testScheduler.currentTime) {
                            expected += slot.id
                            slots[key] = null
                        }
                    }
                }

                repeat(OPERATIONS) {
                    advanceTimeBy((2 * random.nextLong(0, 10)).milliseconds)
                    runCurrent()
                    settle()
                    val key = random.nextInt(KEYS)
                    val duration = 2 * random.nextLong(5, 20) + 1
                    val id = nextId++
                    when (random.nextInt(3)) {
                        0 -> {
                            slots[key] = Slot(id, testScheduler.currentTime + duration)
                            jobs.launchLatest(key) { delay(duration.milliseconds); completed += id }
                        }
                        1 -> {
                            val job = jobs.launchIfIdle(key) { delay(duration.milliseconds); completed += id }
                            if (slots[key] == null) {
                                assertNotNull("seed $seed: idle key must accept a job", job)
                                slots[key] = Slot(id, testScheduler.currentTime + duration)
                            } else {
                                assertNull("seed $seed: busy key must reject a job", job)
                            }
                        }
                        else -> {
                            slots[key] = null
                            jobs.cancel(key)
                        }
                    }
                }

                advanceTimeBy(1_000.milliseconds)
                runCurrent()
                settle()

                assertEquals("seed $seed", expected, completed.toSet())
                assertEquals("seed $seed: a job completed twice", completed.size, completed.toSet().size)
                for (key in 0 until KEYS) {
                    assertFalse("seed $seed: key $key still running", jobs.isRunning(key))
                    assertNotNull("seed $seed: key $key stuck busy", jobs.launchIfIdle(key) { })
                }
            }
        }
    }

    private companion object {
        const val SEEDS = 128
        const val KEYS = 3
        const val OPERATIONS = 10
    }
}
