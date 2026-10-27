# Known deviations

[Back to architecture index](00-index.md)

## Scope

Explicit intentional/current violations or non-yet-satisfied states of architecture rules. Each entry needs rule ID, evidence path, reason, risk, owner/exit condition.

## Entries

### KD-01 — Root `CLAUDE.md` previously stated the wrong primary namespace

**Rule affected:** STR-02 (`03-project-structure.md`)

**Evidence:** `app/build.gradle.kts` — `namespace = "com.dc.android.foundation"`; every `package` declaration under `app/src` matches. Root `CLAUDE.md` §1 previously read `**Primary namespace:** com.dc.foundation`.

**Reason:** `CLAUDE.md` §1 was written before the Android Studio project was generated; the generated project used `com.dc.android.foundation` instead. Per the project's own architecture-authority order (`CLAUDE.md` §6: verified source ranks above the high-level summary document), verified reality wins.

**Risk:** Low — documentation-only mismatch, no code impact.

**Resolution:** Corrected in Phase B — `CLAUDE.md` §1 updated to state `com.dc.android.foundation`. Not treated as an open deviation going forward; kept here as the audit trail. See `23-decision-log.md` DEC-01.

**Status check 2026-10-27:** no new build evidence (build NOT_RUN, see `reports/baseline-verification.md`). Source read still consistent: `CLAUDE.md:8` and `app/build.gradle.kts:12` both state `com.dc.android.foundation`.

### KD-02 — No enforced module boundaries yet

**Rule affected:** DEP-01, DEP-04 (`02-layers-and-dependencies.md`)

**Evidence (original, Phase B):** `settings.gradle.kts` — `include(":app")` only.

**Evidence (current, source-read 2026-10-27):** `settings.gradle.kts:27-39` includes 13 modules: `:foundation`, `:app`, `:ui:designsystem`, `:runtime:concurrency`, `:data:preferences`, `:observability`, `:data:network`, `:data:database`, `:runtime:scheduling`, `:platform:navigation`, `:platform:permission`, `:security`, `:testing`. `app/build.gradle.kts:34-44,63` declares 11 `implementation(project(...))` edges plus `testImplementation(project(":testing"))`; no library module depends on `:app`. `./gradlew :app:dependencies` has not been run for the 13-module layout.

**Reason:** Modules were added in Phase C and later phases (13 now) without an automated checker for DEP-01/DEP-04. `build-logic/src/main/kotlin/` holds convention plugins and the feature scaffold generator only; nothing there inspects dependency edges. Enforcement was manual until lane A1 added the static checker (see Status).

**Risk:** Medium, reduced by A1 — before A1 a forbidden dependency edge could be added silently and no build or CI step would fail; now only an edge the text parser cannot see (for example one added by a convention plugin) escapes the check. `:testing` also exposes four modules as `api`, so test consumers pull them transitively.

**Status:** **Open (partially mitigated by lane A1)** — modules exist (13) and `:app` depends on them one-directionally. Mitigation (source-read; CI run NOT_RUN): `tools/check_architecture.py --boundaries-only` compares every module build file's `project(...)` edges with the allowed/test-only blocks of `dependency-rules.md` and exits 1 on a forbidden, unlisted or unparseable edge, an edge to `:app`, `:testing` in a production configuration, or a cycle; it has 16 unit tests (`tools/test_check_architecture.py`, run locally: PASS) and a mutation check on a scratch copy failed with 4 errors as intended; the repo currently shows 13 modules / 21 production edges / 0 errors. It is wired as the blocking `architecture` job in `.github/workflows/ci.yml`. Remaining exit condition: (a) one real GitHub Actions run of the `architecture` job with evidence under `reports/`, (b) optional Gradle-graph cross-check (resolved `ProjectDependency` graph; would catch edges added implicitly by convention plugins, none do today). Do not mark Resolved before (a). `./gradlew :app:dependencies` is NOT_RUN. (History: marked "Resolved in Phase C" when `:ui:designsystem` was added; reopened 2026-10-27 because modules existing is not boundary enforcement.)

### KD-03 — `android.disallowKotlinSourceSets=false` required for KSP on AGP 9

**Rule affected:** none (build-tooling compatibility, not an architecture rule)

**Evidence:** `gradle.properties`. `./gradlew :app:assembleDebug` failed configuration with `Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin` before this flag was added.

**Reason:** AGP 9.3.1's new built-in-Kotlin compilation pipeline rejects the classic `kotlin.sourceSets` DSL. The KSP Gradle plugin (`2.2.10-2.0.2`, the version matching this project's Kotlin `2.2.10`) still registers its generated-sources directory through that classic DSL — it has not yet published an `android.sourceSets`-based update. `android.disallowKotlinSourceSets=false` is the flag Android's own error message names as the fix, not a project-invented workaround.

**Risk:** Low — a build-tooling compatibility flag, not an architecture/behavior change. Revisit when KSP ships an AGP-9-native release.

**Owner/exit condition:** Remove the flag (and this entry) once a KSP release adds `android.sourceSets` support and the build still succeeds without it.

**Status check 2026-10-27:** no new build evidence (build NOT_RUN, see `reports/baseline-verification.md`). Flag still present at `gradle.properties:24`; KSP is still `2.2.10-2.0.2` in `gradle/libs.versions.toml`. Exit condition unchanged; not re-tested because no Gradle task can run.

### KD-04 — Secure storage (SEC-02) implemented with no real credential flowing through it

**Rule affected:** SEC-02 (`12-security-and-session.md`)

**Evidence:** `architecture/12-security-and-session.md` SEC-02 states no encrypted-storage rule exists yet because no credential exists to store, and that a token/credential data source will be added "when real auth exists."

**Reason:** This build cycle adds a real `EncryptedSharedPreferences`- or Tink-backed secure storage data source, but — unlike the `articles` feature added under DEC-04 — there is no real authentication/login flow consuming it. Building a fake auth flow just to justify secure storage was judged worse than being explicit about the gap.

**Risk:** Low — the code path is unused, not incorrect. The storage mechanism itself is production-grade and real; only SEC-02's "a real credential flows through it" trigger condition is not fully met.

**Owner/exit condition:** Remove this deviation entry once a real auth/session feature is built that actually stores a credential through this data source.

**Status check 2026-10-27:** no new build evidence (build NOT_RUN, see `reports/baseline-verification.md`). Code present in `security/` and `app/.../di/SecurityModule.kt`; the only test is `SecureStorageUnavailableExceptionTest` (no instrumented test). Still Open.

### KD-05 — Analytics/crash reporting implemented with a local no-op backend, no vendor SDK chosen

**Rule affected:** OBS-ANALYTICS-01 (`15-observability/analytics.md`), OBS-CRASH-01 (`15-observability/crash-and-performance.md`)

**Evidence:** `architecture/15-observability/analytics.md` states no analytics vendor has been chosen and building a contract with nothing behind it would be speculative; `crash-and-performance.md` takes the same stance for crash reporting.

**Reason:** This build cycle adds a DC-owned interface + Hilt-bound implementation for both analytics and crash reporting, mirroring the `Logger`/`AndroidLogger` pattern exactly, but deliberately without picking a real vendor SDK (no Firebase/Amplitude/Crashlytics/Sentry dependency added). The bound implementation is a local no-op/logging-only implementation. Vendor choice remains, per both docs' own stated stance, a deliberate decision not to be made speculatively.

**Risk:** Low — the contract and DI wiring are real and usable; only the vendor backend is intentionally absent.

**Owner/exit condition:** Remove this deviation (or rather, replace the no-op implementation with a real vendor-backed implementation behind the same interface) once an actual vendor decision is made for a real downstream app built on this foundation.

**Status check 2026-10-27:** no new build evidence (build NOT_RUN, see `reports/baseline-verification.md`). Code present in `observability/` (`LoggingAnalyticsTracker`, `LoggingCrashReporter`); no vendor chosen. `:observability` now has JVM unit tests for `LoggingAnalyticsTracker`/`LoggingCrashReporter` (lane A2, `observability/src/test`; run NOT_RUN). Still Open.

### KD-06 — Design-system catalog and trip showcase are gated by a runtime check, not by a debug-only source set

**Rule affected:** DEC-05 (`23-decision-log.md`: "place business-specific Trip reference screens in debug `:app` showcase code"); `07-design-system/` README/`previews-and-catalog.md` describe the catalog and showcase as debug tooling.

**Evidence:** `app/src/main/java/com/dc/android/foundation/MainActivity.kt:80,85-88,128-131` registers the `catalog` and `showcase` nav items and the `design-system-catalog` / `trip-showcase` routes in the main source set, guarded only by `AppEnvironmentProvider.current == AppEnvironment.DEBUG` (`config/AppEnvironmentProvider.kt:7`, reads `BuildConfig.DEBUG`). `app/src/main/java/.../showcase/trip/*` is in `src/main`, and `DesignSystemCatalog` is in `ui/designsystem/src/main/.../catalog/`. `app/build.gradle.kts:25-29` sets `optimization { enable = false }` for `release`, so nothing is stripped. Build NOT_RUN: release artifact contents not inspected.

**Reason:** UNKNOWN (not recorded). The runtime gate was the simplest way to hide the entries in release; a `debug` source set or `debugImplementation` split was not done.

**Risk:** Low to medium: demo screens and their trip sample data are compiled and packaged into release builds and are unreachable only while `BuildConfig.DEBUG` is false; larger APK, larger attack/maintenance surface, and a conflict with the DEC-05 wording.

**Owner/exit condition:** Either move the showcase to `app/src/debug` (and keep the catalog reachable only from it), or amend DEC-05 to say "debug-gated at runtime". Natural slots: R4 (design-system registry) or R6.2 (reference app). Status: **Open**.

