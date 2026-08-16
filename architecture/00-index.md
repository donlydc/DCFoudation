# Architecture index

## Mandatory reads for any implementation task

1. [`01-overview-and-invariants.md`](01-overview-and-invariants.md)
2. [`24-ai-reading-guide.md`](24-ai-reading-guide.md)

## Status

This tree is populated phase-by-phase per [`../plan/CLAUDE_IMPLEMENTATION_PLAN.md`](../plan/CLAUDE_IMPLEMENTATION_PLAN.md). As of Phase E, only the documents below exist. A topic-routing row pointing at a not-yet-created file means that phase hasn't run yet — check [`../PLAN_REPORT.md`](../PLAN_REPORT.md) for current progress, do not invent the content.

| Document | Status |
|---|---|
| `01-overview-and-invariants.md` | done (Phase B) |
| `02-layers-and-dependencies.md` | done (Phase B) |
| `03-project-structure.md` | done (Phase B) |
| `04-app-bootstrap-and-lifecycle.md` | done (Phase E) |
| `05-presentation-udf.md` | done (Phase E) |
| `06-navigation.md` | done (Phase E, marked not-implemented) |
| `07-design-system/*` (9 files) | done (Phase C) |
| `08-dependency-injection-hilt.md` | done (Phase D) |
| `20-feature-template.md` | done (Phase E) |
| `22-known-deviations.md` | living document, updated every phase |
| `23-decision-log.md` | living document, updated every phase |
| `24-ai-reading-guide.md` | done (Phase B) |
| everything else below | not yet created — pending its phase |

## Topic routing

| Topic | Primary documents |
|---|---|
| module boundaries / imports | `02-layers-and-dependencies.md`, `03-project-structure.md` |
| app startup / process lifecycle | `04-app-bootstrap-and-lifecycle.md` (Phase E) |
| ViewModel / state / events | `05-presentation-udf.md`, `11-error-and-result-model.md`, `09-concurrency-and-threading.md` (Phase E/F/H) |
| navigation / deep links | `06-navigation.md` (Phase E) |
| colors / typography / spacing / components | `07-design-system/README.md` + the specific subdocument (Phase C) |
| Hilt / scopes / bindings | `08-dependency-injection-hilt.md` (Phase D), `02-layers-and-dependencies.md` |
| coroutine / Flow / threads | `09-concurrency-and-threading.md` (Phase F) |
| repository / API / DB / cache | `10-data/README.md` + specific subdocument (Phase G) |
| errors | `11-error-and-result-model.md` (Phase H) |
| auth / secure data / session | `12-security-and-session.md` (Phase H) |
| runtime permissions | `13-permissions.md` (Phase H) |
| WorkManager / long work | `14-background-work.md` (Phase F) |
| logs / analytics / crashes / performance | `15-observability/README.md` + specific subdocument (Phase H) |
| environments / feature flags | `16-config-feature-flags-environments.md` (Phase H) |
| tests | `17-testing.md` (Phase I) |
| Gradle / versions / build logic | `18-build-logic-and-dependencies.md` (Phase I) |
| CI / release | `19-ci-release-quality-gates.md` (Phase I) |
| new feature | `20-feature-template.md` + topic-specific docs (Phase E) |
| general coding rules | `21-coding-rules.md` (Phase I) |
| intentional exceptions | `22-known-deviations.md` |
| why an architecture choice exists | `23-decision-log.md` |
