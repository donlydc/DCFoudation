package com.dc.android.foundation

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dc.android.foundation.articles.ArticlesSyncWorker
import com.dc.android.foundation.runtime.background.PeriodicSyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

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

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        PeriodicSyncScheduler.enqueue(
            context = this,
            workerClass = ArticlesSyncWorker::class.java,
            uniqueWorkName = ARTICLES_SYNC_WORK_NAME,
            repeatIntervalMinutes = ARTICLES_SYNC_INTERVAL_MINUTES
        )
    }
}
