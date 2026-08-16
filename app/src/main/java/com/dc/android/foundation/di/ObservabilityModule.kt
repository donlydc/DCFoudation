package com.dc.android.foundation.di

import com.dc.android.foundation.config.AppEnvironment
import com.dc.android.foundation.config.AppEnvironmentProvider
import com.dc.android.foundation.observability.AnalyticsTracker
import com.dc.android.foundation.observability.AndroidLogger
import com.dc.android.foundation.observability.CrashReporter
import com.dc.android.foundation.observability.Logger
import com.dc.android.foundation.observability.LoggingAnalyticsTracker
import com.dc.android.foundation.observability.LoggingCrashReporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ObservabilityModule {

    @Provides
    @Singleton
    fun provideLogger(): Logger =
        AndroidLogger(isDebug = AppEnvironmentProvider.current == AppEnvironment.DEBUG)

    /** No-vendor-yet placeholder (KD-05) — see [LoggingAnalyticsTracker]. */
    @Provides
    @Singleton
    fun provideAnalyticsTracker(logger: Logger): AnalyticsTracker = LoggingAnalyticsTracker(logger)

    /** No-vendor-yet placeholder (KD-05) — see [LoggingCrashReporter]. */
    @Provides
    @Singleton
    fun provideCrashReporter(logger: Logger): CrashReporter = LoggingCrashReporter(logger)
}
