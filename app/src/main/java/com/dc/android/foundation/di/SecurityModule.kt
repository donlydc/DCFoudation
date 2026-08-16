package com.dc.android.foundation.di

import android.content.Context
import com.dc.android.foundation.runtime.concurrency.DispatcherProvider
import com.dc.android.foundation.security.EncryptedSharedPreferencesSecureStorage
import com.dc.android.foundation.security.SecureStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Same shape as DataStoreModule — see 08-dependency-injection-hilt.md. */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideSecureStorage(
        @ApplicationContext context: Context,
        dispatcherProvider: DispatcherProvider
    ): SecureStorage = EncryptedSharedPreferencesSecureStorage(context, dispatcherProvider)
}
