package com.dc.android.foundation.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * SAMPLE (D-01 open, not generic). Room row for the `articles` reference feature (DAT-ROOM-01). Mirrors the JSONPlaceholder
 * `/posts` shape plus [fetchedAt] bookkeeping for staleness (DAT-CACHE-01). Never exposed past
 * the owning repository's mapping boundary (DAT-01, INV-07) — the `articles` feature maps this
 * to its own UI model.
 */
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: Int,
    val authorId: Int,
    val title: String,
    val body: String,
    val fetchedAt: Long
)
