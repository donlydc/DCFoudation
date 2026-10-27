# `plan/`

**Role:** historical execution-plan seed package; not an authority for current behavior.

**Owns:** `CLAUDE_IMPLEMENTATION_PLAN.md`, `MODULE_IMPLEMENTATION_PLAN.md`, `DESIGN_SYSTEM_UI_PLAN.md`, `SCHEDULING_MODULE_PLAN.md`, `TARGET_ARCHITECTURE_TREE.md`, a seed copy of the docs in `plan/architecture/`, `ui-style-guide/` and `references/`.

## AI guidance

- Use `../architecture/` and current source instead of anything here; `plan/architecture/` is a stale seed, do not edit it expecting effect.
- Consult plans only for the original rationale or phase ordering of a feature.
- Do not move rules from here into code without checking them against `../architecture/`.
