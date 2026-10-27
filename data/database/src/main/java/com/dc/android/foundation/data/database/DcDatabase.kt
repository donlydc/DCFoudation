package com.dc.android.foundation.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * SAMPLE schema (not generic): exposes only the `articles` reference table. Ownership is open
 * (D-01, see reports/wp/B3-design.md); do not add unrelated entities. Generic parts are
 * [RoomDatabaseFactory], [TransactionRunner] and [StalenessPolicy].
 *
 * App-wide Room database (DAT-ROOM-01), named to match `DcApplication`/`DcTheme` convention.
 */
@Database(entities = [ArticleEntity::class], version = 1, exportSchema = true)
abstract class DcDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
}
