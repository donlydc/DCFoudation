# Changelog

Format: [Keep a Changelog](https://keepachangelog.com/); versioning policy PROPOSED in `reports/wp/C3-design.md` (SemVer, pre-1.0). No release has been cut.

## [Unreleased]

> Build/test verification for everything below is **NOT_RUN** (Gradle could not run in the authoring environment). Evidence per item: `reports/wp/<WP-ID>.md`.

### Added
- Architecture-as-Context (R0-R1): baseline verification report, handover rewrite, module/feature page templates, `context-map.yaml`, `context-protocol.md`, `dependency-rules.md`, 13 module pages + `build-logic`, feature pages (greeting, articles, showcase), MP backlog-ID convention (DEC-09), KD-06.
- `tools/check_architecture.py` (+ self-tests): map/doc/template/edge/cycle drift checks and `--boundaries-only` KD-02 gate; CI `architecture` and `build` jobs (R1.3, R2.1).
- Tests for `:platform:navigation`, `:platform:permission`, `:observability` (R2.2) and contract tests for foundation/concurrency/scheduling (R3).
- Design-system tests and catalog examples (R4).
- Feature generator: `createFeature` now also writes `architecture/features/<name>.md` (DRAFT / NOT_RUN skeleton from `features/TEMPLATE.md`); optional `architectureRoot` input (R7.1, MP:BLD-02).
- README Quickstart (JDK/SDK, convention plugins, generator, checks) and D-05 publishing/API-policy proposal (R7).

### Changed
- Core/runtime hardening with defect fixes: `SingleFlight`, `FlowSharing.throttleFirst`, `DcScheduler` id/spec mismatch rejection, extracted pure scheduler mappers (R3).
- Design-system accessibility and token fixes (R4).

### Deprecated
- `DcSnackbarEffect(String?, SnackbarHostState, ...)` in `:ui:designsystem`: use the `DcSnackbarMessage`-based overload (`ReplaceWith` provided; DEC-08). `:app` migration pending.

### Migration
- None required. Generator CLI contract unchanged (additive output only); a pre-existing `architecture/features/<name>.md` makes `createFeature` abort without writing anything.
