package com.dc.android.foundation.observability

import android.util.Log

/**
 * `d()` is gated on [isDebug] (never spams a release logcat); `e()` always logs — swallowing a
 * real error in release would be worse than a noisy logcat. Never pass a secret/token/PII as
 * [message] (SEC-01) — this class does no redaction, the caller is responsible.
 */
class AndroidLogger(private val isDebug: Boolean) : Logger {
    override fun d(tag: String, message: String) {
        if (isDebug) Log.d(tag, message)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}
