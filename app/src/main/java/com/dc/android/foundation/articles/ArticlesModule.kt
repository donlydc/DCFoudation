package com.dc.android.foundation.articles

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binding lives next to the interface/impl it wires (DI-02), same shape as `GreetingModule`. */
@Module
@InstallIn(SingletonComponent::class)
abstract class ArticlesModule {

    @Binds
    abstract fun bindArticlesNetworkDataSource(
        impl: DefaultArticlesNetworkDataSource
    ): ArticlesNetworkDataSource
}
