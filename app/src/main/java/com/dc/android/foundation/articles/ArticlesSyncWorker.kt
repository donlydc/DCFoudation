package com.dc.android.foundation.articles

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.dc.android.foundation.R
import com.dc.android.foundation.core.AppError
import com.dc.android.foundation.observability.Logger
import com.dc.android.foundation.platform.permission.NotificationPermission
import com.dc.android.foundation.platform.permission.PermissionState
import com.dc.android.foundation.runtime.scheduling.work.DcJobResult
import com.dc.android.foundation.runtime.scheduling.work.DcWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException

private const val LOG_TAG = "ArticlesSyncWorker"
private const val NOTIFICATION_CHANNEL_ID = "articles_sync"
private const val NOTIFICATION_ID = 1001

/**
 * The real BG-01 instance: a periodic, retry-guaranteed sync of `articles` that survives app
 * closure/process death. Deliberately lives in `:app`, not `:runtime:scheduling` — `@HiltWorker`
 * needs the same Hilt component graph as [ArticlesRepository], which is itself an `:app`-only
 * type (14-background-work.md explains the full placement rationale).
 *
 * Reuses [ArticlesRepository.refresh] rather than duplicating its "fetch, then upsert into Room"
 * logic (DAT-CACHE-01 stays the one place that does that mapping).
 */
@HiltWorker
class ArticlesSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val articlesRepository: ArticlesRepository,
    private val logger: Logger
) : DcWorker<Unit>(context, params) {

    override suspend fun run(input: Unit?): DcJobResult {
        return try {
            val succeeded = articlesRepository.refresh()
            // Status only — never log article title/body content (SEC-01).
            logger.d(LOG_TAG, "doWork: succeeded=$succeeded")
            if (succeeded) {
                postSyncNotification()
                DcJobResult.Success
            } else {
                DcJobResult.Retry
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (t: Throwable) {
            logger.e(LOG_TAG, "doWork failed", t)
            // Programming/data failures are terminal; only the explicit false network result retries.
            DcJobResult.Failure(AppError.Unknown(t))
        }
    }

    /**
     * Best-effort only (PERM-01): silently skipped if `POST_NOTIFICATIONS` isn't granted, never
     * crashes on a missing/revoked permission. Channel creation is a required Android O+ step and
     * is intentionally the only piece of notification-channel plumbing this worker owns — no
     * general-purpose channel-management abstraction (see 13-permissions.md PERM-01).
     */
    private fun postSyncNotification() {
        if (NotificationPermission.currentState(applicationContext) != PermissionState.Granted) return

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(
                NOTIFICATION_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_DEFAULT
            )
                .setName("Article sync")
                .build()
        )

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Articles synced")
            .setContentText("New articles are available")
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permission revoked between the check above and notify() — never crash the worker.
        }
    }
}
