# Mapping boundaries

[Back to data](README.md)

## Current implementation

Only one boundary exists today:

```text
DataStore Preferences (raw key/int) → PreferencesDataSource (generic get/setInt)
    → GreetingPreferencesRepository (feature vocabulary: lastNameIndex)
    → GreetingViewModel (GreetingUiState.message)
```

No DTO↔domain↔UI-model chain exists yet (no network), and no Room entity↔domain mapping exists yet (no Room). Both will get their own section here when Phase G actually needs them for a real feature — not sketched in advance.

## Rules

**DAT-01** (defined in [`preferences-datastore.md`](preferences-datastore.md)) is the only mapping rule in force today.

## Related documents

- [`README.md`](README.md), [`repositories.md`](repositories.md), [`preferences-datastore.md`](preferences-datastore.md)
