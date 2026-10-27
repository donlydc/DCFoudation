# Feature template

[Back to architecture index](00-index.md)

## Scope

The file-by-file shape a new feature package follows, generalized from the working `greeting/` example (05-presentation-udf.md).

## Template

A feature package (e.g. `app/src/main/java/com/dc/android/foundation/<feature>/`) contains:

| File | Responsibility |
|---|---|
| `<Feature>UiState.kt` | Data class — everything the screen needs to render. No infra types (DEP-03). |
| `<Feature>UiAction.kt` | Sealed interface — user intents sent up from the screen. |
| `<Feature>UiEffect.kt` | Optional sealed interface for immediate Route-owned framework integration only (for example, launching an OS permission dialog). User-visible ViewModel outcomes belong in `UiState` (UI-02). |
| `<Feature>ViewModel.kt` | `@HiltViewModel`, `@Inject constructor(...)`. Exposes `StateFlow<UiState>` + `onAction(action: UiAction)` (+ a narrowly scoped effect `Flow` only when required). Extends `ViewModel()` directly (UI-03 — no `BaseViewModel`). |
| `<Feature>Screen.kt` | Stateless `@Composable(state, onAction, modifier)`. No DI, no ViewModel import (UI-01). |
| `<Feature>Preview.kt` | Isolated Compose preview fixture using `DcTheme`; keeps preview-only imports out of production screen code. |
| `<Feature>Route.kt` | Stateful `@Composable` — the only place that calls `hiltViewModel()`, collects state/effects, hosts the feature's `Scaffold`. |
| Any `<Thing>Provider.kt` + `<Feature>Module.kt` the feature needs | Colocated in the same package, not a shared `AppModule` (DI-02). |

Any file the feature doesn't need yet (`UiEffect`, a provider/module) is simply omitted — don't pre-create empty ones. A transient user message is modeled as nullable/queued state plus an acknowledgement action, not as a reason to create an effect file.

## Generator

`:app` applies `dc.feature.generator`, which registers a safe `createFeature` task. Minimal use:

```powershell
.\gradlew.bat :app:createFeature -PfeatureName=Profile
```

Optional capabilities are explicit booleans and default to `false`:

```powershell
.\gradlew.bat :app:createFeature -PfeatureName=Orders `
  -Pnavigation=true -Pnetwork=true -Proom=true `
  -Ppermission=true -PbackgroundWork=true
```

`-PfeaturePackage=com.example.app` overrides the Android namespace when needed. The generator validates Kotlin identifiers/package segments, plans every output before writing, fails if any target file already exists, and opens files with create-new semantics. Permission is the only current flag that creates `UiEffect`; other optional flags create narrowly scoped destination/API/DAO/scheduler boundaries without invented business logic.

Since lane C3 (`b7487f0`) `createFeature` also writes `architecture/features/<name>.md` (DRAFT, Last source audit NOT_RUN, Test status NOT_RUN; headings identical to `features/TEMPLATE.md`, known facts filled, the rest TODO) and joins it to the same all-or-nothing existing-file check. Registering the feature in `context-map.yaml` and `module-map.md` stays a manual step (the generated page is not discoverable through the context map until then).

## Rules

Governed by [`05-presentation-udf.md`](05-presentation-udf.md) (UI-01..03) and [`08-dependency-injection-hilt.md`](08-dependency-injection-hilt.md) (DI-01..04). No new rule IDs here — this document is a checklist, not a new rule source.

## Testing / validation

Every `<Feature>ViewModel.kt` gets a matching test covering state transitions, using `MainDispatcherRule` and reusable infrastructure fakes from `:testing`; see `GreetingViewModelTest.kt`/`ArticlesViewModelTest.kt` for the pattern.

`FeatureScaffoldGeneratorTest` verifies minimal/optional output, invalid-name rejection, and collision/no-overwrite behavior in temporary directories; lane C3 added the architecture-page content and heading-parity checks (written, NOT_RUN).

## Related documents

- [`05-presentation-udf.md`](05-presentation-udf.md)
- [`08-dependency-injection-hilt.md`](08-dependency-injection-hilt.md)
