# Baseline verification (R0.1 + R0.2)

- Date (UTC): 2026-10-27T15:48Z
- Branch: `claude/practical-gauss-rmiffo`
- HEAD: `087fcf34c8e0f5b20bad7dd8b93b3e395b0d56e1`
- Working tree at start: clean (`git status --short` empty)
- Scope of this file: facts read from files in this checkout. No Gradle task was run by this agent (R0.2 Gradle evidence comes from the orchestrator logs listed in Part B).

---

## Part A — Inventory (verified from files)

### A1. Toolchain and versions

| Item | Value | Source |
|---|---|---|
| Gradle wrapper | 9.5.0 (`gradle-9.5.0-bin.zip`, sha256 pinned) | `gradle/wrapper/gradle-wrapper.properties` |
| Daemon JVM requirement | `toolchainVersion=25` (foojay toolchain URLs) | `gradle/gradle-daemon-jvm.properties` |
| Toolchain resolver | `org.gradle.toolchains.foojay-resolver-convention` 1.0.0 | `settings.gradle.kts:16` |
| `DC_JAVA_VERSION` | 11 (source/target compatibility and `jvmToolchain` for `dc.kotlin.jvm`) | `build-logic/src/main/kotlin/ConventionHelpers.kt`, `DcKotlinJvmConventionPlugin.kt:11` |
| `DC_COMPILE_SDK` | 37 | `ConventionHelpers.kt` |
| `DC_MIN_SDK` | 24 | `ConventionHelpers.kt` |
| `DC_TARGET_SDK` | 37 (applied in `dc.android.application` only) | `ConventionHelpers.kt`, `DcAndroidApplicationConventionPlugin.kt:13` |
| `android.disallowKotlinSourceSets` | `false` (KD-03) | `gradle.properties:24` |

Key versions from `gradle/libs.versions.toml`:

| Component | Version |
|---|---|
| Android Gradle Plugin (AGP) | 9.3.1 |
| Kotlin | 2.2.10 (Compose compiler via `kotlin.plugin.compose`) |
| KSP | 2.2.10-2.0.2 |
| Compose BOM | 2026.02.01 |
| Hilt (Dagger) | 2.60.1; `androidx.hilt` 1.4.0 |
| Room | 2.8.4 |
| WorkManager | 2.11.2 (`androidx.hilt:hilt-work` 1.4.0) |
| DataStore Preferences | 1.2.1 |
| OkHttp | 5.4.0 |
| Retrofit | 3.0.0 (kotlinx-serialization converter 1.0.0) |
| kotlinx-coroutines | 1.11.0 |
| kotlinx-serialization-json | 1.11.0 |
| Navigation Compose | 2.9.8 |
| Coil | 3.2.0 |

### A2. Module graph (`settings.gradle.kts` → module build files)

Plugin column lists the `dc.*` convention plugins (from `build-logic/build.gradle.kts`) and raw plugins applied directly in each module. `dc.*` plugins apply AGP/Kotlin underneath.

| Module | Plugins applied | Kind | Project dependencies | main .kt | src/test .kt | src/androidTest .kt |
|---|---|---|---|---|---|---|
| `:foundation` | dc.kotlin.jvm, dc.testing | JVM | none | 2 | 1 | 0 |
| `:runtime:concurrency` | dc.kotlin.jvm, dc.testing | JVM | none | 8 | 4 | 0 |
| `:ui:designsystem` | dc.android.library, dc.compose, dc.testing | Android lib | none | 41 | 5 | 2 |
| `:data:preferences` | dc.android.library, dc.testing | Android lib | none | 4 | 1 | 0 |
| `:observability` | dc.android.library, dc.testing | Android lib | none | 6 | 0 | 0 |
| `:data:network` | dc.android.library, kotlin.serialization, dc.testing | Android lib | impl `:foundation`, `:observability` | 5 | 2 | 0 |
| `:data:database` | dc.android.library, dc.room, dc.testing | Android lib | impl `:runtime:concurrency` | 6 | 1 | 0 |
| `:runtime:scheduling` | dc.android.library, dc.testing, kotlin.serialization | Android lib | impl `:foundation`, `:runtime:concurrency` | 15 | 3 | 0 |
| `:platform:navigation` | dc.android.library, kotlin.serialization | Android lib | none | 1 | 0 | 0 |
| `:platform:permission` | dc.android.library, dc.compose | Android lib | none | 2 | 0 | 0 |
| `:security` | dc.android.library, dc.testing | Android lib | impl `:runtime:concurrency` | 2 | 1 | 0 |
| `:testing` | dc.android.library, dc.testing | Android lib | api `:runtime:concurrency`, `:data:preferences`, `:observability`, `:runtime:scheduling` | 5 | 2 | 0 |
| `:app` | dc.android.application, dc.compose, kotlin.serialization, dc.hilt, dc.room, dc.testing, dc.feature.generator | Android app | impl `:foundation`, `:ui:designsystem`, `:runtime:concurrency`, `:data:preferences`, `:data:network`, `:data:database`, `:observability`, `:platform:navigation`, `:platform:permission`, `:runtime:scheduling`, `:security`; testImpl `:testing` | 39 | 4 | 2 |

Totals: 13 modules (matches `settings.gradle.kts`: 13 `include` lines). Main .kt: 136. Test .kt (src/test): 24. androidTest .kt: 4. `build-logic/`: 12 .kt files (not counted as a module above).

Observations from the graph:
- `:app` is the only application module; all others are libraries.
- Two JVM modules (`:foundation`, `:runtime:concurrency`) are not Android; `dc.kotlin.jvm` requests a JDK 11 toolchain (see Part B note).
- `:testing` exposes four production modules as `api`, so test consumers pull them transitively.

### A3. Test inventory (one line per module)

- `:app` (src/test): AppConfigurationRepositoryTest.kt, GreetingViewModelTest.kt, ArticlesViewModelTest.kt, ExampleUnitTest.kt
- `:app` (src/androidTest): GreetingScreenTest.kt, ExampleInstrumentedTest.kt
- `:foundation`: AppErrorTest.kt
- `:runtime:concurrency`: JobRegistryTest.kt, RetryPolicyTest.kt, FlowSharingTest.kt, SingleFlightTest.kt
- `:ui:designsystem` (src/test): DcContentStatePolicyTest.kt, DcNavigationSuiteTest.kt, DcWidgetPolicyTest.kt, DcThemeTokensTest.kt, DcUiScaleTest.kt
- `:ui:designsystem` (src/androidTest): DcWidgetsTest.kt, DcComponentsTest.kt
- `:data:preferences`: DataStorePreferencesDataSourceTest.kt
- `:observability`: none
- `:data:network`: NetworkResultTest.kt, NetworkClientFactoryTest.kt
- `:data:database`: StalenessPolicyTest.kt
- `:runtime:scheduling`: DcAlarmCodecTest.kt, DcWorkPolicyTest.kt, NextOccurrenceTest.kt
- `:platform:navigation`: none
- `:platform:permission`: none
- `:security`: SecureStorageUnavailableExceptionTest.kt
- `:testing`: TestingKitTest.kt, InMemoryPreferencesDataSourceTest.kt

Note: `GreetingScreenTest` is the androidTest that R0 names; it needs a device or emulator (none in this environment).

### A4. CI (`.github/workflows/ci.yml`)

1. Trigger: push to `main`/`master` and all pull requests; runner `ubuntu-latest`; `actions/setup-java@v4` installs Temurin JDK **21**; `gradle/actions/setup-gradle@v4`.
2. Steps: `./gradlew testDebugUnitTest`, `./gradlew lintDebug`, `./gradlew assembleDebug` (all `--console=plain`), then upload `**/build/test-results/**/*.xml` as an artifact.
3. Not in CI: `connectedAndroidTest`; JDK 25 (conflicts with the daemon JVM requirement, see drift D1). No CI run was available in this session, so the workflow is unverified on GitHub Actions.

### A5. CodeGraph

- `codegraph sync` → "Already up to date".
- `codegraph status`: Files 202; Nodes 3,098; Edges 5,163; DB 8.80 MB; WAL 0.01 MB; backend node:sqlite (WAL). Files by language: kotlin 193, properties 4, xml 4, yaml 1. Status line: `[OK] Index is up to date`.
- The status output has no commit field, so the index is verified against the working tree (clean at 15:48Z, HEAD 087fcf3), not against a recorded commit.

### A6. Tooling config

- `.claude/settings.json`:
  - PreToolUse, matcher `Bash|Grep`: `C:/Users/donly/AppData/Local/Programs/Python/Python313/Scripts/graphify.EXE hook-guard search`. **Windows path, not portable.**
  - PreToolUse, matcher `Read|Glob`: same Windows graphify.EXE with `hook-guard read --strict`. **Windows path, not portable.**
  - UserPromptSubmit: `codegraph prompt-hook` (portable if `codegraph` is on PATH).
  - No CodeGraph SessionStart hook.
  - permissions.allow: `mcp__codegraph__*`.
- `.mcp.json`: server `codegraph`, stdio, command `codegraph serve --mcp`, `alwaysLoad: true`. Portable.
- `graphify-out/` does not exist at the repo root (checked with `ls`), although CLAUDE.md §4 and §5 refer to `graphify-out/GRAPH_REPORT.md`.

### Drift found in inventory

- D1. CI pins JDK 21 (`ci.yml`) but `gradle/gradle-daemon-jvm.properties` requires toolchain 25. CI will likely hit the same toolchain-provisioning path as this environment. Not verified on Actions.
- D2. CI runs `testDebugUnitTest`, which exists only in Android modules. By build-file inspection (`dc.kotlin.jvm` applies the plain JVM plugin, which provides `test`, not `testDebugUnitTest`), the JVM modules `:foundation` and `:runtime:concurrency` (5 test files) are likely not run by CI. Inferred from configuration, not run.
- D3. `architecture/22-known-deviations.md` KD-02 evidence still quotes `include(":app")` only. Current `settings.gradle.kts` includes 13 modules (see R0.3).
- D4. `IMPLEMENTATION_STATUS.md:9` already marks the 4-module, 7/7 PASS record as stale. `HANDOVER.md:46-59` still cites it as the current verification.
- D5. CLAUDE.md §4/§5 reference `graphify-out/`, which is absent in this checkout.
- D6. Graphify hooks in `.claude/settings.json` are Windows-only (see A6).

---

## Part B — Build verification

No Gradle command was run by this agent. Evidence below is from orchestrator runs with logs in `reports/logs/`.

| # | Command | Result | Evidence |
|---|---|---|---|
| B1 | `./gradlew projects` | **FAILED** at daemon JVM toolchain provisioning. Required toolchain 25 (`gradle/gradle-daemon-jvm.properties`). Only JDK 21 is installed. Auto-download from `api.foojay.io` returned HTTP 403 (proxy policy). The Gradle 9.5.0 distribution download from `services.gradle.org` succeeded. | `reports/logs/R0.2-gradle-projects.log` |
| B2 | `./gradlew :foundation:test :runtime:concurrency:test` with the daemon criteria temporarily bypassed (local experiment; `gradle/gradle-daemon-jvm.properties` restored, still `toolchainVersion=25`) | **FAILED** at plugin resolution. Root `build.gradle.kts:2` needs `com.android.application` 9.3.1, which could not be resolved. The log shows only that the plugin was not found in the Google/MavenRepo/Gradle Plugin repositories; the cause "`maven.google.com` redirects to `dl.google.com`, which the proxy denies (CONNECT 403)" is an orchestrator observation (curl CONNECT 403 to `dl.google.com`, also visible in proxy status), not in the log. The root build applies every AGP plugin with `apply false`, so resolution blocks the whole build, including the JVM-only modules. | `reports/logs/R0.2-gradle-jvm-test.log` |
| B3 | `./gradlew testDebugUnitTest` (all modules) | **NOT_RUN**. Blocked by B1/B2. | — |
| B4 | `./gradlew lintDebug` | **NOT_RUN**. No fresh lint report can be produced. | — |
| B5 | `./gradlew assembleDebug` | **NOT_RUN**. Blocked by B1/B2. | — |
| B6 | `./gradlew connectedAndroidTest` | **NOT_RUN**. No emulator or device; Android SDK not installed; `ANDROID_HOME` unset; `dl.google.com` blocked, so the SDK cannot be installed. | — |

Notes:
- The last recorded PASS (4 modules, 7/7 tests; `HANDOVER.md:46-59`) is **stale**. It predates the 13-module layout (`IMPLEMENTATION_STATUS.md:9`). It is not evidence for the current HEAD.
- The 12 Android Lint warnings recorded in `HANDOVER.md:54` remain **untriaged**. No fresh report exists.
- Not reached in B2, but from source: `dc.kotlin.jvm` calls `jvmToolchain(11)` (`DcKotlinJvmConventionPlugin.kt:11`). Only JDK 21 is installed, so the JVM modules also need a JDK 11 toolchain, which would also come from `api.foojay.io`.
- CI: the workflow has never run on GitHub Actions in this session (no run URL or job log available). Status: **not verified**.

Blockers and what would unblock them:
1. `dl.google.com` and `maven.google.com`: AGP 9.3.1 plugin and Google Maven artifacts. Also needed for the Android SDK.
2. `api.foojay.io`: JDK 25 (daemon) and JDK 11 (`jvmToolchain`).
3. GitHub release-asset hosts: reported by the orchestrator as blocked. The exact consumer was not confirmed in this pass.
4. Alternative: preinstall JDK 25 and JDK 11, plus the Android SDK (platform 37, build-tools, platform-tools) with `ANDROID_HOME` set, in the environment setup script.

---

## Part C — KD status (architecture/22-known-deviations.md)

"New evidence" means evidence from this run. Build status is NOT_RUN for every row, so no row gains new build evidence.

| KD | Status as written | New evidence | Source check (no build) |
|---|---|---|---|
| KD-01 (namespace in CLAUDE.md) | Resolved (DEC-01); audit trail only | no new evidence (build NOT_RUN) | Consistent: `CLAUDE.md:8` and `app/build.gradle.kts:12` both use `com.dc.android.foundation`. |
| KD-02 (module boundaries) | "Resolved in Phase C" | no new evidence (build NOT_RUN) | Status wording inaccurate: modules exist (13 in `settings.gradle.kts`, `:app` depends on them one-directionally) but enforcement is absent (no checker in `build-logic`); should be Open (exit: R2.1). The evidence text was also stale (quoted `include(":app")` only); fixed in R0.3 (drift D3). `./gradlew :app:dependencies` not run. |
| KD-03 (`android.disallowKotlinSourceSets=false`) | Active compatibility flag; exit = KSP AGP-9-native release | no new evidence (build NOT_RUN) | Flag present at `gradle.properties:24`. Exit condition unchanged. |
| KD-04 (secure storage without a real credential) | Open: no credential flows through the store | no new evidence (build NOT_RUN) | Code present: `security/.../SecureStorage.kt`, `security/.../EncryptedSharedPreferencesSecureStorage.kt`, `app/.../di/SecurityModule.kt`. Only unit test is `SecureStorageUnavailableExceptionTest`. No instrumented test. |
| KD-05 (analytics and crash, no-op backend) | Open: no vendor chosen; local logging backend | no new evidence (build NOT_RUN) | Code present: `observability/.../AnalyticsTracker.kt`, `LoggingAnalyticsTracker.kt`, `CrashReporter.kt`, `LoggingCrashReporter.kt`. Vendor not chosen per the doc. |

KD-02..05 cannot move to "verified" or "resolved" from this run. The R0 gate requires build evidence for that.

---

## Summary for R0.3 and R0.4

- Build verification is blocked by network policy, not by source errors seen so far. Unit tests, lint, assemble, and connected tests are all NOT_RUN.
- Fix the drift items D1–D6 in R0.3 (docs) and R0.4 (hooks, portability).
- Unblock the network (or preinstall JDK 11/25 and the Android SDK) before R0.2 can produce PASS/FAIL evidence.
