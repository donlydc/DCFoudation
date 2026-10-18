# Components

[Back to design system](README.md)

## Scope

Reusable Compose components exposed by `:ui:designsystem`.

## Current implementation

The component set covers policies repeated by both current and expected reference features:

- **`DcText` / `DcTextLink`** — theme default text plus truncation/alignment/color controls; links retain button semantics and the touch-target floor.
- **`DcButton` / `DcPrimaryActionButton` / `DcIconButton` / FABs** — variants, stable-size loading, full-width screen actions, and standard/tonal/soft-destructive icon treatments.
- **`DcTopAppBar`, `DcSectionHeader`, `DcScaffold`, `DcBottomActionBar`** — shared screen chrome, heading semantics and safe/IME/navigation insets.
- **`DcContentState`, `DcSnackbarEffect`, `DcConfirmDialog`, `DcInfoDialog`, `DcErrorDialog`, sheet, skeleton and pull-to-refresh** — priority rules for stale data, durable message acknowledgement and processing-safe overlays.
- **`DcCard`, `DcListRow`, `DcKeyValueRow`, `DcChip`, `DcStatusChip`, `DcDivider`, `DcToggleRow`, `DcDropdownField`** — outlined/elevated surfaces, neutral detail summaries, interactive filters and non-interactive semantic status labels.
- **`DcNavigationSuite`, `DcNavigationDrawerContent`, `DcNavigationDrawerHeader`** — adaptive bar/rail/drawer navigation plus business-neutral drawer anatomy behind DC-owned models.
- **`DcAsyncImage` / `DcAvatar`** — Coil is contained in one component file; callers never receive Coil types.
- **`DcTextField` / `DcPasswordTextField`** — controlled inputs with error semantics, keyboard hooks and caller-supplied localized visibility labels.
- **`DcLoadingState`, `DcEmptyState`, `DcErrorState`** — consistent screen/content feedback; retry remains caller-owned.
- **`DcOfflineBanner`** — semantic error-container connectivity feedback with optional action content.
- **`DcListRow`** — standard list item; when clickable it applies button semantics and the touch-target floor.

New components still require a real shared policy (theme, spacing, accessibility or interaction). Do not wrap a Material component only to rename it.

## Rules

Governed by [`README.md`](README.md) DS-01/DS-02. No new rule IDs here.

## Testing / validation

Every public component renders in `DesignSystemCatalog`'s light/dark preview matrix. `DcContentStatePolicyTest` covers the five state-priority branches; Compose component tests live under `src/androidTest` and should run on an emulator/CI device.

## Related documents

- [`previews-and-catalog.md`](previews-and-catalog.md)
- [`common-screen-patterns.md`](common-screen-patterns.md)
- [`adaptive-accessibility.md`](adaptive-accessibility.md)
