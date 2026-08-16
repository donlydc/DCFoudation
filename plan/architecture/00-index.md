# Architecture index

## Mandatory reads for any implementation task

1. [`01-overview-and-invariants.md`](01-overview-and-invariants.md)
2. [`24-ai-reading-guide.md`](24-ai-reading-guide.md)

## Topic routing

| Topic | Primary documents |
|---|---|
| module boundaries / imports | `02-layers-and-dependencies.md`, `03-project-structure.md` |
| app startup / process lifecycle | `04-app-bootstrap-and-lifecycle.md` |
| ViewModel / state / events | `05-presentation-udf.md`, `11-error-and-result-model.md`, `09-concurrency-and-threading.md` |
| navigation / deep links | `06-navigation.md` |
| colors / typography / spacing / components | `07-design-system/README.md` + the specific subdocument |
| Hilt / scopes / bindings | `08-dependency-injection-hilt.md`, `02-layers-and-dependencies.md` |
| coroutine / Flow / threads | `09-concurrency-and-threading.md` |
| repository / API / DB / cache | `10-data/README.md` + specific subdocument |
| errors | `11-error-and-result-model.md` |
| auth / secure data / session | `12-security-and-session.md` |
| runtime permissions | `13-permissions.md` |
| WorkManager / long work | `14-background-work.md` |
| logs / analytics / crashes / performance | `15-observability/README.md` + specific subdocument |
| environments / feature flags | `16-config-feature-flags-environments.md` |
| tests | `17-testing.md` |
| Gradle / versions / build logic | `18-build-logic-and-dependencies.md` |
| CI / release | `19-ci-release-quality-gates.md` |
| new feature | `20-feature-template.md` + topic-specific docs |
| general coding rules | `21-coding-rules.md` |
| intentional exceptions | `22-known-deviations.md` |
| why an architecture choice exists | `23-decision-log.md` |
