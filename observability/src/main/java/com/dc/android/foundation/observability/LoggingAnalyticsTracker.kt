package com.dc.android.foundation.observability

private const val LOG_TAG = "Analytics"

/**
 * Placeholder [AnalyticsTracker] implementation (KD-05 — no analytics vendor has been chosen
 * yet). Forwards every event to [Logger.d] so it is at least visible in logcat during
 * development, rather than doing literally nothing. Replace with a real vendor-backed
 * implementation behind the same [AnalyticsTracker] interface once a vendor decision is made —
 * no call site should need to change.
 *
 * Event name and properties pass through [Redactor] before logging (MP:SEC-04) — a safety net
 * only; callers must still never pass a secret/token/PII/full content body (SEC-01).
 *
 * Error isolation (MP:OBS-02, PROPOSED): analytics is best-effort telemetry, so a failure in the
 * [Logger] (or in rendering the properties) is swallowed and never reaches the caller.
 */
class LoggingAnalyticsTracker(private val logger: Logger) : AnalyticsTracker {
    override fun trackEvent(name: String, properties: Map<String, String>) {
        try {
            logger.d(
                LOG_TAG,
                "trackEvent: name=${Redactor.redact(name)} properties=${Redactor.redactProperties(properties)}"
            )
        } catch (_: Exception) {
            // Intentionally swallowed: telemetry must never crash the app (OBS-02).
        }
    }
}
