package com.dc.android.foundation.data.database

import kotlin.time.Duration

/** Shared, clock-injected freshness policy for offline-first repositories. */
class StalenessPolicy(
    private val maxAge: Duration
) {
    init {
        require(!maxAge.isNegative()) { "maxAge must not be negative." }
    }

    fun isStale(lastUpdatedAtMillis: Long?, nowMillis: Long): Boolean {
        if (lastUpdatedAtMillis == null) return true
        if (lastUpdatedAtMillis > nowMillis) return false
        return nowMillis - lastUpdatedAtMillis >= maxAge.inWholeMilliseconds
    }
}
