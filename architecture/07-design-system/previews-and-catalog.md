# Previews & catalog

[Back to design system](README.md)

## Scope

How the design system is visually verified without a running app.

## Current implementation

`catalog/DesignSystemCatalog.kt` is organized as twenty visual checks:

- color roles; typography; buttons; fields; app bars; adaptive navigation; drawer; icon buttons; cards/lists; chips; dialogs; sheet/layout; states; and spacing/shapes, followed by `ExtendedCatalog.kt` sections 15–20 (menus/sheets, selection, forms, feedback, badges/tabs/toggles, layout/lists).
- Lane A4 added three examples (`DcButton` slot overload, `DcListRow` slot overload, `DcMultiSegmentedButton`) and removed the legacy snackbar demo; section counts (14 + 6) are unchanged. DS-04 can now be checked against the registry [`ui/designsystem/COMPONENTS.md`](../../ui/designsystem/COMPONENTS.md).
- `DesignSystemCatalog()` remains scrollable and runnable; the debug app exposes it as a navigation destination.
- `DcThemePreviews`, `DcFontScalePreviews`, and `DcWindowPreviews` eliminate repeated preview boilerplate for color, scale and width coverage. The catalog uses `DcTheme`'s deterministic default (`dynamicColor = false`).

This is a manual catalog, not a generated one — there is no Paparazzi/screenshot-test tooling yet (that belongs to Phase I, `17-testing.md`, if adopted at all). A stack recommendation is PROPOSED in DEC-A4-1 (`23-decision-log.md`); nothing was added and MP:UI-09 stays NOT_RUN.

## Rules

**DS-04 — Every new DS component gets a preview in `DesignSystemCatalog`.**

**Why:** A component with no preview has no cheap way to catch a visual regression (Gate C requirement: "Design-system previews/sample screen compile").

**Verification:** Every public composable under `components/` appears in `DesignSystemCatalog()`.

## Testing / validation

`./gradlew :ui:designsystem:compileDebugKotlin` — previews are ordinary `@Composable` functions, so a compile failure here is a real regression, not just a preview-only issue.

## Related documents

- [`components.md`](components.md)
