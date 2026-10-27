package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class FlowSharingTest {

    @Test(timeout = 20_000)
    fun `throttleFirst keeps the first value per window and drops the rest`() = runTest {
        val values = flow {
            emit(1) // t=0, window opens, emitted
            delay(10.milliseconds)
            emit(2) // t=10, inside window, dropped
            delay(10.milliseconds)
            emit(3) // t=20, inside window, dropped
            delay(90.milliseconds)
            emit(4) // t=110, past the 100ms window, emitted
        }.throttleFirst(window = 100.milliseconds).toList()

        assertEquals(listOf(1, 4), values)
    }

    @Test(timeout = 20_000)
    fun `throttleFirst reopens a new window after each emitted value`() = runTest {
        val values = flow {
            emit(0) // t=0, emitted, window until 100
            delay(50.milliseconds)
            emit(1) // t=50, dropped
            delay(51.milliseconds)
            emit(2) // t=101, emitted, window until 201
            delay(19.milliseconds)
            emit(3) // t=120, dropped
            delay(180.milliseconds)
            emit(4) // t=300, emitted
        }.throttleFirst(window = 100.milliseconds).toList()

        assertEquals(listOf(0, 2, 4), values)
    }

    @Test(timeout = 20_000)
    fun `throttleFirst completes when upstream completes instead of waiting out the window`() = runTest {
        val values = flow { emit(1) }.throttleFirst(window = 10.seconds).toList()

        assertEquals(listOf(1), values)
        assertEquals(0, currentTime)
    }

    @Test(timeout = 20_000)
    fun `throttleFirst propagates upstream failure`() = runTest {
        val failure = runCatching {
            flow {
                emit(1)
                throw IllegalStateException("upstream")
            }.throttleFirst(window = 100.milliseconds).toList()
        }.exceptionOrNull()

        assertEquals("upstream", failure?.message)
    }

    @Test(timeout = 20_000)
    fun `WhileUiSubscribed is the shared five second WhileSubscribed policy`() {
        assertEquals(SharingStarted.WhileSubscribed(5_000), WhileUiSubscribed)
    }

    @Test(timeout = 20_000)
    fun `stateInWhileSubscribed exposes the initial value and does not start without subscribers`() = runTest {
        var starts = 0
        val state = flow {
            starts += 1
            emit(1)
            awaitCancellation()
        }.stateInWhileSubscribed(backgroundScope, initialValue = 0)

        runCurrent()
        advanceTimeBy(10.seconds)
        runCurrent()

        assertEquals(0, state.value)
        assertEquals(0, starts)
    }

    @Test(timeout = 20_000)
    fun `upstream stops five seconds after the last subscriber leaves and restarts on resubscribe`() = runTest {
        var starts = 0
        var active = 0
        val state = flow {
            starts += 1
            active += 1
            try {
                emit(starts)
                awaitCancellation()
            } finally {
                active -= 1
            }
        }.stateInWhileSubscribed(backgroundScope, initialValue = 0)

        val first = launch { state.collect { } }
        runCurrent()
        assertEquals(1, starts)
        assertEquals(1, active)
        assertEquals(1, state.value)

        first.cancel()
        runCurrent()
        advanceTimeBy(4.seconds)
        runCurrent()
        assertEquals("still inside the stop timeout", 1, active)

        advanceTimeBy(2.seconds) // t = 6 s after the last subscriber left
        runCurrent()
        assertEquals(0, active)
        assertEquals("the last value is retained while stopped", 1, state.value)

        val second = launch { state.collect { } }
        runCurrent()
        assertEquals(2, starts)
        assertEquals(1, active)
        assertEquals(2, state.value)
        second.cancel()
        runCurrent()
    }

    @Test(timeout = 20_000)
    fun `resubscribing inside the stop timeout keeps the same upstream running`() = runTest {
        var starts = 0
        val state = flow {
            starts += 1
            emit(starts)
            awaitCancellation()
        }.stateInWhileSubscribed(backgroundScope, initialValue = 0)

        val first = launch { state.collect { } }
        runCurrent()
        first.cancel()
        runCurrent()
        advanceTimeBy(3.seconds)
        runCurrent()

        val second = launch { state.collect { } }
        runCurrent()
        advanceTimeBy(30.seconds)
        runCurrent()

        assertEquals(1, starts)
        assertEquals(1, state.value)
        second.cancel()
        runCurrent()
    }

    @Test(timeout = 20_000)
    fun `a custom stop timeout is honored`() = runTest {
        var active = 0
        val state = flow {
            active += 1
            try {
                emit(1)
                awaitCancellation()
            } finally {
                active -= 1
            }
        }.stateInWhileSubscribed(backgroundScope, initialValue = 0, stopTimeout = 1.seconds)

        val collector = launch { state.collect { } }
        runCurrent()
        assertEquals(1, active)

        collector.cancel()
        runCurrent()
        advanceTimeBy(2.seconds)
        runCurrent()

        assertTrue("upstream should have stopped after the 1 s timeout", active == 0)
    }
}
