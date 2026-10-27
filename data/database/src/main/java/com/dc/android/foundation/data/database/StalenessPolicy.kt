package com.dc.android.foundation.data.database

import kotlin.time.Duration

/**
 * GENERIC (reusable, schema-neutral). Freshness policy for offline-first repositories (DAT-CACHE-01).
 *
 * The clock is injected per call: [isStale] takes `nowMillis` and never reads a system clock, so
 * tests pass literal times. Semantics:
 * - `null` timestamp (never fetched) is stale.
 * - A timestamp later than `nowMillis` (negative clock skew) is treated as fresh, never stale.
 * - Otherwise stale when `now - lastUpdated >= maxAge` (the TTL boundary itself is stale).
 * - `maxAge` is compared in whole milliseconds (sub-millisecond values truncate); negative is rejected.
 */
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
