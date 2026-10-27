package com.dc.android.foundation.errors

import com.dc.android.foundation.R
import com.dc.android.foundation.core.AppError
import org.junit.Assert.assertEquals
import org.junit.Test

class AppErrorMessagesTest {

    @Test
    fun `every AppError variant maps to its semantic message`() {
        assertEquals(R.string.error_connectivity, AppError.Connectivity().toMessageRes())
        assertEquals(R.string.error_http, AppError.Http(code = 500).toMessageRes())
        assertEquals(R.string.error_serialization, AppError.Serialization().toMessageRes())
        assertEquals(R.string.error_storage, AppError.Storage().toMessageRes())
        assertEquals(R.string.error_unknown, AppError.Unknown().toMessageRes())
    }

    @Test
    fun `mapping ignores cause and server message`() {
        val cause = java.io.IOException("secret")
        assertEquals(
            AppError.Http(404).toMessageRes(),
            AppError.Http(404, serverMessage = "internal detail", cause = cause).toMessageRes()
        )
        assertEquals(AppError.Connectivity().toMessageRes(), AppError.Connectivity(cause).toMessageRes())
    }

    @Test
    fun `http variants share one message regardless of status code`() {
        assertEquals(AppError.Http(400).toMessageRes(), AppError.Http(503).toMessageRes())
    }
}
