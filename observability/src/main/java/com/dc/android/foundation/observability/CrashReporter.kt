package com.dc.android.foundation.observability

/**
 * Business-neutral crash/error reporting contract (OBS-CRASH-01). Feature code depends on this,
 * never on a vendor SDK directly — keeps a future crash-reporting vendor swap out of every call
 * site, mirroring [Logger].
 */
interface CrashReporter {
    fun recordException(throwable: Throwable)

    /** Breadcrumb-style log line, for context leading up to a later [recordException] call. */
    fun log(message: String)
}
