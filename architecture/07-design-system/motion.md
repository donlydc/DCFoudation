# Motion

[Back to design system](README.md)

## Scope

Animation duration/easing policy.

## Current implementation

`tokens/MotionTokens.kt` defines three duration constants (`DURATION_SHORT_MS` 150, `DURATION_MEDIUM_MS` 300, `DURATION_LONG_MS` 500). `DcExpandableSection` is the first user: chevron rotation and expand/shrink use `DURATION_MEDIUM_MS` (fade-out uses `DURATION_SHORT_MS`) with Compose's default easing. Compose animations respect the system "Remove animations" setting automatically; `DcSkeleton(animated = false)` is the explicit opt-out. No custom easing curve is defined yet, per INV-10.

## Rules

None yet — nothing to enforce until an animation exists.

## Testing / validation

N/A yet.

## Related documents

- [`tokens.md`](tokens.md)
