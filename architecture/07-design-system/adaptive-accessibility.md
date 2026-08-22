# Adaptive & accessibility

[Back to design system](README.md)

## Scope

Window-size/adaptive-layout policy and accessibility floors (touch target, font scale, semantics, RTL).

## Current implementation

- **Accessibility:** `tokens/AccessibilityTokens.kt` defines `MinTouchTarget` (48dp), enforced by interactive button/input/list-row components (DS-02). Loading and input errors expose screen-reader semantics; password visibility labels come from the caller for localization. Typography uses `sp` units throughout (`DcTypography`) and layouts use directional-safe Compose APIs.
- **Adaptive/window-size:** Not implemented. The app is a single `Scaffold` with no layout that would differ across window-size classes. No `WindowSizeClass` dependency or breakpoint policy exists yet — will be added when a screen actually needs to branch on window size (Phase E+), not preemptively.

## Rules

Governed by [`README.md`](README.md) DS-02. No new rule IDs here.

## Testing / validation

Inspect new interactive components for `defaultMinSize`/touch-target sizing as they're added.

## Related documents

- [`components.md`](components.md)
