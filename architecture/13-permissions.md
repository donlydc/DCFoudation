# Permissions

[Back to architecture index](00-index.md)

## Scope

Runtime permission ownership and rationale/denial handling.

## Current implementation (Phase Q)

`AndroidManifest.xml` now declares its first `<uses-permission>`: `android.permission.POST_NOTIFICATIONS`, required on API 33+ to post any notification — needed because `ArticlesSyncWorker` (`14-background-work.md` BG-01) posts a best-effort "Articles synced" notification after a successful periodic sync.

- `:platform:permission` defines `PermissionState` — a DC-owned, engine-neutral sealed type (`NotRequested` / `Granted` / `Denied`) hiding raw `ContextCompat.checkSelfPermission` calls from feature code, the same "infra type instead of a framework leak" shape as `Destination` (`:platform:navigation`, NAV-01) and `PreferencesDataSource` (`:data:preferences`).
- Since lane C1 (`aca166d`) `:platform:permission` also has an additive richer model that the app does not use yet: `PermissionStatus` (`NotRequested | Granted | DeniedShouldShowRationale | PermanentlyDenied | Unavailable`, lossy `toState()`), the pure `PermissionStatusResolver`, a lifecycle-free `PermissionRequester` (`check`/`request`) with `PermissionRequestTracker` and `FakePermissionRequester`. `PermissionState` and the "three states by design" statement below remain true for everything the app uses today. Decision DEC-C1-2 is PROPOSED (pending Opus final review).
- `:platform:permission` also defines `NotificationPermission`, the one real call site: `currentState(context)` (reports `Granted` unconditionally below API 33, since the permission doesn't exist pre-33; reflects the live OS grant on 33+) and `fromRequestResult(granted: Boolean)` (maps a Compose permission-launcher callback to `PermissionState`).
- The UDF wiring lives entirely in `articles` (05-presentation-udf.md's pattern): `ArticlesUiState.notificationPermissionState: PermissionState`, `ArticlesUiAction.RequestNotificationPermission` / `UpdateNotificationPermissionState`, `ArticlesUiEffect.LaunchNotificationPermissionRequest`. `ArticlesScreen` shows an "Enable notifications for sync updates" `DcButton` whenever the state isn't `Granted`; tapping it sends `RequestNotificationPermission` up to `ArticlesViewModel`, which emits the `LaunchNotificationPermissionRequest` effect. `ArticlesRoute` — the legitimate framework-glue layer per DEP-03/INV-07 — collects that effect and calls `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())`, then reports the real result back up via `UpdateNotificationPermissionState`. `ArticlesRoute` also syncs the initial state once via `NotificationPermission.currentState(context)` in a `LaunchedEffect(Unit)`.
- `ArticlesSyncWorker` independently checks `NotificationPermission.currentState(applicationContext)` before posting — if not `Granted`, it skips the notification silently. It never crashes on a missing/revoked permission (a `SecurityException` around `notify()` is caught defensively too).

**Deliberately not built:** a rationale dialog (`shouldShowRequestPermissionRationale`) and a settings deep-link for "permanently denied" — `PermissionState` has no fourth `PermanentlyDenied` case, and a single deny just leaves the button visible for the user to tap again. Building the full rationale/settings-deeplink flow now would be speculative UI polish with no second permission to validate it against; it remains a documented, not-yet-built extension point for whenever a second real permission call site appears.

## Rules

### PERM-01 — Feature code reads `PermissionState`, never a raw Android permission API; only `Route` touches the real permission API

**Rule:** `ArticlesUiState`/`ArticlesViewModel`/`ArticlesScreen` (and any future feature needing a runtime permission) depend only on `com.dc.android.foundation.platform.permission.PermissionState` (and, for the notification case specifically, `NotificationPermission`'s pure `currentState`/`fromRequestResult` functions) — never `ActivityCompat.checkSelfPermission`, `shouldShowRequestPermissionRationale`, or `rememberLauncherForActivityResult` directly. Only a feature's `Route` (the stateful, Android-framework-touching Composable layer, same boundary NAV-01 draws for navigation) is allowed to call `rememberLauncherForActivityResult`/launch the OS permission dialog, and it reports results back into the UDF loop as a normal `UiAction`, never by holding side-channel mutable state.

**Why:** Keeps `ViewModel`/`Screen` testable without Android framework/Robolectric (DEP-03, INV-07) and keeps the "who's allowed to touch the real OS API" boundary consistent with the navigation pattern feature code already follows.

**Verification:** `grep -r "ActivityCompat\|shouldShowRequestPermissionRationale\|rememberLauncherForActivityResult" app/src/main/java/com/dc/android/foundation/articles` matches only `ArticlesRoute.kt`. `ArticlesViewModel.kt` and `ArticlesScreen.kt` have no `android.Manifest`/`ActivityCompat`/`ContextCompat` import — only `com.dc.android.foundation.platform.permission.PermissionState`. `AndroidManifest.xml` has two `<uses-permission>` entries: `POST_NOTIFICATIONS` (runtime, the only one handled by this module) and `INTERNET` (normal permission, added by lane C2 `64da185` because `articles` does Retrofit I/O; it was missing before, a defect fixed by C2).

**Exceptions:** `ArticlesSyncWorker` (in `:app`) also calls `NotificationPermission.currentState` directly — it has no `Route`/UDF layer of its own (it's a background `CoroutineWorker`, not a screen), so it is its own legitimate framework-glue boundary for this one check.

## Testing / validation

Lane C1 added `PermissionStatusTest` (resolver truth table, `toState`, tracker, fake; written, NOT_RUN). `ArticlesViewModelTest` covers `RequestNotificationPermission` (emits `LaunchNotificationPermissionRequest`) and `UpdateNotificationPermissionState` (updates `ArticlesUiState.notificationPermissionState`) with the same hand-written-fake, no-mocking-library, `MainDispatcherRule`+`runTest` pattern as the rest of the suite. `NotificationPermission.currentState`/`fromRequestResult` are not unit-tested in isolation: `currentState` needs a real/Robolectric `Context` and `Build.VERSION.SDK_INT`, and `fromRequestResult` is a one-line `if` with no branch worth a dedicated test beyond what the ViewModel test already exercises through `UpdateNotificationPermissionState`. The OS permission dialog itself (`ArticlesRoute`'s `rememberLauncherForActivityResult` call) is not exercised by any test here — that needs a Compose UI/instrumented test, which (like `GreetingScreenTest`) has never run in this environment.

## Related documents

- [`05-presentation-udf.md`](05-presentation-udf.md) (the UDF pattern this permission flow slots into)
- [`06-navigation.md`](06-navigation.md) (NAV-01 — the same `Route`-is-the-framework-glue-boundary pattern)
- [`14-background-work.md`](14-background-work.md) (BG-01 — the worker that made this permission real)
- [`23-decision-log.md`](23-decision-log.md) (DEC-04 — why this use case exists)
