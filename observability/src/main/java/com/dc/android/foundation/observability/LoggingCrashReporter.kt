package com.dc.android.foundation.observability

private const val LOG_TAG = "CrashReporter"

/**
 * Placeholder [CrashReporter] implementation (KD-05 — no crash-reporting vendor has been chosen
 * yet). Forwards to [Logger.e]/[Logger.d] — matches `AndroidLogger.e()`'s own note that it "is
 * where reporting would hook in." Replace with a real vendor-backed implementation behind the
 * same [CrashReporter] interface once a vendor decision is made — no call site should need to
 * change.
 *
 * Never pass a secret/token/PII/full content body to [recordException]/[log] (SEC-01) — this
 * class does no redaction, the caller is responsible.
 */
class LoggingCrashReporter(private val logger: Logger) : CrashReporter {
    override fun recordException(throwable: Throwable) {
        logger.e(LOG_TAG, "recordException: ${throwable::class.simpleName}", throwable)
    }

    override fun log(message: String) {
        logger.d(LOG_TAG, message)
    }
}
