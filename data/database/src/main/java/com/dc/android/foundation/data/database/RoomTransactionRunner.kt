package com.dc.android.foundation.data.database

import androidx.room.RoomDatabase
import androidx.room.withTransaction

/**
 * GENERIC. Keeps Room's transaction API at the local-data boundary instead of repository call sites.
 *
 * Contract for implementations: [inTransaction] runs [block] atomically; it commits when [block]
 * returns, rolls back and rethrows when [block] throws (including `CancellationException`), and
 * returns [block]'s value.
 */
interface TransactionRunner {
    suspend fun <T> inTransaction(block: suspend () -> T): T
}

/**
 * GENERIC Room-backed [TransactionRunner]. Commit/rollback/cancellation semantics are Room's
 * `withTransaction` (runs on the database's transaction executor, i.e. `DispatcherProvider.io`
 * when built via [RoomDatabaseFactory]). Verifying them needs a real `RoomDatabase`
 * (Robolectric or androidTest), which this module does not configure; see reports/wp/B3.md.
 */
class RoomTransactionRunner(
    private val database: RoomDatabase
) : TransactionRunner {
    override suspend fun <T> inTransaction(block: suspend () -> T): T =
        database.withTransaction { block() }
}
