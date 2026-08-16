package com.dc.android.foundation.config

/** Business-neutral environment marker. Feature flags (when any exist) key off this, not raw `BuildConfig`. */
enum class AppEnvironment { DEBUG, RELEASE }
