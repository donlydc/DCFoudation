# Data

[Back to architecture index](../00-index.md)

## Contract

```text
Repository (feature-owned mapping boundary, e.g. GreetingPreferencesRepository)
  ├─ RemoteDataSource     — ArticlesNetworkDataSource (Retrofit), implemented
  ├─ LocalDataSource      — ArticleDao (Room) / PreferencesDataSource (DataStore), implemented
  └─ Cache/offline policy — ArticlesRepository uses Room as source of truth
```

A feature never talks to `DataStore`/`Room`/Retrofit directly — it goes through a repository that maps the generic data-source API onto the feature's own vocabulary (DAT-01, [`mapping-boundaries.md`](mapping-boundaries.md)).

## Current implementation

- `:data:preferences` — typed `PreferencesDataSource` keys and atomic editor API + `DataStorePreferencesDataSource` (real impl, versioned migrations, safe defaults and `IOException` handling at the boundary per ERR-01).
- `app/.../greeting/GreetingPreferencesRepository` — the one repository that exists, wrapping `PreferencesDataSource` with a feature-specific key (`greeting_last_name_index`).
- `:data:network` provides business-neutral Retrofit/OkHttp construction and `NetworkResult`; `articles` owns its DTO/API/data-source mapping.
- `:data:database` provides the current Room database and articles DAO/entity reference schema.
- `ArticlesRepository` combines remote and local sources: Room emits cached data immediately; refresh upserts network results and preserves stale cache on failure.

## Related documents

- [`repositories.md`](repositories.md), [`network.md`](network.md), [`persistence-room.md`](persistence-room.md), [`preferences-datastore.md`](preferences-datastore.md), [`cache-and-offline.md`](cache-and-offline.md), [`mapping-boundaries.md`](mapping-boundaries.md)
- [`../11-error-and-result-model.md`](../11-error-and-result-model.md) (ERR-01)
