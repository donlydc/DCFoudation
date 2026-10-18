package com.dc.android.foundation.runtime.scheduling.alarm

import android.content.Context
import com.dc.android.foundation.runtime.concurrency.DispatcherProvider
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

interface DcAlarmStore {
    suspend fun put(alarm: DcAlarm)
    suspend fun remove(id: String)
    suspend fun all(): List<DcAlarm>
}

/**
 * Small durable default store. Apps that need alarm queries/indexes can provide a Room-backed
 * [DcAlarmStore] without making the scheduling module depend on Room or DataStore.
 */
class FileDcAlarmStore(
    private val file: File,
    private val dispatchers: DispatcherProvider,
    private val json: Json = Json,
) : DcAlarmStore {
    // Framework receivers and the app facade construct separate store instances. They still write
    // the same file, so the lock must be shared process-wide per canonical path.
    private val mutex = mutexFor(file)

    override suspend fun put(alarm: DcAlarm) = withContext(dispatchers.io) {
        mutex.withLock {
            val alarms = readUnlocked().associateBy(DcAlarm::id).toMutableMap()
            alarms[alarm.id] = alarm
            writeUnlocked(alarms.values.sortedBy(DcAlarm::id))
        }
    }

    override suspend fun remove(id: String) = withContext(dispatchers.io) {
        mutex.withLock {
            writeUnlocked(readUnlocked().filterNot { it.id == id })
        }
    }

    override suspend fun all(): List<DcAlarm> = withContext(dispatchers.io) {
        mutex.withLock { readUnlocked() }
    }

    private fun readUnlocked(): List<DcAlarm> = runCatching {
        if (!file.exists()) emptyList()
        else DcAlarmCodec.decodeList(json, file.readText())
    }.getOrDefault(emptyList())

    private fun writeUnlocked(alarms: List<DcAlarm>) {
        file.parentFile?.mkdirs()
        val temporary = File(file.parentFile, "${file.name}.tmp")
        temporary.writeText(DcAlarmCodec.encodeList(json, alarms))
        if (!temporary.renameTo(file)) {
            temporary.copyTo(file, overwrite = true)
            temporary.delete()
        }
    }

    private companion object {
        const val FILE_NAME = "dc_scheduled_alarms.json"
        val mutexes = ConcurrentHashMap<String, Mutex>()

        fun mutexFor(file: File): Mutex = mutexes.computeIfAbsent(file.absoluteFile.normalize().path) {
            Mutex()
        }
    }

    constructor(context: Context, dispatchers: DispatcherProvider) : this(
        file = File(context.filesDir, FILE_NAME),
        dispatchers = dispatchers,
    )
}
