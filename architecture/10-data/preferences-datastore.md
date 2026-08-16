# Preferences / DataStore

[Back to data](README.md)

## Current implementation

`:data:preferences` module (Android library, namespace `com.dc.android.foundation.data.preferences`):

- `PreferencesDataSource` — `fun getInt(key, default): Flow<Int>`, `suspend fun setInt(key, value)`. Deliberately generic (int-only, key-based) — extend with more types when a feature actually needs them, not preemptively.
- `DataStorePreferencesDataSource` — wraps `DataStore<Preferences>` (injected, provided by `app/.../di/DataStoreModule.kt` via `PreferenceDataStoreFactory.create(produceFile = { context.preferencesDataStoreFile("dc_foundation_prefs") })`). Catches `IOException` from `dataStore.data` and emits `emptyPreferences()` instead of crashing (ERR-01).

Consumer: `GreetingPreferencesRepository` stores `nameIndex` under key `greeting_last_name_index`, giving the greeting feature durability across a full app restart — layered on top of (not replacing) the `SavedStateHandle` restoration from [`../04-app-bootstrap-and-lifecycle.md`](../04-app-bootstrap-and-lifecycle.md): `SavedStateHandle` answers instantly on process-death restore; DataStore is consulted asynchronously in `GreetingViewModel.init` only when `SavedStateHandle` had nothing (i.e. a real cold start).

## Rules

### DAT-01 — Features map the generic preferences API onto their own vocabulary

**Rule:** A feature owns a small repository (e.g. `GreetingPreferencesRepository`) that wraps `PreferencesDataSource` with its own key constants and method names; it never calls `PreferencesDataSource.getInt("some_key", ...)` inline from a ViewModel.

**Why:** Keeps preference keys from leaking across features and gives each feature a typed, discoverable API (DEP-03, DAT boundary).

**Verification:** `grep 'PreferencesDataSource'` outside `greeting/GreetingPreferencesRepository.kt` and the `:data:preferences` module itself — should be empty.

## Testing / validation

`GreetingViewModelTest` uses an in-memory `FakePreferencesDataSource` (test-local, `MutableStateFlow`-backed) — no real DataStore file I/O in unit tests.

## Related documents

- [`README.md`](README.md), [`mapping-boundaries.md`](mapping-boundaries.md)
