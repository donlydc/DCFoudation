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

- **Menus & sheets (W1/W3)** — `DcDropdownMenu`, `DcOverflowMenu`, `DcTooltip`, `DcActionSheet`, `DcSelectionSheet`. Menu entries are `DcMenuItem`; destructive items use error color plus text.
- **Selection (W1/W2)** — `DcSegmentedButton`/`DcMultiSegmentedButton`, `DcRadioGroup`, `DcCheckboxGroup` (tri-state select-all), `DcChipGroup` (`DcSelectionMode`, wrap/scroll), typed `DcDropdownField` overload. Typed choices use `DcOption<T>`; toggle policy is the pure `toggleSelection`.
- **Forms (W2)** — `DcTextArea`, `DcFormSection`, `DcStepper`, `DcSlider`/`DcRangeSlider`, `DcOtpField`, `DcDateField` + `DcDatePickerDialog` (`java.time`; the caller formats dates). `DcTextField` gained additive `maxLength`/counter/`minLines`/`maxLines`.
- **Feedback (W1)** — `DcInlineMessage` (tone banner), `DcLinearProgress`/`DcCircularProgress`, `DcSnackbarHostState`/`DcSnackbarHost`/`DcSnackbarMessage` and a `DcSnackbarEffect` overload (the legacy `String?`/`SnackbarHostState` form is `@Deprecated` since A4; use the `DcSnackbarMessage` form); Material `SnackbarData` is not exposed.
- **Badges, tabs, toggles** — `DcBadge`/`DcBadgedBox` (`DcNavigationItem.badgeCount`), `DcTabRow`, `DcIconToggleButton`, `DcPageIndicator`, `DcAvatarSize`.
- **Layout & lists** — `DcContentContainer` (width cap), `DcAdaptiveGrid`, `DcListAppendState`, `DcExpandableSection`, `DcButtonRow`, `DcSkeletonListRow`/`DcSkeletonCard`, `DcSkeletonSize`; `DcScaffold` accepts `floatingActionButton`.
- **Default strings** — user-visible defaults come from `ui/designsystem/src/main/res/values/strings.xml` (`dc_*`); callers can still override every label.

New components still require a real shared policy (theme, spacing, accessibility or interaction). Do not wrap a Material component only to rename it.

## Rules

Governed by [`README.md`](README.md) DS-01/DS-02. No new rule IDs here.

## Testing / validation

Every public composable renders in `DesignSystemCatalog`'s light/dark preview matrix after lane A4, except `DcDatePickerDialog` (indirect via `DcDateField`). `DcContentStatePolicyTest` covers the eight branch inputs (A4; Gradle NOT_RUN); the full registry is [`ui/designsystem/COMPONENTS.md`](../../ui/designsystem/COMPONENTS.md); Compose component tests live under `src/androidTest` and should run on an emulator/CI device.

## Related documents

- [`previews-and-catalog.md`](previews-and-catalog.md)
- [`common-screen-patterns.md`](common-screen-patterns.md)
- [`adaptive-accessibility.md`](adaptive-accessibility.md)
