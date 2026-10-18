# Preferences / DataStore

[Back to data](README.md)

## Current implementation

`:data:preferences` module (Android library, namespace `com.dc.android.foundation.data.preferences`):

- `PreferencesDataSource` — typed `observe`, `get`, `set`, `remove`, `clear`, and atomic `edit` APIs built on `PreferenceKey<T>`. It supports primitive, enum, string-set and Kotlin-serialization JSON keys, each with a declared safe default.
- `DataStorePreferencesDataSource` — wraps `DataStore<Preferences>` (injected, provided by `app/.../di/DataStoreModule.kt` via `PreferenceDataStoreFactory.create(produceFile = { context.preferencesDataStoreFile("dc_foundation_prefs") })`). It runs idempotent, versioned `PreferencesMigration`s before access; migration and edit callbacks are non-suspending editor-only mutations, so the storage transaction never holds a lock across a callback suspension (INV-05). It catches `IOException` from `dataStore.data` and emits `emptyPreferences()` instead of crashing (ERR-01); malformed persisted enum/JSON values resolve to the key default.

Consumers: `GreetingPreferencesRepository` stores `nameIndex` under key `greeting_last_name_index`; `AppConfigurationRepository` stores theme mode, semantic UI scale and dynamic-color opt-in. Both own their typed keys and map the generic data source into caller vocabulary. Locale is intentionally persisted by Android/AppCompat rather than duplicated here.

## Rules

### DAT-01 — Features map the generic preferences API onto their own vocabulary

**Rule:** A feature or application-level capability owns a small repository (e.g. `GreetingPreferencesRepository` or `AppConfigurationRepository`) that wraps `PreferencesDataSource` with its own key constants and method names; it never calls `PreferencesDataSource` inline from a ViewModel.

**Why:** Keeps preference keys from leaking across features and gives each feature a typed, discoverable API (DEP-03, DAT boundary).

**Verification:** Outside `:data:preferences`, direct `PreferencesDataSource` usage is limited to explicit mapping repositories such as `GreetingPreferencesRepository` and `AppConfigurationRepository`.

## Testing / validation

`GreetingViewModelTest` uses the shared `:testing` `InMemoryPreferencesDataSource` (`MutableStateFlow`-backed) — no real DataStore file I/O in feature unit tests. `DataStorePreferencesDataSourceTest` uses a deterministic in-memory `DataStore` contract double to verify migration ordering, atomic edits, malformed enum fallback and expected `IOException` fallback at the real storage boundary.

## Related documents

- [`README.md`](README.md), [`mapping-boundaries.md`](mapping-boundaries.md)
