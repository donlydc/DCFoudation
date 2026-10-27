package com.dc.android.foundation.observability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/** Contract tests for [LoggingCrashReporter] (OBS-01/OBS-02) using a local [FakeLogger]; no Logcat. */
class LoggingCrashReporterTest {

    private val logger = FakeLogger()
    private val reporter: CrashReporter = LoggingCrashReporter(logger)

    @Test
    fun recordException_logsAtErrorLevel_withCrashReporterTag() {
        reporter.recordException(IllegalStateException("boom"))

        val entry = logger.entries.single()
        assertEquals(FakeLogger.Level.ERROR, entry.level)
        assertEquals("CrashReporter", entry.tag)
    }

    @Test
    fun recordException_passesSameThrowableInstance() {
        val failure = IllegalArgumentException("bad input")

        reporter.recordException(failure)

        assertSame(failure, logger.entries.single().throwable)
    }

    @Test
    fun recordException_message_containsOnlyExceptionClassName() {
        reporter.recordException(IllegalStateException("boom"))

        assertEquals("recordException: IllegalStateException", logger.entries.single().message)
    }

    @Test
    fun recordException_message_doesNotLeakExceptionMessage() {
        reporter.recordException(RuntimeException("token=super-secret"))

        assertFalse(logger.entries.single().message.contains("super-secret"))
    }

    @Test
    fun log_forwardsBreadcrumbAtDebugLevel() {
        reporter.log("user opened article list")

        val entry = logger.entries.single()
        assertEquals(FakeLogger.Level.DEBUG, entry.level)
        assertEquals("CrashReporter", entry.tag)
        assertEquals("user opened article list", entry.message)
        assertNull(entry.throwable)
    }

    @Test
    fun breadcrumbThenException_areLoggedInOrder() {
        reporter.log("step 1")
        reporter.recordException(IllegalStateException("boom"))
        reporter.log("step 2")

        assertEquals(
            listOf(FakeLogger.Level.DEBUG, FakeLogger.Level.ERROR, FakeLogger.Level.DEBUG),
            logger.entries.map { it.level }
        )
    }
}
