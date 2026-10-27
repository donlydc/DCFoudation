package com.dc.android.foundation.runtime.concurrency

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import kotlin.coroutines.ContinuationInterceptor

@OptIn(ExperimentalCoroutinesApi::class)
class DispatcherShortcutsTest {

    private class Slots(
        override val io: CoroutineDispatcher,
        override val default: CoroutineDispatcher,
        override val main: CoroutineDispatcher,
    ) : DispatcherProvider {
        override val mainImmediate: CoroutineDispatcher = main
    }

    private fun TestScope.slots() = Slots(
        io = StandardTestDispatcher(testScheduler, "io"),
        default = StandardTestDispatcher(testScheduler, "default"),
        main = StandardTestDispatcher(testScheduler, "main"),
    )

    @Test(timeout = 20_000)
    fun `onIo runs on the io slot and returns the block result`() = runTest {
        val dispatchers = slots()

        val interceptor = dispatchers.onIo { currentCoroutineContext()[ContinuationInterceptor] }
        val value = dispatchers.onIo { 42 }

        assertSame(dispatchers.io, interceptor)
        assertEquals(42, value)
    }

    @Test(timeout = 20_000)
    fun `onDefault runs on the default slot`() = runTest {
        val dispatchers = slots()

        val interceptor = dispatchers.onDefault { currentCoroutineContext()[ContinuationInterceptor] }

        assertSame(dispatchers.default, interceptor)
    }

    @Test(timeout = 20_000)
    fun `flowOnIo moves upstream onto the io slot`() = runTest {
        val dispatchers = slots()

        val upstreamInterceptor = flow { emit(currentCoroutineContext()[ContinuationInterceptor]) }
            .flowOnIo(dispatchers)
            .first()

        assertSame(dispatchers.io, upstreamInterceptor)
    }

    @Test(timeout = 20_000)
    fun `exceptions from the block propagate to the caller`() = runTest {
        val failure = runCatching { slots().onIo<Unit> { error("io failed") } }.exceptionOrNull()

        assertEquals("io failed", failure?.message)
    }
}
