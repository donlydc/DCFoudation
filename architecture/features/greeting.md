# FEATURE: greeting
Status: DRAFT
Verified commit: UNKNOWN (source read at HEAD 2c0ae05b670964686e09dbc7cde7fee4e19f5de7 plus uncommitted docs; no VERIFIED claim)
Last source audit: 2026-10-27
Test status: NOT_RUN (evidence: reports/baseline-verification.md)

## 1. Identity and scope
- Package / owning module: `com.dc.android.foundation.greeting` in `:app` (`app/src/main/java/com/dc/android/foundation/greeting/`)
- Purpose: smallest canonical UDF feature; cycles a name (Android, World, DC) and persists the index. Non-goals: no network, Room, or domain layer.
- Role: reference (generalised by `20-feature-template.md`; the generator scaffold mirrors it)

## 2. Presentation contract (UDF — UI-01/UI-02)
- Route (stateful): `GreetingRoute` (`GreetingRoute.kt:22`); `hiltViewModel()`, `collectAsStateWithLifecycle`, hosts `DcScaffold` + `DcSnackbarHostState`/`DcSnackbarHost` and the typed `DcSnackbarEffect(DcSnackbarMessage?, state, ...)` (lane C2 `64da185`; the deprecated `String?` overload is no longer used in `:app`)
- Screen (stateless): `GreetingScreen(state, onAction, modifier)` (`GreetingScreen.kt:19`), two previews
- ViewModel: `GreetingViewModel` (`@HiltViewModel`, extends `ViewModel()`, UI-03)
- UiState: `GreetingUiState(message, isRefreshing=false, userMessage: String?=null)`; `userMessage` is durable until acknowledged
- UiAction: sealed `GreetingUiAction` = `Refresh`, `UserMessageShown`
- UiEffect: none (no `GreetingUiEffect.kt`)

## 3. DI and ownership
- Constructor deps: `GreetingProvider` (feature), `GreetingPreferencesRepository` (feature, wraps `PreferencesDataSource` from `:data:preferences`), `DispatcherProvider` (`:runtime:concurrency`), `Logger` (`:observability`), `SavedStateHandle`
- Hilt: `GreetingModule` (`@Module @InstallIn(SingletonComponent)`, `@Binds` `DefaultGreetingProvider` -> `GreetingProvider`, unscoped; DI-02). `GreetingPreferencesRepository` and `DefaultGreetingProvider` are `@Inject` constructors. `PreferencesDataSource` is provided in `app/.../di/DataStoreModule.kt`.
- State owner (CFG-02): name index is feature-local. Short-term owner `SavedStateHandle["nameIndex"]`; long-term owner `GreetingPreferencesRepository` (DataStore key `greeting_last_name_index`, `PreferenceKey.IntKey`, default 0).

## 4. Data flow
```mermaid
flowchart LR
  Screen -->|GreetingUiAction.Refresh| VM[GreetingViewModel]
  VM -->|setLastNameIndex| Repo[GreetingPreferencesRepository] --> DS[PreferencesDataSource / DataStore]
  VM -->|greetingFor on dispatcherProvider.default| Prov[GreetingProvider]
  DS -->|lastNameIndex Flow, first() at init| Repo --> VM
  VM -->|StateFlow GreetingUiState| Screen
```
- Init: if `SavedStateHandle` has no index, VM reads `lastNameIndex().first()`; a non-zero value updates `nameIndex` and `message`. Refresh: `isRefreshing=true`, advance index mod 3, persist, rebuild message, then `userMessage="Refreshed"` (hard-coded English string in `GreetingViewModel`, `userMessage` is still `String?`; left as a HANDOVER follow-up by lane C2).
- Error mapping: none implemented. No call path produces `AppError`; a `PreferencesDataSource` failure in `refresh()`/`init` is not caught in the VM (ERR-01 not exercised by this feature).
- Threading (THR-02): message built via `withContext(dispatcherProvider.default)`; no hard-coded `Dispatchers.*`. DataStore I/O threading is owned by `:data:preferences`.

## 5. Navigation entry
- `Destination.Greeting` (`@Serializable data object`, `:platform:navigation`). Registered in `MainActivity.kt` `DcNavHost`: `composable<Destination.Greeting> { GreetingRoute() }`; it is the `startDestination`. NAV-01 satisfied (feature imports no `androidx.navigation`). No arguments, no deep links. Bottom/rail item id `"greeting"` (string `R.string.nav_greeting`).

## 6. Design system usage
- `DcTheme` (provided by `DcAppRoot`), `DcScaffold`, `DcSnackbarHostState`/`DcSnackbarHost`/`DcSnackbarEffect`, `DcText`, `DcButton(loading=)`, `LocalDcSpacing` (`spacing.medium/small`). No raw `.dp` or colors (DS-01/DS-03 hold). Hard-coded English strings "Refresh"/"Refreshing..." and "Refreshed" in code (not `stringResource`); DS strings policy is DEC-08.

## 7. Tests and evidence
- ViewModel unit test (TST-01): `app/src/test/java/com/dc/android/foundation/greeting/GreetingViewModelTest.kt` - 6 tests (initial state, Refresh advance, wrap-around, user message ack, SavedStateHandle restore, persisted-preference fallback); uses `MainDispatcherRule`, `FakeDispatcherProvider`, `NoOpLogger`, `InMemoryPreferencesDataSource` from `:testing`. Test status: NOT_RUN.
- Compose UI test (TST-02): `app/src/androidTest/java/com/dc/android/foundation/greeting/GreetingScreenTest.kt` - 2 tests (`displaysTheGivenMessage`, `refreshButtonSendsRefreshAction`) on stateless `GreetingScreen`. Compiled only, never executed; needs device/emulator (baseline B6 NOT_RUN).
- Commands (not run here): `./gradlew :app:testDebugUnitTest --tests '*GreetingViewModelTest'`; `./gradlew :app:connectedDebugAndroidTest`.
- Test status: NOT_RUN (evidence: reports/baseline-verification.md)

## 8. Known debt / decisions
- KD-02 (Open, partially mitigated: static `tools/check_architecture.py --boundaries-only`; CI run NOT_RUN). DEC-04 (second reference feature `articles`; greeting stays minimal). No greeting-specific KD.
- Observation: no `GreetingRoute` test; no failure-path test for preference writes.

## 9. AI playbook
- Read -> Graph -> Propose -> Edit -> Test -> Update docs -> Handover.
- Read first: `GreetingViewModel.kt`, `GreetingUiState.kt`, `05-presentation-udf.md`, `20-feature-template.md`. Start query: `codegraph_explore "GreetingRoute GreetingViewModel GreetingUiState GreetingUiAction GreetingScreen GreetingModule GreetingPreferencesRepository"` with `projectPath` = repo root.
- Add an action: add a `GreetingUiAction` case, handle it in the exhaustive `when` in `onAction`, add a field to `GreetingUiState` only if the screen renders it, add a VM test, then wire the Screen. Do not add a `UiEffect` for messages (UI-02).
- DoD: compiles, `GreetingViewModelTest` green, Screen has no DI/VM import, docs updated if the contract changed.
- Sibling features: `createFeature` scaffold (`20-feature-template.md`).
- Rule glosses (text lives in the named doc; gloss only, the rule doc is authoritative): `DI-02` (08-dependency-injection-hilt.md): bindings live next to the interface/impl they wire; `THR-02` (09-concurrency-and-threading.md): dispatcher access goes through DispatcherProvider; `ERR-01` (11-error-and-result-model.md): infrastructure exceptions are caught at the data-source boundary, not in the ViewModel; `UI-01` (05-presentation-udf.md): state flows down, actions flow up; `UI-02` (05-presentation-udf.md): ViewModel outcomes become state; effects only for immediate UI integration; `UI-03` (05-presentation-udf.md): no universal BaseViewModel; `DS-01` (07-design-system/README.md): feature UI themes through DcTheme, not raw tokens; `DS-03` (07-design-system/README.md): no raw spacing/shape literals in feature UI; `NAV-01` (06-navigation.md): feature code does not depend on the navigation engine; only :app does; `CFG-02` (16-config-feature-flags-environments.md): user configuration has one state owner and a typed mutation API; `TST-01` (17-testing.md): every *ViewModel gets a unit test using MainDispatcherRule; `TST-02` (17-testing.md): stateless screens get a Compose UI test, not a mock-heavy one.
