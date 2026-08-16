package com.dc.android.foundation.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.dc.android.foundation.data.preferences.DataStorePreferencesDataSource
import com.dc.android.foundation.data.preferences.PreferencesDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Same cross-module-binding pattern as ConcurrencyModule — see 08-dependency-injection-hilt.md. */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("dc_foundation_prefs") }
        )

    @Provides
    @Singleton
    fun providePreferencesDataSource(dataStore: DataStore<Preferences>): PreferencesDataSource =
        DataStorePreferencesDataSource(dataStore)
}
