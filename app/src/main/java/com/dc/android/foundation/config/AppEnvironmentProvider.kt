package com.dc.android.foundation.config

import com.dc.android.foundation.BuildConfig

/** The only file allowed to read `BuildConfig.DEBUG` directly (CFG-01) — everything else takes [AppEnvironment]. */
object AppEnvironmentProvider {
    val current: AppEnvironment = if (BuildConfig.DEBUG) AppEnvironment.DEBUG else AppEnvironment.RELEASE

    /**
     * Base URL for the `articles` reference feature's remote API (DAT-NET-01). Only one real
     * environment exists today, so this is a single honest constant rather than a
     * per-[AppEnvironment] map — split it out once a second environment (e.g. staging) needs a
     * different base URL.
     */
    const val ARTICLES_BASE_URL: String = "https://jsonplaceholder.typicode.com/"
}
