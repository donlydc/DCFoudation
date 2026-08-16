# Tokens

[Back to design system](README.md)

## Scope

Primitive design values only — no semantic meaning, no Material3 wiring (that's [`semantic-theme.md`](semantic-theme.md)).

## Current implementation

`ui/designsystem/src/main/java/com/dc/android/foundation/designsystem/tokens/`:

- `ColorTokens.kt` — 6 raw `Color` values (`Purple80/40`, `PurpleGrey80/40`, `Pink80/40`), carried over unchanged from the stock template as a neutral seed palette. Not brand-final.
- `SpacingTokens.kt` — `None`(0) / `XSmall`(4) / `Small`(8) / `Medium`(16) / `Large`(24) / `XLarge`(32) / `XXLarge`(48), all `Dp`.
- `ShapeTokens.kt` — `None`/`Small`(4dp)/`Medium`(8dp)/`Large`(16dp)/`Full`(50%) `RoundedCornerShape`.
- `MotionTokens.kt` — `DURATION_SHORT_MS`(150) / `DURATION_MEDIUM_MS`(300) / `DURATION_LONG_MS`(500) `Int` constants.
- `AccessibilityTokens.kt` — `MinTouchTarget` (48dp).

No elevation tokens yet — Material3's default elevation scale is used as-is (see [`spacing-shapes-elevation.md`](spacing-shapes-elevation.md)).

## Rules

Governed by [`README.md`](README.md) DS-01/DS-03. No new rule IDs here.

## Testing / validation

`grep` for raw hex colors (`0xFF`) or `.dp` literals outside `ui/designsystem/.../tokens/` and `theme/`.

## Related documents

- [`semantic-theme.md`](semantic-theme.md)
- [`spacing-shapes-elevation.md`](spacing-shapes-elevation.md)
