# Presentation & UDF

[Back to architecture index](00-index.md)

## Scope

The canonical feature shape: Route/ViewModel/UiState/UiAction/Screen plus an optional UI-integration effect, and the UDF rule (state down, actions up). Concrete per-file responsibilities are generalized into [`20-feature-template.md`](20-feature-template.md).

## Current implementation

Both reference features use this contract: `greeting` proves the minimal state/action/persistence shape; `articles` proves loading, stale-cache state, permission integration, analytics and an offline-first repository. The canonical file shape is:

```text
GreetingUiState.kt    data class — everything GreetingScreen needs to render
GreetingUiAction.kt   sealed interface — user intents (Refresh)
GreetingViewModel.kt  @HiltViewModel — owns StateFlow<GreetingUiState> and onAction(action)
GreetingScreen.kt     stateless @Composable(state, onAction) — no DI, no ViewModel import; previewable
GreetingRoute.kt      stateful @Composable — the only place that calls hiltViewModel(), collects state
                        with collectAsStateWithLifecycle(), renders pending messages, hosts
                        the feature's own Scaffold/SnackbarHost
```

`MainActivity` owns the themed top-level navigation shell and selects `GreetingRoute`/`ArticlesRoute`; feature UI and data logic remain outside the activity.

## Rules

### UI-01 — State flows down, actions flow up

**Rule:** `GreetingScreen` (and every future `*Screen`) takes `state: UiState` and `onAction: (UiAction) -> Unit`; it never reads a ViewModel or mutates state itself.

**Why:** Keeps the screen pure/previewable/testable in isolation from DI and coroutines (DI-03, DEP-03).

**Verification:** `GreetingScreen.kt` has zero `hiltViewModel`/`ViewModel` import; its two `@Preview`s construct `GreetingUiState` by hand.

### UI-02 — ViewModel outcomes become state; effects are limited to immediate UI integration

**Rule:** A ViewModel-originated outcome that the user must observe (message, success/failure, validated navigation readiness) updates `UiState`. A transient message stays present until the Route displays it and sends a `UserMessageShown` action. `UiEffect` is optional and reserved for immediate framework/UI integration that is itself initiated by a foreground user action, such as launching the OS permission dialog; it is not a second transport for business outcomes.

**Why:** State survives temporary collector/lifecycle loss and remains testable/reproducible. Keeping framework launchers in the Route still prevents Android types from leaking into ViewModels.

**Verification:** `GreetingUiState.userMessage` is cleared only by `GreetingUiAction.UserMessageShown`; `greeting` has no effect stream. `articles` retains only `LaunchNotificationPermissionRequest` as an effect because the Route owns the framework permission launcher.

### UI-03 — No universal `BaseViewModel`

**Rule:** ViewModels implement `onAction`/state exposure directly; shared behavior (if it emerges) becomes a small reusable delegate/helper, not a base class every ViewModel inherits.

**Why:** Plan's explicit anti-pattern — a `BaseViewModel` becomes a dumping ground over time.

**Verification:** `GreetingViewModel : ViewModel()` directly, not `: BaseViewModel()`. Re-check this the moment a second ViewModel exists — do they actually share logic, or just superficially look similar?

## Testing / validation

`GreetingViewModelTest` covers initial state, state transitions, durable message acknowledgement, cycling, and `SavedStateHandle`/DataStore restoration. Coroutine tests use the shared `MainDispatcherRule` from `:testing`.

## Related documents

- [`04-app-bootstrap-and-lifecycle.md`](04-app-bootstrap-and-lifecycle.md)
- [`06-navigation.md`](06-navigation.md)
- [`20-feature-template.md`](20-feature-template.md)
- [`08-dependency-injection-hilt.md`](08-dependency-injection-hilt.md) (DI-03)
