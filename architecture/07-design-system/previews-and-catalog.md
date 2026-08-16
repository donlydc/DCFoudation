# Previews & catalog

[Back to design system](README.md)

## Scope

How the design system is visually verified without a running app.

## Current implementation

`catalog/DesignSystemCatalog.kt`:

- `DesignSystemCatalog()` — a plain `Column` exercising every current token/component (`DcText` + `DcButton`, `SpacingTokens.Small` between them, `SpacingTokens.Medium` padding).
- `DesignSystemCatalogLightPreview` / `DesignSystemCatalogDarkPreview` — `@Preview` composables wrapping the catalog in `DcTheme(darkTheme = false/true, dynamicColor = false)`. `dynamicColor` is forced off in previews so they render the token-defined palette deterministically instead of whatever dynamic color the preview host happens to compute.

This is a manual catalog, not a generated one — there is no Paparazzi/screenshot-test tooling yet (that belongs to Phase I, `17-testing.md`, if adopted at all).

## Rules

**DS-04 — Every new DS component gets a preview in `DesignSystemCatalog`.**

**Why:** A component with no preview has no cheap way to catch a visual regression (Gate C requirement: "Design-system previews/sample screen compile").

**Verification:** Every public composable under `components/` appears in `DesignSystemCatalog()`.

## Testing / validation

`./gradlew :ui:designsystem:compileDebugKotlin` — previews are ordinary `@Composable` functions, so a compile failure here is a real regression, not just a preview-only issue.

## Related documents

- [`components.md`](components.md)
