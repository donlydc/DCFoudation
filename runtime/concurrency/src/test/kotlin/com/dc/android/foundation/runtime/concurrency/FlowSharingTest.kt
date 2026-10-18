package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class FlowSharingTest {

    @Test
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
}
