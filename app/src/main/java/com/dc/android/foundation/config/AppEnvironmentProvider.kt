package com.dc.android.foundation.config

import com.dc.android.foundation.BuildConfig

/** The only file allowed to read `BuildConfig.DEBUG` directly (CFG-01) — everything else takes [AppEnvironment]. */
object AppEnvironmentProvider {
    val current: AppEnvironment = if (BuildConfig.DEBUG) AppEnvironment.DEBUG else AppEnvironment.RELEASE
}
