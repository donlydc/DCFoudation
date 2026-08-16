# Security & session

[Back to architecture index](00-index.md)

## Scope

Secure storage, credential/token/session ownership, sensitive-logging policy.

## Current implementation

**Mostly not implemented** — no login/auth/session/credentials exist anywhere in the app. The one policy already in force and verified: nothing secret is logged, because nothing secret exists yet (`Logger.d`/`Logger.e` calls today only log a name index, never a token/credential — see [`15-observability/logging.md`](15-observability/logging.md) SEC-01).

## Rules

### SEC-01 — No secret/token/PII in a log call

**Rule:** A `Logger.d`/`Logger.e` `message` argument never contains a credential, token, session id, or other PII.

**Why:** Prevents secrets leaking into logcat/crash reports (INV-09).

**Verification:** `grep`-review every `logger.d(`/`logger.e(` call site by hand — this rule can't be fully mechanically verified, so re-check it whenever a new log call is added, especially once real auth exists.

### SEC-02 — Reserved (secure storage)

No encrypted-storage rule yet — no credential exists to store. `DataStorePreferencesDataSource` (used for `nameIndex`, which is not sensitive) is explicitly **not** an approved place to store a token/credential; that will need its own (likely `EncryptedSharedPreferences`- or Tink-backed) data source, added when real auth exists.

## Testing / validation

N/A beyond SEC-01's manual review — see `22-known-deviations.md` if this becomes a real gap once auth is added.

## Related documents

- [`15-observability/logging.md`](15-observability/logging.md)
- [`10-data/preferences-datastore.md`](10-data/preferences-datastore.md)
