# Layers & dependency rules

[Back to architecture index](00-index.md)

## Scope

Dependency direction rules between layers/modules, and how they are verified. Does not own package-level naming (see [`03-project-structure.md`](03-project-structure.md)).

## Current implementation

Verified against `settings.gradle.kts`: as of Phase F/G the repository has a 4-module split — `:app`, `:ui:designsystem`, `:runtime:concurrency`, and `:data:preferences` — with Hilt and DataStore wired through real bindings (`app/src/main/java/com/dc/android/foundation/di/*.kt`). There is not yet a `core`/`feature` split within `:app` itself, so module-graph verification (Gradle project dependencies) is meaningful for the 4 existing modules but the rules below still apply at the **package/file** level within `:app` until a feature module boundary exists.

## Target layer direction

```text
app/bootstrap
    ↓
features
    ↓
feature contracts / domain (when justified)
    ↓
repositories
    ↓
data sources / platform integrations

features ─────→ reusable core/platform modules
core/platform ✕ feature implementation
```

This mirrors the target capability map in the root [`CLAUDE.md`](../CLAUDE.md) (`foundation/`, `ui/designsystem`, `data/`, `runtime/`, `platform/`, `security/`, `observability/`, `testing/`, `build-logic/`). Per that document's own guidance and the plan's anti-overengineering constraint, a module is created only when it earns a real dependency/ownership/reuse/testability boundary — not preemptively to match this diagram.

## Rules

### DEP-01 — Core/base modules do not depend on feature implementation

**Rule:** Once core/base modules exist, they must not import feature-specific packages.

**Why:** Prevents cyclic coupling and keeps the base reusable across projects.

**Verification:** Inspect Gradle module dependency declarations once a module split exists; today, inspect that nothing under a would-be "core" package imports app-specific UI.

**Exceptions:** None currently — not yet applicable (single module).

### DEP-02 — A feature must not import another feature's private implementation

**Rule:** Cross-feature interaction uses navigation/public contracts or a deliberately shared contract, not direct imports of another feature's internals.

**Why:** Keeps features independently replaceable and testable.

**Verification:** Search feature packages for imports reaching into another feature's non-public types.

**Exceptions:** None — not yet applicable (no features exist yet beyond the stock template).

### DEP-03 — Presentation does not import infrastructure implementations

**Rule:** No Retrofit service, Room DAO/entity, raw DataStore access, or vendor SDK object in UI contracts.

**Why:** Keeps UI lifecycle-safe, previewable, and testable (see INV-02).

**Verification:** Search UI packages (`app/src/main/java/**/ui/**`, Composables) for infrastructure imports. Currently passes trivially: `MainActivity.kt`/`Greeting()` have zero such imports (Phase A finding).

**Exceptions:** None.

### DEP-04 — Infrastructure-specific types remain behind the owning module boundary

**Rule:** Public/base APIs expose application-neutral contracts, not vendor/infrastructure types.

**Why:** Prevents cross-layer coupling and vendor lock-in leaking through the base.

**Verification:** Inspect public signatures once base/core APIs exist.

**Exceptions:** None currently — not yet applicable.

### DEP-05 — Dependency exceptions are documented, bounded, and must not be silently expanded

**Rule:** Any accepted violation of DEP-01..04 must be recorded in `22-known-deviations.md` with scope and exit condition.

**Why:** Makes technical debt visible instead of normalized.

**Verification:** Cross-check code against `22-known-deviations.md` entries.

## Testing / validation

- `git grep`/`Grep` for cross-boundary imports (repository/DAO/Retrofit types in `ui/` packages, feature-to-feature internal imports) as each layer is introduced.
- Once multiple Gradle modules exist, `./gradlew :module:dependencies` (or the module dependency graph) is the source of truth for DEP-01/04.

## Related documents

- [`01-overview-and-invariants.md`](01-overview-and-invariants.md)
- [`03-project-structure.md`](03-project-structure.md)
- [`22-known-deviations.md`](22-known-deviations.md)
