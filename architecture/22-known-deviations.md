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

**Reason:** Modules were added in Phase C and later phases (13 now) without an automated checker for DEP-01/DEP-04. `build-logic/src/main/kotlin/` holds convention plugins and the feature scaffold generator only; nothing there inspects dependency edges. Enforcement is manual.

**Risk:** Medium — a forbidden dependency edge can be added silently and no build or CI step would fail. `:testing` also exposes four modules as `api`, so test consumers pull them transitively.

**Status:** **Open** — modules exist (13) and `:app` depends on them one-directionally, but no automated check enforces DEP-01/DEP-04. Exit condition: R2.1 Gradle boundary task plus forbidden-edge test passing in CI (`plan/FOUNDATION_MASTER_PLAN.md` §1.2). Manual verification via `./gradlew :app:dependencies` is NOT_RUN. (History: marked "Resolved in Phase C" when `:ui:designsystem` was added; reopened 2026-10-27 because modules existing is not boundary enforcement.)

**Status check 2026-10-27:** no new build evidence (build NOT_RUN, see `reports/baseline-verification.md`). Status corrected to Open on source evidence (no boundary checker in `build-logic`); evidence text refreshed from source. Dependency-graph verification via Gradle remains NOT_RUN.

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

**Status check 2026-10-27:** no new build evidence (build NOT_RUN, see `reports/baseline-verification.md`). Code present in `observability/` (`LoggingAnalyticsTracker`, `LoggingCrashReporter`); no vendor chosen. `:observability` has no tests. Still Open.
