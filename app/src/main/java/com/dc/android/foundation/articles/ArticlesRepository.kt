package com.dc.android.foundation.articles

import com.dc.android.foundation.data.database.ArticleDao
import com.dc.android.foundation.data.database.ArticleEntity
import com.dc.android.foundation.data.network.NetworkResult
import com.dc.android.foundation.observability.CrashReporter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Feature-owned UI/domain model — never [ArticleEntity]/[ArticleDto] past this boundary (DAT-01, INV-07). */
data class Article(
    val id: Int,
    val authorId: Int,
    val title: String,
    val body: String
)

/**
 * Mapping boundary (DAT-01) and the real instance of DAT-CACHE-01: the ONE place that combines a
 * `RemoteDataSource` ([ArticlesNetworkDataSource]) with a `LocalDataSource` ([ArticleDao]).
 *
 * Offline-first policy:
 * - Room is the source of truth. [observeArticles] emits cached rows immediately and keeps
 *   emitting as the table changes — callers never wait on the network to see something.
 * - [refresh] triggers a network fetch. On success the response is upserted into Room, which
 *   [observeArticles] then re-emits through automatically — no separate "apply the response"
 *   step. On failure, cached rows are left untouched and `refresh` returns `false` so the
 *   ViewModel can surface a "showing cached data, refresh failed" signal instead of an error
 *   state or an empty screen.
 */
class ArticlesRepository @Inject constructor(
    private val networkDataSource: ArticlesNetworkDataSource,
    private val articleDao: ArticleDao,
    private val crashReporter: CrashReporter
) {
    fun observeArticles(): Flow<List<Article>> =
        articleDao.observeArticles().map { entities -> entities.map { it.toArticle() } }

    /** Returns `true` on a successful refresh, `false` if the network call failed (stale cache kept). */
    suspend fun refresh(): Boolean =
        when (val result = networkDataSource.fetchArticles()) {
            is NetworkResult.Success -> {
                articleDao.upsertAll(result.data.map { it.toEntity() })
                true
            }
            is NetworkResult.Failure -> {
                // Already-existing ERR-01 error boundary (NetworkResult.Failure) — recording the
                // exception here is an observability side-effect only, it does not change the
                // boundary's contract (refresh() still just returns false).
                crashReporter.recordException(result.throwable)
                false
            }
        }
}

private fun ArticleEntity.toArticle() = Article(id = id, authorId = authorId, title = title, body = body)

private fun ArticleDto.toEntity() = ArticleEntity(
    id = id,
    authorId = userId,
    title = title,
    body = body,
    fetchedAt = System.currentTimeMillis()
)
