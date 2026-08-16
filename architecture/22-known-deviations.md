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

### KD-02 — No enforced module boundaries yet

**Rule affected:** DEP-01, DEP-04 (`02-layers-and-dependencies.md`)

**Evidence:** `settings.gradle.kts` — `include(":app")` only.

**Reason:** The project is pre-Phase-C; no capability has yet earned a dedicated Gradle module per the anti-overengineering constraint (`plan/CLAUDE_IMPLEMENTATION_PLAN.md` §7).

**Risk:** None currently — there is nothing to violate with a single module.

**Status:** **Resolved in Phase C** — `:ui:designsystem` now exists (`settings.gradle.kts`: `include(":app")`, `include(":ui:designsystem")`), and `:app` depends on it one-directionally (`implementation(project(":ui:designsystem"))`). DEP-01/04 are now verifiable via `./gradlew :app:dependencies`. Kept here as history; not an open deviation.

### KD-03 — `android.disallowKotlinSourceSets=false` required for KSP on AGP 9

**Rule affected:** none (build-tooling compatibility, not an architecture rule)

**Evidence:** `gradle.properties`. `./gradlew :app:assembleDebug` failed configuration with `Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin` before this flag was added.

**Reason:** AGP 9.3.1's new built-in-Kotlin compilation pipeline rejects the classic `kotlin.sourceSets` DSL. The KSP Gradle plugin (`2.2.10-2.0.2`, the version matching this project's Kotlin `2.2.10`) still registers its generated-sources directory through that classic DSL — it has not yet published an `android.sourceSets`-based update. `android.disallowKotlinSourceSets=false` is the flag Android's own error message names as the fix, not a project-invented workaround.

**Risk:** Low — a build-tooling compatibility flag, not an architecture/behavior change. Revisit when KSP ships an AGP-9-native release.

**Owner/exit condition:** Remove the flag (and this entry) once a KSP release adds `android.sourceSets` support and the build still succeeds without it.

### KD-04 — Secure storage (SEC-02) implemented with no real credential flowing through it

**Rule affected:** SEC-02 (`12-security-and-session.md`)

**Evidence:** `architecture/12-security-and-session.md` SEC-02 states no encrypted-storage rule exists yet because no credential exists to store, and that a token/credential data source will be added "when real auth exists."

**Reason:** This build cycle adds a real `EncryptedSharedPreferences`- or Tink-backed secure storage data source, but — unlike the `articles` feature added under DEC-04 — there is no real authentication/login flow consuming it. Building a fake auth flow just to justify secure storage was judged worse than being explicit about the gap.

**Risk:** Low — the code path is unused, not incorrect. The storage mechanism itself is production-grade and real; only SEC-02's "a real credential flows through it" trigger condition is not fully met.

**Owner/exit condition:** Remove this deviation entry once a real auth/session feature is built that actually stores a credential through this data source.

### KD-05 — Analytics/crash reporting implemented with a local no-op backend, no vendor SDK chosen

**Rule affected:** OBS-ANALYTICS-01 (`15-observability/analytics.md`), OBS-CRASH-01 (`15-observability/crash-and-performance.md`)

**Evidence:** `architecture/15-observability/analytics.md` states no analytics vendor has been chosen and building a contract with nothing behind it would be speculative; `crash-and-performance.md` takes the same stance for crash reporting.

**Reason:** This build cycle adds a DC-owned interface + Hilt-bound implementation for both analytics and crash reporting, mirroring the `Logger`/`AndroidLogger` pattern exactly, but deliberately without picking a real vendor SDK (no Firebase/Amplitude/Crashlytics/Sentry dependency added). The bound implementation is a local no-op/logging-only implementation. Vendor choice remains, per both docs' own stated stance, a deliberate decision not to be made speculatively.

**Risk:** Low — the contract and DI wiring are real and usable; only the vendor backend is intentionally absent.

**Owner/exit condition:** Remove this deviation (or rather, replace the no-op implementation with a real vendor-backed implementation behind the same interface) once an actual vendor decision is made for a real downstream app built on this foundation.
