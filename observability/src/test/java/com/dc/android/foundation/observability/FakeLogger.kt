package com.dc.android.foundation.observability

/**
 * Local test double for [Logger]. `:observability` tests cannot use `:testing`'s `RecordingLogger`
 * because `:testing` depends on `:observability` (`api`), which would form a project cycle.
 */
internal class FakeLogger : Logger {

    enum class Level { DEBUG, ERROR }

    data class Entry(
        val level: Level,
        val tag: String,
        val message: String,
        val throwable: Throwable?
    )

    val entries = mutableListOf<Entry>()

    override fun d(tag: String, message: String) {
        entries += Entry(Level.DEBUG, tag, message, throwable = null)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        entries += Entry(Level.ERROR, tag, message, throwable)
    }
}
