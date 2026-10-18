package com.dc.android.foundation.runtime.scheduling.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dc.android.foundation.runtime.concurrency.DefaultDispatcherProvider
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Restores persisted alarms after reboot, package replacement, timezone, or wall-clock changes. */
class DcAlarmRestorer : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in RESTORE_ACTIONS) return
        val pendingResult = goAsync()
        restoreScope.launch {
            try {
                withTimeoutOrNull(9.seconds) { defaultAlarmScheduler(context.applicationContext).restore() }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        val restoreScope = CoroutineScope(SupervisorJob() + DefaultDispatcherProvider().default)
        val RESTORE_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
        )
    }
}
