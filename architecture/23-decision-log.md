# Architecture decision log

[Back to architecture index](00-index.md)

## Scope

Durable decisions with date, context, decision, alternatives, consequences, affected rules/files. Keep concise; not a meeting log.

## Entries

PROPOSED entries below (`DEC-A3-n`, `DEC-A4-1`, `DEC-B2-n`, `DEC-B3-1`, `DEC-C1-n`, `DEC-C3-1`) are placeholders from lanes A3/A4/B2/B3/C1/C3 and are labelled "PROPOSED — pending Opus final review"; they are not accepted decisions and final numbers are assigned on merge.

### DEC-A3-1 — 2026-10-27 — PROPOSED — `SingleFlight`: detached run on the injected scope with a per-execution supervisor (MP:CON-01)

**Status: PROPOSED — pending Opus final review.** Placeholder ID; the final number is assigned on merge. Evidence is source reading plus the out-of-repo scratch JVM run (not a Gradle gate); Gradle NOT_RUN. Source: `reports/wp/A3-design.md`, code commit `bb5ce19`.

**Decision:** The block runs detached on the constructor scope; the key is removed on completion. Failure reaches every awaiter and is not cached. A failing block must not cancel the caller's scope (per-execution `SupervisorJob`, completed with the execution so it never keeps a joined scope such as `runBlocking`/`coroutineScope`/`runTest` from finishing). A cancelled scope yields `CancellationException` (`putIfAbsent` plus `invokeOnCompletion` outside the map callback; pre-A3 code threw `IllegalStateException("Recursive update")`). All callers cancelling does not stop the run.

**Alternatives:** ref-counted cancel when the last waiter leaves; run on the first caller.

**Affected:** `runtime/concurrency/.../SingleFlight.kt`, `modules/runtime-concurrency.md`, `09-concurrency-and-threading.md`.

### DEC-A3-2 — 2026-10-27 — PROPOSED — `AppCoroutineScope` ownership and shutdown; static receiver scopes (MP:CON-07)

**Status: PROPOSED — pending Opus final review.** Placeholder ID; the final number is assigned on merge. Evidence is source reading plus the out-of-repo scratch JVM run (not a Gradle gate); Gradle NOT_RUN. Source: `reports/wp/A3-design.md`, code commit `bb5ce19`.

**Decision:** Process-lifetime scope created once by the composition root (`DcApplication`) and never cancelled in production; tests/embedders stop it with `cancel()`; later `launch` calls are silently dropped; the uncaught-exception handler must not throw. Rule text: THR-03 (PROPOSED). Proposed deviation (no KD number yet): `DcAlarmReceiver`/`DcAlarmRestorer` keep static, class-owned scopes because receivers have no DI; bounded by `goAsync` plus a 9 s timeout; not fakeable in tests.

**Alternatives:** add `close()`; Hilt `@Singleton` binding.

**Affected:** `09-concurrency-and-threading.md` (THR-03), `modules/runtime-concurrency.md`, `modules/runtime-scheduling.md`, possibly `22-known-deviations.md`.

### DEC-A3-3 — 2026-10-27 — PROPOSED — Work id must equal `DcJobSpec.name` (MP:SCH-01/03)

**Status: PROPOSED — pending Opus final review.** Placeholder ID; the final number is assigned on merge. Evidence is source reading plus the out-of-repo scratch JVM run (not a Gradle gate); Gradle NOT_RUN. Source: `reports/wp/A3-design.md`, code commit `bb5ce19`.

**Decision:** `DcScheduler` rejects a work schedule whose `id` differs from `spec.name` (`Rejected`), because `cancel(id)`/`observe(id)` address the unique work by name and `id` was previously ignored (silent cancel/observe miss).

**Alternatives:** rename the spec to `id`; document only.

**Affected:** `runtime/scheduling/.../DcScheduler.kt` (`identityMismatch`), `modules/runtime-scheduling.md`, `14-background-work.md`, `modules/testing.md` (`FakeScheduler` does not enforce it).

### DEC-A3-4 — 2026-10-27 — PROPOSED — Idempotent enqueue: KEEP for `After`/`Every`, upsert-by-id for alarms (MP:SCH-01)

**Status: PROPOSED — pending Opus final review.** Placeholder ID; the final number is assigned on merge. Evidence is source reading plus the out-of-repo scratch JVM run (not a Gradle gate); Gradle NOT_RUN. Source: `reports/wp/A3-design.md`, code commit `bb5ce19`.

**Decision:** Documented current behaviour: `After` while unfinished is a no-op returning `Scheduled`; `Every` KEEP means changed interval/constraints are not applied (UPDATE unreachable through the facade, follow-up); alarm `schedule` upserts by id; changing the receiver class under one id leaves the old PendingIntent (cancel first); `DcMissedPolicy` only affects `ExactAt`.

**Alternatives:** REPLACE; expose the policy on the facade.

**Affected:** `modules/runtime-scheduling.md`, `14-background-work.md`.

### DEC-A3-5 — 2026-10-27 — PROPOSED — Per-entry decode of the persisted alarm list (MP:SCH-03)

**Status: PROPOSED — pending Opus final review.** Placeholder ID; the final number is assigned on merge. Evidence is source reading plus the out-of-repo scratch JVM run (not a Gradle gate); Gradle NOT_RUN. Source: `reports/wp/A3-design.md`, code commit `bb5ce19`.

**Decision:** `DcAlarmCodec.decodeList` drops only bad entries. Pre-A3 one stale entry made it return `[]`, and the next `FileDcAlarmStore.put` overwrote every stored alarm.

**Alternatives:** keep whole-list `runCatching`; quarantine file.

**Affected:** `runtime/scheduling/.../alarm/DcAlarmCodec.kt`, `modules/runtime-scheduling.md`, `14-background-work.md`.

### DEC-A3-6 — 2026-10-27 — PROPOSED — `RetryPolicy` (in-process) vs `DcRetryPolicy` (WorkManager) stay distinct (MP:SCH-06)

**Status: PROPOSED — pending Opus final review.** Placeholder ID; the final number is assigned on merge. Evidence is source reading plus the out-of-repo scratch JVM run (not a Gradle gate); Gradle NOT_RUN. Source: `reports/wp/A3-design.md`, code commit `bb5ce19`.

**Decision:** Keep both and document. `RetryPolicy`: per call, exception predicate, jittered coroutine `delay`, dies with the process. `DcRetryPolicy`: survives process death, OS-driven via `Result.retry()`, backoff >= 10 s, no jitter, ceiling enforced in `DcWorker`. Both count `maxAttempts` as total executions. Do not nest `retrying` around a `DcJobResult.Retry` path.

**Alternatives:** merge; adapter.

**Affected:** `modules/runtime-concurrency.md`, `modules/runtime-scheduling.md`, `14-background-work.md`.

### DEC-A3-7 — 2026-10-27 — PROPOSED — `:runtime:scheduling` exposes `:foundation` as `api`

**Status: PROPOSED — pending Opus final review.** Placeholder ID; the final number is assigned on merge. Evidence is source reading plus the out-of-repo scratch JVM run (not a Gradle gate); Gradle NOT_RUN. Source: `reports/wp/A3-design.md`, code commit `bb5ce19`.

**Decision:** `api(project(":foundation"))` because `DcJobState.Failed`/`DcJobResult.Failure` expose `AppError` (applied in `bb5ce19`; edge set unchanged, only the configuration; HANDOVER F2 resolved). Same class of leak NOT fixed: scheduling exposes `DispatcherProvider`, `Flow`, `KSerializer`, `ListenableWorker`; `:data:network` exposes `AppError`. A general `api` vs `implementation` exposure rule still does not exist.

**Alternatives:** hide `AppError`.

**Affected:** `runtime/scheduling/build.gradle.kts`, `dependency-rules.md`, `context-map.yaml`, `module-map.md`.

### DEC-A3-8 — 2026-10-27 — PROPOSED — `AppError` exhaustiveness and cause/`toString` policy (MP:CORE-01)

**Status: PROPOSED — pending Opus final review.** Placeholder ID; the final number is assigned on merge. Evidence is source reading plus the out-of-repo scratch JVM run (not a Gradle gate); Gradle NOT_RUN. Source: `reports/wp/A3-design.md`, code commit `bb5ce19`.

**Decision:** Sealed with exhaustive `when` and no `else`; sites today are `Retryable.kt` and `DcWorker.workKind`; per-variant tests. The WorkManager kind string is lossy by design (`Http` -> code 0, no cause/serverMessage). `cause` is diagnostic only, kept by reference, part of `equals`; `toString` prints it, so never log `"$error"`.

**Alternatives:** stable `code` on `AppError`; redact `toString` (follow-up).

**Affected:** `foundation/.../AppError.kt`, `modules/foundation.md`, `11-error-and-result-model.md`.

### DEC-A4-1 — 2026-10-27 — PROPOSED — Design-system screenshot test stack (MP:UI-09, D-03)

**Status: PROPOSED — pending Opus final review.** Placeholder ID. Source: `reports/wp/A4.md` section E; nothing was added to the build.

**Context:** AGP 9.3.1, Kotlin 2.2.10, BOM 2026.02.01, no Robolectric/screenshot dependency in the catalog, CI without an emulator (RK-11); previews already use multi-preview annotations. Compatibility of any option with AGP 9.3.1 is UNKNOWN (plugins cannot be resolved here).

**Options:** (1) Compose Preview Screenshot Testing (experimental; reuses `@Preview`; AGP 9 compatibility risk); (2) Roborazzi + Robolectric (mature JVM goldens; 3-4 new dependencies); (3) Paparazzi (lags AGP/Compose upgrades); (4) instrumented golden libraries (need an emulator in CI); (5) semantics-only, no goldens.

**Recommendation:** now option 5 plus the registry; after R0 yields a green networked build, time-boxed spike of option 1 on about 5 components (`DcButton`, `DcTextField`, `DcStatusChip`, `DcListRow`, `DcContentState`, light/dark x font 1.0/2.0), falling back to option 2. Criteria: AGP/Kotlin compatibility, no-emulator CI, golden storage size, matrix size. DEC-08 "screenshot testing undecided" stands until Opus rules; MP:UI-09 stays NOT_RUN.

**Affected:** `ui/designsystem/`, `07-design-system/previews-and-catalog.md`, `17-testing.md`.

### DEC-09 — 2026-10-27 — Master-plan backlog items are written `MP:<ID>`; bare IDs mean architecture rules

**Context:** Master-plan backlog IDs (`plan/FOUNDATION_MASTER_PLAN.md` section 4: `PERM-01`, `TST-01`, `SEC-01`, `NAV-01`, `OBS-01`, `BLD-01..03`, `UI-01..03`) reuse the prefix and number of existing architecture rules with a different meaning. R1.1 pages and the R1.4 docs-only trial showed a task titled "PERM-01" is ambiguous.

**Decision:** A bare ID (`PERM-01`) is an architecture rule defined in `architecture/`. A backlog item is written `MP:<ID>` (`MP:PERM-01`). `context-map.yaml` `owners_rules` hold bare rule IDs only. Applied to `architecture/modules/*.md`, `architecture/features/*.md` and the module template; also expected in reports, work-package titles and commit messages.

**Alternatives considered:** Renumber the rules (breaks every existing reference and the stable-ID requirement); renumber the backlog (breaks the master plan); a `BL-` or `-BL-` infix (longer, no better than a colon prefix).

**Consequences:** `plan/AGENT_EXECUTION_PLAN.md` and `plan/FOUNDATION_MASTER_PLAN.md` still use bare backlog IDs (outside the writable scope of R1.5); readers must apply the convention when mapping a plan row to a rule. Documented in `context-protocol.md` section A2.

**Affected:** `architecture/context-protocol.md`, `architecture/modules/**`, `architecture/features/**`, `architecture/context-map.yaml` (comment), `architecture/00-index.md`.

### DEC-08 — 2026-10-18 — Extend the DS widget set with module-owned string defaults and typed models

**Context:** Common widgets (menus, tabs, badges, forms, snackbar, selection groups) were missing, and several components hard-coded English default labels.

**Decision:** Add the widgets described in `07-design-system/component-roadmap.md` (W0–W3) inside `:ui:designsystem`; keep one module. Default labels move to `res/values/strings.xml` (`dc_*`) and remain overridable. Choices use `DcOption<T>`/`DcMenuItem`; sizes use enums (`DcAvatarSize`, `DcSkeletonSize`, `DcProgressSize`); snackbars use `DcSnackbarMessage` instead of Material `SnackbarData`. Existing `Dp`-based `DcAvatar`/`DcSkeleton` overloads stay for source compatibility.

**Alternatives considered:** Split `:ui:components`; keep literal English defaults; expose Material state types.

**Consequences:** Public API is additive. Time field, list-detail scaffold, swipe rows and other W4 items stay deferred until a real consumer exists (DEC-04). Screenshot testing remains undecided.

**Affected:** `ui/designsystem/**`, DS-01..04, INV-08, INV-10.

### DEC-07 — 2026-09-05 — Separate appearance persistence from Android locale ownership

**Context:** Applications need common controls for theme, interface size, dynamic color and language. Persisting all four in custom DataStore state would duplicate Android's per-app locale source of truth; globally overriding Compose density would also interfere with accessibility and responsive breakpoints.

**Decision:** Persist typed appearance values through `AppConfigurationRepository`; apply `DcUiScale` to semantic typography/spacing at the root `DcTheme`, retaining the fixed touch-target floor and system font scale. Manage language only through `AppLanguageController`/`AppCompatDelegate`, with generated locale config and AndroidX compatibility storage. Keep these application-level classes in `:app` until another module consumer earns a separate `:platform:configuration` boundary.

**Alternatives considered:** Store locale twice in DataStore, override `LocalDensity`, let each screen apply configuration, or pre-create a configuration module with one consumer.

**Consequences:** Changes are immediately observable and survive restart; locale stays synchronized with Android App Languages; feature UI receives no raw DataStore or AppCompat APIs. UI scale changes semantic layout/text without weakening accessibility minimums.

**Affected:** `app/config/*`, `MainActivity.kt`, `ui/designsystem/theme/{DcTheme,DcUiScale}.kt`, CFG-02/03, DAT-01, INV-02/08.

### DEC-06 — 2026-09-05 — Promote stable UI primitives, not domain-shaped screens

**Context:** The Trip showcase contained a reusable label/value row and a record-card layout, but copying a Trip-shaped "service screen" into the design system would introduce business assumptions and a large, inflexible API.

**Decision:** Promote the stable label/value policy as business-neutral `DcKeyValueRow`; compose record cards from `DcCard` + `DcListRow` + `DcStatusChip`; keep screen orchestration, filters, formatting, domain status mapping and models in the feature/showcase layer. Product states come from Route/ViewModel and are rendered through `DcContentState`, not through developer-only state controls in the product UI.

**Alternatives considered:** Add a generic `DcServiceScreen`/`DcServiceRecordCard`, or keep duplicating the label/value styling in each feature.

**Consequences:** The same primitives cover trips, orders, bookings, transactions and profiles without leaking their models into `:ui:designsystem`. Feature screens retain layout freedom, while typography, spacing, status and interaction policy stay centralized.

**Affected:** `ui/designsystem/components/DcListComponents.kt`, `app/showcase/trip/*`, `architecture/07-design-system/common-screen-patterns.md`, INV-03, INV-08, DS-04.

### DEC-05 — 2026-09-05 — Apply the UI style guide as a delta over the existing DC design system

**Context:** `:ui:designsystem` already exposed a broad `Dc*` API and a five-layer `DcTheme` override seam, while the requested guide assumed a new `core/design-system` module and `App*` APIs.

**Decision:** Keep `:ui:designsystem` and the `Dc` prefix; install the clean blue/neutral visual language as overridable defaults; keep dynamic color opt-in; preserve adaptive navigation; keep caller-owned dialog visibility; and place business-specific Trip reference screens in debug `:app` showcase code.

**Alternatives considered:** Create the module and API names from the generic prompt, force phone-only `NavigationBar`, or put Trip models in the reusable UI module.

**Consequences:** Existing consumers retain their architecture and can rebrand through `DcTheme`. Public component parameters are additive. Shape defaults intentionally move to 4/8/12/16/20dp, so all shape call sites are audited. Status chips remain visually compact because they are labels; Material owns the touch target of interactive filter chips.

**Affected:** `ui/designsystem/**`, `app/src/main/**`, DS-01..04, INV-03, INV-08.

### DEC-01 — 2026-08-16 — Correct `CLAUDE.md` namespace instead of renaming the codebase

**Context:** Phase B reconnaissance found root `CLAUDE.md` claimed primary namespace `com.dc.foundation`, but the real Android Studio-generated project uses `com.dc.android.foundation` throughout (`app/build.gradle.kts`, every `package` declaration).

**Decision:** Update `CLAUDE.md` to state the verified namespace rather than renaming the package across the codebase.

**Alternatives considered:** Rename `com.dc.android.foundation` → `com.dc.foundation` to match the original intent in `CLAUDE.md`.

**Consequences:** Zero code churn; `CLAUDE.md` now matches reality. If a future decision wants the shorter namespace, it is a deliberate rename with its own decision entry, not a silent fix.

**Affected:** `CLAUDE.md` §1, `architecture/03-project-structure.md` (STR-02), `architecture/22-known-deviations.md` (KD-01).

### DEC-02 — 2026-08-16 — Do not pre-create empty seed docs for unstarted phases

**Context:** `plan/architecture/*` ships placeholder files for all 24 target documents (04-21, `07-design-system/*`, `10-data/*`, `15-observability/*`). Phase B's explicit scope is only `01`, `02`, `03` (plus the always-read `00-index`/`24-ai-reading-guide`).

**Decision:** Only create a root `architecture/*` document when the phase that owns it actually runs. `00-index.md` carries a status table so an AI reader knows a missing file means "not built yet," not "forgotten."

**Alternatives considered:** Copy the full `plan/architecture/` tree into root now as placeholders.

**Consequences:** Keeps `architecture/` honest (INV-10) and avoids the plan's own anti-overengineering constraint (§7) applied to documentation itself — no doc describes an unimplemented capability as if it were designed.

**Affected:** `architecture/00-index.md`, all future phase docs.

### DEC-03 — 2026-08-16 — Look up exact library versions via Maven metadata instead of guessing

**Context:** Phase D needed Hilt/KSP/androidx.hilt versions. An initial `WebSearch`/`WebFetch` pass produced inconsistent numbers (2.56.2 vs 2.59.2 vs 2.60.1) — search-engine-summarized results were not reliable for exact version pins.

**Decision:** For any dependency version that must be exact (build-breaking if wrong), fetch the authoritative `maven-metadata.xml` directly (`repo1.maven.org` via `curl`, or `dl.google.com/dl/android/maven2/.../group-index.xml` for AndroidX/Google artifacts) rather than trusting a search summary.

**Consequences:** Slower than trusting the first search result, but the Gate C/D builds only needed one retry each (both for real compatibility issues, not version mistakes) once this was adopted.

**Affected:** `gradle/libs.versions.toml` (hilt, ksp, androidxHiltCompose), any future phase adding a new dependency.

### DEC-04 — 2026-08-16 — Add a second reference feature (`articles`) instead of building the Reserved capabilities as speculative wrappers

**Context:** Nine architecture areas are still `Reserved`/not-implemented: navigation (NAV-01), network (DAT-NET-01), Room (DAT-ROOM-01), cache (DAT-CACHE-01), background work (BG-01), permissions (PERM-01), and the build-logic convention plugins. Each of those docs already states its own trigger condition for when the capability should move from "Reserved" to "real." The user has asked for all remaining capabilities to be implemented, but the plan's own anti-overengineering constraint (`plan/CLAUDE_IMPLEMENTATION_PLAN.md` §7) and DEC-02 both reject adding a capability with no real call site.

**Decision:** Rather than building the nine Reserved capabilities as speculative wrappers with no real call site, this build cycle adds a second reference feature, `articles` — it fetches from a real remote API, caches offline-first in Room, and is synced periodically via a background worker — specifically so each Reserved capability gets a genuine, non-speculative use case that satisfies its own documented trigger condition:

- NAV-01 triggers on "a second destination exists" → `articles` is that second destination.
- DAT-NET-01 triggers on "a real endpoint exists to call" → `articles` calls one.
- DAT-ROOM-01 triggers on "structured/relational data exists" → the articles cache table.
- DAT-CACHE-01 triggers on "a repository combines RemoteDataSource + LocalDataSource" → `ArticlesRepository`.
- BG-01 triggers on "work must survive app closure/process death with retry guarantees" → the periodic articles sync worker.
- PERM-01 triggers on "a real `<uses-permission>` entry/call site exists" → `POST_NOTIFICATIONS` for the worker.
- convention-plugins trigger on "a 4th or 5th Android module" → this build adds enough new Android modules to cross that threshold.

This decision does **not** cover analytics, crash reporting, or secure storage — those are addressed separately (see `22-known-deviations.md` KD-04/KD-05) because they cannot get a genuine use case within a business-neutral foundation repo without picking a vendor or building fake auth.

**Alternatives considered:** Build the nine capabilities directly as unused/demo wrappers with no real call site; skip the capabilities entirely and leave them `Reserved` despite the user's explicit request.

**Rationale:** This satisfies `plan/CLAUDE_IMPLEMENTATION_PLAN.md` §7's anti-overengineering constraint (no capability added without a genuine near-term use case) while still fulfilling the user's explicit request to implement everything — genuine use cases are manufactured deliberately rather than the constraint being silently ignored.

**Consequences:** NAV-01, DAT-NET-01, DAT-ROOM-01, DAT-CACHE-01, BG-01, PERM-01 rule IDs will be filled in with real rules (not "Reserved") in the phases that follow, once code backs them.

**Affected:** `architecture/06-navigation.md` (NAV-01), `architecture/10-data/network.md` (DAT-NET-01), `architecture/10-data/persistence-room.md` (DAT-ROOM-01), `architecture/10-data/cache-and-offline.md` (DAT-CACHE-01), `architecture/14-background-work.md` (BG-01), `architecture/13-permissions.md` (PERM-01), `build-logic/` convention plugins, all Phase M–T implementation work.

### DEC-B3-1 — 2026-10-27 — PROPOSED — D-01: move `DcDatabase`/`Article*` from `:data:database` to `:app`

**Status: PROPOSED — pending Opus final review.** Placeholder ID. Source: `reports/wp/B3-design.md`, lane B3 commit `c2e7331`. Move NOT performed; Gradle NOT_RUN.

**Context:** `:data:database` ships a sample `articles` schema (`DcDatabase` version 1, `ArticleEntity`, `ArticleDao`, exported `1.json`), so any importer inherits it (CLAUDE.md invariant 9). `:app` is the sole consumer; `RoomDatabaseFactory` is already schema-generic.

**Decision (proposed):** Option 2, executed as a separate refactor after an ADR: move the three sources and `schemas/` to `:app`; `:data:database` keeps factory, `TransactionRunner` and `StalenessPolicy`. Until then Option 1 stands (KDoc GENERIC/SAMPLE labels applied by B3).

**Alternatives:** keep as annotated sample; new `:features:articles-data` module (rejected: speculative module).

**Consequences / constraints:** keep `version = 1`, table `articles`, column names, DB file `dc_foundation_db` and identity hash (diff regenerated `1.json`); preferably keep the `DcDatabase` FQCN; `:app` must apply `dc.room`/KSP (unverified); update `DatabaseModule`, `ArticlesRepository`, test fakes, context-map; own commit, rollback by revert.

**Affected:** `architecture/modules/data-database.md`, `10-data/persistence-room.md`, `context-map.yaml`.

### DEC-B2-1 — 2026-10-27 — PROPOSED — D-04: keep `security-crypto` behind `SecureStorage` now, Tink-based second implementation before MP:SEC-03

**Status: PROPOSED — pending Opus final review.** Placeholder ID. Source: `reports/wp/B2-design.md`, lane B2 commit `8b6a6fe`. Library version/deprecation claims are from model knowledge and UNKNOWN until verified.

**Decision (proposed):** Option A short term (KD-04: no credential flows, nothing to migrate). Before the first real credential (MP:SEC-03) add a Tink `Aead`/`AndroidKeysetManager` implementation as a second `SecureStorage` implementation with an explicit one-time migration (read old, write new, verify, delete old) and a backup stance (exclude the secure prefs file; a restored file without its key is treated as absent). Never swap provider or rotate keys silently. Decide before MP:SEC-03 starts.

**Alternatives:** own Keystore AES-GCM adapter plus DataStore (new crypto code to own); keep indefinitely.

**Lifecycle gaps for the ADR:** no `clear()`; prefs and `MasterKey` rebuilt per call; writes use `apply()`; read path maps `SecurityException` to null (done in B2).

**Affected:** `modules/security.md`, `12-security-and-session.md`, KD-04.

### DEC-B2-2 — 2026-10-27 — PROPOSED — OBS-02: telemetry backends swallow `Logger` failures; `Redactor` safety net

**Status: PROPOSED — pending Opus final review.** Placeholder ID. Source: `reports/wp/B2-design.md`, lane B2 commit `8b6a6fe`.

**Decision (proposed):** `LoggingAnalyticsTracker` and `LoggingCrashReporter` catch `Exception` (not `Throwable`) around the `Logger` call and swallow it silently, because telemetry is best-effort and must never crash the app; `Logger` implementations themselves are not wrapped; future vendor adapters (MP:OBS-04) follow the same rule. `Redactor` (pure Kotlin, in `:observability`) is applied by `Logging*` as a heuristic SEC-01 safety net; callers still own SEC-01.

**Consequences:** failures are invisible (no secondary channel); Bearer/Basic false positives and free-text false negatives are possible; `LoggingCrashReporter` still forwards the throwable to `Logger.e`.

**Affected:** `modules/observability.md`, `15-observability/*`, KD-05 (stays Open).

### DEC-C1-1 — 2026-10-27 — PROPOSED — D-02: minimal navigation contract (`NavigationEffect` + `DestinationRoutes`)

**Status: PROPOSED — pending Opus final review.** Placeholder ID. Source: `reports/wp/C1-design.md`, lane C1 commit `aca166d`.

**Decision (proposed):** keep navigation depth minimal: an engine-neutral `NavigationEffect {Navigate, Back}` plus `DestinationRoutes` (stable route naming, round trip) in `:platform:navigation`. No `Navigator`/`NavController` wrapper and no deep-link table until a second consumer navigates from a ViewModel. Argument encoding (MP:NAV-01) deferred: no destination has arguments.

**Alternatives:** status quo (Destination only); full `Navigator` interface plus fake plus deep-link table (rejected for now).

**Affected:** `modules/platform-navigation.md`, `06-navigation.md`.

### DEC-C1-2 — 2026-10-27 — PROPOSED — MP:PERM-02 shape: additive `PermissionStatus` and lifecycle-free `PermissionRequester`

**Status: PROPOSED — pending Opus final review.** Placeholder ID. Source: `reports/wp/C1-design.md`, lane C1 commit `aca166d`.

**Decision (proposed):** add `PermissionStatus` (5 cases) with a pure resolver, a suspend `PermissionRequester` (`check`/`request`), `PermissionRequestTracker` and `FakePermissionRequester`, leaving the 3-state `PermissionState` untouched. The Flow-based observer and the Activity-bound implementation are deferred to `:app` glue.

**Consequences:** `FakePermissionRequester` lives in the main source set because `:platform:permission` may not depend on `:testing` (follow-up); the `compose`/`activity-compose` build dependencies were removed.

**Affected:** `modules/platform-permission.md`, `13-permissions.md`.

### DEC-C3-1 — 2026-10-27 — PROPOSED — D-05: publishing model, versioning and API-compatibility policy

**Status: PROPOSED — pending Opus final review (and user answers).** Placeholder ID. Source: `reports/wp/C3-design.md`, lane C3 commit `b7487f0`. Nothing is published; no `maven-publish` block exists.

**Decision (proposed):** Option A now (composite build / git submodule via `includeBuild`, no publishing config); trigger for Option B (internal Maven repository plus a `dc-foundation-bom`) is the first consumer in another repository that cannot track foundation HEAD. SemVer per module set via a lock-step BOM, `0.y.z` until modules pass their gate and have multi-consumer evidence; `build-logic` versioned separately in practice (AGP/Gradle coupling). Publish only `maturity >= beta` modules; `:app` is never published. Deprecation: `@Deprecated` with `ReplaceWith` plus CHANGELOG line, at least one MINOR (or 30 days) at WARNING, then ERROR, removal only in a MAJOR. Binary-compatibility-validator (`apiCheck`) adopted only when Option B is triggered.

**Open questions for the user:** an external consumer and hosting; lock-step BOM acceptable; a 1.0 target date.

**Affected:** `modules/build-logic.md`, `README.md`/`CHANGELOG.md`, future `19-ci-release-quality-gates.md`.
