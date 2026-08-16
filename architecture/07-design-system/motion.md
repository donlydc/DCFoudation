# Motion

[Back to design system](README.md)

## Scope

Animation duration/easing policy.

## Current implementation

`tokens/MotionTokens.kt` defines three duration constants (`DURATION_SHORT_MS` 150, `DURATION_MEDIUM_MS` 300, `DURATION_LONG_MS` 500). **Not yet used anywhere** — no animation exists in the app yet, so there is no easing-curve policy to document. This is intentionally left as "not implemented" per INV-10 rather than inventing an easing spec no component uses.

## Rules

None yet — nothing to enforce until an animation exists.

## Testing / validation

N/A yet.

## Related documents

- [`tokens.md`](tokens.md)
