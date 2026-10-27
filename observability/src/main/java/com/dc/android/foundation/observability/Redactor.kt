package com.dc.android.foundation.observability

/**
 * Pure, Android-free redaction policy (MP:SEC-04 / MP:OBS-01, SEC-01 defence in depth). Callers
 * must still never pass secrets to a log call; this is a best-effort safety net applied by the
 * Logging* implementations, not a licence to log sensitive data. It is heuristic: it cannot
 * guarantee that arbitrary free text is secret-free.
 *
 * Two layers: (1) key-name based — a map entry whose key looks sensitive has its whole value
 * replaced; (2) value-pattern based — bearer/basic credentials, JWT-shaped strings, e-mail
 * addresses and inline `key=value` / `key: value` pairs with a sensitive key are masked in text.
 */
object Redactor {

    const val REDACTED = "[REDACTED]"

    private val SENSITIVE_KEY_PARTS = listOf(
        "password", "passwd", "secret", "token", "authorization", "apikey", "cookie",
        "credential", "privatekey", "sessionid", "email"
    )

    private val BEARER = Regex("""\b(Bearer|Basic)\s+[A-Za-z0-9._~+/=-]+""", RegexOption.IGNORE_CASE)
    private val JWT = Regex("""\beyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]*""")
    private val EMAIL = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    private val INLINE_PAIR = Regex(
        """\b([\w-]*(?:password|passwd|secret|token|api[_-]?key|authorization|cookie)[\w-]*)(["']?\s*[=:]\s*)("[^"]*"|[^\s,;&}]+)""",
        RegexOption.IGNORE_CASE
    )

    /** True when [key] names a sensitive field (case-insensitive; `_`, `-`, `.` ignored). */
    fun isSensitiveKey(key: String): Boolean {
        val normalized = key.lowercase().filterNot { it == '_' || it == '-' || it == '.' }
        return SENSITIVE_KEY_PARTS.any { normalized.contains(it) }
    }

    /** Masks secret-looking substrings of free text; non-sensitive text is returned unchanged. */
    fun redact(text: String): String {
        var result = BEARER.replace(text) { "${it.groupValues[1]} $REDACTED" }
        result = INLINE_PAIR.replace(result) { "${it.groupValues[1]}${it.groupValues[2]}$REDACTED" }
        result = JWT.replace(result, REDACTED)
        result = EMAIL.replace(result, REDACTED)
        return result
    }

    /**
     * Returns a new map (input is never mutated, iteration order preserved) with sensitive-key
     * values fully replaced and every other value passed through [redact].
     */
    fun redactProperties(properties: Map<String, String>): Map<String, String> {
        val result = LinkedHashMap<String, String>(properties.size)
        for ((key, value) in properties) {
            result[key] = if (isSensitiveKey(key)) REDACTED else redact(value)
        }
        return result
    }
}
