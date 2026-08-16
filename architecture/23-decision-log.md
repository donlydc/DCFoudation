# Architecture decision log

[Back to architecture index](00-index.md)

## Scope

Durable decisions with date, context, decision, alternatives, consequences, affected rules/files. Keep concise; not a meeting log.

## Entries

### DEC-01 — 2026-08-16 — Correct `CLAUDE.md` namespace instead of renaming the codebase

**Context:** Phase B reconnaissance found root `CLAUDE.md` claimed primary namespace `com.dc.foundation`, but the real Android Studio-generated project uses `com.dc.android.foundation` throughout (`app/build.gradle.kts`, every `package` declaration).

**Decision:** Update `CLAUDE.md` to state the verified namespace rather than renaming the package across the codebase.

**Alternatives considered:** Rename `com.dc.android.foundation` → `com.dc.foundation` to match the original intent in `CLAUDE.md`.

**Consequences:** Zero code churn; `CLAUDE.md` now matches reality. If a future decision wants the shorter namespace, it is a deliberate rename with its own decision entry, not a silent fix.

**Affected:** `CLAUDE.md` §1, `architecture/03-project-structure.md` (STR-02), `architecture/22-known-deviations.md` (KD-01).

### DEC-02 — 2026-08-16 — Do not pre-create empty seed docs for unstarted phases

**Context:** `plan/architecture/*` ships placeholder files for all 24 target documents (04-21, `07-design-system/*`, `10-data/*`, `15-observability/*`). Phase B's explicit scope is only `01`, `02`, `03` (plus the always-read `00-index`/`24-ai-reading-guide`).

**Decision:** Only create a root `architecture/*` document when the phase that owns it actually runs. `00-index.md` carries a status table so an AI reader knows a missing file means "not built yet," not "forgotten."

**Alternatives considered:** Copy the full `plan/architecture/` tree into root now as placeholders.

**Consequences:** Keeps `architecture/` honest (INV-10) and avoids the plan's own anti-overengineering constraint (§7) applied to documentation itself — no doc describes an unimplemented capability as if it were designed.

**Affected:** `architecture/00-index.md`, all future phase docs.
