package com.dc.android.foundation.articles

import kotlinx.serialization.Serializable

/**
 * Wire shape for JSONPlaceholder's `/posts` endpoint. Feature-specific, not business-neutral
 * (DAT-01), so it lives here rather than in `:data:network` — mirrors how
 * `GreetingPreferencesRepository` carries feature vocabulary while `PreferencesDataSource` stays
 * generic. Never exposed past [ArticlesNetworkDataSource]/[ArticlesRepository] (INV-07).
 */
@Serializable
data class ArticleDto(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)
