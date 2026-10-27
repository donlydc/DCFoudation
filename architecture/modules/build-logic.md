# MODULE: includeBuild:build-logic — build-logic
Status: DRAFT
Verified commit: UNKNOWN (drafted against working tree at HEAD 2c0ae05b; not audited)
Last source audit: NOT_RUN
Test status: NOT_RUN (evidence: reports/baseline-verification.md)

> Doc status (`DRAFT|VERIFIED|NEEDS_REFRESH`) and test status (`NOT_RUN|TESTED|FAILED|BLOCKED`) are separate vocabularies; see `TEMPLATE.md`.

## 1. Identity and responsibility
- Owns: the 8 `dc.*` Gradle convention plugins, shared SDK/Java constants, and the feature scaffold generator (`createFeature`). It is an included build (`settings.gradle.kts:2` `includeBuild("build-logic")`, root project `dc-build-logic`), not a project of the main build.
- Does not own: dependency versions (the catalog `gradle/libs.versions.toml`, shared via `build-logic/settings.gradle.kts` `from(files("../gradle/libs.versions.toml"))`), module dependency-edge enforcement (static checker only, outside this module: `tools/check_architecture.py`; no Gradle-graph task, KD-02), `gradle.properties` flags (KD-03), CI (`.github/workflows/ci.yml`).
- Public package/API: default package (no package declaration); plugin ids below.
- Consumer(s): every module `build.gradle.kts` (via `id("dc.*")`); `:app` additionally applies `dc.feature.generator`.

## 2. Context and discovery
- Read first: `architecture/00-index.md`, `18-build-logic-and-dependencies.md` (BLD-01..03), `19-ci-release-quality-gates.md`, `dependency-rules.md`.
- Main code: `build-logic/src/main/kotlin/*.kt` (11 files). Build: `build-logic/build.gradle.kts` (`kotlin-dsl`, `gradlePlugin { register(...) }`).
- Tests: `build-logic/src/test/kotlin/FeatureScaffoldGeneratorTest.kt` (6 `@Test` after lane C3; JUnit via `testImplementation(libs.junit)`).
- CodeGraph entry symbols/callers/callees: `DcAndroidLibraryConventionPlugin`, `DcAndroidApplicationConventionPlugin`, `DcKotlinJvmConventionPlugin`, `DcFeatureGeneratorPlugin` -> `CreateFeatureTask` -> `FeatureScaffoldGenerator.generate` (tested by `FeatureScaffoldGeneratorTest`).

## 3. Dependencies and boundaries
- Allowed: AGP, Kotlin, Compose-compiler, KSP, Hilt and Room Gradle plugins as `implementation` of the build-logic build; catalog libraries via `Project.libs`.
- Forbidden: project `project(":...")` coupling to app code (it is a separate build, `depends_on: []`); hard-coded dependency versions (BLD-01); adding `:testing` to production configurations (BLD-03 - `dc.testing` adds only external test libs, not `:testing`).
- Exceptions (DEC/KD): KD-03 (`android.disallowKotlinSourceSets=false` for KSP on AGP 9; `gradle.properties:24`); KD-02 (module boundaries enforced only by the static checker, no Gradle-graph task; Open, partially mitigated).

## 4. Data/control flow
```mermaid
flowchart LR
  M[module build.gradle.kts id dc.*] --> P[Convention plugin apply]
  P --> H[ConventionHelpers: configureAndroid / addLibrary / addPlatform]
  H --> C[(libs.versions.toml)]
  G[./gradlew :app:createFeature -PfeatureName=X] --> T[CreateFeatureTask] --> S[FeatureScaffoldGenerator.generate] --> F[feature files in src/main/java + src/test/java]
```

## 5. Contracts
| Plugin id | Class | Applies / configures (source) |
|---|---|---|
| `dc.android.application` | `DcAndroidApplicationConventionPlugin` | `com.android.application`; `configureAndroid`; `targetSdk=37`; AndroidJUnitRunner; `buildConfig=true`; `coreLibraryDesugaring` |
| `dc.android.library` | `DcAndroidLibraryConventionPlugin` | `com.android.library`; `configureAndroid`; AndroidJUnitRunner; desugaring |
| `dc.kotlin.jvm` | `DcKotlinJvmConventionPlugin` | `org.jetbrains.kotlin.jvm`; `jvmToolchain(11)` |
| `dc.compose` | `DcComposeConventionPlugin` | `kotlin.plugin.compose`; on app/library: `buildFeatures.compose`, Compose BOM, ui, graphics, tooling-preview, material3, `debugImplementation` tooling |
| `dc.hilt` | `DcHiltConventionPlugin` | `com.google.dagger.hilt.android`, `com.google.devtools.ksp`; `hilt-android`, `ksp(hilt-compiler)` |
| `dc.room` | `DcRoomConventionPlugin` | `com.google.devtools.ksp`, `androidx.room`; `schemaDirectory("$projectDir/schemas")`; room runtime/ktx/compiler |
| `dc.testing` | `DcTestingConventionPlugin` | `testImplementation` junit, coroutines-test, turbine; `androidTestImplementation` androidx-junit, espresso (Android only); Compose UI test deps when `dc.compose` is present |
| `dc.feature.generator` | `DcFeatureGeneratorPlugin` | registers task `createFeature` (group "dc scaffolding") |
- `configureAndroid` (`ConventionHelpers.kt`): `compileSdk=37`, `minSdk=24`, Java 11 source/target, core-library desugaring on, `lint.abortOnError=true`, `lint.checkReleaseBuilds=true`. Constants: `DC_COMPILE_SDK`, `DC_MIN_SDK`, `DC_TARGET_SDK`, `DC_JAVA_VERSION`.
- Generator inputs: `-PfeatureName` (required, one identifier), `-PfeaturePackage` (else inferred from the module namespace), booleans `navigation|network|room|permission|backgroundWork` (default false, other values throw `GradleException`). Output: UiState/UiAction/ViewModel/Route/Screen/Preview + `ViewModelTest` (+ optional UiEffect, `<Name>Destination`, `remote/ApiService`, `local/Dao`, `background/WorkScheduler`). All-or-nothing: aborts if any planned file exists; `CREATE_NEW`, never overwrites. `outputs.upToDateWhen { false }`. Since lane C3 (`b7487f0`) `generate` takes an optional `architectureRoot: Path? = null`; when set (the plugin passes `rootProject/architecture/features` through an additive `@Internal architectureRoot: DirectoryProperty` on `CreateFeatureTask`) it also plans `<root>/<featuresegment>.md`, a skeleton of `architecture/features/TEMPLATE.md` (same numbered headings 1-9, Status DRAFT, Last source audit NOT_RUN, Test status NOT_RUN, known facts filled, the rest TODO). The page takes part in the all-or-nothing existing-file check; with a null root no page is written. The generator stays pure `java.nio` Kotlin. The generated page is NOT registered in `context-map.yaml`/`module-map.md` (manual step; the checker's coverage check may warn until the author registers the feature). CLI contract unchanged.
- Errors: `IllegalArgumentException`/`IllegalStateException` mapped to `GradleException`. Threading/cancellation: configuration-time and a single task action.

## 6. DI, configuration and ownership
- No Hilt (build tooling). `dc.hilt` wires Hilt for `:app` only among current modules. SDK levels, Java level and lint defaults are owned here (BLD-02), not per module. Generated ViewModels use `@HiltViewModel` with an empty `@Inject constructor()`.

## 7. Edge cases, security and performance
- Validation: feature name regex, package segments, Kotlin keywords (`FeatureScaffoldGenerator`); invalid input fails before creating directories (test). No secrets handled. Generator writes only under the module's `src/main/java` and `src/test/java`.
- `dc.testing` + `dc.compose`: Compose UI test deps are added whenever `dc.compose` is applied to the same module, in either order (`pluginManager.withPlugin("dc.compose")` in `DcTestingConventionPlugin.kt:21` fires immediately or on later application).

## 8. Tests and evidence
- Unit: `FeatureScaffoldGeneratorTest` - minimal feature files; capability boundaries; invalid name; existing-file abort; lane C3 added architecture-page content with heading parity against `features/TEMPLATE.md` (when reachable), no-root => no page, existing page aborts with nothing written. Written, NOT_RUN; Kotlin edits uncompiled (`./gradlew -p build-logic test` first). Gate R7 (empty name -> generate -> compile/test -> discoverability) NOT_RUN. Integration/Instrumented: none. No test covers the Gradle plugins themselves.
- Commands (once Gradle runs): `./gradlew -p build-logic test`; boundary gate `python3 tools/check_architecture.py --boundaries-only`; checker self-tests `python3 -m unittest discover -s tools -p 'test_*.py'`; smoke `./gradlew help` (verifies the included build); generator smoke `./gradlew :app:createFeature -PfeatureName=Sample` (then revert).
- Test status: NOT_RUN (evidence: reports/baseline-verification.md)
- Last verified result: none; no Gradle task has been run by R0/R1 agents.

## 9. Known debt / decisions
- CI finding RESOLVED in `ci.yml` by lane A1 (source-read; workflow run NOT_RUN): the `build` job now also runs `./gradlew :foundation:test :runtime:concurrency:test` and `./gradlew -p build-logic test`. Cause (Gradle task-selection semantics, not run-verified): `testDebugUnitTest` is an Android-variant task, so `dc.kotlin.jvm` modules (task `test`) and the included build were not selected. `build-logic` has its own `settings.gradle.kts`, so it is standalone-buildable; root `gradle.properties` and daemon-JVM criteria do not apply to that invocation. Effect of the JDK 21 launcher vs daemon toolchain 25 (`gradle/gradle-daemon-jvm.properties`) stays UNKNOWN until a real CI run.
- KD-03: `dc.hilt` and `dc.room` apply KSP; the flag stays until KSP supports AGP 9 `android.sourceSets` (KSP `2.2.10-2.0.2`, AGP 9.3.1 per baseline A1).
- Backlog (MP 4.14; PROPOSED unless marked PARTIAL): MP:BLD-01 automated dependency-boundary checks, fail CI on forbidden edge (P0, = R2.1/KD-02; PARTIAL: static checker `tools/check_architecture.py --boundaries-only` implemented and wired as the blocking CI `architecture` job, source verified and CI run NOT_RUN; the Gradle-graph task is NOT implemented, see `22-known-deviations.md` KD-02); MP:BLD-02 verify generator output compiles/tests, add doc stub (P1; doc stub done-in-code by lane C3, compile verification NOT_RUN); MP:BLD-03 check KD-03 workaround against resolved versions (P0); MP:BLD-04 repeatable test/lint/assemble + report export for all modules (P0; PARTIAL: test, lint, assemble steps and report uploads added to `ci.yml`, CI run NOT_RUN); MP:BLD-05 release publishing/API compatibility only when shipping artifacts (P1); MP:BLD-06 portable or optional Graphify hooks, keep CodeGraph (P1). Bare `BLD-01..03` are the rules in `18-build-logic-and-dependencies.md`; backlog items are written `MP:<ID>` (convention in `context-protocol.md`).

## 10. Safe extension guide
- New capability = new small `Dc*ConventionPlugin` registered in `build-logic/build.gradle.kts`, composable with others (BLD-02); read versions from the catalog (BLD-01). Add a generator option by extending `FeatureGeneratorOptions`, `CreateFeatureTask`, `DcFeatureGeneratorPlugin` and a test case together. Sample consumer: `:app` (`createFeature`), `:data:database` (`dc.room`).

## 11. AI task playbook
- Read -> Graph -> Propose -> Edit -> Test -> Update docs -> Handover.
- Read: `18-build-logic-and-dependencies.md`, the plugin touched, one consumer `build.gradle.kts`. Graph: `codegraph_explore "FeatureScaffoldGenerator CreateFeatureTask DcAndroidLibraryConventionPlugin"`. Changing a convention affects every module: run `./gradlew help` and `-p build-logic test`, then broader tasks.
- DoD: plugin table in section 5 updated; `dependency-rules.md` if edges change.

## 12. Change log and links
- 2026-10-27 DOC-B (lane C3 `b7487f0`): generator architecture page, D-05 proposal (DEC-C3-1), README Quickstart/CHANGELOG Unreleased. Report: `reports/wp/DOC-B.md`.
- 2026-10-27 — R1.1c — first draft from plugin sources and CodeGraph. Report: `reports/wp/R1.1c.md`.
- Publishing/versioning/API-compatibility policy (D-05): PROPOSED, see DEC-C3-1 and `reports/wp/C3-design.md` (composite build now, Maven + BOM when an external consumer appears; no publishing config exists). Toolchain facts: daemon JVM 25, `DC_JAVA_VERSION=11`, compile/target SDK 37, min SDK 24.
- Links: `context-map.yaml` entry `build-logic`; `18-build-logic-and-dependencies.md`; `19-ci-release-quality-gates.md`; `22-known-deviations.md` (KD-02, KD-03).
- Rule glosses (text lives in the named doc; gloss only, the rule doc is authoritative): `BLD-01` (18-build-logic-and-dependencies.md): no dependency version outside the version catalog; `BLD-02` (18-build-logic-and-dependencies.md): convention plugins are composable capabilities; `BLD-03` (18-build-logic-and-dependencies.md): test utilities never enter production runtime graphs.
