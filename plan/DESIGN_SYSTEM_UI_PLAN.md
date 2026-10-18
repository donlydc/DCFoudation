# Kế hoạch Design System — bộ component thông dụng và cơ chế tùy chỉnh

**Ngày:** 2026-08-23
**Module:** `:ui:designsystem`
**Mục tiêu:** một bộ component phủ 80–90% màn hình của app CRUD/list-detail thông thường, **và** một cơ chế cho phép app dùng lại foundation đổi được thương hiệu, mật độ, hình dáng, biến thể mà **không phải sửa code trong module foundation**.

Trạng thái hiện tại đã xác minh trên source (commit `f29195e`), không lấy từ `graphify-out/`.

---

## 1. Vấn đề hiện tại: component có sẵn nhưng chưa tùy chỉnh được

Module hiện có 9 component (`DcText`, `DcButton`, `DcTextField`, `DcPasswordTextField`, `DcListRow`, `DcLoadingState`, `DcEmptyState`, `DcErrorState`, `DcOfflineBanner`) — đúng hướng, nhưng cả ba lớp tùy chỉnh đều đang khóa cứng:

| Chặn | Bằng chứng | Hệ quả cho app dùng lại |
|---|---|---|
| Theme khóa cứng | `DcTheme` hard-code `DarkColorScheme`/`LightColorScheme` (Purple), `DcTypography`, `DcShapes`; không có tham số nào ngoài `darkTheme`/`dynamicColor` | App không đổi được màu thương hiệu nếu không sửa file trong foundation |
| Token không override được | `SpacingTokens`, `ShapeTokens`, `ColorTokens` đều là `object` hằng số | App không đổi được mật độ (compact/airy) hay bo góc toàn cục |
| Component thiếu tham số cơ bản | `DcText(text, modifier, style)` — không có `maxLines`, `overflow`, `color`, `textAlign`; `DcButton(text, onClick, modifier, enabled)` — không có biến thể, icon, trạng thái loading | Feature buộc phải bỏ qua DS và gọi thẳng Material → mất luôn policy mà DS định khóa (DS-01/DS-03) |

Nói cách khác: DS hiện khóa được policy, nhưng khóa **chặt quá** nên feature sẽ tự lách. Kế hoạch này mở đúng chỗ cần mở.

---

## 2. Kiến trúc tùy chỉnh 4 cấp

Nguyên tắc: **một cơ chế cho mỗi loại thay đổi**, không chồng chéo. Càng lên cao càng ít phải sửa call site.

```text
Cấp 1 — Token/Theme override        đổi toàn app, 1 chỗ duy nhất (DcTheme(...))
   ↓ dùng khi: đổi thương hiệu, mật độ, bo góc, font
Cấp 2 — Tham số + variant            đổi tại call site
   ↓ dùng khi: 1 nút destructive, 1 text 2 dòng
Cấp 3 — Slot content                 đổi cấu trúc bên trong, giữ policy bên ngoài
   ↓ dùng khi: list row cần layout riêng nhưng vẫn muốn touch target + padding chuẩn
Cấp 4 — Escape hatch                 dùng thẳng Material3
       dùng khi: DS không có, và chưa đủ lý do để đưa vào DS
```

**Cấp 4 là một cam kết, không phải thất bại.** Không component nào của DC được là bắt buộc. Nếu app phải fork foundation để làm một màn hình, thiết kế DS đã sai.

### 2.1 Cấp 1 — mở tham số cho `DcTheme` (việc đầu tiên phải làm)

```kotlin
@Composable
fun DcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,                       // đổi mặc định: xem 2.2
    colorScheme: ColorScheme = DcThemeDefaults.colorScheme(darkTheme, dynamicColor),
    typography: Typography = DcThemeDefaults.Typography,
    shapes: Shapes = DcThemeDefaults.Shapes,
    extendedColors: DcColors = DcThemeDefaults.extendedColors(darkTheme),
    spacing: DcSpacing = DcSpacing.Default,
    content: @Composable () -> Unit,
)
```

App dùng lại foundation chỉ viết đúng một chỗ:

```kotlin
// app/ui/AppTheme.kt
@Composable
fun MyAppTheme(content: @Composable () -> Unit) = DcTheme(
    colorScheme = MyBrandColorScheme,
    typography = MyBrandTypography,
    spacing = DcSpacing.Compact,
    content = content,
)
```

Mọi component DC lập tức đi theo, không sửa call site nào.

### 2.2 Đổi `dynamicColor` mặc định `true` → `false`

Dynamic color lấy màu từ hình nền máy người dùng. Với một foundation dùng cho app có thương hiệu, để `true` mặc định nghĩa là màu thương hiệu bị ghi đè trên phần lớn thiết bị Android 12+, và screenshot test không bao giờ xác định được. Giữ `true` là opt-in của từng app.

### 2.3 Token: cái nào cho override, cái nào không

| Token | Hình thức | Override được? | Lý do |
|---|---|---|---|
| `DcSpacing` | `data class` + `LocalDcSpacing` | ✅ | Mật độ là quyết định sản phẩm (app dashboard khác app đọc báo) |
| `DcColors` (success/warning/info/offline) | `data class` + `LocalDcColors` | ✅ | Ngoài Material role, thuộc thương hiệu |
| `ColorScheme`/`Typography`/`Shapes` | tham số `DcTheme` | ✅ | Chuẩn Material3 |
| `MotionTokens` | `object` | ✅ (qua tham số component) | Ít khi cần đổi toàn cục |
| `AccessibilityTokens.MinTouchTarget` | `object`, **không** CompositionLocal | ❌ | 48dp là sàn accessibility, không phải lựa chọn thẩm mỹ. Cho override tức là mời gọi vi phạm DS-02 |

Đây là điểm cần giữ vững: mở gần hết, **trừ** sàn accessibility.

### 2.4 Cấp 2 — `Defaults` object thay vì CompositionLocal riêng cho từng component

Mỗi component có một `object` defaults kiểu Material3, và các default đó **đọc từ theme** chứ không phải hằng số riêng:

```kotlin
object DcButtonDefaults {
    val Shape: Shape @Composable get() = MaterialTheme.shapes.medium
    val ContentPadding: PaddingValues @Composable get() = PaddingValues(
        horizontal = DcTheme.spacing.Medium, vertical = DcTheme.spacing.Small
    )
    @Composable fun colors(variant: DcButtonVariant): ButtonColors = ...
}
```

Nhờ đó **không cần** thêm một tầng `LocalDcButtonStyle` nữa: app đổi `shapes`/`spacing` ở cấp 1 thì mọi nút tự đổi theo. Cố ý không tạo cơ chế override thứ hai — hai nguồn sự thật cho cùng một giá trị là cách nhanh nhất để DS trở nên khó đoán.

---

## 3. Danh mục component

Quy ước cột **Policy khóa được**: lý do component này tồn tại. Không có policy ⇒ không đưa vào DS (giữ nguyên luật hiện hành: "không wrap Material chỉ để đổi tên").

### 3.1 Text — `components/text/`

| Component | Policy khóa được | Ưu tiên | Trạng thái |
|---|---|---|---|
| `DcText` | style mặc định từ theme; **bổ sung** `maxLines`/`overflow`/`color`/`textAlign` | P0 | Có, cần mở tham số |
| `DcTextLink` | vùng chạm ≥48dp cho link trong đoạn văn, semantics `Role.Button`, màu link từ theme | P1 | Mới |
| `DcMarkdownLite` | in đậm/nghiêng/link tối thiểu cho nội dung từ server, không cho HTML tùy ý | P2 | Mới, chỉ khi có use case |

Không tạo `DcTitle`/`DcBody`/`DcCaption` riêng — `DcText(style = MaterialTheme.typography.titleMedium)` đã rõ ràng và không phát sinh 6 API phải bảo trì.

### 3.2 Actions — `components/action/`

| Component | Policy khóa được | Ưu tiên | Trạng thái |
|---|---|---|---|
| `DcButton` | touch target, biến thể, **trạng thái `loading` tự khóa nút** | P0 | Có, cần mở rộng |
| `DcIconButton` | touch target 48dp + bắt buộc `contentDescription` | P0 | Mới |
| `DcFab` / `DcExtendedFab` | vị trí + insets + touch target | P1 | Mới |
| `DcSegmentedButton` | chọn 1-trong-N dạng thanh, semantics nhóm | P2 | Mới |

```kotlin
enum class DcButtonVariant { Primary, Secondary, Tertiary, Destructive, Text }

@Composable
fun DcButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: DcButtonVariant = DcButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,                  // hiển thị spinner VÀ vô hiệu hoá nút
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentPadding: PaddingValues = DcButtonDefaults.ContentPadding,
    colors: ButtonColors = DcButtonDefaults.colors(variant),
)

/** Cấp 3 — slot overload cho nội dung tuỳ ý, giữ nguyên touch target và padding. */
@Composable
fun DcButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: DcButtonVariant = DcButtonVariant.Primary,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
)
```

`loading` là policy thật, không phải tiện ích: nút submit đang chạy mà vẫn bấm được là nguồn của lỗi gửi trùng. Nó ghép trực tiếp với `JobRegistry.launchIfIdle` ở `:runtime:concurrency` — ViewModel chặn job trùng, nút chặn thao tác trùng.

`Destructive` là biến thể riêng chứ không phải "truyền màu đỏ vào": nó khóa cả màu, cả yêu cầu đi kèm dialog xác nhận trong tài liệu component.

### 3.3 Header — `components/header/`

| Component | Policy khóa được | Ưu tiên | Trạng thái |
|---|---|---|---|
| `DcTopAppBar` | insets trạng thái, `scrollBehavior`, hành vi nút back, cắt tiêu đề dài | P0 | Mới |
| `DcSectionHeader` | tiêu đề nhóm trong danh sách: spacing, typography, `heading()` semantics cho TalkBack | P0 | Mới |
| `DcSearchBar` | insets bàn phím, nút xoá, `imeAction`, semantics tìm kiếm | P1 | Mới |

```kotlin
enum class DcTopAppBarStyle { Small, Centered, Large }

@Composable
fun DcTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    style: DcTopAppBarStyle = DcTopAppBarStyle.Small,
    onNavigateBack: (() -> Unit)? = null,      // null = màn hình gốc, không vẽ nút back
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
)
```

`onNavigateBack` nhận `null` thay vì để caller tự quyết vẽ hay không — nếu để caller, mỗi màn hình sẽ tự đặt icon back khác nhau.

### 3.4 Bottom — `components/bottom/`

| Component | Policy khóa được | Ưu tiên | Trạng thái |
|---|---|---|---|
| `DcBottomActionBar` | thanh nút cố định đáy: insets navigation **và** insets bàn phím, elevation khi nội dung cuộn phía dưới | P0 | Mới |
| `DcNavigationBar` | thanh điều hướng chính; ở cấu hình rộng tự đổi sang rail/drawer (xem §5) | P0 | Mới |
| `DcModalBottomSheet` | insets bàn phím, chặn dismiss khi đang xử lý, drag handle nhất quán | P1 | Mới |

`DcBottomActionBar` là component hay bị làm sai nhất: thanh Lưu/Huỷ ở đáy bị bàn phím che, hoặc bị thanh điều hướng hệ thống đè. Khóa nó một lần ở đây tiết kiệm việc sửa lỗi ở từng màn hình form.

### 3.5 Item / List — `components/list/`

| Component | Policy khóa được | Ưu tiên | Trạng thái |
|---|---|---|---|
| `DcListRow` | touch target, semantics; **bổ sung** `leadingContent`, `selected`, slot overload | P0 | Có, cần mở rộng |
| `DcCard` | padding, shape, elevation, vùng chạm khi clickable | P0 | Mới |
| `DcAvatar` | kích thước chuẩn, ảnh lỗi/đang tải, chữ cái thay thế, `contentDescription` | P1 | Mới (cần Coil, §6) |
| `DcChip` | trạng thái chọn, touch target, cắt nhãn dài | P1 | Mới |
| `DcDivider` | độ dày + màu từ theme, inset trái theo `leadingContent` | P1 | Mới |
| `DcSwipeableRow` | hành động vuốt + ngưỡng + haptic + phím tắt tương đương cho accessibility | P2 | Chỉ khi có use case |

```kotlin
@Composable
fun DcListRow(
    headline: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
)

/** Cấp 3 — layout tuỳ ý, vẫn giữ touch target/padding/ripple/semantics của DS. */
@Composable
fun DcListRow(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
)
```

Overload slot chính là câu trả lời cho "dễ tùy chỉnh": app cần một hàng hoàn toàn khác vẫn không phải bỏ DS, chỉ bỏ phần layout.

### 3.6 Input — `components/input/`

| Component | Policy khóa được | Ưu tiên | Trạng thái |
|---|---|---|---|
| `DcTextField` / `DcPasswordTextField` | semantics lỗi, nhãn hiện/ẩn do caller cấp để localize | — | Có, giữ nguyên |
| `DcToggleRow` (checkbox/switch/radio) | **bấm cả hàng**, không chỉ ô vuông; touch target; semantics `toggleable` | P0 | Mới |
| `DcDropdownField` | menu, trạng thái chọn, semantics | P1 | Mới |
| `DcDatePickerField` | định dạng theo locale, vùng ngày hợp lệ | P2 | Mới |

`DcToggleRow` là ví dụ chuẩn của "component xứng đáng vào DS": nó không wrap `Checkbox` để đổi tên, nó khóa quy tắc *toàn hàng là vùng chạm* — thứ mà mỗi màn hình tự làm sẽ tự sai.

### 3.7 Feedback & Overlay — `components/feedback/`

| Component | Policy khóa được | Ưu tiên | Trạng thái |
|---|---|---|---|
| `DcLoadingState` / `DcEmptyState` / `DcErrorState` / `DcOfflineBanner` | — | — | Có |
| `DcContentState<T>` | **thứ tự ưu tiên loading/error/empty/content** — xem §4 | P0 | Mới |
| `DcSnackbarEffect` | gắn `userMessage` trong state với `SnackbarHostState`, đảm bảo gọi `onMessageShown` | P0 | Mới |
| `DcSkeleton` | khung xám nhấp nháy khi tải, tôn trọng "giảm chuyển động" | P1 | Mới |
| `DcAlertDialog` / `DcConfirmDialog` | chặn dismiss khi đang xử lý, thứ tự nút, nút destructive | P1 | Mới |
| `DcBadge` | số lớn rút gọn ("99+"), semantics | P2 | Mới |

### 3.8 Container — `components/layout/`

| Component | Policy khóa được | Ưu tiên | Trạng thái |
|---|---|---|---|
| `DcScaffold` | insets, vị trí `SnackbarHost`, ghép `DcTopAppBar` + `DcBottomActionBar`/`DcNavigationBar` | P0 | Mới |
| `DcPullToRefreshBox` | đồng bộ với `isRefreshing` trong UiState | P0 | Mới (dùng Material3 có sẵn, §6) |

---

## 4. `DcContentState` — component tiết kiệm code nhiều nhất

Mọi màn hình list/detail đều viết lại đúng một khối rẽ nhánh bốn trạng thái, và mỗi màn hình sai theo một kiểu khác nhau.

```kotlin
@Composable
fun <T> DcContentState(
    value: T?,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    isEmpty: (T) -> Boolean = { false },
    onRetry: (() -> Unit)? = null,
    loadingLabel: String,
    emptyTitle: String? = null,
    // Cấp 3: thay từng trạng thái mà vẫn giữ quy tắc ưu tiên
    loadingContent: @Composable () -> Unit = { DcLoadingState(loadingLabel) },
    errorContent: @Composable (String) -> Unit = { DcErrorState(it, onRetry = onRetry) },
    emptyContent: @Composable () -> Unit = { DcEmptyState(emptyTitle.orEmpty()) },
    content: @Composable (T) -> Unit,
)
```

Quy tắc ưu tiên nằm **bên trong** component, đây mới là giá trị thật của nó:

1. Có dữ liệu cũ + đang tải ⇒ hiển thị dữ liệu cũ kèm chỉ báo, **không** nháy sang spinner toàn màn hình.
2. Có dữ liệu cũ + có lỗi ⇒ hiển thị dữ liệu cũ + `DcOfflineBanner`/thông báo, không thay bằng màn hình lỗi trắng.
3. Không dữ liệu + có lỗi ⇒ màn hình lỗi kèm retry.
4. Không dữ liệu + đang tải ⇒ loading.
5. Có dữ liệu nhưng rỗng ⇒ empty state.

Ghép trực tiếp với `ArticlesUiState` đang có (`isRefreshing`, `isShowingStaleData`, `userMessage`).

---

## 5. Adaptive: một API, ba hình dạng

`DcNavigationBar` không nên chỉ là thanh dưới đáy. Dùng `NavigationSuiteScaffold` của `material3-adaptive` để cùng một khai báo tự đổi hình dạng theo kích thước cửa sổ:

```text
compact  → navigation bar (đáy)
medium   → navigation rail (trái)
expanded → permanent drawer
```

```kotlin
@Composable
fun DcNavigationSuite(
    items: List<DcNavigationItem>,
    selected: DcNavigationItem,
    onSelect: (DcNavigationItem) -> Unit,
    content: @Composable () -> Unit,
)
```

Đây là lý do nên thêm thư viện adaptive sớm: viết `DcNavigationBar` thủ công bây giờ rồi migrate sang rail/drawer sau là làm hai lần.

---

## 6. Thư viện đề xuất

Nguyên tắc: mỗi thư viện phải trả lời "app nào cũng cần?" — nếu không, để app tự thêm.

### 6.1 Nên thêm

| Thư viện | Coordinate | Mở ra | Ghi chú |
|---|---|---|---|
| Material3 Adaptive | `androidx.compose.material3.adaptive:adaptive`, `:adaptive-navigation-suite` | `DcNavigationSuite`, list-detail pane, `currentWindowAdaptiveInfo()` | Bắt buộc nếu muốn hỗ trợ tablet/gập mà không viết lại |
| Coil 3 | `io.coil-kt.coil3:coil-compose` | `DcAvatar`, `DcAsyncImage`, ảnh thumbnail trong list | Xem đánh đổi ở 6.2 |
| Compose UI Test | đã có qua `dc.testing` | Test component | — |

**Đã có sẵn trong Material3, không cần thêm gì:** `PullToRefreshBox`, `ModalBottomSheet`, `SearchBar`, `DatePicker`, `SegmentedButton`, `Badge`, `Card`, `NavigationBar`, `Chip`. Phần lớn "mẫu mã" của kế hoạch này chỉ là bọc chúng lại kèm policy — không phát sinh dependency mới.

### 6.2 Đánh đổi của Coil trong `:ui:designsystem`

Đưa Coil vào design system nghĩa là **mọi app** dùng foundation đều kéo theo nó, kể cả app không có ảnh. Hai phương án:

- **A (khuyến nghị):** Coil nằm trong `:ui:designsystem`, nhưng chỉ lộ ra qua đúng một file `DcAsyncImage.kt`. Đổi thư viện tải ảnh về sau = sửa một file, không call site nào đổi.
- **B:** `DcAvatar(painter: Painter)` nhận `Painter` từ ngoài, app tự chọn thư viện tải ảnh.

Chọn A vì tải ảnh bất đồng bộ là nhu cầu gần như phổ quát và phương án B đẩy phần khó (cache, placeholder, lỗi) về lại từng app — đúng thứ foundation phải giải quyết. Nhưng ghi rõ ranh giới trong `STRUCTURE.md` để việc thay thế sau này là một file.

### 6.3 Icon — quyết định riêng, không mặc định thêm

`androidx.compose.material:material-icons-extended` tiện nhưng rất lớn và Google đã ngừng bổ sung icon mới cho artifact này; khuyến nghị:

- Dùng `androidx.compose.material:material-icons-core` (đã có qua Material3) cho nhóm icon phổ biến.
- Khi cần bộ icon riêng, tạo module `:ui:resources` chứa `ImageVector` được export từ file thiết kế — vừa nhỏ, vừa là chỗ đặt icon thương hiệu.

Cần xác minh phiên bản/độ tương thích với Compose BOM `2026.02.01` tại thời điểm thêm; không chốt version trong tài liệu này để tránh ghi số đã cũ.

### 6.4 Cố ý không thêm

| Thư viện | Lý do |
|---|---|
| Accompanist | Phần lớn đã được hấp thụ vào Compose/Material3; thêm vào là ôm nợ kỹ thuật |
| Thư viện shimmer riêng | `DcSkeleton` tự viết ~40 dòng bằng `Brush.linearGradient` + `rememberInfiniteTransition`; không đáng một dependency |
| Lottie | Chỉ thêm khi thiết kế thật sự giao file Lottie; không đưa vào sẵn |
| Thư viện chart | Không phải nhu cầu phổ quát; app nào cần thì tự thêm |
| Thư viện blur/glass effect | Thẩm mỹ theo mùa, không phải policy |

---

## 7. Catalog, preview và test

### 7.1 Preview matrix bằng multipreview annotation

```kotlin
// preview/DcPreviews.kt
@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class DcThemePreviews

@Preview(name = "Font 1.0x", fontScale = 1.0f)
@Preview(name = "Font 1.5x", fontScale = 1.5f)
@Preview(name = "Font 2.0x", fontScale = 2.0f)
annotation class DcFontScalePreviews

@Preview(name = "Compact", widthDp = 360)
@Preview(name = "Medium", widthDp = 700)
@Preview(name = "Expanded", widthDp = 1200)
annotation class DcWindowPreviews
```

Một annotation thay 6 khối `@Preview` copy-paste ở mỗi component. Giữ nguyên DS-04 (mọi component mới đều xuất hiện trong `DesignSystemCatalog`).

### 7.2 Catalog thành màn hình chạy được, không chỉ là preview

`DesignSystemCatalog` hiện là preview. Nâng thành một destination bật được ở debug build: đây là nơi kiểm tra nhanh khi app đổi theme ở cấp 1 — mở catalog trong app của mình, thấy ngay toàn bộ component với thương hiệu mới.

### 7.3 Test bắt buộc (module hiện 0 test)

Chỉ test phần **policy**, không test getter/setter:

1. `DcContentState` — 5 nhánh ưu tiên ở §4.
2. `DcButton(loading = true)` — không phát `onClick`.
3. `DcSnackbarEffect` — gọi `onMessageShown` đúng một lần cho mỗi message.
4. `DcToggleRow` — bấm vào nhãn cũng đổi trạng thái (không chỉ ô control).
5. `DcTextField` — semantics lỗi đọc được bởi TalkBack.
6. Touch target — mọi component tương tác đạt ≥ `AccessibilityTokens.MinTouchTarget`.

---

## 8. Thứ tự triển khai

| Bước | Nội dung | Vì sao trước |
|---|---|---|
| 1 | Cấp 1: `DcTheme` nhận tham số, `DcSpacing`/`DcColors` thành `data class` + CompositionLocal, `dynamicColor = false` | Mọi component sau đó đọc default từ theme; làm sau nghĩa là viết lại default của từng component |
| 2 | Mở tham số `DcText`, `DcButton` (variant/icon/loading), `DcListRow` (leading/selected/slot) | Ngăn feature lách sang Material ngay từ bây giờ |
| 3 | `DcScaffold` + `DcTopAppBar` + `DcBottomActionBar` + `DcSectionHeader` | Khung màn hình; mọi màn hình mới dựng trên đây |
| 4 | `DcContentState` + `DcSnackbarEffect` | Cần `AppError`→text ở tầng presentation; đây là bước tiết kiệm code lớn nhất |
| 5 | `DcCard`, `DcIconButton`, `DcToggleRow`, `DcChip`, `DcDivider` | Bộ lấp đầy màn hình thường gặp |
| 6 | Thêm adaptive + `DcNavigationSuite`; thêm Coil + `DcAsyncImage`/`DcAvatar` | Hai dependency mới, tách riêng để dễ đánh giá/rollback |
| 7 | `DcModalBottomSheet`, `DcAlertDialog`/`DcConfirmDialog`, `DcSkeleton`, `DcPullToRefreshBox` | Lớp hoàn thiện |
| 8 | Preview matrix + catalog chạy được + 6 test policy | Chốt lại, và là điều kiện để bật screenshot test ở P2 |

Bước 1–2 nên đi cùng một lần: mở theme mà không mở tham số component thì app vẫn kẹt, và ngược lại.

**Ước lượng:** ~1.100–1.300 dòng sản phẩm + ~350 dòng test, đưa module từ 555 lên khoảng 1.900 dòng.

---

## 9. Definition of done

- App dùng lại foundation đổi được thương hiệu, mật độ, bo góc, font **chỉ bằng tham số `DcTheme`** — không sửa file nào trong `:ui:designsystem`.
- Mọi component tương tác có overload slot (cấp 3) hoặc lý do ghi rõ vì sao không cần.
- Không component nào là bắt buộc: tài liệu nêu rõ cấp 4 (dùng thẳng Material3) là hợp lệ.
- `AccessibilityTokens.MinTouchTarget` vẫn không override được, và mọi component tương tác vẫn tôn trọng nó (DS-02).
- Mọi component mới có mặt trong `DesignSystemCatalog` (DS-04) và có `@DcThemePreviews`.
- 6 test policy ở §7.3 pass.
- Mỗi component có KDoc nêu **policy nó khóa**, không chỉ mô tả tham số.
- Thư viện mới được ghi vào `libs.versions.toml` và chỉ `:ui:designsystem` phụ thuộc trực tiếp; feature không import Coil/adaptive.
- Cập nhật `architecture/07-design-system/components.md` và `STRUCTURE.md`; chạy `graphify update .` sau bước 6 (thay đổi topology thật).
