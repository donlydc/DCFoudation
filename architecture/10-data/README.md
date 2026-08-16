# Data

[Back to architecture index](../00-index.md)

## Contract

```text
Repository (feature-owned mapping boundary, e.g. GreetingPreferencesRepository)
  ├─ RemoteDataSource   — not implemented (no network need yet)
  ├─ LocalDataSource    — PreferencesDataSource (DataStore), implemented
  └─ Cache/offline policy — not implemented (nothing to cache yet)
```

A feature never talks to `DataStore`/`Room`/Retrofit directly — it goes through a repository that maps the generic data-source API onto the feature's own vocabulary (DAT-01, [`mapping-boundaries.md`](mapping-boundaries.md)).

## Current implementation (Phase G)

- `:data:preferences` — `PreferencesDataSource` (generic `getInt`/`setInt` contract) + `DataStorePreferencesDataSource` (real impl, catches `IOException` at the boundary per ERR-01).
- `app/.../greeting/GreetingPreferencesRepository` — the one repository that exists, wrapping `PreferencesDataSource` with a feature-specific key (`greeting_last_name_index`).
- Room (`persistence-room.md`) and network (`network.md`) are **not implemented** — no feature needs structured local storage or a remote API yet.

## Related documents

- [`repositories.md`](repositories.md), [`network.md`](network.md), [`persistence-room.md`](persistence-room.md), [`preferences-datastore.md`](preferences-datastore.md), [`cache-and-offline.md`](cache-and-offline.md), [`mapping-boundaries.md`](mapping-boundaries.md)
- [`../11-error-and-result-model.md`](../11-error-and-result-model.md) (ERR-01)
