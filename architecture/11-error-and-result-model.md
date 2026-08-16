# Error & result model

[Back to architecture index](00-index.md)

## Scope

How infrastructure exceptions get mapped before they cross into presentation.

## Current implementation

No generic `Resource<T>`/`Result<T>` wrapper exists — per the plan ("do not force every operation into one generic `Resource<T>` if feature state is more expressive"), `GreetingUiState` just doesn't have an error case yet because nothing user-visible can currently fail: `DataStorePreferencesDataSource.getInt` catches `IOException` at the data-source boundary and falls back to `emptyPreferences()` (→ the caller's `default`) instead of throwing, so `GreetingViewModel` never sees an exception to handle.

## Rules

### ERR-01 — Infrastructure exceptions are caught at the data-source boundary, not the ViewModel

**Rule:** A `*DataSource` catches its own infrastructure exceptions (`IOException`, network failures, etc.) and maps them to a safe default or a domain-level error type; a ViewModel never catches `IOException`/`SQLException`/HTTP-client exceptions directly.

**Why:** Keeps infra exception types out of presentation-layer code (DEP-04, INV-07).

**Verification:** `DataStorePreferencesDataSource.getInt` — `.catch { cause -> if (cause is IOException) emit(emptyPreferences()) else throw cause }`. `grep 'catch.*IOException'` should only ever appear in `*DataSource` classes.

## Testing / validation

No test yet exercises the catch branch (would need to inject a failing `DataStore`) — flagged here rather than silently skipped; add one the day a second data source's failure mode actually needs verifying.

## Related documents

- [`10-data/preferences-datastore.md`](10-data/preferences-datastore.md)
