package com.dc.android.foundation.di

import android.content.Context
import androidx.room.Room
import com.dc.android.foundation.data.database.ArticleDao
import com.dc.android.foundation.data.database.DcDatabase
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
    fun provideDcDatabase(@ApplicationContext context: Context): DcDatabase =
        Room.databaseBuilder(context, DcDatabase::class.java, "dc_foundation_db").build()

    @Provides
    @Singleton
    fun provideArticleDao(database: DcDatabase): ArticleDao = database.articleDao()
}
