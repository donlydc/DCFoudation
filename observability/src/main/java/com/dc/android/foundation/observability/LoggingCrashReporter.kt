package com.dc.android.foundation.observability

private const val LOG_TAG = "CrashReporter"

/**
 * Placeholder [CrashReporter] implementation (KD-05 — no crash-reporting vendor has been chosen
 * yet). Forwards to [Logger.e]/[Logger.d] — matches `AndroidLogger.e()`'s own note that it "is
 * where reporting would hook in." Replace with a real vendor-backed implementation behind the
 * same [CrashReporter] interface once a vendor decision is made — no call site should need to
 * change.
 *
 * Breadcrumbs pass through [Redactor] (MP:SEC-04); [recordException] logs only the exception
 * class name as its message (the throwable itself is still forwarded to the [Logger]). Callers
 * must still never pass a secret/token/PII/full content body (SEC-01).
 *
 * Error isolation (MP:OBS-02, PROPOSED): a failure in the [Logger] is swallowed so reporting a
 * crash can never cause another crash.
 */
class LoggingCrashReporter(private val logger: Logger) : CrashReporter {
    override fun recordException(throwable: Throwable) {
        try {
            logger.e(LOG_TAG, "recordException: ${throwable::class.simpleName}", throwable)
        } catch (_: Exception) {
            // Intentionally swallowed (OBS-02).
        }
    }

    override fun log(message: String) {
        try {
            logger.d(LOG_TAG, Redactor.redact(message))
        } catch (_: Exception) {
            // Intentionally swallowed (OBS-02).
        }
    }
}
