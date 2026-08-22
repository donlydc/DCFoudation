# `:data:preferences`

**Role:** generic DataStore Preferences boundary.

**Owns:** `PreferencesDataSource` and `DataStorePreferencesDataSource`.

## AI guidance

- Keep keys and APIs generic; feature vocabulary/mapping belongs in the feature repository.
- Catch only expected storage failures at this boundary and expose safe contracts upstream.
- Do not add Room, network, UI, or feature dependencies.
- Read `architecture/10-data/preferences-datastore.md` and `mapping-boundaries.md` before API changes.
- Verify with `:data:preferences:testDebugUnitTest`.
