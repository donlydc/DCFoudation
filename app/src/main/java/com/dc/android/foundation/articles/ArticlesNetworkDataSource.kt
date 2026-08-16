package com.dc.android.foundation.articles

import com.dc.android.foundation.data.network.NetworkResult
import com.dc.android.foundation.data.network.safeApiCall
import javax.inject.Inject

/**
 * Data-source boundary for the `articles` feature (ERR-01, DAT-NET-01) — the only place that
 * touches [ArticlesApiService] directly. Infra failures are already caught and mapped to
 * [NetworkResult] by `safeApiCall` in `:data:network`; nothing past this point ever sees a raw
 * `IOException`/`HttpException` (INV-07, DEP-03).
 */
interface ArticlesNetworkDataSource {
    suspend fun fetchArticles(): NetworkResult<List<ArticleDto>>
}

class DefaultArticlesNetworkDataSource @Inject constructor(
    private val apiService: ArticlesApiService
) : ArticlesNetworkDataSource {
    override suspend fun fetchArticles(): NetworkResult<List<ArticleDto>> =
        safeApiCall { apiService.getPosts() }
}
