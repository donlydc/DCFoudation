# Repositories

[Back to data](README.md)

## Current implementation

One repository exists: `GreetingPreferencesRepository` (`app/.../greeting/`). Shape: constructor-injected data source(s), `Flow`-returning reads, `suspend fun` writes, feature-specific method names (`lastNameIndex()`, `setLastNameIndex(index)`) instead of generic `get`/`set`.

No `RemoteDataSource` exists — no repository combines local + remote yet, so there's no real cache/source-of-truth merge logic to document (see [`cache-and-offline.md`](cache-and-offline.md)).

## Rules

Governed by [`preferences-datastore.md`](preferences-datastore.md) DAT-01. No new rule IDs here yet — a repository combining `RemoteDataSource` + `LocalDataSource` will earn its own rule ID (`DAT-02`+) when one is built.

## Testing / validation

`GreetingViewModelTest` covers `GreetingPreferencesRepository` indirectly through `GreetingViewModel` (real repository class, fake data source underneath).

## Related documents

- [`README.md`](README.md), [`mapping-boundaries.md`](mapping-boundaries.md)
