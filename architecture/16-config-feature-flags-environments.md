# Config, feature flags & environments

[Back to architecture index](00-index.md)

## Scope

Environment/build-variant awareness and feature-flag ownership.

## Current implementation

- `app/.../config/AppEnvironment.kt` — `enum class AppEnvironment { DEBUG, RELEASE }`.
- `app/.../config/AppEnvironmentProvider.kt` — the **only** file allowed to read `BuildConfig.DEBUG` directly; exposes `AppEnvironmentProvider.current: AppEnvironment`.
- `app/.../config/AppConfiguration.kt` — typed user configuration: `AppThemeMode`, `DcUiScale`, and dynamic-color opt-in.
- `AppConfigurationRepository` — maps stable application keys onto the generic `PreferencesDataSource`; exposes `configuration`, `setThemeMode`, `setUiScale`, `setDynamicColor`, atomic `update`, and `reset`.
- `AppConfigurationViewModel` — process-visible state owner used by the app shell and Settings screen.
- `AppLanguageController` — sets/reads Android per-app locale through `AppCompatDelegate`; a null/blank tag restores the system language. AndroidX owns locale persistence so the same value is not duplicated in DataStore.
- `MainActivity` is an `AppCompatActivity`, observes configuration once at the shell, and applies it through `DcTheme`. `AppConfigurationScreen` is the business-neutral reference UI.
- Consumer: `di/ObservabilityModule.kt` builds `AndroidLogger(isDebug = AppEnvironmentProvider.current == AppEnvironment.DEBUG)` — a real use, not speculative plumbing.
- No feature flags exist — nothing branches on one yet. When one is needed, it reads `AppEnvironment`, not raw `BuildConfig`, and stays provider-neutral (no LaunchDarkly/Firebase Remote Config dependency without an explicit decision — plan §H).
- Only one build type is meaningfully differentiated today (`debug` vs `release`, both declared in `app/build.gradle.kts`; `release.optimization.enable = false` — R8/minify is off, noted as-is, not a target-state claim).

## Rules

### CFG-01 — `BuildConfig.DEBUG` is read in exactly one place

**Rule:** Only `AppEnvironmentProvider` reads `BuildConfig.DEBUG`; everything else takes `AppEnvironment`.

**Why:** One seam to extend when a real feature-flag provider is adopted, instead of `BuildConfig.DEBUG` checks scattered across the codebase.

**Verification:** `grep 'BuildConfig\.DEBUG'` across `app/src` — should only match `AppEnvironmentProvider.kt`.

### CFG-02 — User configuration has one state owner and typed mutation API

**Rule:** Screens send `AppConfigurationAction`; only `AppConfigurationViewModel` invokes `AppConfigurationRepository`. Raw preference keys remain private to the repository, and the app shell is the only place that applies theme/scale configuration.

**Why:** Prevents screens from disagreeing about theme or size and preserves the DataStore mapping boundary (DAT-01).

**Verification:** `PreferencesDataSource` usage for app configuration appears only in `AppConfigurationRepository`; `DcTheme(uiScale=...)` is applied at `MainActivity`'s root.

### CFG-03 — Language follows the Android per-app locale contract

**Rule:** Language changes go through `AppLanguageController.setApplicationLanguage(languageTag)` using BCP-47 tags. `null`/blank means system default. Locale is not copied into the application DataStore.

**Why:** Android 13+ can synchronize the choice with system App Languages settings, while AppCompat supplies the compatible path for API 24–32 and recreates the Compose host when needed.

**Verification:** The Compose host extends `AppCompatActivity`; generated locale config is enabled; translated strings live in qualified `values-*` resources.

## Usage

Observe and apply configuration once at the application shell:

```kotlin
val configuration by viewModel.configuration.collectAsStateWithLifecycle()
DcTheme(
    darkTheme = configuration.themeMode.resolveDarkTheme(isSystemInDarkTheme()),
    dynamicColor = configuration.dynamicColor,
    uiScale = configuration.uiScale
) {
    AppNavigation()
}
```

Change one value or update several atomically:

```kotlin
viewModel.onAction(AppConfigurationAction.SetUiScale(DcUiScale.Comfortable))

repository.update(
    AppConfiguration(
        themeMode = AppThemeMode.Dark,
        uiScale = DcUiScale.Standard,
        dynamicColor = false
    )
)
```

Change language from a main-thread UI event:

```kotlin
AppLanguageController.setApplicationLanguage("vi")
AppLanguageController.setApplicationLanguage("en-US")
AppLanguageController.useSystemLanguage()
```

Only declare language tags for which the app ships localized resources. Changing locale reloads `stringResource`/Android resources; it does not translate hard-coded strings.

## Testing / validation

- `AppConfigurationRepositoryTest` covers defaults, typed setters, atomic update/reset and theme resolution.
- `DcUiScaleTest` covers standard, compact and comfortable scaling.
- Compile/assemble verifies generated locale config and the AppCompat host integration.

## Related documents

- [`15-observability/logging.md`](15-observability/logging.md)
