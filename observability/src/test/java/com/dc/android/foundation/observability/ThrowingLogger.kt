package com.dc.android.foundation.observability

/** Test double: a [Logger] whose backend always fails (OBS-02 error isolation). */
internal class ThrowingLogger : Logger {
    var calls = 0
        private set

    override fun d(tag: String, message: String) {
        calls++
        throw IllegalStateException("logger backend down")
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        calls++
        throw IllegalStateException("logger backend down")
    }
}
