package com.dc.android.foundation.observability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure JVM tests for [Redactor] (MP:SEC-04 / MP:OBS-01); no Logcat, no Android. */
class RedactorTest {

    @Test
    fun redact_leavesPlainTextUnchanged() {
        assertEquals("user opened article list", Redactor.redact("user opened article list"))
    }

    @Test
    fun redact_masksBearerToken() {
        val out = Redactor.redact("Authorization: Bearer abc.def-123_xyz")
        assertFalse(out.contains("abc.def-123_xyz"))
    }

    @Test
    fun redact_masksJwtShapedString() {
        val jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.c2lnbmF0dXJl"
        assertFalse(Redactor.redact("got $jwt from server").contains(jwt))
    }

    @Test
    fun redact_masksEmailAddress() {
        val out = Redactor.redact("login failed for jane.doe+x@example.com today")
        assertFalse(out.contains("jane.doe"))
        assertTrue(out.contains(Redactor.REDACTED))
    }

    @Test
    fun redact_masksInlineKeyValueSecrets_keepingKeyName() {
        assertEquals("password=${Redactor.REDACTED}", Redactor.redact("password=hunter2"))
        assertEquals("api_key: ${Redactor.REDACTED}", Redactor.redact("api_key: sk-12345"))
        assertFalse(Redactor.redact("""{"refresh_token":"r-secret-value"}""").contains("r-secret-value"))
    }

    @Test
    fun isSensitiveKey_isCaseAndSeparatorInsensitive() {
        assertTrue(Redactor.isSensitiveKey("Access_Token"))
        assertTrue(Redactor.isSensitiveKey("api-key"))
        assertTrue(Redactor.isSensitiveKey("userEmail"))
        assertFalse(Redactor.isSensitiveKey("article_id"))
    }

    @Test
    fun redactProperties_replacesSensitiveKeyValuesAndKeepsOthers() {
        val out = Redactor.redactProperties(
            mapOf("id" to "42", "token" to "abc123", "Password" to "p", "source" to "list")
        )
        assertEquals(
            mapOf(
                "id" to "42",
                "token" to Redactor.REDACTED,
                "Password" to Redactor.REDACTED,
                "source" to "list"
            ),
            out
        )
        assertEquals(listOf("id", "token", "Password", "source"), out.keys.toList())
    }

    @Test
    fun redactProperties_masksPatternsInNonSensitiveKeys_andDoesNotMutateInput() {
        val input = mutableMapOf("note" to "mail me at a@b.io")
        val out = Redactor.redactProperties(input)
        assertFalse(out.getValue("note").contains("a@b.io"))
        assertEquals(mapOf("note" to "mail me at a@b.io"), input)
    }
}
