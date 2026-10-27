package com.dc.android.foundation.observability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * MP:SEC-04 (no secret in logs/crash/analytics sinks) and MP:OBS-02 (a throwing [Logger] must not
 * crash the caller). Uses the local [FakeLogger]/[ThrowingLogger]; no Logcat.
 */
class SecretLeakAndIsolationTest {

    private val secrets = listOf("hunter2-pass", "tok_live_ABC123", "jane.doe@example.com", "sk-SECRETKEY")

    @Test
    fun analytics_neverLogsSecretsFromPropertiesOrName() {
        val logger = FakeLogger()
        LoggingAnalyticsTracker(logger).trackEvent(
            "login_for_jane.doe@example.com",
            mapOf(
                "password" to "hunter2-pass",
                "access_token" to "tok_live_ABC123",
                "note" to "api_key=sk-SECRETKEY",
                "article_id" to "42"
            )
        )

        val message = logger.entries.single().message
        secrets.forEach { assertFalse("leaked $it in: $message", message.contains(it)) }
        assertTrue(message.contains("article_id=42"))
    }

    @Test
    fun crashBreadcrumb_neverLogsSecrets() {
        val logger = FakeLogger()
        LoggingCrashReporter(logger).log("retry with Authorization: Bearer tok_live_ABC123 for jane.doe@example.com")

        val message = logger.entries.single().message
        secrets.forEach { assertFalse("leaked $it in: $message", message.contains(it)) }
    }

    @Test
    fun crashRecordException_messageHasNoThrowableMessage() {
        val logger = FakeLogger()
        LoggingCrashReporter(logger).recordException(RuntimeException("password=hunter2-pass"))

        assertFalse(logger.entries.single().message.contains("hunter2-pass"))
    }

    @Test
    fun analytics_throwingLogger_doesNotPropagate() {
        val logger = ThrowingLogger()

        LoggingAnalyticsTracker(logger).trackEvent("e", mapOf("k" to "v"))

        assertEquals(1, logger.calls)
    }

    @Test
    fun crash_throwingLogger_doesNotPropagate_fromRecordOrLog() {
        val logger = ThrowingLogger()
        val reporter = LoggingCrashReporter(logger)

        reporter.recordException(IllegalStateException("boom"))
        reporter.log("breadcrumb")

        assertEquals(2, logger.calls)
    }

    @Test
    fun analytics_afterLoggerFailure_laterCallsStillAttempted() {
        val logger = ThrowingLogger()
        val tracker = LoggingAnalyticsTracker(logger)

        tracker.trackEvent("a")
        tracker.trackEvent("b")

        assertEquals(2, logger.calls)
    }
}
