# Final review — R0–R7 lane execution (Opus, critical-architecture-auditor)

Scope: `0ca6da2..0b87226` (198 files). Method: targeted `git diff` of every changed production `.kt`/`.kts`/`.xml`/`.yml`,
symbol cross-checks with grep, `tools/` checker + self-tests run locally (16/16 OK; boundaries ERROR 0; drift ERROR 0 WARN 3),
one scratch `kotlinc` probe (Gradle 9.5's bundled Kotlin 2.3.20, `-language-version 2.2`: `catch (_: Exception)` compiles).
Gradle was NOT run. Everything below marked "risk" is a reading-based judgement, not build evidence.

## 1. Blocking

None found. No unresolved reference, missing import, visibility error or non-exhaustive `when` was found in the
changed production sources. Checked: `SingleFlight`, `throttleFirst` (`coroutineContext.cancelChildren()`
resolves to the producer scope, cancels only window timers), `RetryPolicy.delayFor(random)`, `DcScheduler.identityMismatch`,
`DcRestoreDecision`/`restoreDecision` (exhaustive over the 5 `DcTrigger` subtypes), `DcWorkVerdict`/`toVerdict`/`errorFromKind`
(internal types used only from a private member, so visibility is fine), `dcJobState`, `allTags()` (`Set + String` -> `Set`), `AppError.toMessageRes`
(exhaustive over 5 variants), `@StringRes userMessage` and the typed `DcSnackbarEffect(message, state, onMessageShown)`/`DcSnackbarHost`
signatures, `EncryptedSharedPreferencesSecureStorage` (internal primary + public secondary constructor; private companion is legal in the
delegation call; `SecurityModule` still uses the `(Context, DispatcherProvider)` form), `Redactor`, the `Logger` fakes against
`Logger.e(..., throwable: Throwable? = null)`, DS token/`DcButton`/`DcListRow`/`DcMultiSegmentedButton` references, and the build-logic generator.
`DcApplication` schedules with `id == DcJobSpec.name` (`ARTICLES_SYNC_WORK_NAME`), so the new `Rejected` path does not affect the app.
`:platform:permission` main imports only `android.*`/`androidx.core`. `:app` declares `activity-compose` itself, and the `dc.testing` compose
branch is keyed on `withPlugin("dc.compose")`. Removing `dc.compose`/`activity-compose` breaks nothing.

## 2. Should-fix (ordered by risk)

1. `app/.../articles/ArticlesSyncWorker.kt:43-51`: every `AppError` becomes `DcJobResult.Retry`, including non-retryable
   `Http` 4xx/`Serialization`, so WorkManager burns all 5 attempts on a permanent failure. Fix: `val error = refresh()`;
   `null -> Success`; `error.isRetryable -> Retry`; else `Failure(error)` (existing property in `foundation/.../core/Retryable.kt:4`). Update the "explicit false" comment.
2. `platform/navigation/.../NavigationEffect.kt` `DestinationRoutes`: (a) `of()` uses `KClass.qualifiedName`, but type-safe
   navigation routes use `serializer().descriptor.serialName`. They are equal today, but they diverge under R8 or `@SerialName`. Derive from the serial name
   (`serializer(type)` or one `when` per object). (b) the private `all` list is hand-maintained (F13). Replace it with an exhaustive
   `when (d) { Greeting, Articles -> ... }` helper so that a new `Destination` causes a compile error, not a silent `null`.
3. `.github/workflows/ci.yml` (build + instrumentation jobs): the first run depends on foojay provisioning JDK 25 (daemon) and JDK 11
   (`dc.kotlin.jvm`). Make it deterministic: `setup-java` with `java-version: |\n 11\n 25\n 21` and
   `ORG_GRADLE_PROJECT_org.gradle.java.installations.fromEnv=JAVA_HOME_11_X64,JAVA_HOME_25_X64`
   (or `-Porg.gradle.java.installations.fromEnv=...`). Pre-existing first-run risk, not introduced by these lanes: Kotlin 2.2.10 + AGP 9.3.1
   running on a JDK 25 daemon. If KGP rejects JDK 25, set `toolchainVersion=21` in `gradle/gradle-daemon-jvm.properties`.
4. `runtime/concurrency/.../FlowSharing.kt` `throttleFirst`: `var windowOpen` is written by the timer child and read by the collector.
   On a multi-threaded dispatcher this is a data race (pre-existing). Use `AtomicBoolean` or `@Volatile`.
5. `runtime/scheduling/.../alarm/AndroidDcAlarmScheduler.kt` `restore()` (F19): wrap each `scheduleAt` in `runCatching` so that one bad alarm
   does not abort the rest. Same spirit as DEC-A3-5.
6. `:testing` compiles `FakeScheduler` against `DcScheduleRequest`/`DcJobSpec` -> `DcWorker : CoroutineWorker`, but work-runtime
   is `implementation` in `:runtime:scheduling`. If the first build fails with "Cannot access class CoroutineWorker", add
   `api(libs.androidx.work.runtime.ktx)` to `:runtime:scheduling` (F20 class of leak; consistent with DEC-A3-7 amended below).
7. `LoggingCrashReporter.recordException` forwards the raw throwable (F16). Acceptable for logcat only. Vendor adapters (MP:OBS-04)
   must send class + redacted message.

## 3. Decisions

| ID | Verdict | Rationale | Exact file changes |
|---|---|---|---|
| D-01 / DEC-B3-1 | **Accept (deferred)** | Sample schema in a reusable module breaks invariant 9. Moving needs KSP/Room in `:app` (KD-03) and a green build first. | After R0.2 is green: move `ArticleEntity.kt`, `ArticleDao.kt`, `DcDatabase.kt`, `data/database/schemas/` -> `app/` (+`dc.room` in `app/build.gradle.kts`); keep identity hash/table/db name; update `di/DatabaseModule.kt`, `context-map.yaml`, `modules/data-database.md`. Own commit. |
| D-02 / DEC-C1-1 | **Amend** | Minimal contract is right. Route naming must match the nav engine and be exhaustive. | `NavigationEffect.kt`: Should-fix 2; `DestinationRoutesTest` asserts `of(x) == serializer<x>().descriptor.serialName`. |
| D-03 / DEC-A4-1 | **Accept** | No screenshot deps until a networked build exists. | None now. After a green build: spike Compose Preview Screenshot Testing (5 components, light/dark x font 1.0/2.0) with a time box, falling back to Roborazzi. MP:UI-09 stays NOT_RUN. |
| D-04 / DEC-B2-1 | **Accept + amend** | No credential flows (KD-04), so there is nothing to migrate yet. | Before MP:SEC-03: a Tink `SecureStorage` impl plus a one-time migration, `clear()`, cached prefs; add `<exclude domain="sharedpref" path="dc_foundation_secure_prefs.xml"/>` to `app/src/main/res/xml/backup_rules.xml` and `data_extraction_rules.xml` (also closes part of F9). |
| D-05 / DEC-C3-1 | **Accept Option A** | No external consumer yet. Publishing config would be speculative. | None. Keep the 3 user questions open in HANDOVER. |
| DEC-A3-1 | Accept | Verified: per-execution supervisor completed on finish, LAZY + `putIfAbsent`, handler outside the CHM callback. No leak or recursion found. | none |
| DEC-A3-2 / THR-03 | Accept | Single creator; static receiver scopes bounded by `goAsync` + 9 s. | Record receiver scopes as **KD-07** (accepted deviation) in `22-known-deviations.md`; THR-03 -> ACCEPTED. |
| DEC-A3-3 | Accept | A silent cancel/observe miss is worse than an explicit `Rejected`. | none |
| DEC-A3-4 | Accept | Documented KEEP. UPDATE is not reachable through the facade. | Backlog item: `DcScheduleRequest.Work(updateExisting)` before any consumer changes intervals. |
| DEC-A3-5 | Accept | Correct fix for whole-store loss. | Add Should-fix 5 (same policy for `restore()`). |
| DEC-A3-6 | Accept | Different lifetimes; do not nest. | none |
| DEC-A3-7 | **Amend -> rule** | New rule DEP-API: "a module whose public signatures expose another module's/library's types declares it `api`". | `dependency-rules.md` add the rule; `data/network/build.gradle.kts` `api(project(":foundation"))`; scheduling: `api` for coroutines-core, serialization-core, work-runtime-ktx, `:runtime:concurrency` (F20). |
| DEC-A3-8 | Accept | Exhaustive `when` is already relied on (`toMessageRes`, `workKind`). | none |
| DEC-B2-2 | Accept | `catch (Exception)` (not Throwable) is the right boundary. The redactor is a safety net only. | none (F14 false positives stay a backlog item) |
| DEC-C1-2 | Accept + amend | Shape is fine. Fakes belong in `:testing` (house rule, e.g. `FakeDispatcherProvider`). | Move `FakePermissionRequester` -> `testing/src/main/java/.../testing/FakePermissionRequester.kt`; `testing/build.gradle.kts` `api(project(":platform:permission"))` (edge already in context-map); its test -> `testing/src/test`. |
| F12 follow-up | Decided above | | |
| DB-02 / F18 | **androidTest + `room-testing`** | `MigrationTestHelper` is androidTest-native and needed for any v2 schema. Robolectric is not added for one class. | `libs.versions.toml`: `androidx-room-testing`; `data/database/build.gradle.kts`: `androidTestImplementation`; in-memory DB tests for commit/rollback/cancel in `data/database/src/androidTest`. Runs in the non-blocking emulator job; stays NOT_RUN until then. Revisit if D-03 brings in Robolectric. |
| KD-02 | **Stay Open** | Exit condition (one real CI run with evidence) not met. | none |
| KD-06 | **Fix direction: source-set split, no new module** | DEC-05 stays as written; do not weaken it to "runtime-gated". | R6.2: `showcase/trip/*` -> `app/src/debug`; `ShowcaseEntries` in `src/debug` + empty stub in `src/release`; move `DesignSystemCatalog`/`ExtendedCatalog` to `app/src/debug` if they use public DS API only (verify), otherwise keep them in DS and keep the KD Open. Verify by release dex inspection. |

## 4. Gate verdicts (master plan §7)

| Phase | Verdict | Why |
|---|---|---|
| R0 | **Done** | Gate is honesty of status. HANDOVER marks every gate NOT_RUN or READ_FROM_FILES. |
| R1 | **Done** | context-map + module pages + checker; checker re-run here: ERROR 0. |
| R2 | Implemented, unverified (NOT_RUN) | Python checks run locally. Gradle commands and CI have never run. |
| R3 | Implemented, unverified (NOT_RUN) | Contract tests passed only in an out-of-repo scratch JVM (newer Kotlin, older coroutines, Android stubs). |
| R4 | Doc gate met. Code NOT_RUN | Registry 133/133 rows (3 stale `File:line` per A4 review). DS code edits are not compiled. |
| R5 | Implemented, unverified (NOT_RUN) | Gate needs failure paths demonstrated by tests that ran. |
| R6 | Implemented, unverified (NOT_RUN) | 2 journeys exist in source; instrumented blocker is explicit, but nothing is compiled. |
| R7 | Implemented, unverified (NOT_RUN) | Gate requires generate -> compile/test. Not run. |

No test may be marked TESTED and no phase R2–R7 may be reported "done" until the checklist below yields evidence.

## 5. First-run checklist (when network is available; stop at first failure, log to `reports/logs/`)

1. Preconditions: reach `dl.google.com`, `maven.google.com`, `plugins.gradle.org`, `repo.maven.apache.org`, `api.foojay.io`
   (or preinstall JDK 25 + 11 + Android SDK 37, `ANDROID_HOME`).
2. `./gradlew -p build-logic test`: build-logic compiles and generator tests run (isolates convention plugins).
3. `./gradlew projects --configuration-cache`: daemon JVM 25 / KGP 2.2.10 compatibility (Should-fix 3).
4. `./gradlew :foundation:test :runtime:concurrency:test`: R3 concurrency contracts on the real coroutines version.
5. `./gradlew :runtime:scheduling:testDebugUnitTest :testing:testDebugUnitTest`: `api(:foundation)` and the FakeScheduler classpath (Should-fix 6).
6. `./gradlew :observability:testDebugUnitTest :security:testDebugUnitTest :data:network:testDebugUnitTest :data:preferences:testDebugUnitTest :data:database:testDebugUnitTest :platform:navigation:testDebugUnitTest :platform:permission:testDebugUnitTest :ui:designsystem:testDebugUnitTest`
7. `./gradlew :app:kspDebugKotlin :app:testDebugUnitTest`: Hilt graph without `SavedStateHandle`, typed snackbar, `R.string` ids.
8. `./gradlew testDebugUnitTest`: full sweep, matching CI.
9. `./gradlew lintDebug` (`abortOnError = true`): triage the 12 historical warnings from the fresh report.
10. `./gradlew assembleDebug`, then confirm `INTERNET` in `app/build/intermediates/merged_manifests/debug/**/AndroidManifest.xml` (F1).
11. `./gradlew :app:createFeature -PfeatureName=probe` -> `./gradlew :app:testDebugUnitTest` -> `python3 tools/check_architecture.py`, then delete the probe (R7 gate).
12. `./gradlew connectedDebugAndroidTest` on API 34 emulator/device (R6 instrumented evidence).
13. Push branch, record the GitHub Actions run URL for all 3 jobs (closes KD-02 exit condition).
14. Update HANDOVER §3 statuses, flip PROPOSED -> ACCEPTED per §3 table, `codegraph sync`, `graphify update .`.
