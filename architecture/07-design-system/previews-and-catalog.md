# Previews & catalog

[Back to design system](README.md)

## Scope

How the design system is visually verified without a running app.

## Current implementation

`catalog/DesignSystemCatalog.kt` is organized as fourteen visual checks:

- color roles; typography; buttons; fields; app bars; adaptive navigation; drawer; icon buttons; cards/lists; chips; dialogs; sheet/layout; states; and spacing/shapes.
- `DesignSystemCatalog()` remains scrollable and runnable; the debug app exposes it as a navigation destination.
- `DcThemePreviews`, `DcFontScalePreviews`, and `DcWindowPreviews` eliminate repeated preview boilerplate for color, scale and width coverage. The catalog uses `DcTheme`'s deterministic default (`dynamicColor = false`).

This is a manual catalog, not a generated one — there is no Paparazzi/screenshot-test tooling yet (that belongs to Phase I, `17-testing.md`, if adopted at all).

## Rules

**DS-04 — Every new DS component gets a preview in `DesignSystemCatalog`.**

**Why:** A component with no preview has no cheap way to catch a visual regression (Gate C requirement: "Design-system previews/sample screen compile").

**Verification:** Every public composable under `components/` appears in `DesignSystemCatalog()`.

## Testing / validation

`./gradlew :ui:designsystem:compileDebugKotlin` — previews are ordinary `@Composable` functions, so a compile failure here is a real regression, not just a preview-only issue.

## Related documents

- [`components.md`](components.md)
