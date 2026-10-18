# Adaptive & accessibility

[Back to design system](README.md)

## Scope

Window-size/adaptive-layout policy and accessibility floors (touch target, font scale, semantics, RTL).

## Current implementation

- **Accessibility:** `tokens/AccessibilityTokens.kt` defines `MinTouchTarget` (48dp), enforced by interactive button/input/list-row components (DS-02). Loading and input errors expose screen-reader semantics; password visibility labels come from the caller for localization. Typography uses `sp` units throughout (`DcTypography`) and layouts use directional-safe Compose APIs.
- **Adaptive/window-size:** `DcNavigationSuite` owns the only shared adaptive policy today. Material3 Adaptive renders the same item list as a bottom bar, rail or permanent drawer based on window information; feature code remains independent of adaptive implementation types. Other pane/layout branching remains caller-owned until a real list-detail use case needs a shared policy.

- **Width cap:** `DcContentContainer` (`Readable` 640dp / `Wide` 840dp / `Full`) centers and limits page content on medium/expanded windows without reading window classes. `DcAdaptiveGrid` derives columns from a minimum cell width. `DcButtonRow` stacks when narrow or when font scale >= 1.3.
- **Group semantics:** radio/checkbox/chip/segmented groups expose `selectableGroup`; badges, tone messages and status never rely on color alone; `DcOtpField` is a single text node for screen readers.

## Rules

Governed by [`README.md`](README.md) DS-02. No new rule IDs here.

## Testing / validation

Inspect new interactive components for `defaultMinSize`/touch-target sizing as they're added.

`DcChip` is visually compact while Material supplies the minimum interactive touch target around it. `DcStatusChip` is a non-interactive label, so it intentionally has no 48dp visual minimum; its required text and indicator prevent color-only state communication.

## Related documents

- [`components.md`](components.md)
