package com.dc.android.foundation.di

import com.dc.android.foundation.runtime.concurrency.DefaultDispatcherProvider
import com.dc.android.foundation.runtime.concurrency.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * `:runtime:concurrency` is DI-framework-agnostic (INV-03), so the composition root (`:app`)
 * wires the binding — see 08-dependency-injection-hilt.md's note on cross-module bindings.
 */
@Module
@InstallIn(SingletonComponent::class)
object ConcurrencyModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}
