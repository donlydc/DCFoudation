package com.dc.android.foundation.observability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Contract tests for [LoggingAnalyticsTracker] (OBS-01/OBS-02) using a local [FakeLogger]; no Logcat. */
class LoggingAnalyticsTrackerTest {

    private val logger = FakeLogger()
    private val tracker: AnalyticsTracker = LoggingAnalyticsTracker(logger)

    @Test
    fun trackEvent_forwardsSingleDebugEntry_withAnalyticsTag() {
        tracker.trackEvent("article_opened", mapOf("id" to "42"))

        assertEquals(1, logger.entries.size)
        val entry = logger.entries.single()
        assertEquals(FakeLogger.Level.DEBUG, entry.level)
        assertEquals("Analytics", entry.tag)
        assertEquals(null, entry.throwable)
    }

    @Test
    fun trackEvent_message_containsNameAndProperties() {
        tracker.trackEvent("article_opened", mapOf("id" to "42", "source" to "list"))

        assertEquals(
            "trackEvent: name=article_opened properties={id=42, source=list}",
            logger.entries.single().message
        )
    }

    @Test
    fun trackEvent_withoutProperties_usesEmptyMap() {
        tracker.trackEvent("app_started")

        assertEquals("trackEvent: name=app_started properties={}", logger.entries.single().message)
    }

    @Test
    fun trackEvent_emptyName_isStillForwarded() {
        tracker.trackEvent("")

        assertEquals("trackEvent: name= properties={}", logger.entries.single().message)
    }

    @Test
    fun trackEvent_neverLogsAtErrorLevel() {
        tracker.trackEvent("a")
        tracker.trackEvent("b", mapOf("k" to "v"))

        assertTrue(logger.entries.none { it.level == FakeLogger.Level.ERROR })
    }

    @Test
    fun trackEvent_preservesCallOrder() {
        tracker.trackEvent("first")
        tracker.trackEvent("second")
        tracker.trackEvent("third")

        assertEquals(
            listOf(
                "trackEvent: name=first properties={}",
                "trackEvent: name=second properties={}",
                "trackEvent: name=third properties={}"
            ),
            logger.entries.map { it.message }
        )
    }

    @Test
    fun trackEvent_doesNotMutateCallerProperties() {
        val properties = mutableMapOf("k" to "v")

        tracker.trackEvent("event", properties)

        assertEquals(mapOf("k" to "v"), properties)
    }
}
