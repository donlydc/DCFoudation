# MODULE: :platform:permission — platform-permission
Status: DRAFT
Verified commit: UNKNOWN (drafted against working tree at HEAD 2c0ae05b; not audited)
Last source audit: NOT_RUN
Test status: NOT_RUN (evidence: reports/baseline-verification.md)

> Doc status (`DRAFT|VERIFIED|NEEDS_REFRESH`) and test status (`NOT_RUN|TESTED|FAILED|BLOCKED`) are separate vocabularies; see `TEMPLATE.md`.

## 1. Identity and responsibility
- Owns: engine-neutral runtime-permission state `PermissionState` (`NotRequested`, `Granted`, `Denied`) and the single concrete adapter `NotificationPermission` for `android.permission.POST_NOTIFICATIONS`.
- Does not own: launching the OS dialog (done by `ArticlesRoute` via `rememberLauncherForActivityResult`), rationale/settings flow, rationale UI, any permission other than notifications. No state machine or requester interface exists.
- Public package/API: `com.dc.android.foundation.platform.permission` -> `PermissionState`, `NotificationPermission.{permissionName, currentState(Context), fromRequestResult(Boolean)}`.
- Consumer(s): `:app` `articles` package: `ArticlesRoute` (all three members), `ArticlesSyncWorker.postSyncNotification` (`currentState`), `ArticlesUiAction`/`ArticlesUiState`/`ArticlesScreen`/`ArticlesViewModel` (`PermissionState`).

## 2. Context and discovery
- Read first: `architecture/00-index.md`, `architecture/13-permissions.md` (PERM-01), `architecture/dependency-rules.md`.
- Main code: `platform/permission/src/main/java/com/dc/android/foundation/platform/permission/{PermissionState,NotificationPermission}.kt`.
- Tests: `platform/permission/src/test/.../PermissionStateTest.kt` (10 tests, lane A2; `test_roots: ["platform/permission/src/test"]`; run NOT_RUN): `fromRequestResult` mapping, state transitions, exhaustive `when`, `permissionName`. Consumer side: `app/src/test/.../articles/ArticlesViewModelTest.kt` has `UpdateNotificationPermissionState updates the permission field in state` (CodeGraph).
- Build config: `platform/permission/build.gradle.kts` (`dc.android.library`, `dc.compose`, `dc.testing`; `implementation` of `androidx.core.ktx`, `androidx.activity.compose`).
- CodeGraph entry symbols/callers/callees: `NotificationPermission` <- `ArticlesRoute`, `ArticlesSyncWorker`; callees `ContextCompat.checkSelfPermission`, `Build.VERSION.SDK_INT`.

## 3. Dependencies and boundaries
- Allowed: no project dependencies (`:platform:permission -> (none)`). Android `core-ktx` for `ContextCompat`.
- Forbidden: edges to `:app`/features (DEP-01), to `:data:*`/`:security`/`:ui:designsystem` (PROPOSED policy, not a rule); permission UI inside the platform module (MP:PERM-04 intent); production edge to `:testing` (BLD-03).
- Exceptions (DEC/KD): none recorded. KD-02 (Open, partially mitigated: static `tools/check_architecture.py --boundaries-only`; CI run NOT_RUN) applies.

## 4. Data/control flow
```mermaid
flowchart LR
  Screen[ArticlesScreen click] -->|RequestNotificationPermission| VM[ArticlesViewModel]
  VM -->|effect LaunchNotificationPermissionRequest| Route[ArticlesRoute]
  Route -->|launch permissionName| OS[OS dialog]
  OS -->|granted Boolean| NP[NotificationPermission.fromRequestResult]
  Route -->|currentState context| NP
  NP -->|UpdateNotificationPermissionState| VM
```
Source refs: `ArticlesViewModel.onAction`/`requestNotificationPermission`; `ArticlesRoute` (launcher, initial `currentState(context)` in `LaunchedEffect(Unit)`); `ArticlesSyncWorker.kt:68` guards posting on `currentState != Granted`.

## 5. Contracts
- `PermissionState`: sealed interface, 3 `data object`s. `NotRequested` is a caller-seeded default; `NotificationPermission.currentState` never returns it (KDoc).
- `currentState(context)`: pre-API 33 returns `Granted` (`Build.VERSION_CODES.TIRAMISU` check); on 33+ maps `checkSelfPermission` to `Granted`/`Denied`. `fromRequestResult(granted)`: `Granted`/`Denied`.
- Denied vs "denied with rationale" vs permanently denied is NOT modelled (KDoc: three states by design; no `shouldShowRequestPermissionRationale` tracking, no settings deep link).
- Activity for rationale: UNKNOWN / not designed. No code in the repo obtains an `Activity` or calls `shouldShowRequestPermissionRationale` (scan of `*.kt`: only KDoc mentions in `PermissionState.kt`). `ArticlesRoute` holds `LocalContext.current` (a `Context`) and an `ActivityResultContracts.RequestPermission` launcher only. Under PERM-01 any such unwrap belongs in the Route, not in this module or the ViewModel; where it lives and whether `PermissionState` gains a 4th state is an open decision (needs a DEC before `MP:PERM-01`/`MP:PERM-04` work).
- Errors: none thrown, no `AppError` mapping. Cancellation/lifecycle: none owned; the launcher lifecycle is in the Route. Threading: synchronous, cheap framework call; safe on main.

## 6. DI, configuration and ownership
- No Hilt binding; `NotificationPermission` is a Kotlin `object`, not injected (so tests cannot substitute it; consumers test via `PermissionState` in UiState). `POST_NOTIFICATIONS` is declared in `app/src/main/AndroidManifest.xml:6`.

## 7. Edge cases, security and performance
- Request is user-initiated (button in `ArticlesScreen` shown while state != `Granted`); the code audited does not auto-prompt at startup (only `currentState` is read in `LaunchedEffect`). No secrets/PII.
- A process-death or rotation re-reads `currentState`; a permanent denial is indistinguishable from a first denial (see section 5).

## 8. Tests and evidence
- Unit: none in module. Integration: none. Instrumented: none.
- Commands (once Gradle runs): `./gradlew :platform:permission:testDebugUnitTest`; consumer check `./gradlew :app:testDebugUnitTest --tests "*ArticlesViewModelTest"`.
- How to unit-test (lane A2 added the module's plain-JVM tests; no Robolectric dependency and no `isReturnDefaultValues` setting exist):
  - Done, plain JVM: `PermissionState`, `NotificationPermission.fromRequestResult(Boolean)` (pure) and the `permissionName` constant (`PermissionStateTest`).
  - GAP, needs more than plain JVM: `currentState(Context)` reads static `Build.VERSION.SDK_INT` (below 33 it returns Granted; 33+ reads the live grant) and calls `ContextCompat.checkSelfPermission`. PROPOSED options, none present: (a) extract a pure `(sdkInt, checkResult) -> PermissionState` function and test that on JVM; (b) Robolectric with explicit SDK levels (new dependency, needs an ADR); (c) instrumented test on a device/emulator (pre-33 and 33+ behaviour needs two API levels). Rule PERM-01 OS-level behaviour therefore stays open.
  - Consumer coverage also in `ArticlesViewModelTest` (`UpdateNotificationPermissionState updates the permission field in state`).
- Last verified result: none; tests exist (lane A2) but no Gradle run has succeeded (NOT_RUN).

## 9. Known debt / decisions
- PROPOSED backlog (MP 4.8, none implemented): MP:PERM-01 state machine + notification contract with per-transition tests (P0); MP:PERM-02 lifecycle-safe requester/observer interface + fake driver (P1); MP:PERM-03 Android-version capability matrix (notifications, Bluetooth, camera, media, exact alarms) (P1); MP:PERM-04 `DcPermissionRationale` as design-system UI, not in platform (P1).
- Observation: `dc.compose` and `androidx.activity.compose` are declared but no Compose import appears in the module's two source files (CONFIRMED_IN_CODE by import scan); intent UNKNOWN, possibly reserved for MP:PERM-02. Not changed here.
- ID note: bare `PERM-01` is the rule in `13-permissions.md`; `MP:PERM-01` is the state-machine backlog item (convention in `context-protocol.md`). No KD/DEC specific to this module.

## 10. Safe extension guide
- Add a permission only with a manifest declaration and a real use case (module STRUCTURE.md). Keep raw launcher calls in the Route; expose state via `PermissionState`. Do not move rationale UI here (MP:PERM-04). Sample consumer: `ArticlesRoute` + `ArticlesViewModel`. Tests for new transitions should be pure (no Android) where possible.

## 11. AI task playbook
- Read -> Graph -> Propose -> Edit -> Test -> Update docs -> Handover.
- Read: `13-permissions.md`, both source files, `ArticlesRoute.kt`. Graph: `codegraph_explore "NotificationPermission PermissionState ArticlesRoute ArticlesSyncWorker"`.
- DoD: `:app:assembleDebug` + consumer unit tests pass (with evidence under `reports/`); this page and `context-map.yaml` updated for API changes.

## 12. Change log and links
- 2026-10-27 — R1.1c — first draft from source and CodeGraph. Report: `reports/wp/R1.1c.md`.
- Links: `context-map.yaml` entry `platform-permission`; `13-permissions.md`; `dependency-rules.md`.
- Rule glosses (text lives in the named doc; gloss only, the rule doc is authoritative): `DEP-01` (02-layers-and-dependencies.md): core/base modules do not depend on feature implementation (or on :app); `PERM-01` (13-permissions.md): feature code reads PermissionState, never a raw Android permission API; only the Route touches the real API; `BLD-03` (18-build-logic-and-dependencies.md): test utilities never enter production runtime graphs.
