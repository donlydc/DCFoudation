package com.dc.android.foundation.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** SAMPLE (D-01 open, not generic). Reads are a cold [Flow], writes are `suspend` — same read/write shape as `PreferencesDataSource`. */
@Dao
interface ArticleDao {

    @Query("SELECT * FROM articles ORDER BY id ASC")
    fun observeArticles(): Flow<List<ArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles")
    suspend fun clear()
}
