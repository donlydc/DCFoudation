# Components

[Back to design system](README.md)

## Scope

Reusable Compose components exposed by `:ui:designsystem`.

## Current implementation

Two components exist — deliberately minimal, sized to what `MainActivity.kt` actually needs today (anti-overengineering constraint):

- **`DcText`** (`components/DcText.kt`) — wraps Material3 `Text`; defaults `style` to `MaterialTheme.typography.bodyLarge`.
- **`DcButton`** (`components/DcButton.kt`) — wraps Material3 `Button`; applies `Modifier.defaultMinSize(minWidth = AccessibilityTokens.MinTouchTarget, minHeight = AccessibilityTokens.MinTouchTarget)` before the caller's own modifier, then delegates label rendering to Material3 `Text`. Not yet used by `app` (no button exists in the sample screen yet) but included in the catalog.

No other components exist. Do not add a `DcCard`/`DcTextField`/etc. speculatively — add it when a feature (Phase E onward) actually needs it, per `plan/CLAUDE_IMPLEMENTATION_PLAN.md` §7.

## Rules

Governed by [`README.md`](README.md) DS-01/DS-02. No new rule IDs here.

## Testing / validation

Both components render in `DesignSystemCatalog`'s light/dark previews.

## Related documents

- [`previews-and-catalog.md`](previews-and-catalog.md)
- [`adaptive-accessibility.md`](adaptive-accessibility.md)
