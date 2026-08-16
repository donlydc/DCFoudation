package com.dc.android.foundation.di

import com.dc.android.foundation.articles.ArticlesApiService
import com.dc.android.foundation.config.AppEnvironment
import com.dc.android.foundation.config.AppEnvironmentProvider
import com.dc.android.foundation.data.network.NetworkClientFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

/** Same cross-module-provider pattern as DataStoreModule/ConcurrencyModule (DAT-NET-01). */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        NetworkClientFactory.createOkHttpClient(
            isDebug = AppEnvironmentProvider.current == AppEnvironment.DEBUG
        )

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        NetworkClientFactory.createRetrofit(
            baseUrl = AppEnvironmentProvider.ARTICLES_BASE_URL,
            okHttpClient = okHttpClient
        )

    @Provides
    @Singleton
    fun provideArticlesApiService(retrofit: Retrofit): ArticlesApiService =
        retrofit.create(ArticlesApiService::class.java)
}
