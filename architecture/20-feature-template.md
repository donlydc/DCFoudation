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
| `<Feature>UiEffect.kt` | Sealed interface — one-shot events (UI-02). Omit the file entirely if the feature has none yet — don't invent one. |
| `<Feature>ViewModel.kt` | `@HiltViewModel`, `@Inject constructor(...)`. Exposes `StateFlow<UiState>` + `onAction(action: UiAction)` (+ effect `Flow` if `UiEffect` exists). Extends `ViewModel()` directly (UI-03 — no `BaseViewModel`). |
| `<Feature>Screen.kt` | Stateless `@Composable(state, onAction, modifier)`. No DI, no ViewModel import (UI-01). Carries its own `@Preview`s. |
| `<Feature>Route.kt` | Stateful `@Composable` — the only place that calls `hiltViewModel()`, collects state/effects, hosts the feature's `Scaffold`. |
| Any `<Thing>Provider.kt` + `<Feature>Module.kt` the feature needs | Colocated in the same package, not a shared `AppModule` (DI-02). |

Any file the feature doesn't need yet (`UiEffect`, a provider/module) is simply omitted — don't pre-create empty ones (mirrors the anti-overengineering constraint applied to `architecture/` itself in DEC-02).

## Rules

Governed by [`05-presentation-udf.md`](05-presentation-udf.md) (UI-01..03) and [`08-dependency-injection-hilt.md`](08-dependency-injection-hilt.md) (DI-01..04). No new rule IDs here — this document is a checklist, not a new rule source.

## Testing / validation

Every `<Feature>ViewModel.kt` gets a matching `app/src/test/.../<feature>/<Feature>ViewModelTest.kt` covering its state transitions, using `MainDispatcherRule` — see `GreetingViewModelTest.kt` for the pattern.

## Related documents

- [`05-presentation-udf.md`](05-presentation-udf.md)
- [`08-dependency-injection-hilt.md`](08-dependency-injection-hilt.md)
