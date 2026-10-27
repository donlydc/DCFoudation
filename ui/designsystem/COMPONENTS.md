# `:ui:designsystem` — Component registry (MP:UI-01)

Status: DRAFT (generated from source by work package A4 / R4.1-R4.5; Sonnet review APPROVE, see reports/wp/A4.review.md; Compose code NOT_RUN)
Source snapshot: working tree on top of `2492236` (2026-10-27). Registry rows were produced by scanning the public top-level declarations of `ui/designsystem/src/main/java/.../designsystem/**` and cross-checking consumers, catalog sections, previews and tests against source - not against docs.
Test status: **NOT_RUN** for everything here. Gradle/Android cannot build in the authoring environment, so no Compose code, preview or test in this file has been compiled or executed. Evidence reference: `reports/baseline-verification.md`, `reports/wp/A4.md`.

Owner of every row: `:ui:designsystem` (single module; `:app` is the only consumer, see `architecture/modules/ui-designsystem.md`).

## 1. How to read this registry

| Term | Meaning |
|---|---|
| Symbol kind | `fn` public `@Composable`; `helper` public non-composable function; `enum`/`data`/`object`/`class`/`val`/`annotation` as declared |
| File:line | declaration site under `ui/designsystem/src/main/java/com/dc/android/foundation/designsystem/` |
| Catalog | `DSCn` = section *n* of `catalog/DesignSystemCatalog.kt` (1-14); `EXTn` = section *n* of `catalog/ExtendedCatalog.kt` (15-20); `indirect` = only rendered as part of another demo; `n/a` = constants/helpers with nothing to render |
| Preview | a `private @DcThemePreviews` function that renders it; `DesignSystemCatalogPreview` (light/dark + 3 widths) previews everything that has a catalog entry |
| Tests | `U:` JVM unit test (`src/test`, never executed), `I:` instrumented Compose test (`src/androidTest`, compiled only, never executed). `(A4, NOT_RUN)` = added by this WP |
| App refs | textual mentions (imports + call sites) in `app/src/main`; overloaded names share one count. `0` = no `:app` consumer today |
| Status (proposal) | **STABLE** = has an `:app` consumer or a unit/instrumented test, documented contract, no open contract defect; **EXPERIMENTAL** = catalog-only (no consumer) or still has an open finding; **DEPRECATED** = superseded, kept for source compatibility. All values are PROPOSED until Opus finalizes (plan R4 gate) |

State-matrix coverage is recorded in the *Variants / states (API)* column (what the API can express) together with *Preview* and *Catalog* (what is actually rendered). A component whose preview/catalog shows fewer states than the API expresses is called out in the cell or in section 5.

## 2. Summary (generated)

| Slice | Count |
|---|---|
| Public symbols registered | 133 (components 113, theme 7, tokens 9, catalog 1, preview 3) |
| `components/`: composable functions | 81 declarations / 75 distinct names (6 overloaded names with two overloads each: `DcButton`, `DcListRow`, `DcAvatar`, `DcSkeleton`, `DcDropdownField`, `DcSnackbarEffect`) |
| `components/`: models, enums, objects, helpers | 6 data classes, 16 enums, 5 `*Defaults` objects, 1 state-holder class, 4 public non-composable helpers |
| Proposed status | STABLE 55 / EXPERIMENTAL 77 / DEPRECATED 1 |
| `:app` consumers | 29 distinct symbols referenced from `app/src/main` (e.g. `DcTheme`, `DcText`, `DcScaffold`, `DcButton`, `DcCard`, `DcNavigationSuite`, `DcContentState`, `DcSnackbarEffect`) |
| Catalog gaps after A4 | none for composables: A4 added the three missing examples (`DcButton` slot overload, `DcListRow` slot overload, `DcMultiSegmentedButton`). `DcDatePickerDialog` is exercised only through `DcDateField` (indirect) |
| `@Preview` functions | 22 private functions (21 in `components/`, 1 for the catalog); symbols without their own preview rely on `DesignSystemCatalogPreview` |
| Tests | 30 `@Test` unit (27 + 3 added by A4) and 21 `@Test` instrumented (19 + 2 added by A4); all NOT_RUN |

Gate R4 (plan): every public UI component registered with code path, catalog entry or explicit exclusion, state matrix and test status - met as **documentation coverage** by this file. It is not a line-coverage claim.

## 3. Registry

### Theme (7)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `DcColors` (data) | `theme/DcColors.kt:10` | success/warning/info/offline (+on*, containers) | contrast: success pair 3.3:1 (UI-02 F-02) | - | U: DcThemeTokensTest | DSC1 (3 roles) | 0 | STABLE |
| `LocalDcColors` (val) | `theme/DcColors.kt:28` | CompositionLocal | - | - | - | DSC1 | 0 | STABLE |
| `DcShapes` (val) | `theme/DcShapes.kt:7` | maps ShapeTokens to M3 Shapes | - | - | U: semanticScalesMap | DSC14 | 0 | STABLE |
| `DcTheme` (fn) | `theme/DcTheme.kt:99` | darkTheme, dynamicColor (API31+), scheme/typography/shapes/colors/spacing/uiScale | root provider | all previews | U: via tokens | DSC | 8 | STABLE |
| `DcThemeDefaults` (object) | `theme/DcTheme.kt:124` | Typography, Shapes, colorScheme(), extendedColors() | - | - | U: DcThemeTokensTest | - | 0 | STABLE |
| `DcTypography` (val) | `theme/DcTypography.kt:20` | 11 styles set; 4 M3 roles inherit defaults | - | - | U: typographyUsesDcScale | DSC2 | 0 | STABLE |
| `DcUiScale` (enum) | `theme/DcUiScale.kt:13` | Compact .90 / Standard / Comfortable 1.10 | touch floor + system font scale unchanged | - | U: DcUiScaleTest (3) | NO (no scale demo) | 13 | STABLE |

### Tokens (9)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `AccessibilityTokens` (object) | `tokens/AccessibilityTokens.kt:7` | MinTouchTarget 48dp | - | - | I: touch-floor tests | - | 0 | STABLE |
| `ColorTokens` (object) | `tokens/ColorTokens.kt:9` | primitive palette | feature UI must not import (DS-01) | - | U: DcThemeTokensTest | DSC1 (via roles) | 0 | STABLE |
| `DcSpacing` (data) | `tokens/DcSpacing.kt:16` | none..xxLarge + component; Default / Compact preset | - | - | U: DcUiScaleTest | DSC14 | 0 | STABLE |
| `LocalDcSpacing` (val) | `tokens/DcSpacing.kt:40` | CompositionLocal (scaled by DcUiScale) | - | - | U: via DcUiScale | DSC14 | 11 | STABLE |
| `MotionTokens` (object) | `tokens/MotionTokens.kt:4` | 150/300/500 ms; no easing policy | no reduce-motion hook | - | - | NO | 0 | EXPERIMENTAL |
| `ShapeTokens` (object) | `tokens/ShapeTokens.kt:8` | 7 radii | - | - | U: DcThemeTokensTest | DSC14 | 0 | STABLE |
| `SpacingTokens` (object) | `tokens/SpacingTokens.kt:7` | duplicates DcSpacing.Default; not scaled by DcUiScale (UI-02 F-05) | - | - | U: 1 assertion | NO | 0 | EXPERIMENTAL |
| `ElevationTokens` (object) | `tokens/SurfaceTokens.kt:11` | Level0..3 | - | - | U: DcThemeTokensTest | NO | 0 | STABLE |
| `StrokeTokens` (object) | `tokens/SurfaceTokens.kt:19` | Hairline 1dp | - | - | U: DcThemeTokensTest | NO | 0 | STABLE |

### Text (2)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `DcText` (fn) | `components/DcText.kt:17` | style, color, maxLines, overflow, align | - | via others | I: indirect | DSC3 + all | 11 | STABLE |
| `DcTextLink` (fn) | `components/DcTextLink.kt:14` | enabled/disabled | role=Button, 48dp | - | - | DSC3 | 0 | EXPERIMENTAL |

### Actions (11)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `DcIconButtonVariant` (enum) | `components/DcActionComponents.kt:21` | Standard, Tonal, SoftDestructive | - | - | - | DSC8 | 0 | STABLE |
| `DcIcon` (fn) | `components/DcActionComponents.kt:34` | decorative (null desc) or described | caller-owned contentDescription | via others | - | DSC8,EXT19 | 0 | STABLE |
| `DcIconButton` (fn) | `components/DcActionComponents.kt:46` | 3 variants x enabled/disabled | 48dp floor; description required | - | I: indirect (stepper floor) | DSC5,8 EXT15 | 4 | STABLE |
| `DcFab` (fn) | `components/DcActionComponents.kt:96` | single style | 48dp floor; description required | - | - | DSC3 | 0 | EXPERIMENTAL |
| `DcExtendedFab` (fn) | `components/DcActionComponents.kt:112` | single style | icon description duplicates visible text (see UI-03 C-10) | - | - | DSC3 | 0 | EXPERIMENTAL |
| `DcIconToggleButton` (fn) | `components/DcActionComponents.kt:132` | checked/unchecked, 2 variants, disabled | toggle semantics, constant description | - | - | EXT19 | 0 | EXPERIMENTAL |
| `DcButtonVariant` (enum) | `components/DcButton.kt:35` | Primary, Secondary, Tertiary, Destructive, Text | - | DcButtonPreview | - | DSC3 | 2 | STABLE |
| `DcButtonDefaults` (object) | `components/DcButton.kt:38` | MinHeight, ProgressSize/Stroke, ContentPadding, colors(variant) | - | - | - | n/a | 0 | STABLE |
| `DcButton` (fn) | `components/DcButton.kt:75` | 5 variants x enabled/disabled/loading/leading+trailing icon | 48dp floor; loading disables clicks; no busy state announced | DcButtonPreview | I: loadingDisabled, loadingKeepsHeight, touchFloor | DSC3 | 7 | STABLE |
| `DcPrimaryActionButton` (fn) | `components/DcButton.kt:118` | full-width; Primary or Destructive (Boolean) | as DcButton | - | - | DSC3 | 2 | STABLE |
| `DcButton` (fn) | `components/DcButton.kt:141` | slot content; same variant/touch policy | 48dp floor | - | - | DSC3 (A4) | 7 | STABLE |

### Input (15)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `DcDropdownField` (fn) | `components/DcDropdownField.kt:20` | String options, caller-owned expanded; no per-option enabled | menu anchor semantics | - | I: dropdownFieldOpensAndSelects | DSC4 | 4 | STABLE |
| `DcTextArea` (fn) | `components/DcFormComponents.kt:61` | wraps DcTextField, 3..8 lines, maxLength | inherits | DcFormPreview | U: maxLength policy | EXT17 | 0 | EXPERIMENTAL |
| `DcFormSection` (fn) | `components/DcFormComponents.kt:91` | optional title/support | heading via DcSectionHeader | DcFormPreview | - | EXT17 | 0 | EXPERIMENTAL |
| `clampStep` (helper) | `components/DcFormComponents.kt:104` | public pure fn | - | - | U: stepperClampsToRange | - | 0 | EXPERIMENTAL |
| `DcStepper` (fn) | `components/DcFormComponents.kt:116` | min/max disable, live region value | live region; 48dp buttons | DcFormPreview | U: clamp; I: disablesAtMin, touchFloor | EXT17 | 0 | EXPERIMENTAL |
| `DcSlider` (fn) | `components/DcFormComponents.kt:158` | enabled/disabled, steps | contentDescription + stateDescription | DcFormPreview | - | EXT17 | 0 | EXPERIMENTAL |
| `DcRangeSlider` (fn) | `components/DcFormComponents.kt:190` | enabled/disabled, steps | same | - | - | EXT17 | 0 | EXPERIMENTAL |
| `filterOtpInput` (helper) | `components/DcFormComponents.kt:220` | public pure fn | - | - | U: otpKeepsOnlyDigits | - | 0 | EXPERIMENTAL |
| `DcOtpField` (fn) | `components/DcFormComponents.kt:227` | length, error, onComplete | boxes cleared from a11y tree; field has no label/semantics (dc_digit_of_code unused) | DcFormPreview | U: filter policy | EXT17 | 0 | EXPERIMENTAL |
| `DcDatePickerDialog` (fn) | `components/DcFormComponents.kt:297` | caller-owned visibility, min/max | Material dialog | - | U: date bounds, utc round trip | indirect via DcDateField | 0 | EXPERIMENTAL |
| `DcDateField` (fn) | `components/DcFormComponents.kt:336` | value/null, error, min/max | click overlay role=Button | DcFormPreview | U: date helpers | EXT17 | 0 | EXPERIMENTAL |
| `DcTextField` (fn) | `components/DcInputComponents.kt:42` | enabled/readOnly/error(isError or errorText)/placeholder/leading/trailing/counter | error semantics; counter description; 56dp min height literal | DcTextFieldPreview, DcTextFieldFontScalePreview | U: maxLength; I: errorSemantics x2 | DSC4 | 0 | STABLE |
| `DcPasswordTextField` (fn) | `components/DcInputComponents.kt:134` | reveal toggle (saveable), error | 48dp toggle; caller labels | - | - | DSC4 | 0 | EXPERIMENTAL |
| `DcSearchBar` (fn) | `components/DcSearchBar.kt:16` | query/clear/search IME; clear now DcButton (A4) | clear button 48dp after A4 | - | - | DSC4 | 0 | EXPERIMENTAL |
| `DcDropdownField` (fn) | `components/DcSelectionComponents.kt:218` | typed DcOption<T>; per-option enabled not rendered; selects by label (duplicate labels collide, UI-03 C-06) | as untyped | - | - | EXT16 | 4 | EXPERIMENTAL |

### Selection (8)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `DcChipGroupLayout` (enum) | `components/DcSelectionComponents.kt:34` | Wrap, Scroll | - | - | - | EXT16 | 0 | EXPERIMENTAL |
| `DcSegmentedButton` (fn) | `components/DcSelectionComponents.kt:41` | single choice, per-option enabled | Material segmented | DcSelectionPreview | - | EXT16 | 0 | EXPERIMENTAL |
| `DcMultiSegmentedButton` (fn) | `components/DcSelectionComponents.kt:63` | multi choice | Material segmented | - | - | EXT16 (A4) | 0 | EXPERIMENTAL |
| `DcRadioGroup` (fn) | `components/DcSelectionComponents.kt:100` | optional label, per-option enabled | selectableGroup, heading label | DcSelectionPreview | I: selectsClickedOption | EXT16 | 0 | EXPERIMENTAL |
| `DcCheckboxGroup` (fn) | `components/DcSelectionComponents.kt:127` | tri-state select-all | triStateToggleable | DcSelectionPreview | U: selectAllState | EXT16 | 0 | EXPERIMENTAL |
| `DcChipGroup` (fn) | `components/DcSelectionComponents.kt:185` | 3 modes x wrap/scroll | selectableGroup | DcSelectionPreview | I: singleModeKeepsSelection | EXT16 | 0 | EXPERIMENTAL |
| `DcToggleControl` (enum) | `components/DcToggleRow.kt:21` | Checkbox/Switch/Radio | - | - | I: A4 switch role | DSC4 (Switch only) | 2 | STABLE |
| `DcToggleRow` (fn) | `components/DcToggleRow.kt:28` | 3 controls x checked/disabled | toggleable; role fixed in A4 (Switch -> Role.Switch) | - | I: respondsWhenLabelTapped, switchRole (A4, NOT_RUN) | DSC4 (Switch only) | 2 | STABLE |

### Collections (9)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `DcListAppendState` (fn) | `components/DcLayoutComponents.kt:125` | loading / error+retry / end note | live region | DcLayoutPreview | - | EXT20 (3 states) | 0 | EXPERIMENTAL |
| `DcCardVariant` (enum) | `components/DcListComponents.kt:40` | Outlined, Elevated | - | DcCardPreview | - | DSC9 | 0 | STABLE |
| `DcCard` (fn) | `components/DcListComponents.kt:50` | 2 variants x static/clickable | clickable: role=Button + 48dp | DcCardPreview | - | DSC9 | 7 | STABLE |
| `DcKeyValueRow` (fn) | `components/DcListComponents.kt:94` | normal / emphasized | label/value as text | DcKeyValueRowPreview | I: exposesLabelAndValue | DSC9 | 7 | STABLE |
| `DcChip` (fn) | `components/DcListComponents.kt:133` | selected/unselected/disabled (FilterChip) | Material chip semantics | via DcSelectionPreview | I: via chipGroup | DSC10 | 2 | STABLE |
| `DcStatusChip` (fn) | `components/DcListComponents.kt:158` | 5 tones x indicator on/off; non-interactive | text carries meaning; light Neutral text 4.35:1 (UI-02 F-03) | DcStatusChipPreview | I: statusChipIsReadable | DSC10 | 2 | STABLE |
| `DcDivider` (fn) | `components/DcListComponents.kt:226` | optional leading inset | decorative | DcKeyValueRowPreview | - | DSC9 | 2 | STABLE |
| `DcListRow` (fn) | `components/DcListRow.kt:30` | headline/supporting/leading/trailing; selected, enabled, click, long-click | role=Button when clickable, 48dp; selected state not exposed to a11y | DcListRowPreview | - | DSC9 | 2 | STABLE |
| `DcListRow` (fn) | `components/DcListRow.kt:65` | slot variant | same | - | - | DSC9 (A4) | 2 | STABLE |

### Containers (13)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `DcContentWidth` (enum) | `components/DcLayoutComponents.kt:62` | Readable 640 / Wide 840 / Full | - | - | - | EXT20 (Readable only) | 0 | EXPERIMENTAL |
| `DcContentContainer` (fn) | `components/DcLayoutComponents.kt:78` | width cap + horizontal padding | - | - | - | EXT20 | 0 | EXPERIMENTAL |
| `DcAdaptiveGrid` (fn) | `components/DcLayoutComponents.kt:98` | Adaptive(minCell) lazy grid | caller keys | - | - | EXT20 | 0 | EXPERIMENTAL |
| `DcGridDefaults` (object) | `components/DcLayoutComponents.kt:115` | MinCell 160dp | - | - | - | n/a | 0 | EXPERIMENTAL |
| `DcExpandableSection` (fn) | `components/DcLayoutComponents.kt:160` | caller-owned expanded; chevron animation | stateDescription, heading, 48dp, role=Button | DcLayoutPreview | I: togglesThroughCallback | EXT20 | 0 | EXPERIMENTAL |
| `DcButtonRow` (fn) | `components/DcLayoutComponents.kt:275` | side by side or stacked (width<320dp or fontScale>=1.3) | reading order primary last | - | U: stack policy | EXT20 | 0 | EXPERIMENTAL |
| `DcButtonRowDefaults` (object) | `components/DcLayoutComponents.kt:300` | StackThreshold, StackFontScale | - | - | U: via policy | n/a | 0 | EXPERIMENTAL |
| `DcPullToRefreshBox` (fn) | `components/DcPullToRefreshBox.kt:12` | isRefreshing controlled | Material semantics | - | - | DSC12 | 0 | EXPERIMENTAL |
| `DcTopAppBarStyle` (enum) | `components/DcScreenComponents.kt:33` | Small/Centered/Large | - | DcTopAppBarPreview | - | DSC5 | 0 | STABLE |
| `DcTopAppBar` (fn) | `components/DcScreenComponents.kt:43` | 3 styles, back, actions, scroll | back description localized | DcTopAppBarPreview | I: backIconUsesLocalizedDescription | DSC5 | 6 | STABLE |
| `DcSectionHeader` (fn) | `components/DcScreenComponents.kt:91` | title/supporting | heading() | - | - | DSC all sections | 6 | STABLE |
| `DcBottomActionBar` (fn) | `components/DcScreenComponents.kt:120` | nav+ime padding | - | - | - | DSC12 | 2 | STABLE |
| `DcScaffold` (fn) | `components/DcScreenComponents.kt:138` | safeDrawing insets, snackbarHost, FAB | - | - | - | DSC12 EXT18 | 10 | STABLE |

### Navigation UI (10)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `DcPageIndicator` (fn) | `components/DcLayoutComponents.kt:205` | N dots, current | single contentDescription | DcLayoutPreview | - | EXT19 | 0 | EXPERIMENTAL |
| `DcTabStyle` (enum) | `components/DcLayoutComponents.kt:309` | Primary, Secondary | - | - | - | EXT19 | 0 | EXPERIMENTAL |
| `DcTabRow` (fn) | `components/DcLayoutComponents.kt:313` | fixed or scrollable (>4), badges, disabled tab | Material Tab semantics | DcLayoutPreview | - | EXT19 | 0 | EXPERIMENTAL |
| `DcTabRowDefaults` (object) | `components/DcLayoutComponents.kt:354` | FixedTabLimit 4 | - | - | - | n/a | 0 | EXPERIMENTAL |
| `DcDrawerItem` (data) | `components/DcNavigationDrawer.kt:31` | id,label,icon,description | description duplicates label (UI-03 C-10) | DcNavigationDrawerContentPreview | - | DSC7 | 0 | EXPERIMENTAL |
| `DcDrawerSection` (data) | `components/DcNavigationDrawer.kt:39` | title?, items | - | DcNavigationDrawerContentPreview | - | DSC7 | 0 | EXPERIMENTAL |
| `DcNavigationDrawerContent` (fn) | `components/DcNavigationDrawer.kt:50` | sections, selection, header | selected via Material item | DcNavigationDrawerContentPreview | - | DSC7 | 0 | EXPERIMENTAL |
| `DcNavigationDrawerHeader` (fn) | `components/DcNavigationDrawer.kt:118` | title, subtitle, avatar slot | - | DcNavigationDrawerHeaderPreview | - | DSC7 | 0 | EXPERIMENTAL |
| `DcNavigationItem` (data) | `components/DcNavigationSuite.kt:14` | id,label,icon,description,badgeCount | description defaults to label (UI-03 C-10) | - | U: via suite count | DSC6 | 11 | STABLE |
| `DcNavigationSuite` (fn) | `components/DcNavigationSuite.kt:28` | bar/rail/drawer by window; 3..5 items enforced | selected semantics via Material; alpha dependency 1.5.0-alpha24 | - | U: DcNavigationSuiteTest (3) | DSC6 | 4 | STABLE |

### Menus & sheets (5)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `DcDropdownMenu` (fn) | `components/DcMenuComponents.kt:32` | items enabled/destructive; caller-owned expanded | Material menu | via DcOverflowMenuPreview | - | EXT15 | 0 | EXPERIMENTAL |
| `DcOverflowMenu` (fn) | `components/DcMenuComponents.kt:67` | internal expanded (saveable) | localized description | DcOverflowMenuPreview | I: opensAndReportsItem | EXT15 | 0 | EXPERIMENTAL |
| `DcTooltip` (fn) | `components/DcMenuComponents.kt:95` | plain tooltip | supplements description | - | - | EXT15 | 0 | EXPERIMENTAL |
| `DcActionSheet` (fn) | `components/DcSheetComponents.kt:17` | items enabled | rows 48dp | - | - | EXT15 | 0 | EXPERIMENTAL |
| `DcSelectionSheet` (fn) | `components/DcSheetComponents.kt:50` | selected row check icon + selected | selected flag visual only | - | - | EXT15 | 0 | EXPERIMENTAL |

### Feedback (21)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `formatBadgeCount` (helper) | `components/DcBadgeComponents.kt:24` | "99+" cap policy; public pure fn | - | - | U: DcWidgetPolicyTest | - | 0 | EXPERIMENTAL |
| `DcBadge` (fn) | `components/DcBadgeComponents.kt:47` | dot / count, 5 tones, cap | optional description; dot has none if null | DcBadgePreview | U: policy; I: badgeShowsCappedCount | EXT19 | 0 | EXPERIMENTAL |
| `DcBadgedBox` (fn) | `components/DcBadgeComponents.kt:85` | anchors badge top-end | - | via DcBadgePreview/Layout | - | EXT19 | 0 | EXPERIMENTAL |
| `DcSnackbarEffect` (fn) | `components/DcFeedbackComponents.kt:97` | LEGACY Material SnackbarHostState + String | - | - | I: snackbarAcknowledgesOneMessageOnce | removed from catalog by A4 | 4 (legacy overload) | DEPRECATED |
| `DcSkeletonListRow` (fn) | `components/DcLayoutComponents.kt:230` | lines, leading, animated flag | clearAndSet label; animation ignores reduce-motion unless caller passes animated=false | DcLayoutPreview | - | EXT18 | 0 | EXPERIMENTAL |
| `DcSkeletonCard` (fn) | `components/DcLayoutComponents.kt:263` | title+block skeleton | none | - | - | EXT18 | 0 | EXPERIMENTAL |
| `DcStatusTone` (enum) | `components/DcListComponents.kt:151` | Neutral/Info/Success/Warning/Error | text always required | DcStatusChipPreview | I: statusChip | DSC10,EXT18 | 6 | STABLE |
| `DcInlineMessageDefaults` (object) | `components/DcMessageComponents.kt:47` | icon(tone) | - | - | - | n/a | 0 | EXPERIMENTAL |
| `DcInlineMessage` (fn) | `components/DcMessageComponents.kt:60` | 5 tones, title, action, dismiss | live region only for Warning/Error | DcInlineMessagePreview | I: dismissInvokesCallback | EXT18 | 0 | EXPERIMENTAL |
| `DcProgressSize` (enum) | `components/DcMessageComponents.kt:108` | Small/Medium/Large | - | - | - | EXT18 | 0 | EXPERIMENTAL |
| `DcLinearProgress` (fn) | `components/DcMessageComponents.kt:115` | determinate (lambda) / indeterminate | contentDescription label | DcInlineMessagePreview | - | EXT18 | 0 | EXPERIMENTAL |
| `DcCircularProgress` (fn) | `components/DcMessageComponents.kt:132` | determinate / indeterminate, 3 sizes | contentDescription label | DcInlineMessagePreview | - | EXT18 | 0 | EXPERIMENTAL |
| `DcMessageDuration` (enum) | `components/DcMessageComponents.kt:149` | Short/Long/Indefinite | - | - | U: indefiniteRequiresAction | EXT18 (Short only) | 0 | EXPERIMENTAL |
| `DcSnackbarResult` (enum) | `components/DcMessageComponents.kt:152` | ActionPerformed/Dismissed | - | - | - | n/a | 0 | EXPERIMENTAL |
| `DcSnackbarMessage` (data) | `components/DcMessageComponents.kt:155` | text, tone, action, duration | Material snackbar announces | - | U: indefiniteRequiresAction | EXT18 | 0 | EXPERIMENTAL |
| `DcSnackbarHostState` (class) | `components/DcMessageComponents.kt:170` | show() suspends; single `current` (race when queued, UI-04 G-04) | - | - | - | EXT18 | 0 | EXPERIMENTAL |
| `DcSnackbarHost` (fn) | `components/DcMessageComponents.kt:202` | tone colors | - | - | - | EXT18 | 0 | EXPERIMENTAL |
| `DcSnackbarEffect` (fn) | `components/DcMessageComponents.kt:229` | typed message one-shot + onAction | - | - | I: typedSnackbarAcknowledgesOneMessageOnce (A4, NOT_RUN) | EXT18 | 0 | EXPERIMENTAL |
| `DcSkeletonSize` (enum) | `components/DcOverlayComponents.kt:288` | Line/Title/Block | - | - | - | indirect | 0 | EXPERIMENTAL |
| `DcSkeleton` (fn) | `components/DcOverlayComponents.kt:296` | semantic size overload | decorative | - | - | indirect (list row/card) | 0 | EXPERIMENTAL |
| `DcSkeleton` (fn) | `components/DcOverlayComponents.kt:309` | Dp overload kept by DEC-08, animated flag | decorative | - | - | DSC13 | 0 | EXPERIMENTAL |

### Overlays (5)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `DcModalBottomSheet` (fn) | `components/DcOverlayComponents.kt:46` | caller-owned; isProcessing blocks dismiss callback (swipe edge case UI-04 G-02) | Material sheet | - | - | DSC12 | 0 | EXPERIMENTAL |
| `DcConfirmDialog` (fn) | `components/DcOverlayComponents.kt:87` | normal/destructive, processing guard, icon | button order stable | DcConfirmDialogPreview, DcDestructiveConfirmDialogPreview | - | DSC11 | 2 | STABLE |
| `DcAlertDialog` (fn) | `components/DcOverlayComponents.kt:136` | single action, separate onConfirm/onDismissRequest | overlaps DcInfoDialog | - | - | DSC11 | 0 | EXPERIMENTAL |
| `DcInfoDialog` (fn) | `components/DcOverlayComponents.kt:159` | single action, confirmText/onDismiss | - | DcInfoDialogPreview | - | DSC11 | 0 | EXPERIMENTAL |
| `DcErrorDialog` (fn) | `components/DcOverlayComponents.kt:184` | single action, warning icon | recovery message only | DcErrorDialogPreview | - | DSC11 | 0 | EXPERIMENTAL |

### Screen states (6)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `DcContentState` (fn) | `components/DcFeedbackComponents.kt:36` | Content / Error / Loading / Empty by priority (stale content wins); no distinct Refreshing/Offline branch | progress now labelled (A4); banner not live-region | - | U: DcContentStatePolicyTest (8 of 8 branch inputs) | DSC13 (stale+loading+error case only) | 2 | STABLE |
| `DcLoadingSize` (enum) | `components/DcStateComponents.kt:26` | Inline, Screen | - | DcStateComponentsPreview | - | DSC13 | 0 | STABLE |
| `DcLoadingState` (fn) | `components/DcStateComponents.kt:33` | Inline / Screen | indicator labelled + visible text (double read) | DcStateComponentsPreview | - | DSC13 | 0 | STABLE |
| `DcEmptyState` (fn) | `components/DcStateComponents.kt:80` | title, description, icon, action slot | - | DcStateComponentsPreview | - | DSC13 | 5 | STABLE |
| `DcErrorState` (fn) | `components/DcStateComponents.kt:120` | title/message/retry (needs label+callback) | - | DcStateComponentsPreview | - | DSC13 | 0 | STABLE |
| `DcOfflineBanner` (fn) | `components/DcStateComponents.kt:165` | message + action slot; uses errorContainer, not DcColors.offline | not live region | DcStateComponentsPreview | - | DSC13 | 0 | STABLE |

### Media (4)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `DcAsyncImage` (fn) | `components/DcAsyncImage.kt:22` | model only; no placeholder/error/crossfade params (plan 5.2 gap) | caller description | - | - | DSC9 (null model only) | 0 | EXPERIMENTAL |
| `DcAvatarSize` (enum) | `components/DcAsyncImage.kt:37` | Small/Medium/Large/XLarge | - | - | - | EXT19 | 0 | EXPERIMENTAL |
| `DcAvatar` (fn) | `components/DcAsyncImage.kt:46` | initial fallback + image; size enum | description defaults to name | - | - | DSC7,9 EXT19 | 0 | EXPERIMENTAL |
| `DcAvatar` (fn) | `components/DcAsyncImage.kt:58` | Dp-size overload kept by DEC-08; KDoc says circular but shape is shapes.large | same | - | - | DSC7,9 | 0 | EXPERIMENTAL |

### Models (4)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `DcOption` (data) | `components/DcModels.kt:8` | value,label,enabled,icon,supportingText | - | via selection previews | I: via groups | EXT16-20 | 0 | STABLE |
| `DcMenuItem` (data) | `components/DcModels.kt:18` | id,label,icon,enabled,destructive | - | via menu preview | I: via overflow | EXT15 | 0 | EXPERIMENTAL |
| `DcSelectionMode` (enum) | `components/DcModels.kt:27` | Single/SingleOptional/Multiple | - | - | U: 3 selection tests | EXT16 | 0 | EXPERIMENTAL |
| `toggleSelection` (helper) | `components/DcModels.kt:42` | public pure fn | - | - | U: 3 selection tests | - | 0 | EXPERIMENTAL |

### Catalog (1)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `DesignSystemCatalog` (fn) | `catalog/DesignSystemCatalog.kt:44` | 14 sections + Extended 15-20 | - | DesignSystemCatalogPreview (light/dark/3 widths) | - | self | 2 | STABLE |

### Preview (3)

| Symbol | File:line | Variants / states (API) | A11y notes | Preview | Tests | Catalog | App refs | Status |
|---|---|---|---|---|---|---|---|---|
| `DcThemePreviews` (annotation) | `preview/DcPreviews.kt:9` | Light + Dark | - | n/a | - | n/a | 0 | STABLE |
| `DcFontScalePreviews` (annotation) | `preview/DcPreviews.kt:15` | 1.0/1.5/2.0x; used by 1 preview only | - | n/a | - | n/a | 0 | STABLE |
| `DcWindowPreviews` (annotation) | `preview/DcPreviews.kt:21` | 360/700/1200dp; used by catalog only | - | n/a | - | n/a | 0 | STABLE |
## 4. Explicit exclusions (not registered above)

| Excluded | Why |
|---|---|
| 22 `private fun *Preview()` functions | Tooling, not API (listed in the Preview column) |
| `internal` helpers: `resolveDcContentStateBranch`/`DcContentStateBranch`, `solidToneColors`, `enforceMaxLength`, `selectAllState`, `shouldStackButtons`, `requireSupportedNavigationItemCount`, `toUtcMillis`/`utcMillisToLocalDate`/`isDateSelectable`, `DcUiScale.applyTo`, enum properties `dp`/`max`/`factor`, `DcSnackbarHostState.host`/`current`, `CatalogSection`, `ExtendedCatalogSections` | Module-internal; behaviour is covered through the public symbol (unit tests reach them because tests share the module) |
| `private` composables/values: `GroupLabel`, `DcSingleActionDialog`, `Modifier.rowModifier`, `MinusIcon`, `LightColorScheme`/`DarkColorScheme`, `Sans`, catalog section/sample helpers | Implementation detail |
| Generated `R` class and `res/values/strings.xml` (`dc_*`) | Resources, not components; 23 strings, 7 currently unreferenced (see C-16) |
| `src/test`, `src/androidTest` | Tests are the evidence column, not registry rows |
| `:app` feature UI (`greeting/*`, `articles/*`, `config/*`, `showcase/trip/*`) | Consumers, not part of the DS (DEC-06) |
| Material3/Coil types behind the wrappers | Not DS API; Coil stays confined to `DcAsyncImage.kt` |

## 5. Duplicates and overlap

| # | Items | Relationship | Recommendation (PROPOSED) |
|---|---|---|---|
| O-01 | `DcSnackbarEffect(String?, SnackbarHostState, ...)` vs `DcSnackbarEffect(DcSnackbarMessage?, DcSnackbarHostState, ...)` | Legacy Material-typed vs DEC-08 typed overload | **Done in A4:** legacy deprecated with `ReplaceWith`; removed from the catalog. Migrating the two `:app` callers (`GreetingRoute`, `ArticlesRoute`) also means swapping Material `SnackbarHost` for `DcSnackbarHost` - outside this WP's write scope |
| O-02 | `DcDropdownField` untyped (`DcDropdownField.kt`) vs typed (`DcSelectionComponents.kt`) | Typed delegates to untyped; `:app` uses the untyped one (3 call sites in `AppConfigurationScreen`) | Keep both (DEC-08). Migrate `:app` to typed, then deprecate the untyped overload. Fix typed to render disabled options and key by value (C-06) |
| O-03 | `DcDropdownField` vs `DcSelectionSheet` vs `DcSegmentedButton` vs `DcRadioGroup` vs `DcChipGroup` vs `DcTabRow` | Overlapping single-choice UIs, differentiated only by KDoc ("2-5 options" / "long lists") | Add a decision table to the usage guide (done in section 8); no code change |
| O-04 | `DcAlertDialog` vs `DcInfoDialog` | Both wrap the private single-action dialog with the same icon colour; parameter names differ (`confirmLabel/onConfirm/onDismissRequest` vs `confirmText/onDismiss`); `DcErrorDialog` is a third variant | Decide: keep `DcInfoDialog`/`DcErrorDialog`, deprecate `DcAlertDialog` (or align names). Not changed because it needs an owner decision |
| O-05 | `DcButton(text)` vs `DcButton(slot)` vs `DcPrimaryActionButton` | Intentional layering; `DcPrimaryActionButton(destructive: Boolean)` is a second spelling of `variant = Destructive` + `fillMaxWidth()` | Keep (both overloads in use); document that `DcPrimaryActionButton` = full-width Primary/Destructive |
| O-06 | `DcOfflineBanner` vs `DcInlineMessage(Error/Warning)` | Same banner shape; `DcOfflineBanner` uses `errorContainer`, ignores the `DcColors.offline/onOffline` roles that exist solely for it, and is not a live region | Reimplement `DcOfflineBanner` on `DcInlineMessage`, or switch it to the `offline` roles; decision for the owner |
| O-07 | `DcLoadingState` (Inline) vs `DcCircularProgress` vs `DcSkeleton*` vs the raw `LinearProgressIndicator` in `DcContentState` | Four loading representations | Keep; usage guide says: whole region -> `DcLoadingState`/skeleton, inline progress -> `DcCircularProgress`/`DcLinearProgress` |
| O-08 | `SpacingTokens` vs `DcSpacing`/`LocalDcSpacing` | Same numeric scale twice; only `LocalDcSpacing` is scaled by `DcUiScale` (DEC-07). `SpacingTokens` is referenced only by one test assertion | Doc fix: DS-03 should point feature code at `LocalDcSpacing.current` (see UI-02 F-05). `DcSpacing.Compact` (preset) and `DcUiScale.Compact` (factor) share a name with different meaning |
| O-09 | `DcAvatar` size-enum vs `Dp` overload; `DcSkeleton` semantic vs `Dp` overload | DEC-08 explicitly keeps the `Dp` forms | Keep; deprecate only if a future DEC says so |
| O-10 | `formatBadgeCount`, `clampStep`, `filterOtpInput`, `toggleSelection` (public) vs `enforceMaxLength`, `selectAllState`, `shouldStackButtons` (internal) | Inconsistent visibility for the same kind of policy helper | Cannot narrow without breaking source compatibility; decide intent, then document as public utility or deprecate |
| O-11 | `DcExpandableSection` (plan: "Expandable card/accordion - Candidate") | Already exists as a section row, not a card | Registry status Existing; "card" variant remains Candidate |

## 6. Audit findings

Numbers are **measured offline** (WCAG relative-luminance script over `ColorTokens`) or **read from source**; nothing here ran on a device. IDs are stable for follow-up work.

### 6.1 MP:UI-02 - theme, light/dark/dynamic, `DcUiScale`, tokens

| ID | Finding | Evidence | Action |
|---|---|---|---|
| F-01 | Non-text contrast: `outline` is below 3:1 against its surface in both schemes (light `Grey300` on white 1.47:1; dark `Ink500` on `Ink800` 1.74:1). `outline` draws unfocused text-field borders, the secondary button border and unchecked control borders | `theme/DcTheme.kt:45`, `:81`; `DcButton.kt`, `DcInputComponents.kt` | PROPOSED: darken `outline` roles (token decision, visual change) |
| F-02 | Light `onSuccess = White` on `success = Green600` is 3.30:1 (< 4.5:1 text). Used for Success snackbar text and `DcBadge(Success)` count (11sp label) | `DcThemeDefaults.extendedColors`; `solidToneColors`; `DcSnackbarHost` | PROPOSED: `onSuccess = Grey900` gives 5.38:1, or darken `Green600`; no change made (palette decision, `DcThemeTokensTest` pins `success`) |
| F-03 | Light `onSurfaceVariant Grey500` on `surfaceVariant Grey100` is 4.35:1 (< 4.5:1). Affects `DcStatusChip(Neutral)` 12sp text and any caption on `surfaceVariant` | `DcListComponents.kt` | PROPOSED: darken `onSurfaceVariant` slightly, or use `onSurface` for Neutral chip text |
| F-04 | `DcColors.offline/onOffline` exist and are unit-tested but no component reads them | grep of `components/` | See O-06 |
| F-05 | `SpacingTokens` duplicates `DcSpacing` and is not scaled by `DcUiScale`; `architecture/07-design-system/README.md` DS-03 tells feature UI to use `SpacingTokens` while DEC-07 scales only `LocalDcSpacing`. `:app` correctly uses `LocalDcSpacing` (0 `SpacingTokens` refs) | `tokens/SpacingTokens.kt`; app refs | Doc fix (section D of the report); optionally `@Deprecated` `SpacingTokens` later |
| F-06 | 39 lines with `.dp` literals remain in `components/` (41 before A4; +9 in `catalog/`). Classified: component-intrinsic metrics (icon 24/20, avatar 32-80, skeleton 16/24/96, badge 8/16, status dot 6, page dot 8, progress 16/24/48 + stroke 2, OTP cell 44x56, stepper value 40, text-field min height 56, content widths 640/840, grid cell 160, button-row threshold 320, `DcButtonDefaults` 48/18/2) vs real token bypasses. A4 fixed the bypasses that had an existing token: `PaddingValues(0.dp)` -> `LocalDcSpacing.current.none` (`DcFormSection`), OTP unfocused border `1.dp` -> `StrokeTokens.Hairline`, badge preview `16.dp` -> `LocalDcSpacing.current.medium` | `DcFormComponents.kt`, `DcBadgeComponents.kt` | PROPOSED: a `DcDimens`/per-component `*Defaults` set for the intrinsic metrics (icon size, indicator sizes, field min height). Not done: new public API |
| F-07 | Dynamic colour: opt-in (`dynamicColor=false`, API >= 31). Extended `DcColors` stay the static brand status colours even with a dynamic `ColorScheme`; no preview or test exercises dynamic scheme | `DcThemeDefaults.colorScheme` | Document as intended; add a dynamic preview when previews are compiled |
| F-08 | `DcUiScale` scales spacing + typography only. Component-intrinsic sizes (F-06) do not scale, so at `Comfortable` text grows but icons/avatars/OTP cells do not. 48dp touch floor and system font scale are unchanged (by design). `DcTheme` recomputes `applyTo(spacing)` and `applyTo(typography)` (15 `TextStyle.copy`) on each `DcTheme` recomposition without `remember`; cost `UNKNOWN` (no benchmark) | `theme/DcUiScale.kt`, `theme/DcTheme.kt:111-116` | PROPOSED: `remember(uiScale, spacing/typography)`; add a test that `AccessibilityTokens.MinTouchTarget` is independent of scale |
| F-09 | Motion: only three durations, no easing policy and no reduced-motion hook. Skeleton shimmer and expandable animation ignore the system animator scale unless the caller sets `animated=false` | `MotionTokens.kt`, `DcOverlayComponents.kt` | PROPOSED: honour `Settings.Global.ANIMATOR_DURATION_SCALE` centrally |
| F-10 | Large-font previews: `DcFontScalePreviews` is used by exactly one preview (`DcTextField`); `DcWindowPreviews` by the catalog only | grep | Add font-scale previews to button/list/dialog when previews can be compiled |
| F-11 | `DcTypography` sets 11 of 15 roles (`display*/headline*` Large/Medium inherit Material defaults) - consistent with its KDoc | `theme/DcTypography.kt` | none |

### 6.2 MP:UI-03 - contract alignment (button, field, selection, list, menu, badge)

Baseline contract: plan section 5.3 (state hoisting, `enabled`, `modifier`, semantic `variant`, localized labels, touch floor).

| ID | Finding | Status |
|---|---|---|
| C-01 | `DcButton`: `loading` forces `enabled=false` (touch floor, no double submit - good) but assistive tech sees only "disabled", no busy announcement | Open (PROPOSED: `stateDescription`) |
| C-02 | Untyped `DcDropdownField` cannot disable options; options are plain strings | Open (O-02) |
| C-03 | Single-action dialogs have inconsistent parameter names | Open (O-04) |
| C-04 | `DcAvatar` size-enum overload declares `size` (required) after optional parameters, so callers must name it | Open (minor) |
| C-05 | `DcTextField`: both `isError` and `errorText`; trailing slot is `trailingContent`, leading is `leadingIcon: ImageVector` (mixed vocabulary); labels use raw M3 `Text`, not `DcText` | Open, API change needed |
| C-06 | Typed `DcDropdownField` maps back from a **label**: duplicate labels collide, `DcOption.enabled` is only checked on click, not rendered disabled | Open (PROPOSED: build the menu from `DcOption` directly) |
| C-07 | `DcSearchBar` clear action used a raw Material `TextButton` (40dp min height, below the DS-02 floor) | **Fixed in A4** (`DcButton` Text variant, honours `enabled`) |
| C-08 | `DcToggleRow(Switch)` exposed `Role.Checkbox` | **Fixed in A4** (`Role.Switch`; test added, NOT_RUN) |
| C-09 | `DcOtpField`: boxes are cleared from the a11y tree and the input has no label/description; `dc_digit_of_code` string is unused | Open |
| C-10 | Icon `contentDescription` equal to the visible label (read twice by TalkBack): `DcExtendedFab`, `DcNavigationItem.contentDescription` default, `DcDrawerItem.contentDescription` default | Open (PROPOSED: nullable description when a label is shown) |
| C-11 | `DcListRow.selected` is visual only (background); KDoc of `DcSelectionSheet` says rows have `selected` semantics - they do not | Open (doc/code drift) |
| C-12 | `DcAvatar` KDoc says "Circular" but clips with `MaterialTheme.shapes.large` | Open (doc drift; visual change if "fixed") |
| C-13 | `DcDatePickerDialog` uses `DatePicker`'s own labels; `dc_select_date` unused | Open |
| C-14 | `DcBadge` dot (`count == null`) is invisible to TalkBack unless `contentDescription` is passed | Open (documented in KDoc) |
| C-15 | `DcContentState` shows `DcOfflineBanner` for any stale-content error message, not only connectivity | Open (see H-03) |
| C-16 | 7 of 23 `dc_*` strings are unreferenced: `dc_close`, `dc_end_of_list`, `dc_select_all`, `dc_select_date`, `dc_select_time`, `dc_badge_count_overflow`, `dc_digit_of_code`; only an English default exists | Open (keep: apps may override; lint `UnusedResources` risk if enabled) |
| C-17 | Legacy `DcSnackbarEffect(String?, SnackbarHostState, ...)` | **Deprecated in A4** (DEC-08): `@Deprecated` + `ReplaceWith`; `:app` callers will show a deprecation warning (no `allWarningsAsErrors` in the repo) |

### 6.3 MP:UI-04 - feedback and overlays (gap list vs plan section 5 / UI-04)

Acceptance: "back press / dismiss / single event / lifecycle tests".

| ID | Gap | Evidence | Disposition |
|---|---|---|---|
| G-01 | No test of back-press or dismiss behaviour for any dialog/sheet; overlay visibility is caller-owned everywhere (matches DEC-05 and plan 5.3) | `src/androidTest` has none | PROPOSED instrumented tests (need device) |
| G-02 | `DcModalBottomSheet(isProcessing=true)` only suppresses the `onDismissRequest` callback; the Material `SheetState` is not guarded (`confirmValueChange`), so a swipe-down can hide the sheet while the caller still composes it. A safe fix needs a stable `confirmValueChange` (a new lambda per recomposition resets `rememberModalBottomSheetState`) | `DcOverlayComponents.kt:46-77` | PROPOSED; not changed (cannot be compiled/verified here) |
| G-03 | `DcAlertDialog/Info/Error` have no `isProcessing` guard (only `DcConfirmDialog` does) | `DcOverlayComponents.kt` | By design for single-action dialogs; document |
| G-04 | `DcSnackbarHostState.show` stores the message in one `current` field; two concurrent `show()` calls (queued by Material) make the first snackbar render with the second message's tone, and the first `finally` no longer clears | `DcMessageComponents.kt:170-197` | PROPOSED: carry tone in `SnackbarVisuals` instead of a side field |
| G-05 | Snackbar tone is conveyed by colour; text always present (OK). `DcInlineMessage` announces only Warning/Error politely - Success/Info are silent | `DcInlineMessage` | Intended; document |
| G-06 | No test for snackbar duration/queue/action path (`ActionPerformed -> onAction`) | tests | PROPOSED |
| G-07 | `DcLoadingState` exposes the same label twice (indicator `contentDescription` + visible text) | `DcStateComponents.kt` | Minor; PROPOSED |
| G-08 | Loading/progress: determinate lambdas, labels required - aligned with the plan | - | OK |

### 6.4 MP:UI-05 - screen states (`DcContentState`, empty/error/offline/loading/refreshing)

| ID | Gap | Disposition |
|---|---|---|
| H-01 | Branch policy tests covered 5 of 8 `(hasContent, isLoading, hasError)` inputs; **A4 added the remaining 3** so all 8 are pinned (NOT_RUN). No Compose-level test of what each branch renders | Compose test PROPOSED |
| H-02 | No distinct *Refreshing* state: refresh = stale content + `isLoading` -> linear progress. A4 gave that progress bar a `contentDescription` (`loadingLabel`). `DcPullToRefreshBox` is separate and never combined with `DcContentState` in a sample | PROPOSED sample |
| H-03 | No *Offline* branch: stale content + `errorMessage` -> `DcOfflineBanner` (red container, generic wording); empty + error -> `DcErrorState`. Full-screen offline = caller `errorContent` | PROPOSED (O-06) |
| H-04 | Retry: `onRetry` is honoured only in the empty-error state. The stale banner has no retry although `DcOfflineBanner` has an `action` slot | PROPOSED additive: pass a Text-variant `DcButton` as `action` when `onRetry != null` (changes what `ArticlesScreen` renders, so left for the owner) |
| H-05 | `errorContent: (String) -> Unit` - no typed error model; matches plan 5.3 (consumer maps errors) | OK |
| H-06 | Empty/Loading/Error branches use `fillMaxSize()`; behaviour inside scroll parents is `UNKNOWN` | Note |
| H-07 | New state component needed by an existing consumer? **No.** `:app` uses only `DcContentState` and `DcEmptyState`. Nothing was added. | Decision recorded |
| H-08 | Catalog shows one `DcContentState` case (stale + loading + error); full Loading/Empty/Error/Content permutation sample is missing | PROPOSED |

UI-06 (navigation UI / adaptive container): `DcNavigationSuite`, `DcNavigationDrawer*`, `DcContentContainer`, `DcAdaptiveGrid`, `DcScaffold` (safeDrawing) exist; no consumer demands more, so nothing planned (plan: "only if consumer requires"). UI-07 (expandable card, date range, timeline, data table) and UI-08 (log/metric widgets) stay `Candidate` - section 7.

## 7. Plan section 5.2 capability coverage

| Group | Baseline in plan | Registry reality |
|---|---|---|
| Theme | Reported existing | Existing: `DcTheme`, `DcColors`, `DcShapes`, `DcTypography`, `DcUiScale`, tokens; findings F-01..F-11 |
| Text | `DcText`, `DcTextLink` | Existing; no selection (`SelectionContainer`) or contrast helpers |
| Actions | Buttons, icon/tonal/text/destructive/loading | Existing (`DcButton` 5 variants, icon buttons, FABs, toggle) |
| Input | TextField, search, form, validation | Existing; validation is caller-owned (`isError`/`errorText`) |
| Selection | Checkbox/radio/switch/dropdown/toggle row | Existing (groups, segmented, chips, `DcToggleRow`) |
| Collections | List/list-row/menu | Existing; no item-key helper or paging boundary beyond `DcListAppendState` |
| Feedback | Badge, message, snackbar, loading, error | Existing; findings G-02, G-04 |
| Containers | Layout, screen, sheet, overlay | Existing |
| Navigation UI | Drawer/navigation suite | Existing (UI shell only) |
| Media | AsyncImage | Partial: no placeholder/error/crossfade parameters |
| Refresh | PullToRefreshBox | Existing (thin wrapper, no cancel/error UX) |
| Empty/Error | `DcContentState` | Existing; H-02..H-04 |
| Disclosure | Expandable card/accordion | Partial: `DcExpandableSection` (no card, no state restoration beyond caller) |
| Date/Time | Picker/range/calendar | Partial: `DcDateField` + `DcDatePickerDialog`; no range, no time |
| Status | Timeline, elapsed progress | Missing (Candidate) |
| Data | Table/filter/sort | Missing (Candidate) |
| Diagnostics | Log viewer, metrics | Missing (optional extension; must stay out of the minimum DS) |

## 8. Usage, preview and testing guide (MP:UI-10)

### 8.1 Find and use a component

1. Search section 3 by group, then open the file at the given line. Use `codegraph_explore "<Symbol>"` for callers before changing a signature.
2. Root the tree in `DcTheme` (once, at the app root; `:app` does this in `MainActivity.DcAppRoot`). Apply `DcUiScale` there only.
3. Read colours/type/shape from `MaterialTheme.*`, spacing from `LocalDcSpacing.current`, extra status colours from `LocalDcColors.current`. Never import `tokens.ColorTokens`/`DcTypography` in feature code (DS-01) and use no raw `.dp` or `RoundedCornerShape` there (DS-03).
4. State is hoisted: pass `value` + `onValueChange` (or `checked`/`expanded`/`selected`), `enabled`, `modifier` first optional. Overlays (dialogs, sheets, menus) are composed by the caller only while visible - the DS never owns modal visibility (DEC-05).
5. Errors arrive as already-mapped, localized strings; do not pass exception messages.
6. Labels default to `dc_*` resources and are overridable; pass localized strings for anything feature-specific.

Choosing between overlapping components:

| Need | Use |
|---|---|
| 2-5 short mutually exclusive options | `DcSegmentedButton` |
| Filter chips (multi/single) | `DcChipGroup` |
| Radio list with descriptions | `DcRadioGroup` |
| Many options in a form | `DcDropdownField` (typed) |
| Many options, phone | `DcSelectionSheet` |
| Row of 3-5 top-level destinations | `DcNavigationSuite` |
| Sibling views inside one screen | `DcTabRow` |
| Whole-region loading / empty / error | `DcContentState` (+ `DcLoadingState`, `DcEmptyState`, `DcErrorState`) |
| Transient message | `DcSnackbarHostState` + `DcSnackbarHost` + `DcSnackbarEffect(DcSnackbarMessage ...)` |
| Persistent message | `DcInlineMessage` |

Anti-patterns: raw Material `Button/TextButton/Text` in feature code; `Dp` literals; reading DAO/repository from a composable; storing dialog visibility in the DS; composing a destructive action without a confirmation (`DcConfirmDialog(destructive = true)`); passing `contentDescription` equal to the visible label; using the deprecated `DcSnackbarEffect(String?, SnackbarHostState, ...)`.

### 8.2 Add or change a component (checklist)

1. Confirm a **shared policy** (theme, spacing, accessibility, interaction); do not wrap Material only to rename it.
2. Put it in the matching `components/Dc*Components.kt` group; public composable takes `modifier`, hoisted state, `enabled`, a semantic `variant` enum when it has variants, and localized labels with `dc_*` defaults.
3. Interactive: `defaultMinSize` with `AccessibilityTokens.MinTouchTarget`; icon-only controls require a non-null `contentDescription`.
4. No new `.dp` literal that has a token; intrinsic metrics go in a `*Defaults` object (F-06).
5. Add a `@DcThemePreviews` private preview (and `@DcFontScalePreviews` if text can wrap), a **catalog** example in `DesignSystemCatalog`/`ExtendedCatalog` (DS-04), and a row in this registry (symbol, file:line, states, a11y, preview, tests, catalog, status).
6. Pure policy -> JVM unit test in `src/test`; semantics/touch/callback -> Compose test in `src/androidTest`.
7. Additive changes only: deprecate (`@Deprecated` + `ReplaceWith`) rather than remove; keep DEC-08 overloads.
8. Update `architecture/modules/ui-designsystem.md` / `07-design-system/components.md` when public API or boundary changes.

### 8.3 Previews

- `@DcThemePreviews` (Light + Dark), `@DcFontScalePreviews` (1.0/1.5/2.0x), `@DcWindowPreviews` (360/700/1200dp) in `preview/DcPreviews.kt`. Wrap every preview body in `DcTheme { ... }`.
- `DesignSystemCatalog()` is the executable documentation and is previewed with theme + window matrices; the debug app exposes it as a destination.
- Registry gaps today: font-scale previews exist for `DcTextField` only (F-10); no dynamic-colour preview (F-07).

### 8.4 Testing

| Layer | Where | What | Status |
|---|---|---|---|
| Pure policy | `src/test` | state-branch resolution (8/8 inputs), selection modes, stepper clamp, OTP filter, max length, date bounds, button-row stacking, snackbar rules, UI-scale math, token mappings | 30 tests, NOT_RUN |
| Semantics / callbacks | `src/androidTest` (`createComposeRule`) | loading button disabled + same height, touch floor, error semantics, toggle role, dropdown flow, menu, chip/radio groups, stepper, expandable, badge, inline message, snackbar one-shot | 21 tests, NOT_RUN (needs emulator) |
| Visual | none | no screenshot/golden tests (DEC-08 left undecided; D-03 proposal in `reports/wp/A4.md`) | missing |
| Performance | none | no recomposition/macrobenchmark (UI-09) | missing |

Commands (NOT_RUN here): `./gradlew :ui:designsystem:testDebugUnitTest`, `./gradlew :ui:designsystem:compileDebugKotlin`, `./gradlew :ui:designsystem:connectedDebugAndroidTest`. Do not run accessibility-tree test tooling during CPU measurements (plan 5.5).

## 9. Change log

- 2026-10-27 - A4 (R4.1-R4.5): first registry generated from source; audit findings F/C/G/H recorded; small fixes: legacy `DcSnackbarEffect` deprecated, `DcSearchBar` clear button on `DcButton`, `DcToggleRow` Switch role, `DcContentState` progress label, token-bypass cleanups (F-06), 3 catalog examples, 3 branch-policy tests, 2 instrumented tests (not executed). Report: `reports/wp/A4.md`.
