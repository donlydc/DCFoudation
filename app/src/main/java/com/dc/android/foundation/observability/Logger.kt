package com.dc.android.foundation.observability

/**
 * Business-neutral logging contract. Feature code depends on this, never on `android.util.Log`
 * or a vendor SDK directly (OBS-01) — keeps a future analytics/crash vendor swap out of every
 * call site.
 */
interface Logger {
    fun d(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}
