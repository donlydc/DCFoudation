# Dependency rules (module edges)

[Back to architecture index](00-index.md) · Rules source: [`02-layers-and-dependencies.md`](02-layers-and-dependencies.md) (DEP-01..05), [`01-overview-and-invariants.md`](01-overview-and-invariants.md) (INV-01, INV-02, INV-07), [`18-build-logic-and-dependencies.md`](18-build-logic-and-dependencies.md) (BLD-03).

**Enforcement status: static check only, not run in CI yet.** `python3 tools/check_architecture.py --boundaries-only` is the enforcing command for the allowed-edge block below (exit 1 on a forbidden, unlisted or unparseable edge, `:app`, `:testing` in production, or a cycle); an unparseable dependency form is an ERROR, so only `xxx(project(":a:b"))`, `path =` and `add()` forms are supported. It is wired as the blocking CI `architecture` job (run NOT_RUN). KD-02 stays Open, partially mitigated ([`22-known-deviations.md`](22-known-deviations.md)); a Gradle-graph check is not implemented. `./gradlew :app:dependencies` has NOT_RUN (no Gradle evidence: `reports/baseline-verification.md`, Part B). The edge list below was read from `*/build.gradle.kts` (status: file-verified, not build-verified).

## Rules applied to edges

| Rule | Edge consequence |
|---|---|
| DEP-01 / INV-01 | No reusable module (everything except `:app`) depends on `:app` or on a feature package. Graph must be acyclic. |
| DEP-02 | Feature packages in `:app` (`greeting`, `articles`, `showcase`, `config`) do not import each other's internals. |
| DEP-03 / INV-02 | UI (`:ui:designsystem`, Composables) has no edge to data/runtime/security infrastructure modules. |
| DEP-04 / INV-07 | Infrastructure types (Retrofit, Room, DataStore, vendor SDK) stay inside the owning data/security module's public contracts. |
| DEP-05 | Any exception is recorded in `22-known-deviations.md` with scope and exit condition, then added to the exceptions list below. |
| BLD-03 | `:testing` appears only as `testImplementation` / `androidTestImplementation`; never in `implementation`/`api` of production code. |

## Current actual edges (13 modules, source: module `build.gradle.kts`)

| From | To (config) |
|---|---|
| `:app` | `:foundation`, `:ui:designsystem`, `:runtime:concurrency`, `:data:preferences`, `:data:network`, `:data:database`, `:observability`, `:platform:navigation`, `:platform:permission`, `:runtime:scheduling`, `:security` (all `implementation`); `:testing` (`testImplementation`) |
| `:runtime:scheduling` | `:foundation` (`api`, since lane A3 `bb5ce19`; `AppError` is in public types), `:runtime:concurrency` (`implementation`) |
| `:data:network` | `:foundation` (`implementation`); the former unused `:observability` edge was removed in lane B1 (`7a8a804`) |
| `:data:database` | `:runtime:concurrency` (`implementation`) |
| `:security` | `:runtime:concurrency` (`implementation`) |
| `:testing` | `:runtime:concurrency`, `:data:preferences`, `:observability`, `:runtime:scheduling` (`api`) |
| `:foundation`, `:runtime:concurrency`, `:ui:designsystem`, `:data:preferences`, `:observability`, `:platform:navigation`, `:platform:permission` | none |

`build-logic` is an included build (`settings.gradle.kts` `includeBuild`), not a project dependency of any module.

## Machine-checkable allowed-edge table (CURRENT rule = actual edges only)

Format: `from -> allowed targets` (production configurations). Any edge not listed is FORBIDDEN. This block equals the `project(...)` declarations in the build files today; nothing here is proposed.

```text
:app                  -> :foundation, :ui:designsystem, :runtime:concurrency, :runtime:scheduling, :data:preferences, :data:network, :data:database, :observability, :platform:navigation, :platform:permission, :security
:foundation           -> (none)
:runtime:concurrency  -> (none)
:runtime:scheduling   -> :foundation, :runtime:concurrency
:ui:designsystem      -> (none)
:data:preferences     -> (none)
:data:network         -> :foundation
:data:database        -> :runtime:concurrency
:platform:navigation  -> (none)
:platform:permission  -> (none)
:security             -> :runtime:concurrency
:observability        -> (none)
:testing              -> :runtime:concurrency, :runtime:scheduling, :data:preferences, :observability
```

```text
# test-only configurations (testImplementation / androidTestImplementation)
:app -> :testing
# every other module -> :testing is currently absent; add here only with BLD-03 review
```

### PROPOSED (not current rule; do not enforce until confirmed in R2.1)

`:foundation` as the shared Android-free contract module could be allowed for libraries that do not depend on it today. No existing rule ID requires or grants this; it is a candidate policy.

```text
# PROPOSED additions (each line is additive to the CURRENT block above)
:data:preferences     -> :foundation
:data:database        -> :foundation
:security             -> :foundation
:observability        -> :foundation
:testing              -> :foundation
```

## Forbidden edges (check list)

### Grounded in an existing rule

| Forbidden | Rule | Why |
|---|---|---|
| any module -> `:app` | DEP-01 | `:app` is the composition root and holds the feature implementations. |
| any production config -> `:testing` | BLD-03 | Test fakes and JUnit must not ship. |
| `:ui:designsystem` -> `:data:*`, `:security` | DEP-03 / INV-02 | UI must not reach infrastructure implementations. |
| any cycle | INV-01 | Dependency direction follows the documented graph (acyclic). |
| feature package A -> feature package B internals (inside `:app`) | DEP-02 | Package/import-level check until a feature module exists. |

### PROPOSED policy (no existing rule ID; not yet enforceable)

| Candidate | Basis | Note |
|---|---|---|
| `:foundation`, `:runtime:concurrency` -> any Android module | Build fact: both apply `dc.kotlin.jvm` (`foundation/build.gradle.kts:2`, `runtime/concurrency/build.gradle.kts:2`) | Keeps them JVM-only; no INV/DEP ID states this. |
| `:ui:designsystem` -> `:runtime:*`, `:platform:*` | none (extends DEP-03 intent) | Design system stays dependency-free today. |
| `:platform:*` -> `:data:*`, `:security`, `:ui:designsystem` | none (NAV-01/PERM-01 concern feature code, not module edges) | Platform contracts stay engine-neutral today. |
| `:data:*` -> `:ui:*`, `:platform:*`, `:security` | none (DEP-04 covers types behind boundaries, not edges) | |
| `:data:database` <-> `:data:network` | none | Repositories in `:app` compose them today. |
| `:observability` -> `:security`, `:data:*`, `:ui:*` | none | Leaf in the current graph (no project deps). |

## Exceptions

None recorded (DEP-05). Source-level DEP-02/DEP-03 import checks are manual today.

## Maintenance

Change a `project(...)` dependency => update the tables here, `depends_on` in [`context-map.yaml`](context-map.yaml), and [`module-map.md`](module-map.md) in the same change (MP 10.4). Prefer proving edges with `./gradlew :<module>:dependencies` once Gradle works (status: NOT_RUN).
