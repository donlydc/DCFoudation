# Cache & offline

[Back to data](README.md)

## Current implementation

**Not implemented.** There is no remote source of truth yet, so there's nothing to cache and no offline/stale-data policy to define. `GreetingPreferencesRepository`'s DataStore-backed value is already durable local state, not a cache of something else.

## Rules

**DAT-CACHE-01 — Reserved.** No rule yet — will be defined against a real repository once one combines `RemoteDataSource` + `LocalDataSource`.

## Related documents

- [`README.md`](README.md), [`repositories.md`](repositories.md)
