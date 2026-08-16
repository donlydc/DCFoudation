# Navigation

[Back to architecture index](00-index.md)

## Scope

How the app moves between destinations, and how far the navigation engine is abstracted behind a contract.

## Current implementation

**Not implemented.** The app has exactly one destination (`GreetingRoute`), hosted directly by `MainActivity` (`DcTheme { GreetingRoute() }`) with no `NavHost`, no `androidx.navigation` dependency, and no destination/navigation contract. Per the anti-overengineering constraint (`plan/CLAUDE_IMPLEMENTATION_PLAN.md` §7 — "do not add a capability when the project has no current or near-term use case"), a navigation library is not added until a second destination actually exists to navigate to or from.

## Target direction (not yet built)

When a second destination is added, the root `CLAUDE.md` target map's `platform/navigation` capability applies: a thin destination/navigation contract that hides the underlying navigation engine (`androidx.navigation` Compose or otherwise) from feature code, so features depend on a DC-owned contract rather than the engine directly. That contract will be designed against a real second screen, not speculatively now.

## Rules

**NAV-01 — Reserved.** No rule yet — nothing to enforce with a single hard-coded destination. Will be written against the first real multi-destination case instead of guessed in advance.

## Testing / validation

N/A yet.

## Related documents

- [`05-presentation-udf.md`](05-presentation-udf.md)
