# Start prompt for Claude Code

Execute `CLAUDE_IMPLEMENTATION_PLAN.md` from start to finish.

Non-negotiable execution rules:

1. Read `CLAUDE.md` first.
2. Use `architecture/00-index.md` and `architecture/24-ai-reading-guide.md` to load only task-relevant architecture docs.
3. Do not read the entire architecture folder for normal implementation tasks.
4. Respect the explicit model assignment in `.claude/agents/*.md`; do not use `inherit`.
5. Default implementation work uses Sonnet 5. Cheap discovery/index/check work uses Haiku 4.5. Opus 5 is escalation-only under the conditions defined in the plan.
6. Never copy BLE-specific architecture from `references/style-only/`; use only its documentation style.
7. Document architectural facts only after verifying them against source/build files.
8. After each phase, run the phase gate before proceeding.
9. Do not silently introduce a new abstraction, framework, module, or dependency. Record architecture decisions and deviations.
10. Complete the final sanitized ZIP packaging step only after build/test/documentation gates pass.

Do not ask for confirmation between phases unless execution is blocked by missing credentials, missing repository content, or an irreversible external action. Prefer best-effort progress and record blockers in the final report.
