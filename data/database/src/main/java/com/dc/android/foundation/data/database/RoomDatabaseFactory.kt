package com.dc.android.foundation.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.dc.android.foundation.runtime.concurrency.DispatcherProvider
import java.util.concurrent.Executor
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Standard Room construction policy for application-owned schemas. Query and transaction work
 * always use the supplied I/O dispatcher, while schema/entity ownership stays with the app or
 * feature that declares the concrete [RoomDatabase].
 */
object RoomDatabaseFactory {

    fun <T : RoomDatabase> create(
        context: Context,
        databaseClass: Class<T>,
        name: String,
        dispatcherProvider: DispatcherProvider,
        migrations: List<Migration> = emptyList(),
        allowDestructiveMigrationInDebugOrTest: Boolean = false
    ): T {
        require(name.isNotBlank()) { "Database name must not be blank." }

        val ioExecutor = dispatcherProvider.io.asExecutor()
        val builder = Room.databaseBuilder(context.applicationContext, databaseClass, name)
            .setQueryExecutor(ioExecutor)
            .setTransactionExecutor(ioExecutor)

        if (migrations.isNotEmpty()) builder.addMigrations(*migrations.toTypedArray())
        if (allowDestructiveMigrationInDebugOrTest) {
            builder.fallbackToDestructiveMigration(dropAllTables = true)
        }

        return builder.build()
    }

    private fun kotlinx.coroutines.CoroutineDispatcher.asExecutor(): Executor = Executor { command ->
        dispatch(EmptyCoroutineContext, command)
    }
}
