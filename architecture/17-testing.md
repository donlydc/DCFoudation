# Testing

[Back to architecture index](00-index.md)

## Scope

Unit vs. Compose UI test boundaries, fakes, and the dispatcher rule every coroutine test uses.

## Current implementation

- **Shared `:testing` kit:** `MainDispatcherRule`, `FakeDispatcherProvider`, no-op/recording Logger, AnalyticsTracker and CrashReporter implementations. The module is consumed only through test configurations and keeps feature-specific builders/fakes in their owning tests.
- **Unit tests** (`app/src/test/`): `GreetingViewModelTest` and `ArticlesViewModelTest` cover state transitions, message acknowledgement, restoration, offline-first behavior, analytics/crash calls and the permission-launch integration effect. Shared infrastructure fakes come from `:testing`; feature-specific API/DAO/provider fakes remain local. No mocking framework is required.
- **Compose UI tests** (`app/src/androidTest/`): `GreetingScreenTest` (2 tests) — exercises the stateless `GreetingScreen` directly (no ViewModel/DI involved, per DI-03/UI-01), using `createComposeRule()`. **Requires a device/emulator**; not run as part of this project's automated verification in this environment (see `PLAN_REPORT.md`'s consolidated-verification entry for exactly what was and wasn't executed) — CI (`19-ci-release-quality-gates.md`) does not currently run `connectedAndroidTest` either, since there's no emulator/device runner configured. Flagged honestly rather than claimed as passing.

## Test inventory and status (DOC-B, 2026-10-27)

All counts are `@Test` occurrences in source. Status: **NOT_RUN everywhere** — Gradle cannot build in this environment (network policy blocks `dl.google.com` and `api.foojay.io`), none of the Kotlin below has been compiled or executed by Gradle, and no test is marked TESTED. The lane A3 scratch JVM run is scratch evidence only, not a Gradle gate.

| Module | `@Test` | Lane / notes |
|---|---|---|
| `:foundation` | 12 | A3 |
| `:runtime:concurrency` | 64 | A3 (SingleFlight, JobRegistry, RetryPolicy, FlowSharing, AppCoroutineScope) |
| `:runtime:scheduling` | 79 | A3 (scheduler identity, alarm restore, retry delay) |
| `:testing` | 5 | A3 |
| `:ui:designsystem` | 30 | A4 (registry, a11y/token fixes) |
| `:platform:navigation` | 14 | A2 `DestinationTest` 9, C1 `DestinationRoutesTest` 5 |
| `:platform:permission` | 18 | A2 `PermissionStateTest` 10, C1 `PermissionStatusTest` 8 |
| `:observability` | 27 | A2 13, B2 `RedactorTest` 8 + `SecretLeakAndIsolationTest` 6 |
| `:security` | 13 | B2 `EncryptedSharedPreferencesSecureStorageTest` 10 (JVM, fake `SharedPreferences`) + exception test 3 |
| `:data:network` | 22 | B1 `NetworkErrorMappingTest` 14 + 8 existing |
| `:data:preferences` | 15 | B1 `PreferencesContractTest` 11 + 4 existing |
| `:data:database` | 12 | B3 `StalenessPolicyTest` (was 3) |
| `build-logic` | 6 | C3 added 2 generator tests |
| `:app` unit | 26 | C2: `ArticlesViewModelTest` 12 (J1/J2 journeys), `AppErrorMessagesTest` 3 |
| `:app` instrumented | 3 | compiled-only, never run |

Known gaps: MP:DB-02 `RoomTransactionRunner` commit/rollback/cancellation tests need a real `RoomDatabase` (Robolectric or `androidTest` plus `room-testing`, not in the version catalog); the real Keystore path of `SecureStorage` is instrumentation-only; instrumented journey tests (airplane mode, `GrantPermissionRule`, rotation, process death) are not written; gate R6/R7 evidence is NOT_RUN. Several test helpers are uncompiled (risk spots recorded per lane in `reports/wp/`).

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
