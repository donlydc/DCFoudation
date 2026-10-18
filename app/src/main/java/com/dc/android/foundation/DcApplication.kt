package com.dc.android.foundation

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dc.android.foundation.articles.ArticlesSyncWorker
import com.dc.android.foundation.observability.Logger
import com.dc.android.foundation.runtime.concurrency.AppCoroutineScope
import com.dc.android.foundation.runtime.concurrency.DispatcherProvider
import com.dc.android.foundation.runtime.scheduling.AndroidDcScheduler
import com.dc.android.foundation.runtime.scheduling.DcScheduleRequest
import com.dc.android.foundation.runtime.scheduling.DcTrigger
import com.dc.android.foundation.runtime.scheduling.work.DcConstraints
import com.dc.android.foundation.runtime.scheduling.work.DcJobSpec
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.launch

private const val ARTICLES_SYNC_WORK_NAME = "articles-sync"
private const val ARTICLES_SYNC_INTERVAL_MINUTES = 15L

/**
 * Hilt entry point for the whole app (INV-04: constructor injection is the default DI mechanism).
 *
 * Implements `Configuration.Provider` so WorkManager's on-demand initialization picks up a
 * Hilt-aware `WorkerFactory` (standard `androidx.hilt:hilt-work` pattern, BG-01) — the default
 * `androidx.startup` WorkManager initializer is removed in `AndroidManifest.xml` so this
 * `Configuration` is the only one WorkManager ever sees. `workerFactory` is injected as a field
 * because `Application` has no constructor Hilt can call; `@HiltAndroidApp` performs the member
 * injection automatically before `onCreate()` runs.
 */
@HiltAndroidApp
class DcApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    @Inject
    lateinit var logger: Logger

    private val applicationScope by lazy {
        AppCoroutineScope(dispatcherProvider) { throwable ->
            logger.e("DcApplication", "Uncaught application-scope failure", throwable)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            val outcome = AndroidDcScheduler(
                context = this@DcApplication,
                dispatchers = dispatcherProvider,
            ).schedule(
                id = ARTICLES_SYNC_WORK_NAME,
                trigger = DcTrigger.Every(ARTICLES_SYNC_INTERVAL_MINUTES.minutes),
                request = DcScheduleRequest.work(
                    DcJobSpec<Unit>(
                        name = ARTICLES_SYNC_WORK_NAME,
                        worker = ArticlesSyncWorker::class,
                        constraints = DcConstraints.Network,
                    ),
                ),
            )
            logger.d("DcApplication", "Articles sync schedule outcome=${outcome::class.simpleName}")
        }
    }
}
