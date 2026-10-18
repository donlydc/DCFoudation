# Component roadmap (đề xuất)

[Back to design system](README.md)

> **Trạng thái: W0–W3 đã implement (2026-10-18); W4 và một phần W5 còn hoãn — xem mục 8.** Phần còn lại của tài liệu giữ làm thiết kế gốc; Theo INV-10, mọi API trong tài liệu này là thiết kế dự kiến. Nguồn sự thật về cái *đã có* là [`components.md`](components.md) và source trong `ui/designsystem/.../components/`. Khi một widget được implement, chuyển mô tả của nó sang `components.md` và đánh dấu ✅ ở đây.

## 1. Mục tiêu và phạm vi

Bổ sung bộ widget cơ bản mà hầu hết ứng dụng Android cần, theo hướng business-neutral và tái sử dụng, mà không phá vỡ:

```text
Primitive Tokens → Semantic Theme (DcTheme) → Reusable Dc* Components → Feature UI
```

Nguyên tắc chọn widget (kế thừa `components.md` và DEC-06):

1. Chỉ thêm khi widget **sở hữu một chính sách dùng chung thật** (theme, spacing, accessibility, interaction, adaptive). Không bọc Material chỉ để đổi tên.
2. Promote primitive ổn định, **không** promote màn hình mang hình dạng nghiệp vụ.
3. API **additive**: không đổi chữ ký public hiện có; thay đổi lớn đi qua overload mới + `@Deprecated`.
4. Không lộ kiểu của thư viện triển khai (Material adaptive, Coil, Material `SnackbarData`…) qua API public khi có thể tránh.

### Vị trí module

Không có module `:ui:components` riêng; mọi component đang nằm ở `:ui:designsystem/components/`. **Đề xuất giữ nguyên** trong roadmap này: tách module chỉ có giá trị khi (a) có consumer chỉ cần token/theme mà không cần component, hoặc (b) component kéo dependency nặng (ví dụ material3-adaptive-layout, date-time). Nếu xảy ra, ghi một DEC mới trước khi tách.

## 2. Khảo sát hiện trạng (đã có)

| Nhóm | Đã có | Ghi chú/giới hạn quan sát được |
|---|---|---|
| Theme | `DcTheme` (colorScheme/typography/shapes/`DcColors`/`DcSpacing`, `dynamicColor=false`), `DcUiScale` (0.9/1.0/1.1), `DcColors` success/warning/info/offline | Đủ làm nền; chưa có token motion được dùng |
| Text | `DcText`, `DcTextLink` | — |
| Actions | `DcButton` (Primary/Secondary/Tertiary/Destructive/Text, loading, icon), `DcPrimaryActionButton`, `DcIcon`, `DcIconButton` (Standard/Tonal/SoftDestructive), `DcFab`, `DcExtendedFab` | Chưa có menu/overflow dù `DcTopAppBar` khuyến nghị "đặt action thừa vào overflow"; chưa có segmented/toggle |
| Inputs | `DcTextField`, `DcPasswordTextField`, `DcSearchBar`, `DcDropdownField` | `DcDropdownField` chỉ nhận `List<String>` (so khớp theo label); chưa có counter/maxLength, date/time, slider, OTP |
| Selection | `DcToggleRow` (Checkbox/Switch/Radio), `DcChip` | Chưa có group semantics (`selectableGroup`) cho radio/chip; chưa có tri-state |
| Feedback/state | `DcContentState`, `DcLoadingState`, `DcEmptyState`, `DcErrorState`, `DcOfflineBanner`, `DcSkeleton`, `DcSnackbarEffect`, `DcPullToRefreshBox`, `DcStatusChip` | Banner chỉ có tone offline; chưa có progress determinate; snackbar không có tone/action model |
| Overlays | `DcModalBottomSheet`, `DcConfirmDialog`, `DcAlertDialog`, `DcInfoDialog`, `DcErrorDialog` | Chưa có menu, tooltip, picker dialog, action/selection sheet |
| Lists/cards | `DcCard` (Outlined/Elevated), `DcListRow` (+ slot overload), `DcKeyValueRow`, `DcDivider`, `DcSectionHeader` | Chưa có append/paging footer, expandable section, swipe action |
| Navigation/app bar | `DcTopAppBar` (Small/Centered/Large), `DcNavigationSuite`, `DcNavigationDrawerContent/Header`, `DcBottomActionBar` | Chưa có tab row, page indicator |
| Media | `DcAsyncImage` (Coil cô lập), `DcAvatar` | `DcAvatar.size: Dp = 40.dp` là kích thước thô; chưa có badge |
| Layout | `DcScaffold` (insets/IME) | Chưa có giới hạn độ rộng nội dung cho màn rộng; list-detail vẫn caller-owned |
| Verification | `DesignSystemCatalog` (14 nhóm), `DcThemePreviews`/`DcFontScalePreviews`/`DcWindowPreviews`, unit test `DcContentStatePolicyTest`, `DcNavigationSuiteTest`, `DcThemeTokensTest`, `DcUiScaleTest`, androidTest `DcComponentsTest` | Chưa có screenshot test |

### Nợ kỹ thuật cần xử lý trước (ảnh hưởng mọi widget mới)

- **D1 — Chuỗi mặc định tiếng Anh trong DS:** khoảng 8 tham số mặc định (`"Cancel"`, `"Retry"`, `"Loading"`, `"Back"`, `"Clear"`…) nằm trong `components/`. Widget mới sẽ nhân bản vấn đề nếu không có chính sách.
- **D2 — Lựa chọn dựa trên `String`:** `DcDropdownField` dùng label làm khóa; các widget selection mới cần một model lựa chọn có kiểu.
- **D3 — Kích thước thô trong API:** `DcAvatar(size = 40.dp)`, `DcSkeleton(height = 16.dp)` để feature truyền `.dp` tự do, trái tinh thần DS-03.

## 3. Quy ước API chung cho mọi widget mới

Áp dụng thống nhất để widget mới "đọc giống" các `Dc*` hiện có:

| Chủ đề | Quy ước |
|---|---|
| Thứ tự tham số | dữ liệu bắt buộc → callback bắt buộc → `modifier` → tùy chọn → slot `content` cuối |
| State hoisting | Controlled: widget nhận `value`/`selected`/`expanded` + `onXChange`. Không giữ state nghiệp vụ nội bộ; chỉ state thuần UI (scroll, animation) được `remember` bên trong |
| Hiển thị overlay | Caller sở hữu visibility (`if (show) DcXDialog(...)`) — giữ đúng DEC-05 |
| Chuỗi | Mọi text hiển thị và `contentDescription` do caller truyền, hoặc mặc định lấy từ `stringResource(R.string.dc_*)` của `:ui:designsystem` (xem Phase W0) — không hard-code chuỗi literal |
| Tone | Dùng lại `DcStatusTone` (Neutral/Info/Success/Warning/Error) cho banner, snackbar, badge, progress — không tạo enum tone mới |
| Kích thước | Enum có nghĩa (`DcAvatarSize.Small/Medium/Large`, `DcBadgeSize`) thay vì `Dp` tự do; giá trị nằm trong `XxxDefaults` lấy từ token |
| Defaults | Mỗi widget có `object DcXxxDefaults` (giống `DcButtonDefaults`) đọc từ `MaterialTheme`/`LocalDcSpacing`/`LocalDcColors` |
| Accessibility | Touch target ≥ `AccessibilityTokens.MinTouchTarget` cho phần tử tương tác (DS-02); `Role`, `stateDescription`, `selectableGroup`, `heading()`, `liveRegion` khi phù hợp; không truyền tải trạng thái chỉ bằng màu |
| Adaptive | Widget không tự đọc window size trừ khi chính sách adaptive là lý do tồn tại của nó (giống `DcNavigationSuite`) |
| Kiểu thư viện | Không lộ `SnackbarData`, `DatePickerState`, `WindowAdaptiveInfo`… qua tham số public; bọc bằng model `Dc*` |

### Model dùng chung đề xuất

```kotlin
/** Một lựa chọn có kiểu; [value] là khóa so sánh, [label] là text đã bản địa hóa. */
@Immutable
data class DcOption<out T>(
    val value: T,
    val label: String,
    val enabled: Boolean = true,
    val icon: ImageVector? = null,
    val supportingText: String? = null
)

/** Mục menu/action sheet. [destructive] chọn màu error, không đổi hành vi. */
@Immutable
data class DcMenuItem(
    val id: String,
    val label: String,
    val icon: ImageVector? = null,
    val enabled: Boolean = true,
    val destructive: Boolean = false
)

/** Tin nhắn snackbar có kiểu, tách khỏi Material SnackbarData. */
@Immutable
data class DcSnackbarMessage(
    val text: String,
    val tone: DcStatusTone = DcStatusTone.Neutral,
    val actionLabel: String? = null,
    val duration: DcMessageDuration = DcMessageDuration.Short
)
enum class DcMessageDuration { Short, Long, Indefinite }
```

## 4. Đề xuất widget theo nhóm

Ký hiệu ưu tiên: **P0** = gần như mọi app cần, làm trước; **P1** = phổ biến; **P2** = có điều kiện/khi có use case thật.

### 4.1 Actions / buttons

| Widget | Ưu tiên | API dự kiến | Biến thể | Token semantic | A11y / adaptive |
|---|---|---|---|---|---|
| `DcSegmentedButton` | P0 | `fun <T> DcSegmentedButton(options: List<DcOption<T>>, selected: T, onSelect: (T) -> Unit, modifier, enabled = true)` + `DcMultiSegmentedButton(options, selected: Set<T>, onSelectionChange: (Set<T>) -> Unit, …)` | single / multi; icon tùy chọn | `secondaryContainer`/`onSecondaryContainer`, `outline`, `shapes.small`, `labelLarge` | `selectableGroup`, `Role.RadioButton`/`Checkbox`, ≥48dp; ≤5 segment, quá thì dùng `DcDropdownField` |
| `DcIconToggleButton` | P1 | `DcIconToggleButton(checked, onCheckedChange, checkedIcon, uncheckedIcon, contentDescription, modifier, enabled, variant = DcIconButtonVariant.Standard)` | Standard/Tonal (dùng lại enum) | như `DcIconButton` + `primary` khi checked | `Role.Switch`, `stateDescription` do caller truyền (ví dụ "Đã lưu") |
| `DcButtonRow` (layout hành động đôi) | P1 | `DcButtonRow(primary: @Composable RowScope.() -> Unit, secondary: (@Composable RowScope.() -> Unit)? = null, modifier, stackBelowWidth = DcButtonRowDefaults.StackThreshold)` | ngang / tự xếp dọc | `LocalDcSpacing.component` | Tự chuyển dọc khi chiều rộng hẹp hoặc font scale lớn; primary luôn cuối theo hướng đọc (RTL-safe) |

### 4.2 Inputs / forms

| Widget | Ưu tiên | API dự kiến | Ghi chú thiết kế |
|---|---|---|---|
| Mở rộng `DcTextField` | P0 | Thêm tham số additive: `maxLength: Int? = null`, `showCounter: Boolean = maxLength != null`, `counterContentDescription: ((Int, Int) -> String)? = null` | Counter đọc qua `supportingText` slot; không cắt input âm thầm — chặn ở `onValueChange` là policy của DS (có test) |
| `DcTextArea` | P1 | `DcTextArea(label, value, onValueChange, modifier, minLines = 3, maxLines = 8, maxLength, supportingText, errorText, enabled)` | Wrapper multi-line với `singleLine=false` cố định + IME `Default` (không `Done`) |
| `DcDropdownField` typed | P0 | Overload: `fun <T> DcDropdownField(label, options: List<DcOption<T>>, selected: T?, onSelect: (T) -> Unit, expanded, onExpandedChange, modifier, enabled, placeholder: String? = null, errorText: String? = null)`; overload `String` cũ giữ nguyên rồi deprecate | Giải D2 |
| `DcDateField` + `DcDatePickerDialog` | P1 | `DcDateField(label, value: LocalDate?, onValueChange, formattedValue: String, modifier, enabled, errorText, minDate, maxDate)`; dialog: `DcDatePickerDialog(initialDate, onConfirm: (LocalDate) -> Unit, onDismissRequest, confirmLabel, dismissLabel, selectableDates)` | Feature sở hữu format/locale (`formattedValue`), DS không định dạng ngày (theo common-screen-patterns). Không lộ `DatePickerState`. |
| `DcTimeField` + `DcTimePickerDialog` | P2 | Tương tự với `LocalTime`, `is24Hour` do caller truyền | Chỉ khi có use case đặt lịch |
| `DcSlider` / `DcRangeSlider` | P1 | `DcSlider(value: Float, onValueChange, modifier, valueRange = 0f..1f, steps = 0, label: String, valueText: (Float) -> String, enabled)` | `valueText` cấp `stateDescription` cho TalkBack; label bắt buộc |
| `DcStepper` | P1 | `DcStepper(value: Int, onValueChange: (Int) -> Unit, range: IntRange, label: String, increaseLabel: String, decreaseLabel: String, modifier, enabled)` | Hai `DcIconButton` + text; clamp vào `range` là policy thuần, unit-test được |
| `DcOtpField` | P1 | `DcOtpField(value: String, onValueChange, length = 6, modifier, isError, errorText, onComplete: (String) -> Unit = {})` | Một `BasicTextField` ẩn + ô hiển thị; chỉ nhận chữ số; `KeyboardType.NumberPassword`; `AutofillType.SmsOtpCode`; TalkBack đọc như một field |
| `DcFormSection` | P2 | `DcFormSection(title: String?, modifier, supportingText, content: @Composable ColumnScope.() -> Unit)` | Chỉ là spacing/heading chuẩn giữa các field; không chứa logic validate (validate thuộc ViewModel) |

### 4.3 Selection

| Widget | Ưu tiên | API dự kiến | A11y |
|---|---|---|---|
| `DcRadioGroup` | P0 | `fun <T> DcRadioGroup(options: List<DcOption<T>>, selected: T?, onSelect: (T) -> Unit, modifier, label: String? = null)` dựng trên `DcToggleRow(control = Radio)` | `Modifier.selectableGroup()`, label là heading của group |
| `DcCheckboxGroup` | P1 | `fun <T> DcCheckboxGroup(options, selected: Set<T>, onSelectionChange: (Set<T>) -> Unit, modifier, label, selectAllLabel: String? = null)` | Mục "chọn tất cả" dùng `TriStateCheckbox` (`ToggleableState.Indeterminate`) |
| `DcChipGroup` | P0 | `fun <T> DcChipGroup(options, selected: Set<T>, onSelectionChange, modifier, selectionMode = DcSelectionMode.Single, layout = DcChipGroupLayout.Wrap)`; `enum class DcSelectionMode { Single, SingleOptional, Multiple }`; `enum class DcChipGroupLayout { Wrap, Scroll }` | `FlowRow` (Wrap) hoặc `LazyRow` (Scroll) dựng trên `DcChip`; policy toggle (single/multi) là hàm thuần có unit test |

### 4.4 Feedback / state

| Widget | Ưu tiên | API dự kiến | Ghi chú |
|---|---|---|---|
| `DcInlineMessage` (banner có tone) | P0 | `DcInlineMessage(message: String, tone: DcStatusTone, modifier, title: String? = null, icon: ImageVector? = DcInlineMessageDefaults.icon(tone), action: (@Composable () -> Unit)? = null, onDismiss: (() -> Unit)? = null, dismissLabel: String? = null)` | Tổng quát hóa `DcOfflineBanner` (giữ `DcOfflineBanner` như preset). Màu từ `DcColors.*Container`/`colorScheme.errorContainer`; icon + text, không chỉ màu; Error/Warning dùng `liveRegion = Polite` |
| `DcProgressIndicator` | P0 | `DcLinearProgress(progress: (() -> Float)?, modifier, label: String)` (null = indeterminate), `DcCircularProgress(progress, modifier, size = DcProgressSize.Medium, label)` | Lambda progress tránh recomposition; `progressBarRangeInfo` + label cho TalkBack. `DcLoadingState` tái dùng bên trong |
| `DcSnackbarHost` | P0 | `DcSnackbarHost(state: DcSnackbarHostState, modifier)`; `DcSnackbarHostState.show(message: DcSnackbarMessage): DcSnackbarResult`; nâng cấp `DcSnackbarEffect(message: DcSnackbarMessage?, state, onMessageShown, onAction = {})` (overload additive) | Tone ánh xạ màu `DcColors`; `Indefinite` bắt buộc có action (assert debug); `DcScaffold.snackbarHost` nhận nó. Không lộ `SnackbarData` |
| `DcSkeleton` presets | P1 | `DcSkeletonListRow(modifier, lines = 2, showLeading = true)`, `DcSkeletonCard`; thay `height: Dp` bằng `DcSkeletonSize` (giải D3, overload cũ deprecate) | Tổ hợp `DcSkeleton`; `clearAndSetSemantics` + một label "Loading" cho cả khối |
| `DcBadge` (xem 4.8) | P0 | — | — |

### 4.5 Overlays (dialog / sheet / menu / tooltip)

| Widget | Ưu tiên | API dự kiến | Ghi chú |
|---|---|---|---|
| `DcDropdownMenu` | P0 | `DcDropdownMenu(expanded, onDismissRequest, items: List<DcMenuItem>, onItemClick: (DcMenuItem) -> Unit, modifier)` | Mục destructive dùng `colorScheme.error`; mỗi mục ≥48dp |
| `DcOverflowMenu` | P0 | `DcOverflowMenu(items: List<DcMenuItem>, onItemClick, contentDescription: String, modifier)` — tự giữ `expanded` (state thuần UI) | Đặt trong `DcTopAppBar.actions`; hoàn tất khuyến nghị "≤3 action, còn lại overflow" |
| `DcActionSheet` | P1 | `DcActionSheet(items: List<DcMenuItem>, onItemClick, onDismissRequest, modifier, title: String? = null)` dựng trên `DcModalBottomSheet` + `DcListRow` | Thay menu trên phone khi danh sách dài/cần mô tả |
| `DcSelectionSheet` | P1 | `fun <T> DcSelectionSheet(title, options: List<DcOption<T>>, selected: T?, onSelect, onDismissRequest, modifier)` | Thay `DcDropdownField` khi >7 option hoặc cần tìm kiếm (biến thể `searchQuery`/`onSearchQueryChange` P2) |
| `DcTooltip` | P1 | `DcTooltip(text: String, modifier, content: @Composable () -> Unit)` (plain tooltip) ; thêm `tooltip: String? = null` additive vào `DcIconButton` | Long-press/hover; không thay `contentDescription` |
| `DcFullScreenDialog` | P2 | `DcFullScreenDialog(title, onDismissRequest, confirmLabel, onConfirm, modifier, isProcessing, content)` | Chỉ khi có form chỉnh sửa phức tạp trên phone; trên expanded nên là dialog thường — adaptive policy chính là lý do tồn tại |

### 4.6 Lists / cards

| Widget | Ưu tiên | API dự kiến | Ghi chú |
|---|---|---|---|
| `DcListFooterState` (append/paging) | P0 | `DcListAppendState(isLoading: Boolean, errorMessage: String?, onRetry: (() -> Unit)?, modifier, endReachedText: String? = null, retryLabel: String)` | Dùng trong `item {}` cuối `LazyColumn`; trung lập với Paging3 (feature ánh xạ `LoadState`) — đúng cách `DcContentState` đang làm |
| `DcExpandableSection` | P1 | `DcExpandableSection(title, expanded, onExpandedChange, modifier, supportingText, content)` | `stateDescription` Expanded/Collapsed do caller truyền; animation dùng `MotionTokens.DURATION_MEDIUM_MS` → lần đầu motion được dùng, cập nhật `motion.md` |
| `DcSwipeActionRow` | P2 | `DcSwipeActionRow(onSwipe: (DcSwipeDirection) -> Unit, startAction: DcMenuItem?, endAction: DcMenuItem?, modifier, content)` | **Bắt buộc** cung cấp `customActions` semantics tương đương cho TalkBack/bàn phím; chỉ làm khi có use case |
| `DcSelectableCard` | P2 | `DcSelectableCard(selected, onClick, modifier, content)` | Chỉ nếu `DcCard(onClick)` + `selected` không đủ; ưu tiên thêm `selected` additive vào `DcCard` |

### 4.7 Navigation / app bar

| Widget | Ưu tiên | API dự kiến | Ghi chú |
|---|---|---|---|
| `DcTabRow` | P0 | `fun <T> DcTabRow(tabs: List<DcOption<T>>, selected: T, onSelect: (T) -> Unit, modifier, style = DcTabStyle.Primary, scrollable: Boolean = tabs.size > DcTabRowDefaults.FixedTabLimit)`; `enum class DcTabStyle { Primary, Secondary }` | Badge tùy chọn theo tab qua `badgeCount: (T) -> Int?`; `Role.Tab`; không sở hữu pager (feature tự ghép `HorizontalPager`) |
| `DcPageIndicator` | P1 | `DcPageIndicator(pageCount: Int, currentPage: Int, modifier, contentDescription: (Int, Int) -> String)` | Không tương tác; onboarding/carousel; nhận số trang thay vì `PagerState` |
| `DcTopAppBar` mở rộng | P1 | Additive: `subtitle: String? = null`, `navigationIcon: DcNavIcon = Back/Close/None` | `Close` cho luồng modal/full-screen |
| `DcSearchTopBar` | P2 | Chỉ khi có search mở rộng trên app bar; hiện `DcSearchBar` đủ | — |

### 4.8 Media / avatar / badge

| Widget | Ưu tiên | API dự kiến | Ghi chú |
|---|---|---|---|
| `DcBadge` / `DcBadgedBox` | P0 | `DcBadge(count: Int? = null, modifier, maxCount = 99, tone = DcStatusTone.Error)` (null = dot); `DcBadgedBox(badge: @Composable () -> Unit, modifier, content)`; thêm `badgeCount: Int?` additive vào `DcNavigationItem`/`DcDrawerItem` | Format "99+" là hàm thuần có test; semantics đọc số qua `contentDescription` do caller (`badgeLabel: (Int) -> String`) |
| `DcAvatar` sizes | P0 (W0) | Overload `DcAvatar(name, modifier, imageModel, contentDescription, size: DcAvatarSize = Medium)`; `enum class DcAvatarSize { Small /*32*/, Medium /*40*/, Large /*56*/, XLarge /*80*/ }` | Giải D3; fallback chữ cái đầu đã có |
| `DcAvatarGroup` | P2 | `DcAvatarGroup(avatars: List<DcAvatarModel>, maxVisible = 3, overflowLabel: (Int) -> String, size)` | Chỉ khi có màn cộng tác/thành viên |
| `DcAsyncImage` slot | P1 | Additive: `placeholder: DcImagePlaceholder = Skeleton`, `error: (@Composable () -> Unit)? = null`, `shape: Shape? = null` | Vẫn cô lập Coil |

### 4.9 Layout scaffolds / adaptive

| Widget | Ưu tiên | API dự kiến | Ghi chú |
|---|---|---|---|
| `DcContentContainer` | P0 | `DcContentContainer(modifier, maxWidth = DcContentWidth.Readable, horizontalPadding = true, content)`; `enum class DcContentWidth { Readable /*~640dp*/, Wide /*~840dp*/, Full }` | Chính sách adaptive dùng chung thật: trên expanded, form/detail không kéo giãn toàn màn. Không đọc window class — chỉ `widthIn(max)` + căn giữa |
| `DcAdaptiveGrid` | P1 | `DcAdaptiveGrid(minCellWidth = DcGridDefaults.MinCell, modifier, contentPadding, content: LazyGridScope.() -> Unit)` | `GridCells.Adaptive`; spacing từ `LocalDcSpacing` |
| `DcListDetailScaffold` | P2 (có điều kiện) | `DcListDetailScaffold(selectedId: String?, onBack, list: @Composable () -> Unit, detail: @Composable (String) -> Unit, detailPlaceholder)` | Cần dependency `material3-adaptive-layout` → **cần DEC mới** và một feature thật; adaptive-accessibility.md hiện quy định caller-owned cho tới khi đó |
| `DcScaffold` mở rộng | P1 | Additive: `floatingActionButton: @Composable () -> Unit = {}`, `contentWindowInsets` | FAB hiện phải tự đặt |

## 5. Plan theo phase

Mỗi phase là một PR/commit nhỏ, có thể review độc lập. Thứ tự dựa trên (tần suất dùng × số chỗ đang thiếu) và phụ thuộc giữa các phase.

### Phase W0 — Nền tảng API (bắt buộc trước)

**Việc:**
1. Chính sách chuỗi: thêm `ui/designsystem/src/main/res/values/strings.xml` với `dc_*` (cancel, retry, loading, back, clear, close, more_options, show/hide password…); đổi default literal sang `stringResource(...)` — **không** đổi chữ ký (giải D1).
2. Thêm `DcOption<T>`, `DcMenuItem`, `DcSelectionMode` vào `components/model/` (giải D2 cho các phase sau).
3. `DcAvatarSize`, `DcSkeletonSize` + overload; deprecate overload `Dp` (giải D3).
4. Viết checklist "thêm component mới" vào `components.md` (mục 6 dưới đây).

**Hoàn thành khi:** compile `:ui:designsystem` + `:app`; không còn literal chuỗi hiển thị trong `components/` (`grep '= "'` chỉ còn id/không hiển thị); catalog chạy; một ADR `DEC-08 — DS string defaults via module resources`.

### Phase W1 — P0 phổ biến nhất

`DcDropdownMenu`, `DcOverflowMenu`, `DcTabRow`, `DcBadge`/`DcBadgedBox` (+ `badgeCount` cho navigation item), `DcInlineMessage`, `DcLinearProgress`/`DcCircularProgress`, `DcSnackbarHost` + `DcSnackbarMessage`, `DcSegmentedButton`, `DcRadioGroup`, `DcChipGroup`, `DcDropdownField` typed, `DcContentContainer`, `DcListAppendState`.

**Hoàn thành khi:** mỗi widget đạt DoD mục 6; `TripHistoryShowcaseScreen` hoặc `articles` dùng ít nhất `DcOverflowMenu` + `DcChipGroup` + `DcListAppendState` để chứng minh API trên use case thật; `components.md` cập nhật.

### Phase W2 — Forms (P1)

Mở rộng `DcTextField` (maxLength/counter), `DcTextArea`, `DcDateField` + `DcDatePickerDialog`, `DcSlider`/`DcRangeSlider`, `DcStepper`, `DcOtpField`, `DcCheckboxGroup`, `DcFormSection`.

**Điều kiện tiên quyết:** đã xác minh `isCoreLibraryDesugaringEnabled = true` trong `build-logic/ConventionHelpers.kt`, nên API dùng `java.time` được; kiểm tra lại module `:ui:designsystem` có dependency `coreLibraryDesugaring` khi implement.
**Hoàn thành khi:** có một form mẫu trong showcase (debug) dùng ≥5 widget; test IME action/focus traversal; font scale 200% không cắt nhãn.

### Phase W3 — Overlays, list nâng cao, motion (P1)

`DcActionSheet`, `DcSelectionSheet`, `DcTooltip` (+ `tooltip` cho `DcIconButton`), `DcExpandableSection`, `DcSkeletonListRow`/`DcSkeletonCard`, `DcPageIndicator`, `DcIconToggleButton`, `DcButtonRow`, `DcAsyncImage` slots, `DcTopAppBar` subtitle/close, `DcScaffold` FAB, `DcAdaptiveGrid`.

**Hoàn thành khi:** `motion.md` có chính sách easing/duration đầu tiên (vì `DcExpandableSection` là animation đầu tiên dùng `MotionTokens`), tôn trọng "Remove animations" của hệ thống; DoD mục 6.

### Phase W4 — Có điều kiện (P2, chỉ khi có use case thật)

`DcListDetailScaffold` (DEC mới + dependency adaptive-layout), `DcSwipeActionRow`, `DcFullScreenDialog`, `DcTimeField`, `DcAvatarGroup`, `DcSelectableCard`, `DcSearchTopBar`. Mỗi mục cần một feature/consumer nêu rõ trước khi bắt đầu (DEC-04: không làm wrapper đầu cơ).

### Phase W5 — Củng cố kiểm chứng (song song từ W1)

- Quyết định screenshot test (Roborazzi chạy JVM hoặc Paparazzi) qua DEC trong `17-testing.md`; nếu chấp nhận, snapshot catalog theo ma trận theme × font scale × width.
- Tách `DesignSystemCatalog` theo file mỗi nhóm khi vượt ~20 nhóm, giữ entry point chung.
- (Tùy chọn) lint/detekt rule cấm import `androidx.compose.material3.Button|Text|TextField|…` ngoài `:ui:designsystem`.

### Tóm tắt thứ tự

```text
W0 nền API ─► W1 P0 (menu, tabs, badge, banner, progress, snackbar, selection, container, append)
                 ├─► W2 forms
                 └─► W3 overlays/list/motion
                          └─► W4 có điều kiện
W5 kiểm chứng: chạy song song từ W1
```

## 6. Definition of done cho mỗi widget

1. **API:** theo quy ước mục 3; KDoc nêu policy mà widget sở hữu; không lộ kiểu thư viện; mọi thay đổi với API cũ là additive.
2. **Token:** chỉ đọc `MaterialTheme.*`, `LocalDcColors`, `LocalDcSpacing`, `XxxDefaults`; không hex/`.dp` literal ngoài `Defaults`/tokens.
3. **Accessibility:** touch target ≥48dp cho phần tử tương tác; role/state/group semantics đúng; trạng thái không chỉ bằng màu; kiểm TalkBack thủ công một lần; font scale 200% không vỡ layout; RTL preview.
4. **Preview/catalog (DS-04):** có mục trong `DesignSystemCatalog` hiển thị mọi biến thể + enabled/disabled/error; preview riêng dùng `@DcThemePreviews`, và `@DcFontScalePreviews`/`@DcWindowPreviews` khi widget có text dài hoặc hành vi adaptive.
5. **Test:**
   - Unit test (`src/test`) cho mọi policy thuần: format badge "99+", toggle selection single/multi/optional, clamp stepper, lọc ký tự OTP, maxLength, quyết định stack của `DcButtonRow`.
   - Compose test (`src/androidTest`, mở rộng `DcComponentsTest`): semantics (`Role`, `stateDescription`, `selectableGroup`), callback được gọi đúng, `assertTouchHeightIsAtLeast(48.dp)`, disabled không nhận click.
6. **Tài liệu:** chuyển widget từ file này sang `components.md`; cập nhật `adaptive-accessibility.md`/`motion.md` nếu thêm policy; DEC nếu thêm dependency hoặc quyết định bền vững.
7. **Kiểm tra:** `./gradlew :ui:designsystem:testDebugUnitTest :ui:designsystem:assembleDebug :app:compileDebugKotlin`; `graphify update .` sau phase lớn.

## 7. Câu hỏi mở cần quyết định

| # | Câu hỏi | Đề xuất mặc định |
|---|---|---|
| Q1 | Chuỗi mặc định: resource trong `:ui:designsystem` hay bắt caller truyền mọi label? | Resource `dc_*` làm default, caller vẫn override được |
| Q2 | Ngày giờ dùng `java.time` (cần desugaring nếu minSdk < 26) hay kiểu nguyên thủy? | `java.time` — desugaring đã bật trong convention plugin |
| Q3 | Có thêm `material3-adaptive-layout` cho list-detail không? | Hoãn tới khi có feature list-detail thật (W4) |
| Q4 | Công cụ screenshot test | Roborazzi (JVM, không cần emulator) — cần DEC |
| Q5 | Có tách `:ui:components` không? | Không, cho tới khi xuất hiện consumer chỉ cần theme hoặc dependency nặng |

## Related documents

- [`components.md`](components.md) — nguồn sự thật cho component đã có
- [`common-screen-patterns.md`](common-screen-patterns.md)
- [`adaptive-accessibility.md`](adaptive-accessibility.md), [`motion.md`](motion.md), [`previews-and-catalog.md`](previews-and-catalog.md)
- [`../23-decision-log.md`](../23-decision-log.md) — DEC-04, DEC-05, DEC-06

## 8. Trạng thái triển khai (2026-10-18)

| Phase | Trạng thái | Ghi chú |
|---|---|---|
| W0 | ✅ | `strings.xml` `dc_*`; `DcOption`, `DcMenuItem`, `DcSelectionMode`, `toggleSelection`; `DcAvatarSize`, `DcSkeletonSize`. Overload `Dp` cũ **chưa** `@Deprecated` để tránh warning cho caller hiện có |
| W1 | ✅ | Menu/overflow, tab, badge (+`DcNavigationItem.badgeCount`), inline message, progress, snackbar host, segmented/radio/chip group, typed dropdown, content container, append state |
| W2 | ✅ một phần | `DcTextField` maxLength/counter, text area, date field/picker, slider/range, stepper, OTP, checkbox group, form section. **Chưa có** `DcTimeField` (P2) và autofill `SmsOtpCode` cho OTP |
| W3 | ✅ một phần | Action/selection sheet, tooltip, expandable, skeleton presets, page indicator, icon toggle, button row, adaptive grid, `DcScaffold` FAB. **Chưa làm:** `tooltip` tích hợp trong `DcIconButton`, `DcTopAppBar` subtitle/close, slot placeholder/error của `DcAsyncImage` |
| W4 | ⏸ hoãn | Cần consumer thật (DEC-04): list-detail, swipe row, full-screen dialog, time field, avatar group, selectable card, search top bar |
| W5 | ⏸ một phần | Catalog mở rộng + unit test đã có; chưa chọn công cụ screenshot test; `androidTest` mới chỉ được compile, chưa chạy trên thiết bị |

Chưa kiểm thử thủ công bằng TalkBack/font scale 200%/RTL; các widget chưa được dùng trong màn hình `app` showcase (DoD của W1 yêu cầu một consumer thật — việc còn lại).
