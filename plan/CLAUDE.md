# Claude Code project instructions

## Mandatory first read

For every task, read only:

1. `architecture/00-index.md`
2. `architecture/01-overview-and-invariants.md`
3. the minimum task-specific documents selected by `architecture/24-ai-reading-guide.md`

Do **not** preload the entire architecture directory. Context minimization is an architectural requirement.

## Architecture authority

Order of authority:

1. explicit user/task requirement
2. `architecture/01-overview-and-invariants.md`
3. task-specific architecture document
4. accepted decision records
5. existing implementation
6. conventions inferred from nearby code

If two architecture documents conflict, stop that design decision and use the critical architecture auditor. Do not guess.

## Coding principles

- Prefer composition over inheritance.
- UI never directly accesses Retrofit, Room, DataStore, or repositories from Composables.
- Feature code depends on contracts/core APIs, not implementation details of other features.
- Infrastructure exceptions do not leak into UI contracts.
- Hilt is the default DI mechanism; constructor injection is preferred.
- Coroutine dispatcher ownership and main-safety follow the threading document.
- Design-system consumers use semantic tokens/components rather than ad-hoc visual constants.
- Public/core abstractions must remain business-neutral unless an explicit architecture decision says otherwise.

## Documentation discipline

When changing architecture:

- update the smallest relevant architecture document;
- update `22-known-deviations.md` if the implementation intentionally violates a rule;
- add/update `23-decision-log.md` for a durable architecture decision;
- update `00-index.md` only when routing/scope changes.

Architecture docs describe verified reality and intended rules. Never document aspirational behavior as already implemented.

## Token discipline

- Search first; read narrow ranges/files second.
- Delegate repository mapping and mechanical consistency checks to Haiku agents.
- Use Sonnet for implementation and normal architecture writing.
- Use Opus only for defined escalation cases.
- Prefer concise context packets instead of forwarding raw logs or entire files between agents.
