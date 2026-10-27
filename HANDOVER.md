# Task Handover — DC Android Foundation

**Re-authored:** 2026-10-27 (R0.3) · **Base commit:** `087fcf3` (branch `claude/practical-gauss-rmiffo`; working tree also holds uncommitted R0 report/plan/agent edits)
**Authority:** `settings.gradle.kts`, current source and `architecture/` outrank this file. Evidence: `reports/baseline-verification.md`.

## 1. Current state

- 13 Gradle modules + the `build-logic/` included build (12 `.kt` convention-plugin files). Source counts: 136 main `.kt`, 24 `src/test`, 4 `src/androidTest`.
- Modules: `:app`, `:foundation`, `:runtime:{concurrency,scheduling}`, `:ui:designsystem`, `:data:{preferences,network,database}`, `:platform:{navigation,permission}`, `:security`, `:observability`, `:testing`.
- Reference features in `:app`: `greeting` and `articles`.
- Toolchain (read from files, not built): Gradle 9.5.0, daemon JVM 25, AGP 9.3.1, Kotlin 2.2.10, KSP 2.2.10-2.0.2, compileSdk 37, minSdk 24.
- Roadmap: [`plan/FOUNDATION_MASTER_PLAN.md`](plan/FOUNDATION_MASTER_PLAN.md) (R0–R9) and [`plan/AGENT_EXECUTION_PLAN.md`](plan/AGENT_EXECUTION_PLAN.md) (work packages, agents, report template). R0 is in progress; R1 is next.

## 2. Where to start reading

1. [`CLAUDE.md`](CLAUDE.md), then [`architecture/00-index.md`](architecture/00-index.md).
2. [`architecture/22-known-deviations.md`](architecture/22-known-deviations.md) and [`architecture/23-decision-log.md`](architecture/23-decision-log.md).
3. [`reports/baseline-verification.md`](reports/baseline-verification.md) for the current inventory and build evidence.
4. [`PLAN_REPORT.md`](PLAN_REPORT.md) for the detailed A–L log; [`IMPLEMENTATION_STATUS.md`](IMPLEMENTATION_STATUS.md) is a secondary summary (its file counts differ slightly from the baseline report; the baseline report was taken from files at `087fcf3`).

## 3. Verification (current HEAD `087fcf3`)

Legend: VERIFIED = run on current HEAD with evidence; READ_FROM_FILES = checked by reading files, nothing executed; NOT_RUN = not executed; STALE = ran on an older layout, not valid for HEAD.

| Gate | Status | Evidence / reason |
|---|---|---|
| Gradle configuration (`./gradlew projects`) | NOT_RUN (failed at toolchain provisioning) | `reports/logs/R0.2-gradle-projects.log`. Daemon needs JDK 25; only JDK 21 installed; `api.foojay.io` returned 403. |
| JVM unit tests (`:foundation:test`, `:runtime:concurrency:test`) | NOT_RUN (failed at plugin resolution) | `reports/logs/R0.2-gradle-jvm-test.log`. AGP 9.3.1 not found in the Google/MavenRepo/Gradle Plugin repos (log). The `dl.google.com` CONNECT 403 cause is an orchestrator observation (curl, also in proxy status), not in the log. Run required a temporary daemon-criteria bypass; file restored. |
| `testDebugUnitTest` (all modules) | NOT_RUN | Blocked by the two failures above. |
| `lintDebug` | NOT_RUN | No fresh lint report exists. |
| `assembleDebug` | NOT_RUN | Blocked as above. |
| `connectedAndroidTest` (`GreetingScreenTest`, DS instrumented tests) | NOT_RUN | No emulator/device, Android SDK absent, `ANDROID_HOME` unset. |
| Last recorded PASS (4 modules, 7/7 tests, 0 lint errors, 2026-08-16) | STALE | Predates the 13-module layout (`IMPLEMENTATION_STATUS.md:9`). Not evidence for HEAD. |
| GitHub Actions CI (`.github/workflows/ci.yml`) | NOT_RUN | Never executed; no run URL or log available. |
| CodeGraph index | VERIFIED (index only) | `codegraph status`: 202 files, 3,098 nodes, 5,163 edges, up to date vs clean tree at 2026-10-27T15:48Z (`reports/wp/R0.1.md`). |
| Source/build-file inventory (modules, versions, tests) | READ_FROM_FILES | `reports/baseline-verification.md` Part A. |

Network blockers: `dl.google.com`/`maven.google.com`, `api.foojay.io`, GitHub release-asset downloads. The failures seen are environment/policy failures, not source errors.

## 4. Known deviations (see `architecture/22-known-deviations.md`)

| ID | Status | Note |
|---|---|---|
| KD-01 | Resolved (audit trail) | Namespace is `com.dc.android.foundation`. |
| KD-02 | Open (partially mitigated) | Static checker `tools/check_architecture.py --boundaries-only` + blocking CI job `architecture` added by lane A1 (checker run locally: PASS; CI run NOT_RUN). Exit: one real CI run with evidence, optional Gradle-graph check. `:app:dependencies` NOT_RUN. |
| KD-03 | Active | `android.disallowKotlinSourceSets=false` until KSP is AGP-9-native. |
| KD-04 | Open | Secure storage exists with no real credential flowing through it. |
| KD-05 | Open | Analytics/crash use local logging backends; no vendor chosen. |
| KD-06 | Open | Design-system catalog and trip showcase are gated by a runtime `BuildConfig.DEBUG` check in main source sets, so they ship in release (DEC-05 says debug-only). Added by R1.5. |

R0.3 reopened KD-02 (module boundaries exist but are not automatically enforced); no other KD changed status. There is no new build evidence.

## 5. Open gaps

- No current PASS evidence for unit tests, lint, assemble, or instrumented tests (section 3).
- Lint triage NOT_RUN. The 12 warnings recorded in August are untriaged and no fresh report can be produced yet. Do not treat them as fixed, false-positive, or accepted.
- Drift follow-ups (not fixed here): CI uses JDK 21 vs daemon toolchain 25; CI `testDebugUnitTest` likely skips JVM modules `:foundation` and `:runtime:concurrency`; Graphify hooks in `.claude/settings.json` use a Windows path; `graphify-out/` is absent although `CLAUDE.md` §4 refers to it; `jvmToolchain(11)` in `dc.kotlin.jvm` needs a JDK 11 toolchain.
- `:observability`, `:platform:navigation`, `:platform:permission` have no unit tests.
- `reports/` and the plan/agent edits are uncommitted.

## 6. Next actions

1. R0.4: make the Graphify hook portable or optional; add a CodeGraph `SessionStart` hook (requires user permission to edit `.claude/settings.json`).
2. R0.2 re-run: needs either network access (`dl.google.com`, `api.foojay.io`, GitHub releases) or an environment preinstalled with JDK 25, JDK 11, and the Android SDK (platform 37, build-tools, platform-tools, `ANDROID_HOME` set). Then run `testDebugUnitTest`, `lintDebug`, `assembleDebug`, and `connectedAndroidTest` if a device exists, and triage lint from the fresh report.
3. R1 (architecture-as-context) next per `plan/AGENT_EXECUTION_PLAN.md`. Build-dependent phases (R2–R7) stay blocked until step 2 yields real results.
4. Fix the CI drift items after step 2 (align JDK, run JVM-module `test`).

## 7. History (phases A–L, unchanged labels)

Executed 2026-08-16 under `plan/CLAUDE_IMPLEMENTATION_PLAN.md`; detail in `PLAN_REPORT.md`. Original handover reported 4 modules at that time.

| Phase | Result | Commit |
|---|---|---|
| A Repository reconnaissance | done | `21973b1` |
| B Invariants & dependency boundaries | done | `ec3d9ad` |
| C Design System foundation | done | `523c161` |
| D Hilt DI | done | `3f2dc8c` |
| E Presentation, navigation, lifecycle | done | `3b2d215` |
| F Concurrency & background execution | done | `adfc80d` |
| G Data platform | done | `adfc80d` |
| H Error, security, permissions, config, observability | done | `8d2538b` |
| I Testing, build logic, CI, quality | done | `144902e` |
| J Architecture documentation finalization | done | `595a089` |
| K Critical audit | skipped (no escalation condition fired) | `595a089` |
| L Final validation & sanitized ZIP | done (ZIP is git-ignored under `deliverables/`) | not recorded |

Later work (scheduling, network, database, navigation, permission, security, observability, testing, `articles`, DEC-05..DEC-08) is recorded in `architecture/23-decision-log.md` and `IMPLEMENTATION_STATUS.md`; it has no current build verification.

## 8. Follow-ups from the R1 docs audit (mapped to target phase)

Source: `reports/wp/R1.0..R1.5.md`, `*.review.md`. Observations only; none is fixed here, none has a KD because no documented rule or decision is contradicted (except KD-06). Verify against source/build before acting. Backlog items are `MP:<ID>` (DEC-09).

| # | Follow-up | Evidence | Target |
|---|---|---|---|
| F1 | `AndroidManifest.xml` declares no `INTERNET` permission while `articles` performs network calls; the refresh may always fail to the stale path. Merged manifest not inspected (build NOT_RUN). No architecture rule covers manifest permissions, so no KD. | `app/src/main/AndroidManifest.xml:6` (only `POST_NOTIFICATIONS`); `reports/wp/R1.2.md` D5 | R5.1 (MP:NET-05 articles sample); check the merged manifest first once R0.2 yields a build |
| F2 | `:runtime:scheduling` exposes `AppError` in public types (`DcJobState.Failed`, `DcJobResult.Failure`) but declares `:foundation` as `implementation`; consumers must add `:foundation` themselves. No rule on `api` vs `implementation` exposure exists. Decide `api(project(":foundation"))` or document. | `runtime/scheduling/build.gradle.kts`; `DcJobState.kt:9`; `DcWorker.kt:17`; `reports/wp/R1.1a.md` D3 | R2.1 (edge policy) then R3.3 |
| F3 | Edge `:data:network -> :observability` is declared but nothing under `data/network/src` imports it (only a KDoc mention in `NetworkClientConfig.kt:7`). | `data/network/build.gradle.kts:13`; `reports/wp/R1.1b.md` D1, R1.1c D4 | R2.1 |
| F4 | `:platform:permission` applies `dc.compose` and `androidx.activity.compose` but its two sources import no Compose; intent UNKNOWN. | `platform/permission/build.gradle.kts`; `reports/wp/R1.1c.md` D5 | R2.1; R6.1 if MP:PERM-02 needs them |
| F5 | `ArticlesViewModel` injects `SavedStateHandle` and never reads it. | `ArticlesViewModel.kt:36` | R6.2 |
| F6 | `modules/app.md` section 6 intro says all bindings are `@InstallIn(SingletonComponent)` and `@Singleton`; `GreetingModule` and `ArticlesModule` are unscoped `@Binds`. Reword to "`@Singleton` on the `@Provides` in `di/*`". | `reports/wp/R1.1d-R1.2.review.md` | next architecture-writer pass (R6.2 docs) |
| F7 | Mermaid blocks in `modules/data-preferences.md` and `modules/data-database.md` use `&lt;`/`&gt;` entities inside fences; Mermaid form is `#lt;`/`#gt;`, so they may render literally. | `reports/wp/R1.1b.review.md` | R2.3 (docs-check) or next doc pass |
| F8 | Hard-coded English strings in `ArticlesScreen.kt` (lines 40, 45, 54, 61) and showcase screens; legacy `DcSnackbarEffect(String?, SnackbarHostState)` still public next to the DEC-08 typed overload; `GreetingRoute` KDoc "only destination" is outdated. | `reports/wp/R1.1d.md`, `R1.2.md` D7, D8 | R4.3 (DS), R6.2 (app) |
| F9 | `app/src/main/res/xml/backup_rules.xml` and `data_extraction_rules.xml` are Android defaults with `allowBackup=true`; backup coverage of DataStore, Room and secure prefs is UNKNOWN. `ArticlesRepository` sends raw `AppError.cause` to `CrashReporter` while the `AppError` KDoc says never log raw cause. | `reports/wp/R1.1b.md` D7; `R1.4.md` cross-cutting 5 | R5.2 |
| F10 | Unused public APIs (`SingleFlight`, `JobRegistry`, `RetryPolicy`, Flow helpers, `FakeScheduler`, `TransactionRunner`, `StalenessPolicy`) have no production consumer. | `reports/wp/R1.1a.md` D2, D6; `R1.1b.md` D2 | R3.2, R3.3, R5.3 |
| F11 | `tools/check_architecture.py` hardening (non-blocking review items: strip `/* */`, multi-arg `include`, WARN on `projects.` accessors, return 2 on malformed YAML, tests for entry-symbol/TESTED/link checks). `plan/AGENT_EXECUTION_PLAN.md` and the master plan still use bare backlog IDs (DEC-09). | `reports/wp/R1.3.review.md` | R2.1 / R2.3; plan edit by the main session |

