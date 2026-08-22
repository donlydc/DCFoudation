# Architecture index

## Mandatory reads for any implementation task

1. [`01-overview-and-invariants.md`](01-overview-and-invariants.md)
2. [`24-ai-reading-guide.md`](24-ai-reading-guide.md)

## Status

This tree began as a phase-by-phase implementation plan and is now a living description of the verified source. Check [`../PLAN_REPORT.md`](../PLAN_REPORT.md) and [`../BASE_PROJECT_ROADMAP.md`](../BASE_PROJECT_ROADMAP.md) for historical progress and the current P0–P2 backlog. A document must distinguish implemented behavior from optional/target behavior.

| Document | Status |
|---|---|
| `01-overview-and-invariants.md` | done (Phase B) |
| `02-layers-and-dependencies.md` | done (Phase B) |
| `03-project-structure.md` | done (Phase B) |
| `04-app-bootstrap-and-lifecycle.md` | done (Phase E) |
| `05-presentation-udf.md` | done (Phase E) |
| `06-navigation.md` | implemented (type-safe top-level Navigation Compose) |
| `07-design-system/*` (9 files) | implemented tokens/theme plus P0 state, input and list components |
| `08-dependency-injection-hilt.md` | done (Phase D) |
| `09-concurrency-and-threading.md` | done (Phase F) |
| `10-data/*` (7 files) | implemented reference paths for DataStore, Retrofit, Room and offline-first cache |
| `11-error-and-result-model.md` | done (Phase H) |
| `12-security-and-session.md` | secure storage implemented; auth/session flow remains optional |
| `13-permissions.md` | notification permission path implemented |
| `14-background-work.md` | periodic WorkManager sync reference implemented |
| `15-observability/*` (4 files) | logging/analytics/crash contracts implemented; vendor/performance tracing optional |
| `16-config-feature-flags-environments.md` | done (Phase H) |
| `17-testing.md` | implemented shared `:testing` kit plus feature/module tests |
| `18-build-logic-and-dependencies.md` | implemented conventions and safe feature generator |
| `19-ci-release-quality-gates.md` | done (Phase I) |
| `20-feature-template.md` | implemented template plus `createFeature` generator |
| `21-coding-rules.md` | done (Phase I) |
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
