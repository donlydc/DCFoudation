package com.dc.android.foundation.di

import android.content.Context
import com.dc.android.foundation.data.database.ArticleDao
import com.dc.android.foundation.data.database.DcDatabase
import com.dc.android.foundation.data.database.RoomDatabaseFactory
import com.dc.android.foundation.runtime.concurrency.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Same cross-module-provider pattern as DataStoreModule (DAT-ROOM-01). */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDcDatabase(
        @ApplicationContext context: Context,
        dispatcherProvider: DispatcherProvider
    ): DcDatabase = RoomDatabaseFactory.create(
        context = context,
        databaseClass = DcDatabase::class.java,
        name = "dc_foundation_db",
        dispatcherProvider = dispatcherProvider
    )

    @Provides
    @Singleton
    fun provideArticleDao(database: DcDatabase): ArticleDao = database.articleDao()
}
