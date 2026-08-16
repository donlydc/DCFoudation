# Security & session

[Back to architecture index](00-index.md)

## Scope

Secure storage, credential/token/session ownership, sensitive-logging policy.

## Current implementation

No login/auth/session/credential flow exists anywhere in the app — see `22-known-deviations.md` KD-04. The one policy already in force and verified: nothing secret is logged, because nothing secret exists yet (`Logger.d`/`Logger.e` calls today only log a name index/count/status, never a token/credential — see [`15-observability/logging.md`](15-observability/logging.md) SEC-01).

`:security` (real `androidx.security:security-crypto`-backed module): `SecureStorage` interface (`putString`/`getString`/`remove`, string key/value — same minimal shape as `PreferencesDataSource`'s int key/value), `EncryptedSharedPreferencesSecureStorage` (real `MasterKey` + `EncryptedSharedPreferences` implementation, no `@Inject` constructor — hand-constructed in `app/.../di/SecurityModule.kt`, same pattern as `AndroidLogger`/`ObservabilityModule`). Bound as a Hilt singleton via `SecurityModule.provideSecureStorage`.

**Per KD-04, this has zero call sites today** — the storage mechanism is production-grade and real, but no real credential exists yet to store through it. Do not add a fake auth flow to manufacture one; wait for a real auth/session feature.

## Rules

### SEC-01 — No secret/token/PII in a log call

**Rule:** A `Logger.d`/`Logger.e` `message` argument never contains a credential, token, session id, or other PII.

**Why:** Prevents secrets leaking into logcat/crash reports (INV-09).

**Verification:** `grep`-review every `logger.d(`/`logger.e(` call site by hand — this rule can't be fully mechanically verified, so re-check it whenever a new log call is added, especially once real auth exists.

### SEC-02 — A credential/token/session id is stored only through `SecureStorage`, never `PreferencesDataSource`

**Rule:** Any credential, auth token, or session id a feature needs to persist goes through the `:security` module's `SecureStorage` interface (`EncryptedSharedPreferencesSecureStorage` — real `MasterKey`-backed `EncryptedSharedPreferences`). `DataStorePreferencesDataSource` (`:data:preferences`, used today for `nameIndex`, which is not sensitive) is explicitly **not** an approved place to store a token/credential — plaintext DataStore has no encryption-at-rest guarantee.

**Why:** Prevents a credential from ending up in an unencrypted preferences file readable by anything with root/backup access to app-private storage (INV-09).

**Verification:** `grep` any future auth/session/token-handling code for `DataStore`/`PreferencesDataSource` usage — a credential-shaped key going through that path instead of `SecureStorage` is a violation.

**Exceptions/current status:** Per `22-known-deviations.md` KD-04, `SecureStorage`/`EncryptedSharedPreferencesSecureStorage` exist and are real, but have no consumer yet — no real auth/session flow has been built in this business-neutral foundation repo, so there is intentionally no credential flowing through this rule's enforcement point today. The rule is written for when one exists.

## Testing / validation

SEC-01's manual review remains the process for every new log call site (see `15-observability/logging.md`). `EncryptedSharedPreferencesSecureStorage` needs a real Android `Context`/Keystore to construct a `MasterKey`, so it is not feasibly unit-testable outside instrumentation — a known gap (same class as the Phase Q worker-execution gap, see `HANDOVER.md`). `security/src/test/.../SecureStorageUnavailableExceptionTest.kt` covers the one Android-independent piece of logic (the ERR-01 exception-wrapping).

## Related documents

- [`15-observability/logging.md`](15-observability/logging.md)
- [`10-data/preferences-datastore.md`](10-data/preferences-datastore.md)
