# `architecture/`

**Role:** authoritative, living architecture documents (outranks the root `CLAUDE.md`/`AGENTS.md` summaries).

**Owns:** numbered docs `00`–`24` plus the `07-design-system/`, `10-data/` and `15-observability/` sub-trees; `22-known-deviations.md` and `23-decision-log.md` are updated every phase.

## AI guidance

- Start at `00-index.md` (topic routing), then `01-overview-and-invariants.md` and `24-ai-reading-guide.md`; read only task-specific documents.
- Document implemented behavior separately from optional/target behavior.
- Update only when a durable rule, decision or boundary changes; record intentional exceptions in `22`, rationale in `23`.
- Keep `03-project-structure.md` in sync with `settings.gradle.kts` (STR-01).
