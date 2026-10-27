# Module map

[Back to architecture index](00-index.md) · Machine form: [`context-map.yaml`](context-map.yaml) · Edge policy: [`dependency-rules.md`](dependency-rules.md)

Source of truth: `settings.gradle.kts` (13 modules + `includeBuild("build-logic")`) and module build files, via `reports/baseline-verification.md` Part A. Direct deps are production project deps; `:app` also has `testImplementation(:testing)`. Doc status is `DRAFT` (pages not yet written; R1.1/R1.2 set it). Test status (`NOT_RUN | TESTED | FAILED | BLOCKED`) is `NOT_RUN` everywhere: no Gradle test run succeeded in this environment (evidence: `reports/baseline-verification.md`, `reports/logs/`). Module docs are created in R1.1; links below are expected to resolve after that.

| Module | Kind | Responsibility (1 line) | Direct deps | Doc | Doc status | Test status |
|---|---|---|---|---|---|---|
| `:foundation` | jvm | Android-free shared contracts: `AppError` vocabulary and retryability. | none | [foundation](modules/foundation.md) | DRAFT | NOT_RUN |
| `:runtime:concurrency` | jvm | Dispatcher ownership, `SingleFlight`, `JobRegistry`, retry and Flow utilities. | none | [runtime-concurrency](modules/runtime-concurrency.md) | DRAFT | NOT_RUN |
| `:runtime:scheduling` | android-library | Unified scheduler over WorkManager jobs and exact alarms (`DcScheduler`, `DcWorker`). | `:foundation`, `:runtime:concurrency` | [runtime-scheduling](modules/runtime-scheduling.md) | DRAFT | NOT_RUN |
| `:ui:designsystem` | android-library | Tokens, semantic `DcTheme`, reusable `Dc*` components and the design-system catalog. | none | [ui-designsystem](modules/ui-designsystem.md) | DRAFT | NOT_RUN |
| `:data:preferences` | android-library | DataStore-backed typed preferences contract (`PreferencesDataSource`, `PreferenceKey`). | none | [data-preferences](modules/data-preferences.md) | DRAFT | NOT_RUN |
| `:data:database` | android-library | Room infrastructure: `DcDatabase`, factory, transaction runner, staleness policy. | `:runtime:concurrency` | [data-database](modules/data-database.md) | DRAFT | NOT_RUN |
| `:data:network` | android-library | OkHttp/Retrofit factory, `NetworkResult`, HTTP error parsing. | `:foundation`, `:observability` | [data-network](modules/data-network.md) | DRAFT | NOT_RUN |
| `:platform:navigation` | android-library | Type-safe `Destination` contract (engine owned by `:app`, NAV-01). | none | [platform-navigation](modules/platform-navigation.md) | DRAFT | NOT_RUN |
| `:platform:permission` | android-library | `PermissionState` and notification-permission helper (PERM-01). | none | [platform-permission](modules/platform-permission.md) | DRAFT | NOT_RUN |
| `:security` | android-library | `SecureStorage` contract with EncryptedSharedPreferences implementation (SEC-02). | `:runtime:concurrency` | [security](modules/security.md) | DRAFT | NOT_RUN |
| `:observability` | android-library | `Logger`, `AnalyticsTracker`, `CrashReporter` contracts with local logging backends. | none | [observability](modules/observability.md) | DRAFT | NOT_RUN |
| `:testing` | android-library | Shared test kit: `MainDispatcherRule`, fakes; consumed from test configurations only (BLD-03). | `:runtime:concurrency`, `:data:preferences`, `:observability`, `:runtime:scheduling` (api) | [testing](modules/testing.md) | DRAFT | NOT_RUN |
| `:app` | android-app | Composition root: Hilt modules, `greeting`/`articles`/`showcase`/`config` features, startup. | the 11 non-`:testing` library modules above; test: `:testing` | [app](modules/app.md) | DRAFT | NOT_RUN |
| `build-logic` | included-build | `dc.*` convention plugins and the `createFeature` scaffold generator. | none (included build) | [build-logic](modules/build-logic.md) | DRAFT | NOT_RUN |

## Features (packages in `:app`)

| Feature | Package | Doc | Doc status | Test status |
|---|---|---|---|---|
| greeting | `com.dc.android.foundation.greeting` | [greeting](features/greeting.md) | DRAFT | NOT_RUN |
| articles | `com.dc.android.foundation.articles` | [articles](features/articles.md) | DRAFT | NOT_RUN |
| showcase | `com.dc.android.foundation.showcase` | [showcase](features/showcase.md) | DRAFT | NOT_RUN |

Not in this map by design: `:app` `config` is documented inside `modules/app.md` (no separate feature page); no module exists for a capability that has no code (CLAUDE.md section 1).
