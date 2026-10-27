# B2-design — D-04 SecureStorage provider/lifecycle + OBS-02 isolation (PROPOSED; Opus finalizes)
- Agent / model: Sonnet 5.5 (architecture-writer). Status: PROPOSED, no ADR written. All version/status claims below are from model knowledge, NOT verified online in this environment -> UNKNOWN until checked.

## 1. D-04 — SecureStorage provider
As-is: `EncryptedSharedPreferencesSecureStorage` over `androidx.security:security-crypto:1.1.0` (catalog `androidxSecurityCrypto`). Contract `SecureStorage` (put/get/remove) hides the provider (DEP-04), so a swap is a one-class change + DI line (`app/.../di/SecurityModule.kt`).
Known (to be verified): Google marked the `androidx.security:security-crypto` library (EncryptedSharedPreferences/EncryptedFile/MasterKey) deprecated (2025 releases); it still works but receives no new features. Recommended replacements in Google guidance: Android Keystore directly, or Tink, plus DataStore for persistence. Stability of the 1.1.0 artifact: UNKNOWN (verify on maven.google.com / release notes).

### Option A — Keep security-crypto 1.1.0 behind the adapter (status quo)
+ no migration, no key rotation, no data loss; zero change risk. - Deprecated dependency; no fixes; ESP known quirks (Keystore invalidation -> undecryptable data, slow first init, rebuild per call as-is).
### Option B — Own adapter: Android Keystore AES-GCM key + DataStore (or plain prefs file) holding ciphertext (Tink `AndroidKeysetManager`/`Aead` optional)
+ not deprecated path, DataStore-friendly (async, no `apply()` races), key/alias ownership explicit. - New crypto code to own/test (instrumented only), needs new deps (Tink/datastore) -> catalog change, needs a one-time migration reading old ESP file and re-encrypting (reversible: keep old file until verified).
### Option C — Tink directly (`Aead` + `AndroidKeysetManager`) over SharedPreferences ciphertext
+ well-maintained primitive library, keyset rotation supported, smaller own-crypto surface than B. - Adds Tink dep (size), still needs migration + instrumented tests; ESP internally already uses Tink, so conceptual reuse is high.

## Recommendation
Short term **A** (keep; KD-04: no credential flows yet, so no data to migrate or protect). Do NOT swap provider or rotate keys silently (MP 4.9). Record an ADR (MP:SEC-02, P1) with version evidence; before the first real credential (SEC-03) implement **C** (Tink) as a second `SecureStorage` impl with explicit one-time migration (read old ESP -> write new -> verify -> delete old) and a documented backup stance (`allowBackup`/data-extraction rules exclude the secure prefs file + key invalid after restore -> treat as absent). Decision gate: choose before SEC-03 starts, not now.
Lifecycle gaps found (for the ADR): (1) no `clear()`/logout in `SecureStorage` (SEC-01 asks for clear tests; needs a contract addition + all implementers/fakes in `:testing` -> cross-module, not done in B2); (2) prefs/MasterKey rebuilt on every call (no caching; safe but slow; cache needs a retry-on-failure policy); (3) writes use `apply()` (async, no failure signal); (4) Keystore invalidation surfaced as `SecurityException` on read — B2 maps it to `null` ("absent").

## 2. OBS-02 — error isolation (PROPOSED decision)
Question: should `LoggingAnalyticsTracker`/`LoggingCrashReporter` survive a throwing `Logger`?
Decision proposed: **Yes.** Telemetry is best-effort and must never crash the app (MP OBS-02 acceptance "SDK failure không làm crash app"). `Logging*` catch `Exception` (not `Throwable`: OOM/`Error` still propagate) around the Logger call and swallow silently (nothing safe to report to — the logger *is* the sink). Not applied to `Logger` implementations themselves (`AndroidLogger` is not wrapped) nor to `CancellationException` (calls are non-suspend). Future vendor adapters (OBS-04) must adopt the same rule; consider documenting it on the `AnalyticsTracker`/`CrashReporter` interfaces (doc delta, architecture/15-observability).
Redaction (SEC-04): `Redactor` in `:observability` (pure Kotlin) applied by `Logging*` as a safety net only; callers still own SEC-01. Heuristic, not a guarantee.
