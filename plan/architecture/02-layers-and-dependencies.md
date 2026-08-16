# Layers & dependency rules

> Seed. Replace the conceptual graph with the actual Gradle module graph after repository inspection.

## Conceptual direction

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

## Candidate review rules

**DEP-01 — Core/base modules do not depend on feature implementation.**

**DEP-02 — A feature must not import another feature's private implementation.** Cross-feature interaction uses navigation/public contracts or a deliberately shared contract.

**DEP-03 — Presentation does not import infrastructure implementations.** No Retrofit service, Room DAO/entity, raw DataStore access, or vendor SDK object in UI contracts.

**DEP-04 — Infrastructure-specific types remain behind the owning module boundary.** Public/base APIs expose application-neutral contracts.

**DEP-05 — Dependency exceptions are documented, bounded, and must not be silently expanded.**

## Verification

Claude must translate these into actual module/import checks after Phase A. Record current breaches in `22-known-deviations.md` instead of rewriting history.
