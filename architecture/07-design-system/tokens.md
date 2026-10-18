# Tokens

[Back to design system](README.md)

## Scope

Primitive design values only — no semantic meaning, no Material3 wiring (that's [`semantic-theme.md`](semantic-theme.md)).

## Current implementation

`ui/designsystem/src/main/java/com/dc/android/foundation/designsystem/tokens/`:

- `ColorTokens.kt` — complete blue, neutral, red, green and amber ramps. The default brand primary is Blue600 (`#2563EB`); Grey50 (`#F7F8FA`) is the light app background.
- `DcSpacing.kt` — overrideable `DcSpacing` and `LocalDcSpacing`; `Default` is 0/4/8/12/16/24/32/48dp, where `component` is the 12dp step. `Compact` supplies a denser product option. `SpacingTokens.kt` remains a source-compatible primitive reference.
- `ShapeTokens.kt` — `None`/`ExtraSmall`(4dp)/`Small`(8dp)/`Medium`(12dp)/`Large`(16dp)/`ExtraLarge`(20dp)/`Full`(50%) `RoundedCornerShape`.
- `SurfaceTokens.kt` — `StrokeTokens.Hairline` (1dp) and a restrained `ElevationTokens` scale (0dp/1dp).
- `MotionTokens.kt` — `DURATION_SHORT_MS`(150) / `DURATION_MEDIUM_MS`(300) / `DURATION_LONG_MS`(500) `Int` constants.
- `AccessibilityTokens.kt` — `MinTouchTarget` (48dp).

Primitive values do not carry component meaning. Semantic roles are selected in `DcTheme` and reusable components.

## Rules

Governed by [`README.md`](README.md) DS-01/DS-03. No new rule IDs here.

## Testing / validation

`grep` for raw hex colors (`0xFF`) or `.dp` literals outside `ui/designsystem/.../tokens/` and `theme/`.

## Related documents

- [`semantic-theme.md`](semantic-theme.md)
- [`spacing-shapes-elevation.md`](spacing-shapes-elevation.md)
