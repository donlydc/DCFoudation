# Spacing, shapes, elevation

[Back to design system](README.md)

## Scope

Layout spacing scale, corner-radius scale, and elevation policy.

## Current implementation

- **Spacing:** `DcSpacing` provides 4/8/12/16/24/32/48dp steps through `LocalDcSpacing`; components and feature UI consume that overridable semantic scale.
- **Shapes:** `DcShapes` maps 4/8/12/16/20dp onto Material `extraSmall` through `extraLarge`. Chips use small (8dp), fields/drawer items medium (12dp), cards and banners large (16dp), and dialogs/sheets extra-large (20dp).
- **Elevation/stroke:** `StrokeTokens.Hairline` is 1dp; `ElevationTokens.Level0/Level1` are 0dp/1dp. Surfaces prefer an outline before adding elevation. `DcCard` is outlined/0dp by default and exposes an elevated/1dp variant for busy backgrounds.

## Rules

No new rule IDs — governed by [`README.md`](README.md) DS-03.

## Testing / validation

`grep '\.dp'` and `RoundedCornerShape(` outside `ui/designsystem/` and showcase-only reference UI; each remaining hit requires a documented geometric exception.

## Related documents

- [`tokens.md`](tokens.md)
