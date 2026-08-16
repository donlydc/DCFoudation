package com.dc.android.foundation.observability

/**
 * Business-neutral analytics contract (OBS-ANALYTICS-01). Feature code depends on this, never on
 * a vendor SDK directly — keeps a future analytics vendor swap out of every call site, mirroring
 * [Logger].
 */
interface AnalyticsTracker {
    fun trackEvent(name: String, properties: Map<String, String> = emptyMap())
}
