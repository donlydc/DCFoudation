# CI, release & quality gates

[Back to architecture index](00-index.md)

## Scope

The canonical commands that gate a change, and what CI actually runs.

## Current implementation

`.github/workflows/ci.yml` (lane A1, source-read; **no workflow run has happened, CI result NOT_RUN**) — on every push to `main`/`master` and every PR, three jobs with `permissions: contents: read` and per-ref concurrency cancel:

| Job | Policy | Steps |
|---|---|---|
| `architecture` | blocking | `python3 -m unittest discover -s tools -p 'test_*.py'` (checker self-tests); `python3 tools/check_architecture.py --boundaries-only` (KD-02 gate); full drift check with `--md/--json`; uploads `architecture-report` |
| `build` | blocking | `./gradlew testDebugUnitTest`; `./gradlew :foundation:test :runtime:concurrency:test`; `./gradlew -p build-logic test`; `lintDebug`; `assembleDebug`; uploads `test-results` and `lint-reports` |
| `instrumentation` | NON-BLOCKING (`continue-on-error: true`) | `connectedDebugAndroidTest` on an API 34 emulator via `reactivecircus/android-emulator-runner`; uploads `instrumentation-results` |

Failure policy (master plan R2): architecture, unit, lint and assemble are blocking; instrumentation stays non-blocking until a stable emulator runner exists, then `continue-on-error` is removed. A green workflow mark does not prove instrumentation passed unless the "Instrumentation tests" step itself succeeded. The Gradle launcher is JDK 21 while the daemon toolchain (JDK 25, `gradle/gradle-daemon-jvm.properties`) and the Kotlin JDK 11 toolchains are foojay-provisioned, so the `build` job needs egress to `api.foojay.io`, `dl.google.com` and the Gradle/Maven repositories; the effect of the JDK 21 launcher is UNKNOWN until a real run. `-p build-logic` is standalone (own `settings.gradle.kts`), so root `gradle.properties` and daemon-JVM criteria do not apply to it.

No release/versioning policy exists yet beyond `versionCode`/`versionName` in `app/build.gradle.kts` (both still at their template defaults, `1`/`"1.0"`) — this is a library-shaped foundation repo, not something released to a store, so a real release policy is deferred until a consuming app actually ships from it.

## Rules

### CI-01 — Canonical local validation commands

```bash
./gradlew testDebugUnitTest                      # Android-variant unit tests (Android modules + :app only)
./gradlew :foundation:test :runtime:concurrency:test   # dc.kotlin.jvm modules (task is `test`)
./gradlew -p build-logic test                   # included build's own tests
./gradlew lintDebug                             # Android Lint
./gradlew assembleDebug                         # full compile + package
python3 tools/check_architecture.py --boundaries-only   # module-edge gate (KD-02)
```

**Why:** The same commands the CI `build` and `architecture` jobs run — no separate "what CI actually checks" tribal knowledge.

**Verification:** `.github/workflows/ci.yml` steps match this list.

**Coverage limit (resolved in `ci.yml` by lane A1; run NOT_RUN):** `testDebugUnitTest` selects only projects that have that Android-variant task, so it does not select the `dc.kotlin.jvm` modules (`:foundation`, `:runtime:concurrency`; task `test`) nor the included build (`build-logic/src/test`). Before A1, CI therefore did not execute those tests (baseline drift D2); the `build` job now has explicit steps for them. The inference rests on Gradle task-selection semantics and has not been run-verified (see `modules/build-logic.md` section 9). Android modules added later are covered by `testDebugUnitTest`; a new `dc.kotlin.jvm` module must be added to the `:<module>:test` step by hand.

## Testing / validation

See `PLAN_REPORT.md`'s consolidated-verification entry for the historical local run of the original three Gradle commands. No Gradle command or workflow run has succeeded since (`reports/baseline-verification.md`).

## Related documents

- [`17-testing.md`](17-testing.md), [`18-build-logic-and-dependencies.md`](18-build-logic-and-dependencies.md), [`22-known-deviations.md`](22-known-deviations.md)
