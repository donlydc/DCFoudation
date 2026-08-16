package com.dc.android.foundation.articles

import androidx.lifecycle.SavedStateHandle
import com.dc.android.foundation.MainDispatcherRule
import com.dc.android.foundation.data.database.ArticleDao
import com.dc.android.foundation.data.database.ArticleEntity
import com.dc.android.foundation.data.network.NetworkResult
import com.dc.android.foundation.observability.Logger
import com.dc.android.foundation.runtime.concurrency.FakeDispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Gate covering the second reference feature's offline-first cache policy (DAT-CACHE-01). */
@OptIn(ExperimentalCoroutinesApi::class)
class ArticlesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeDispatcherProvider = FakeDispatcherProvider(Dispatchers.Main)

    private fun createViewModel(
        dao: FakeArticleDao = FakeArticleDao(),
        networkDataSource: FakeArticlesNetworkDataSource = FakeArticlesNetworkDataSource()
    ) = ArticlesViewModel(
        ArticlesRepository(networkDataSource, dao),
        fakeDispatcherProvider,
        NoOpLogger,
        SavedStateHandle()
    )

    @Test
    fun `initial state shows cached data from Room`() = runTest {
        val dao = FakeArticleDao(
            initial = listOf(ArticleEntity(id = 1, authorId = 1, title = "Cached", body = "Body", fetchedAt = 0L))
        )
        val viewModel = createViewModel(dao = dao)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.articles.size)
        assertEquals("Cached", viewModel.uiState.value.articles.single().title)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertFalse(viewModel.uiState.value.isShowingStaleData)
    }

    @Test
    fun `Refresh success upserts network data into Room and clears the stale flag`() = runTest {
        val networkDataSource = FakeArticlesNetworkDataSource(
            result = NetworkResult.Success(
                listOf(ArticleDto(id = 2, userId = 5, title = "Fresh", body = "Body"))
            )
        )
        val viewModel = createViewModel(networkDataSource = networkDataSource)

        viewModel.onAction(ArticlesUiAction.Refresh)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.articles.size)
        assertEquals("Fresh", viewModel.uiState.value.articles.single().title)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertFalse(viewModel.uiState.value.isShowingStaleData)
    }

    @Test
    fun `Refresh failure keeps cached data and sets the stale flag`() = runTest {
        val dao = FakeArticleDao(
            initial = listOf(ArticleEntity(id = 1, authorId = 1, title = "Cached", body = "Body", fetchedAt = 0L))
        )
        val networkDataSource = FakeArticlesNetworkDataSource(
            result = NetworkResult.Failure(java.io.IOException("offline"))
        )
        val viewModel = createViewModel(dao = dao, networkDataSource = networkDataSource)

        viewModel.onAction(ArticlesUiAction.Refresh)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.articles.size)
        assertEquals("Cached", viewModel.uiState.value.articles.single().title)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertTrue(viewModel.uiState.value.isShowingStaleData)
    }

    @Test
    fun `Refresh failure emits a ShowMessage effect describing the stale state`() = runTest {
        val networkDataSource = FakeArticlesNetworkDataSource(
            result = NetworkResult.Failure(java.io.IOException("offline"))
        )
        val viewModel = createViewModel(networkDataSource = networkDataSource)
        val effects = mutableListOf<ArticlesUiEffect>()
        val collector = launch { viewModel.effects.toList(effects) }

        viewModel.onAction(ArticlesUiAction.Refresh)
        advanceUntilIdle()

        assertEquals(1, effects.size)
        assertEquals(
            "Refresh failed, showing cached data",
            (effects.single() as ArticlesUiEffect.ShowMessage).text
        )
        collector.cancel()
    }
}

private object NoOpLogger : Logger {
    override fun d(tag: String, message: String) = Unit
    override fun e(tag: String, message: String, throwable: Throwable?) = Unit
}

private class FakeArticleDao(
    initial: List<ArticleEntity> = emptyList()
) : ArticleDao {
    private val state = MutableStateFlow(initial)

    override fun observeArticles(): Flow<List<ArticleEntity>> = state

    override suspend fun upsertAll(articles: List<ArticleEntity>) {
        val byId = state.value.associateBy { it.id }.toMutableMap()
        articles.forEach { byId[it.id] = it }
        state.value = byId.values.sortedBy { it.id }
    }

    override suspend fun clear() {
        state.value = emptyList()
    }
}

private class FakeArticlesNetworkDataSource(
    private val result: NetworkResult<List<ArticleDto>> = NetworkResult.Success(emptyList())
) : ArticlesNetworkDataSource {
    override suspend fun fetchArticles(): NetworkResult<List<ArticleDto>> = result
}
