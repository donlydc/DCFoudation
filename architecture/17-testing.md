# Testing

[Back to architecture index](00-index.md)

## Scope

Unit vs. Compose UI test boundaries, fakes, and the dispatcher rule every coroutine test uses.

## Current implementation

- **Shared `:testing` kit:** `MainDispatcherRule`, `FakeDispatcherProvider`, no-op/recording Logger, AnalyticsTracker and CrashReporter implementations. The module is consumed only through test configurations and keeps feature-specific builders/fakes in their owning tests.
- **Unit tests** (`app/src/test/`): `GreetingViewModelTest` and `ArticlesViewModelTest` cover state transitions, message acknowledgement, restoration, offline-first behavior, analytics/crash calls and the permission-launch integration effect. Shared infrastructure fakes come from `:testing`; feature-specific API/DAO/provider fakes remain local. No mocking framework is required.
- **Compose UI tests** (`app/src/androidTest/`): `GreetingScreenTest` (2 tests) — exercises the stateless `GreetingScreen` directly (no ViewModel/DI involved, per DI-03/UI-01), using `createComposeRule()`. **Requires a device/emulator**; not run as part of this project's automated verification in this environment (see `PLAN_REPORT.md`'s consolidated-verification entry for exactly what was and wasn't executed) — CI (`19-ci-release-quality-gates.md`) does not currently run `connectedAndroidTest` either, since there's no emulator/device runner configured. Flagged honestly rather than claimed as passing.

## Rules

### TST-01 — Every `*ViewModel` gets a matching unit test using `MainDispatcherRule`

**Rule:** A ViewModel with `viewModelScope` coroutine work gets a matching `<Feature>ViewModelTest`, `@get:Rule val mainDispatcherRule = MainDispatcherRule()` from `:testing`, shared infrastructure fakes where available, and feature-owned fakes (not mocks) for domain/data collaborators unless a fake is genuinely impractical.

**Why:** Deterministic, fast, no device required (`20-feature-template.md`).

**Verification:** Every `@HiltViewModel` class has a corresponding `*Test.kt`.

### TST-02 — Stateless screens get a Compose UI test, not a mock-heavy one

**Rule:** A `*Screen.kt` (stateless) gets a `*ScreenTest.kt` that calls it directly with a hand-built `UiState`/a captured `onAction` lambda — never a full `*Route` + real ViewModel + Hilt test rule for something a plain state/callback test already covers.

**Why:** Keeps UI tests fast and independent of DI wiring (mirrors UI-01/DI-03 at the test level).

**Verification:** `GreetingScreenTest` takes `GreetingScreen(state, onAction)` directly, no `hiltViewModel()`/`@HiltAndroidTest`.

## Testing / validation

See `PLAN_REPORT.md` for the actual, run, pass/fail evidence — this document describes the pattern, not a claim that everything above has been executed in every environment.

## Related documents

- [`05-presentation-udf.md`](05-presentation-udf.md), [`09-concurrency-and-threading.md`](09-concurrency-and-threading.md), [`20-feature-template.md`](20-feature-template.md)
