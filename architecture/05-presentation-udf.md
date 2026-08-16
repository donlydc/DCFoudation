# Presentation & UDF

[Back to architecture index](00-index.md)

## Scope

The canonical feature shape: Route/ViewModel/UiState/UiAction/UiEffect/Screen, and the UDF rule (state down, actions up). Concrete per-file responsibilities are generalized into [`20-feature-template.md`](20-feature-template.md).

## Current implementation

`greeting/` is the first (and so far only) feature built to this contract:

```text
GreetingUiState.kt    data class — everything GreetingScreen needs to render
GreetingUiAction.kt   sealed interface — user intents (Refresh)
GreetingUiEffect.kt   sealed interface — one-shot events (ShowMessage), never replayed as state
GreetingViewModel.kt  @HiltViewModel — owns StateFlow<GreetingUiState>, Channel<GreetingUiEffect>, onAction(action)
GreetingScreen.kt     stateless @Composable(state, onAction) — no DI, no ViewModel import; previewable
GreetingRoute.kt       stateful @Composable — the only place that calls hiltViewModel(), collects state
                        with collectAsStateWithLifecycle(), collects effects with a LaunchedEffect, hosts
                        the feature's own Scaffold/SnackbarHost
```

`MainActivity` is now just `DcTheme { GreetingRoute() }` — the app shell, not feature logic.

## Rules

### UI-01 — State flows down, actions flow up

**Rule:** `GreetingScreen` (and every future `*Screen`) takes `state: UiState` and `onAction: (UiAction) -> Unit`; it never reads a ViewModel or mutates state itself.

**Why:** Keeps the screen pure/previewable/testable in isolation from DI and coroutines (DI-03, DEP-03).

**Verification:** `GreetingScreen.kt` has zero `hiltViewModel`/`ViewModel` import; its two `@Preview`s construct `GreetingUiState` by hand.

### UI-02 — One-shot events use a `UiEffect` channel, not `UiState`

**Rule:** Events that should fire exactly once (snackbar, navigation trigger) go through a `Channel`/`SharedFlow` of a `UiEffect` sealed type, collected in the Route via `LaunchedEffect`; they are never stored as a boolean/nullable field on `UiState` (which would re-fire on recomposition/rotation).

**Why:** Avoids the classic "snackbar shows again on rotation" bug class.

**Verification:** `GreetingViewModel._effects` is a `Channel`, not a `StateFlow` field; `GreetingRoute`'s `LaunchedEffect(viewModel) { viewModel.effects.collect { ... } }` keyed on `viewModel` (stable across recomposition) not `Unit`.

### UI-03 — No universal `BaseViewModel`

**Rule:** ViewModels implement `onAction`/state exposure directly; shared behavior (if it emerges) becomes a small reusable delegate/helper, not a base class every ViewModel inherits.

**Why:** Plan's explicit anti-pattern — a `BaseViewModel` becomes a dumping ground over time.

**Verification:** `GreetingViewModel : ViewModel()` directly, not `: BaseViewModel()`. Re-check this the moment a second ViewModel exists — do they actually share logic, or just superficially look similar?

## Testing / validation

`GreetingViewModelTest` (5 tests) covers: initial state, a single state transition, action cycling back to the start, effect emission (collected via `Channel.receiveAsFlow().toList(...)` in a background `launch`), and `SavedStateHandle` restoration. Uses `MainDispatcherRule` (`app/src/test/.../MainDispatcherRule.kt`) to make `viewModelScope` deterministic under `runTest`.

## Related documents

- [`04-app-bootstrap-and-lifecycle.md`](04-app-bootstrap-and-lifecycle.md)
- [`06-navigation.md`](06-navigation.md)
- [`20-feature-template.md`](20-feature-template.md)
- [`08-dependency-injection-hilt.md`](08-dependency-injection-hilt.md) (DI-03)
