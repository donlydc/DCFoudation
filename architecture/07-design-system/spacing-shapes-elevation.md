# Spacing, shapes, elevation

[Back to design system](README.md)

## Scope

Layout spacing scale, corner-radius scale, and elevation policy.

## Current implementation

- **Spacing:** `tokens/SpacingTokens.kt` (see [`tokens.md`](tokens.md)). Not yet consumed anywhere — `MainActivity.kt`'s only padding comes from `Scaffold`'s own `innerPadding`, not a token or literal, so there is nothing to migrate yet.
- **Shapes:** `tokens/ShapeTokens.kt` → `theme/DcShapes.kt` maps `Small`/`Medium`/`Large` onto `MaterialTheme.shapes`. `DcButton` inherits Material3's default button shape (`MaterialTheme.shapes.*` via the underlying `Button`), not yet explicitly overridden.
- **Elevation:** Not implemented. Material3's built-in elevation scale is used as-is; no DC elevation token exists because no component has needed one yet (anti-overengineering constraint — `plan/CLAUDE_IMPLEMENTATION_PLAN.md` §7).

## Rules

No new rule IDs — governed by [`README.md`](README.md) DS-03.

## Testing / validation

`grep '\.dp'` and `RoundedCornerShape(` outside `ui/designsystem/` — currently zero hits in `app/src`.

## Related documents

- [`tokens.md`](tokens.md)
