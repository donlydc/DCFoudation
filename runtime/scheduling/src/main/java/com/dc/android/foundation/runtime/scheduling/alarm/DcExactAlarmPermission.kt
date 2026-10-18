package com.dc.android.foundation.runtime.scheduling.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object DcExactAlarmPermission {
    fun canScheduleExact(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    @SuppressLint("InlinedApi") // Guarded below; the action exists only on API 31+.
    fun settingsIntent(context: Context): Intent {
        require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            "Exact-alarm settings are available from API 31."
        }
        return Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Uri.parse("package:${context.packageName}"),
        )
    }
}

object DcBatteryOptimization {
    fun isIgnoringBatteryOptimizations(context: Context): Boolean =
        context.getSystemService(android.os.PowerManager::class.java)
            .isIgnoringBatteryOptimizations(context.packageName)
}
