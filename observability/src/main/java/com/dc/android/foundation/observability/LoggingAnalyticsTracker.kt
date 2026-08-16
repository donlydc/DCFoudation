package com.dc.android.foundation.observability

private const val LOG_TAG = "Analytics"

/**
 * Placeholder [AnalyticsTracker] implementation (KD-05 — no analytics vendor has been chosen
 * yet). Forwards every event to [Logger.d] so it is at least visible in logcat during
 * development, rather than doing literally nothing. Replace with a real vendor-backed
 * implementation behind the same [AnalyticsTracker] interface once a vendor decision is made —
 * no call site should need to change.
 *
 * Never pass a secret/token/PII/full content body in [AnalyticsTracker.trackEvent]'s properties
 * (SEC-01) — this class does no redaction, the caller is responsible.
 */
class LoggingAnalyticsTracker(private val logger: Logger) : AnalyticsTracker {
    override fun trackEvent(name: String, properties: Map<String, String>) {
        logger.d(LOG_TAG, "trackEvent: name=$name properties=$properties")
    }
}
