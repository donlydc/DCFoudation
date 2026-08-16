package com.dc.android.foundation.di

import com.dc.android.foundation.config.AppEnvironment
import com.dc.android.foundation.config.AppEnvironmentProvider
import com.dc.android.foundation.observability.AndroidLogger
import com.dc.android.foundation.observability.Logger
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
}
