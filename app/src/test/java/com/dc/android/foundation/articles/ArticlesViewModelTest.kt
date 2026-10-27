package com.dc.android.foundation.articles

import com.dc.android.foundation.R
import com.dc.android.foundation.core.AppError
import com.dc.android.foundation.data.database.ArticleDao
import com.dc.android.foundation.data.database.ArticleEntity
import com.dc.android.foundation.data.network.NetworkResult
import com.dc.android.foundation.platform.permission.NotificationPermission
import com.dc.android.foundation.platform.permission.PermissionState
import com.dc.android.foundation.testing.FakeDispatcherProvider
import com.dc.android.foundation.testing.MainDispatcherRule
import com.dc.android.foundation.testing.NoOpLogger
import com.dc.android.foundation.testing.RecordingAnalyticsTracker
import com.dc.android.foundation.testing.RecordingCrashReporter
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
        networkDataSource: FakeArticlesNetworkDataSource = FakeArticlesNetworkDataSource(),
        crashReporter: RecordingCrashReporter = RecordingCrashReporter(),
        analyticsTracker: RecordingAnalyticsTracker = RecordingAnalyticsTracker()
    ) = ArticlesViewModel(
        ArticlesRepository(networkDataSource, dao, crashReporter),
        fakeDispatcherProvider,
        NoOpLogger,
        analyticsTracker
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
    fun `Refresh success tracks an articles_refreshed analytics event`() = runTest {
        val networkDataSource = FakeArticlesNetworkDataSource(
            result = NetworkResult.Success(
                listOf(ArticleDto(id = 2, userId = 5, title = "Fresh", body = "Body"))
            )
        )
        val analyticsTracker = RecordingAnalyticsTracker()
        val viewModel = createViewModel(networkDataSource = networkDataSource, analyticsTracker = analyticsTracker)

        viewModel.onAction(ArticlesUiAction.Refresh)
        advanceUntilIdle()

        assertEquals(1, analyticsTracker.events.size)
        assertEquals("articles_refreshed", analyticsTracker.events.single().name)
    }

    @Test
    fun `Refresh failure records the exception with the crash reporter and does not track analytics`() = runTest {
        val failure = java.io.IOException("offline")
        val networkDataSource = FakeArticlesNetworkDataSource(
            result = NetworkResult.Failure(AppError.Connectivity(failure))
        )
        val crashReporter = RecordingCrashReporter()
        val analyticsTracker = RecordingAnalyticsTracker()
        val viewModel = createViewModel(
            networkDataSource = networkDataSource,
            crashReporter = crashReporter,
            analyticsTracker = analyticsTracker
        )

        viewModel.onAction(ArticlesUiAction.Refresh)
        advanceUntilIdle()

        assertEquals(listOf(failure), crashReporter.recordedExceptions)
        assertTrue(analyticsTracker.events.isEmpty())
    }

    @Test
    fun `Refresh failure keeps cached data and sets the stale flag`() = runTest {
        val dao = FakeArticleDao(
            initial = listOf(ArticleEntity(id = 1, authorId = 1, title = "Cached", body = "Body", fetchedAt = 0L))
        )
        val networkDataSource = FakeArticlesNetworkDataSource(
            result = NetworkResult.Failure(AppError.Connectivity(java.io.IOException("offline")))
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
    fun `Refresh failure exposes a user message until acknowledged`() = runTest {
        val networkDataSource = FakeArticlesNetworkDataSource(
            result = NetworkResult.Failure(AppError.Connectivity(java.io.IOException("offline")))
        )
        val viewModel = createViewModel(networkDataSource = networkDataSource)
        viewModel.onAction(ArticlesUiAction.Refresh)
        advanceUntilIdle()

        assertEquals(R.string.error_connectivity, viewModel.uiState.value.userMessage)

        viewModel.onAction(ArticlesUiAction.UserMessageShown)

        assertEquals(null, viewModel.uiState.value.userMessage)
    }

    @Test
    fun `Refresh success exposes the refreshed message`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ArticlesUiAction.Refresh)
        advanceUntilIdle()

        assertEquals(R.string.articles_refreshed, viewModel.uiState.value.userMessage)
    }

    @Test
    fun `Refresh failure maps the AppError variant to its message`() = runTest {
        val viewModel = createViewModel(
            networkDataSource = FakeArticlesNetworkDataSource(
                result = NetworkResult.Failure(AppError.Http(code = 503))
            )
        )
        viewModel.onAction(ArticlesUiAction.Refresh)
        advanceUntilIdle()

        assertEquals(R.string.error_http, viewModel.uiState.value.userMessage)
    }

    // Journey 1 (data flow): network -> Room -> UI; see reports/wp/C2.md.
    @Test
    fun `journey offline then online - stale cache is shown then replaced by fresh data`() = runTest {
        val dao = FakeArticleDao(
            initial = listOf(ArticleEntity(id = 1, authorId = 1, title = "Cached", body = "Body", fetchedAt = 0L))
        )
        val network = MutableFakeArticlesNetworkDataSource(
            NetworkResult.Failure(AppError.Connectivity(java.io.IOException("offline")))
        )
        val viewModel = ArticlesViewModel(
            ArticlesRepository(network, dao, RecordingCrashReporter()),
            fakeDispatcherProvider,
            NoOpLogger,
            RecordingAnalyticsTracker()
        )
        advanceUntilIdle()

        viewModel.onAction(ArticlesUiAction.Refresh)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isShowingStaleData)
        assertEquals("Cached", viewModel.uiState.value.articles.single().title)

        network.result = NetworkResult.Success(
            listOf(ArticleDto(id = 2, userId = 5, title = "Fresh", body = "Body"))
        )
        viewModel.onAction(ArticlesUiAction.Refresh)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isShowingStaleData)
        assertEquals(listOf("Cached", "Fresh"), viewModel.uiState.value.articles.map { it.title })
    }

    // Journey 2 (platform): notification permission request -> effect -> result -> state.
    @Test
    fun `journey notification permission - request emits effect and denial is reflected in state`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<ArticlesUiEffect>()
        val collector = launch { viewModel.effects.toList(effects) }

        viewModel.onAction(ArticlesUiAction.RequestNotificationPermission)
        advanceUntilIdle()
        assertEquals(listOf<ArticlesUiEffect>(ArticlesUiEffect.LaunchNotificationPermissionRequest), effects)

        viewModel.onAction(
            ArticlesUiAction.UpdateNotificationPermissionState(NotificationPermission.fromRequestResult(false))
        )
        advanceUntilIdle()
        assertEquals(PermissionState.Denied, viewModel.uiState.value.notificationPermissionState)
        collector.cancel()
    }

    @Test
    fun `RequestNotificationPermission emits a LaunchNotificationPermissionRequest effect`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<ArticlesUiEffect>()
        val collector = launch { viewModel.effects.toList(effects) }

        viewModel.onAction(ArticlesUiAction.RequestNotificationPermission)
        advanceUntilIdle()

        assertEquals(1, effects.size)
        assertEquals(ArticlesUiEffect.LaunchNotificationPermissionRequest, effects.single())
        collector.cancel()
    }

    @Test
    fun `UpdateNotificationPermissionState updates the permission field in state`() = runTest {
        val viewModel = createViewModel()
        assertEquals(PermissionState.NotRequested, viewModel.uiState.value.notificationPermissionState)

        viewModel.onAction(ArticlesUiAction.UpdateNotificationPermissionState(PermissionState.Granted))
        advanceUntilIdle()

        assertEquals(PermissionState.Granted, viewModel.uiState.value.notificationPermissionState)
    }
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

private class MutableFakeArticlesNetworkDataSource(
    var result: NetworkResult<List<ArticleDto>>
) : ArticlesNetworkDataSource {
    override suspend fun fetchArticles(): NetworkResult<List<ArticleDto>> = result
}
