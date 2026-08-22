package com.dc.android.foundation.testing

import com.dc.android.foundation.observability.AnalyticsTracker
import com.dc.android.foundation.observability.CrashReporter
import com.dc.android.foundation.observability.Logger

/** A logger for tests that do not need to assert logging interactions. */
public object NoOpLogger : Logger {
    override fun d(tag: String, message: String) = Unit

    override fun e(tag: String, message: String, throwable: Throwable?) = Unit
}

/** One captured logging call from [RecordingLogger]. */
public data class LogRecord(
    val level: Level,
    val tag: String,
    val message: String,
    val throwable: Throwable?
) {
    public enum class Level {
        DEBUG,
        ERROR
    }
}

/**
 * In-memory logger for tests that need to verify diagnostics without a vendor SDK or logcat.
 * Snapshots are returned from [records] so callers cannot mutate the recorder's state.
 */
public class RecordingLogger : Logger {
    private val lock = Any()
    private val mutableRecords = mutableListOf<LogRecord>()

    public val records: List<LogRecord>
        get() = synchronized(lock) { mutableRecords.toList() }

    override fun d(tag: String, message: String) {
        add(LogRecord(LogRecord.Level.DEBUG, tag, message, throwable = null))
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        add(LogRecord(LogRecord.Level.ERROR, tag, message, throwable))
    }

    public fun clear() {
        synchronized(lock) { mutableRecords.clear() }
    }

    private fun add(record: LogRecord) {
        synchronized(lock) { mutableRecords += record }
    }
}

/** One captured analytics event from [RecordingAnalyticsTracker]. */
public data class AnalyticsEvent(
    val name: String,
    val properties: Map<String, String>
)

/** A no-op analytics implementation for tests that do not assert telemetry. */
public object NoOpAnalyticsTracker : AnalyticsTracker {
    override fun trackEvent(name: String, properties: Map<String, String>) = Unit
}

/** In-memory analytics recorder with defensive snapshots/copies of event properties. */
public class RecordingAnalyticsTracker : AnalyticsTracker {
    private val lock = Any()
    private val mutableEvents = mutableListOf<AnalyticsEvent>()

    public val events: List<AnalyticsEvent>
        get() = synchronized(lock) { mutableEvents.toList() }

    override fun trackEvent(name: String, properties: Map<String, String>) {
        synchronized(lock) {
            mutableEvents += AnalyticsEvent(name, properties.toMap())
        }
    }

    public fun clear() {
        synchronized(lock) { mutableEvents.clear() }
    }
}

/** A no-op crash reporter for tests that do not assert error-reporting interactions. */
public object NoOpCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable) = Unit

    override fun log(message: String) = Unit
}

/** In-memory crash reporter that captures exceptions and breadcrumb messages separately. */
public class RecordingCrashReporter : CrashReporter {
    private val lock = Any()
    private val mutableExceptions = mutableListOf<Throwable>()
    private val mutableMessages = mutableListOf<String>()

    public val recordedExceptions: List<Throwable>
        get() = synchronized(lock) { mutableExceptions.toList() }

    public val loggedMessages: List<String>
        get() = synchronized(lock) { mutableMessages.toList() }

    override fun recordException(throwable: Throwable) {
        synchronized(lock) { mutableExceptions += throwable }
    }

    override fun log(message: String) {
        synchronized(lock) { mutableMessages += message }
    }

    public fun clear() {
        synchronized(lock) {
            mutableExceptions.clear()
            mutableMessages.clear()
        }
    }
}
