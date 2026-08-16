package com.dc.android.foundation.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

/** App-wide Room database (DAT-ROOM-01), named to match `DcApplication`/`DcTheme` convention. */
@Database(entities = [ArticleEntity::class], version = 1, exportSchema = false)
abstract class DcDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
}
