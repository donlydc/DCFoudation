# Adaptive & accessibility

[Back to design system](README.md)

## Scope

Window-size/adaptive-layout policy and accessibility floors (touch target, font scale, semantics, RTL).

## Current implementation

- **Accessibility:** `tokens/AccessibilityTokens.kt` defines `MinTouchTarget` (48dp), enforced by `DcButton` (DS-02). Typography uses `sp` units throughout (`DcTypography`), so it scales with the user's system font-size setting automatically — no extra work needed for that. RTL: no directional (`start`/`end` vs `left`/`right`) layout logic exists yet to get wrong; `Modifier.padding(innerPadding)` from `Scaffold` is already RTL-safe by construction.
- **Adaptive/window-size:** Not implemented. The app is a single `Scaffold` with no layout that would differ across window-size classes. No `WindowSizeClass` dependency or breakpoint policy exists yet — will be added when a screen actually needs to branch on window size (Phase E+), not preemptively.

## Rules

Governed by [`README.md`](README.md) DS-02. No new rule IDs here.

## Testing / validation

Inspect new interactive components for `defaultMinSize`/touch-target sizing as they're added.

## Related documents

- [`components.md`](components.md)
