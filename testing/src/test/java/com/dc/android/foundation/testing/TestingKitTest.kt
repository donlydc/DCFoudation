package com.dc.android.foundation.testing

import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class TestingKitTest {

    @Test
    fun `recording observability fakes capture calls without exposing mutable storage`() {
        val logger = RecordingLogger()
        val analytics = RecordingAnalyticsTracker()
        val crashReporter = RecordingCrashReporter()
        val failure = IllegalStateException("failure")
        val properties = mutableMapOf("source" to "unit-test")

        logger.d("Test", "started")
        logger.e("Test", "failed", failure)
        analytics.trackEvent("completed", properties)
        crashReporter.log("before failure")
        crashReporter.recordException(failure)
        properties["source"] = "mutated-after-call"

        assertEquals(2, logger.records.size)
        assertSame(failure, logger.records.last().throwable)
        assertEquals(listOf(AnalyticsEvent("completed", mapOf("source" to "unit-test"))), analytics.events)
        assertEquals(listOf(failure), crashReporter.recordedExceptions)
        assertEquals(listOf("before failure"), crashReporter.loggedMessages)
    }

    @Test
    fun `fake dispatcher provider shares one dispatcher across all slots`() {
        val dispatcher = Dispatchers.Unconfined
        val provider = FakeDispatcherProvider(dispatcher)

        assertSame(dispatcher, provider.main)
        assertSame(dispatcher, provider.mainImmediate)
        assertSame(dispatcher, provider.io)
        assertSame(dispatcher, provider.default)
    }
}
