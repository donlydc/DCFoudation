package com.dc.android.foundation.greeting

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Binding lives next to the interface/impl it wires, not in a shared AppModule
 * (DI-02 — "bindings live near the module/capability that owns them").
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class GreetingModule {

    @Binds
    abstract fun bindGreetingProvider(impl: DefaultGreetingProvider): GreetingProvider
}
