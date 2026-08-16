package com.dc.android.foundation.articles

import retrofit2.http.GET

/**
 * Retrofit contract for the JSONPlaceholder articles endpoint. Feature-specific (DAT-01) — only
 * [ArticlesNetworkDataSource] talks to this directly; no Composable/ViewModel ever imports it
 * (DEP-03).
 */
interface ArticlesApiService {
    @GET("posts")
    suspend fun getPosts(): List<ArticleDto>
}
