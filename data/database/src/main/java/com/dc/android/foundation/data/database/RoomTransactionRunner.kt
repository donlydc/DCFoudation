package com.dc.android.foundation.data.database

import androidx.room.RoomDatabase
import androidx.room.withTransaction

/** Keeps Room's transaction API at the local-data boundary instead of repository call sites. */
interface TransactionRunner {
    suspend fun <T> inTransaction(block: suspend () -> T): T
}

class RoomTransactionRunner(
    private val database: RoomDatabase
) : TransactionRunner {
    override suspend fun <T> inTransaction(block: suspend () -> T): T =
        database.withTransaction { block() }
}
