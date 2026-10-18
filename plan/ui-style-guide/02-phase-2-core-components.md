# Phase 2 — Component lõi

**Mục tiêu:** các component dùng nhiều nhất khớp đúng spec kích thước/bo góc/viền/trạng thái của style guide.
**Phụ thuộc:** Phase 1 đã merge.
**Nguyên tắc xuyên suốt:** tham số mới có default, thêm ở cuối (D-08); không đọc `ColorTokens` trực tiếp trong component — đọc qua `MaterialTheme`/`LocalDcColors`.

---

## T2.1 — `DcButton` (G-07)

**File:** `components/DcButton.kt`

### Vấn đề hiện tại
`loading = true` render `CircularProgressIndicator()` kích thước mặc định (~40dp) trong `Row` → nút phình to, chữ dịch chuyển. Style guide §7.5: *"Loading state must keep the same button dimensions. The text should not jump."*

### Thay đổi

1. Thêm hằng số kích thước vào `DcButtonDefaults`:

```kotlin
object DcButtonDefaults {
    /** Chiều cao tối thiểu cho nút cấp màn hình (style guide §7.1: 48–52dp). */
    val MinHeight: Dp = 48.dp
    /** Vòng loading nằm gọn trong nút, không làm đổi chiều cao. */
    val ProgressSize: Dp = 18.dp
    val ProgressStroke: Dp = 2.dp
    ...
}
```

2. Overload slot áp `defaultMinSize(minHeight = DcButtonDefaults.MinHeight, minWidth = AccessibilityTokens.MinTouchTarget)` và `shape = MaterialTheme.shapes.medium` (12dp) cho cả ba nhánh `Button` / `OutlinedButton` / `TextButton`.

3. Overload text: loading không thay chỗ của icon mà **thay thế icon slot bằng indicator cùng kích thước**, giữ nguyên text:

```kotlin
if (loading) {
    CircularProgressIndicator(
        modifier = Modifier.size(DcButtonDefaults.ProgressSize),
        color = LocalContentColor.current,
        strokeWidth = DcButtonDefaults.ProgressStroke
    )
} else {
    leadingIcon?.let { DcIcon(imageVector = it, contentDescription = null) }
}
Spacer(Modifier.width(LocalDcSpacing.current.small))   // chỉ khi có icon hoặc loading
DcText(text = text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
```

> `color = LocalContentColor.current` thay cho `MaterialTheme.colorScheme.onPrimary` cứng — nút Secondary/Text đang vẽ indicator trắng trên nền trắng.

4. Màu theo variant trong `DcButtonDefaults.colors(variant)`:

| Variant | container | content | viền |
|---|---|---|---|
| Primary | `primary` | `onPrimary` | — |
| Secondary | `surface` (transparent) | `primary` | `outline`, 1dp (`StrokeTokens.Hairline`) |
| Tertiary / Text | transparent | `primary` | — |
| Destructive | `error` | `onError` | — |

`OutlinedButton` phải truyền `border = BorderStroke(StrokeTokens.Hairline, MaterialTheme.colorScheme.outline)` và `colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)`.

5. Thêm helper cho nút cấp màn hình (style guide §7.1 "Prefer full-width primary buttons"):

```kotlin
/** Nút hành động chính chiếm hết chiều ngang, dùng ở đáy màn hình hoặc trong DcBottomActionBar. */
@Composable
fun DcPrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    destructive: Boolean = false,
)
```
Cài đặt = `DcButton(..., modifier = modifier.fillMaxWidth(), variant = if (destructive) Destructive else Primary)`. Không thêm logic khác.

6. Trạng thái `disabled`: dùng màu disabled của M3, không tự vẽ alpha.

### Kiểm chứng
Preview ma trận 4 variant × 4 trạng thái (normal / disabled / loading / có leading icon). Đo: chiều cao nút ở trạng thái normal và loading phải bằng nhau.

---

## T2.2 — `DcTextField` (G-08)

**File:** `components/DcInputComponents.kt`

Thêm tham số (cuối danh sách, có default):

```kotlin
@Composable
fun DcTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null,
    // mới:
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    errorText: String? = null
)
```

Quy tắc hợp nhất lỗi — viết đúng như sau, không phát minh thêm:

```kotlin
val hasError = isError || errorText != null
val support = errorText ?: supportingText
```

`hasError` truyền vào `isError` của `OutlinedTextField`; `support` hiển thị ở `supportingText`. `semantics { error(support) }` khi `hasError && support != null` (đã có, giữ lại, đổi biến).

Ràng buộc thị giác:

```kotlin
shape = MaterialTheme.shapes.medium,                       // 12dp
modifier = modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
leadingIcon = leadingIcon?.let { { DcIcon(it, contentDescription = null) } },
placeholder = placeholder?.let { { DcText(it, style = MaterialTheme.typography.bodyLarge) } },
colors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    errorBorderColor = MaterialTheme.colorScheme.error,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)
```

Không đổ bóng quanh input (style guide §8).

`DcPasswordTextField`: nút hiện/ẩn hiện là `TextButton` chữ dài, làm field bị co. Đổi sang `DcIconButton` với `Icons.Filled.Visibility`/`VisibilityOff` **nếu có trong `material-icons-core`**; nếu không có (D-11), giữ `TextButton` nhưng thêm `maxLines = 1` và rút gọn nhãn. Kiểm tra trước bằng compile, không đoán.

---

## T2.3 — `DcCard` (G-09)

**File:** `components/DcListComponents.kt`

Style guide §14: nền trắng, radius 16, padding 16, viền 1dp `#E5E7EB`, elevation 0–1dp, ưu tiên viền hơn bóng.

```kotlin
enum class DcCardVariant {
    /** Mặc định: nền surface + viền mảnh, không bóng. */
    Outlined,
    /** Dùng khi card nổi trên nền có nội dung, ví dụ card trong bottom sheet. */
    Elevated
}

@Composable
fun DcCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(LocalDcSpacing.current.medium),
    variant: DcCardVariant = DcCardVariant.Outlined,
    content: @Composable ColumnScope.() -> Unit
)
```

- `shape = MaterialTheme.shapes.large` (16dp) — **đổi từ `medium`**.
- `Outlined`: `border = BorderStroke(StrokeTokens.Hairline, MaterialTheme.colorScheme.outlineVariant)`, `colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)`, `elevation = CardDefaults.cardElevation(ElevationTokens.Level0)`.
- `Elevated`: không viền, `elevation = CardDefaults.cardElevation(ElevationTokens.Level1)`.
- Vùng chạm: giữ `defaultMinSize(minHeight = AccessibilityTokens.MinTouchTarget)` khi `onClick != null`.

---

## T2.4 — `DcStatusChip` (mới, G-10) và chỉnh `DcChip`

**File:** `components/DcListComponents.kt` (giữ quy ước gộp nhóm)

### `DcStatusChip` — nhãn trạng thái, không tương tác

```kotlin
/** Sắc thái trạng thái. Text luôn bắt buộc — không truyền trạng thái chỉ bằng màu (D-09). */
enum class DcStatusTone { Neutral, Info, Success, Warning, Error }

@Composable
fun DcStatusChip(
    text: String,
    tone: DcStatusTone,
    modifier: Modifier = Modifier,
    showIndicator: Boolean = true
)
```

Cài đặt:
- `Surface(shape = MaterialTheme.shapes.small, color = container, contentColor = onContainer)`.
- Bảng màu lấy từ `LocalDcColors.current` + `MaterialTheme.colorScheme`:

| tone | container | content |
|---|---|---|
| Neutral | `surfaceVariant` | `onSurfaceVariant` |
| Info | `dcColors.infoContainer` | `dcColors.onInfoContainer` |
| Success | `dcColors.successContainer` | `dcColors.onSuccessContainer` |
| Warning | `dcColors.warningContainer` | `dcColors.onWarningContainer` |
| Error | `colorScheme.errorContainer` | `colorScheme.onErrorContainer` |

- Nội dung: `Row` padding `horizontal = small (8dp)`, `vertical = xSmall (4dp)`; chấm 6dp màu `success/warning/info/error` (màu đậm, không phải container) khi `showIndicator`; `DcText(text, style = labelMedium, maxLines = 1)`.
- **Không** đặt `clickable`, **không** ép 48dp — đây là nhãn, không phải control. Ghi rõ trong KDoc để không bị coi là vi phạm DS-02.
- `semantics { contentDescription = text }` không cần thêm: text đã tự đọc được.

### `DcChip` (filter) — bỏ ép chiều cao 48dp (D-10)

Bỏ `.defaultMinSize(minHeight = AccessibilityTokens.MinTouchTarget)`; `FilterChip` của M3 đã tự áp `minimumInteractiveComponentSize()` cho vùng chạm. Thêm `shape = MaterialTheme.shapes.small`. Kết quả: hàng filter (`All / Completed / Pending / Cancelled`) cao ~32dp như style guide §22, vùng chạm vẫn 48dp.

Ghi một dòng vào `architecture/07-design-system/adaptive-accessibility.md` ở bước tài liệu: DS-02 được thỏa bằng vùng chạm, không bằng kích thước vẽ.

---

## T2.5 — `DcIconButton` (G-12)

**File:** `components/DcActionComponents.kt`

```kotlin
enum class DcIconButtonVariant {
    /** Không nền, dùng trong top app bar và list row. */
    Standard,
    /** Nền tint nhạt, dùng khi cần nhấn mạnh một hành động đơn lẻ. */
    Tonal,
    /** Nền error-container mềm cho hành động phá hủy (style guide §13). */
    SoftDestructive
}
```

Thêm `variant: DcIconButtonVariant = DcIconButtonVariant.Standard` vào cuối tham số. Cài đặt:
- `Standard` → `IconButton` (giữ nguyên hành vi hiện tại).
- `Tonal` → `FilledTonalIconButton`, `shape = MaterialTheme.shapes.medium`.
- `SoftDestructive` → `FilledTonalIconButton` với `colors(containerColor = errorContainer, contentColor = onErrorContainer)`, `shape = MaterialTheme.shapes.medium`.
- Mọi variant: container 40–48dp, icon 24dp, vùng chạm ≥48dp (`AccessibilityTokens.MinTouchTarget`).
- `contentDescription` vẫn bắt buộc (không cho default `null` ở API công khai này).

---

## T2.6 — `DcTopAppBar` (G-11)

**File:** `components/DcScreenComponents.kt`

### Vấn đề
Nút back đang là `TextButton { DcText("Back") }`. Style guide §9 yêu cầu navigation icon 24dp trong vùng chạm 48dp, tiêu đề 18–20sp SemiBold, elevation 0.

### Thay đổi

```kotlin
val navigationIcon: @Composable () -> Unit = {
    onNavigateBack?.let { back ->
        DcIconButton(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = navigateBackLabel,
            onClick = back
        )
    }
}
```

- Giữ tham số `navigateBackLabel: String = "Back"` nhưng đổi vai trò thành `contentDescription` — cập nhật KDoc; đây là thay đổi ngữ nghĩa, không phải chữ ký.
- `title` dùng `MaterialTheme.typography.titleLarge`, `maxLines = 1`, `TextOverflow.Ellipsis` (đã có).
- `colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface)`, không elevation.
- Ghi giới hạn vào KDoc: tối đa 2–3 action nhìn thấy, phần còn lại đưa vào overflow. Không cần enforce bằng code.

`Icons.AutoMirrored.Filled.ArrowBack` nằm trong `material-icons-core` — không thêm dependency.

---

## T2.7 — `DcSectionHeader`

**File:** `components/DcScreenComponents.kt`

Style guide §10: 16sp SemiBold, cách trên 24dp, cách dưới 8–12dp, không viết hoa toàn bộ.

```kotlin
modifier = modifier.padding(
    start = LocalDcSpacing.current.medium,
    end = LocalDcSpacing.current.medium,
    top = LocalDcSpacing.current.large,        // 24dp
    bottom = LocalDcSpacing.current.small      // 8dp
)
```

Giữ `titleMedium` (đúng 16sp SemiBold sau Phase 1) và `semantics { heading() }`. Thêm tham số `contentPadding: PaddingValues? = null` để màn hình đã tự quản padding không bị cộng dồn — mặc định `null` giữ hành vi cũ.

---

## T2.8 — `DcListRow`

**File:** `components/DcListRow.kt`

Style guide §14: thông tin chính đậm, phụ nhạt, căn lề đoán được, vùng bấm rõ.

- `headline` → `bodyLarge` + `color = onSurface` (giữ).
- `supportingText` → `bodyMedium` + `color = MaterialTheme.colorScheme.onSurfaceVariant` (**thêm màu**, hiện đang để mặc định nên chữ phụ đen như chữ chính).
- Nền khi `selected`: đổi `secondaryContainer` → `primaryContainer` (style guide §12: "Selected item should have a light primary container background"). Kiểm tra tương phản `onPrimaryContainer` ở cả hai theme.
- Overload slot: đổi `vertical = small` → `vertical = component` (12dp) để row không bị chật khi có 2 dòng.

---

## T2.9 — `DcText`

**File:** `components/DcText.kt`

Xác minh (đọc file) rằng đã có đủ `modifier, style, color, maxLines, overflow, textAlign`. Thiếu cái nào thì thêm với default giữ nguyên hành vi. Không đổi thứ tự tham số hiện có.

---

## Nghiệm thu Phase 2

```bash
./gradlew :ui:designsystem:compileDebugKotlin :ui:designsystem:testDebugUnitTest :app:assembleDebug
```

- [ ] Nút: chiều cao normal == chiều cao loading (kiểm bằng preview cạnh nhau)
- [ ] Nút Secondary loading: vòng xoay nhìn thấy được (không trắng trên trắng)
- [ ] TextField lỗi: có viền đỏ **và** dòng chữ lỗi bên dưới
- [ ] Card: viền mảnh, không bóng, góc 16dp
- [ ] `DcStatusChip` 5 tone đọc được ở light + dark; có chấm chỉ báo
- [ ] Hàng `DcChip` cao ~32dp nhưng bấm được thoải mái
- [ ] Top bar: icon mũi tên, có `contentDescription`, tiêu đề 20sp
- [ ] `DcComponentsTest` (androidTest) còn xanh; nếu test khẳng định hành vi cũ (ví dụ nút back là text), cập nhật test cùng commit và ghi lý do
