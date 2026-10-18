# Phase 3 — Điều hướng và lớp phủ

**Mục tiêu:** bottom navigation, navigation drawer, dialog system và bottom sheet đúng anatomy của style guide.
**Phụ thuộc:** Phase 2 đã merge.

---

## T3.1 — Bottom navigation (G-13)

**File:** `components/DcNavigationSuite.kt`

Style guide §11: 3–5 đích, cao 64–72dp, selected = primary, unselected = text secondary, icon 22–24dp, nhãn 11–12sp.

### Giữ kiến trúc adaptive
`DcNavigationSuite` dùng `NavigationSuiteScaffold` → tự thành navigation bar / rail / drawer theo kích thước cửa sổ. Đây là điểm mạnh của foundation so với style guide (vốn chỉ nói về điện thoại). **Không thay bằng `NavigationBar` cứng.** Style guide §24 cũng yêu cầu cân nhắc navigation rail cho màn rộng.

### Thay đổi

1. Ràng buộc số lượng đích ở API:

```kotlin
@Composable
fun DcNavigationSuite(
    items: List<DcNavigationItem>,
    selected: DcNavigationItem,
    onSelect: (DcNavigationItem) -> Unit,
    content: @Composable () -> Unit
) {
    require(items.size in 3..5) {
        "DcNavigationSuite hỗ trợ 3–5 đích cấp cao nhất; chuyển các mục còn lại vào 'More' hoặc drawer."
    }
    ...
}
```

2. Chính sách màu/kiểu chữ, truyền qua `NavigationSuiteDefaults.itemColors(...)`:
   - selected icon/label → `primary`
   - unselected icon/label → `onSurfaceVariant`
   - indicator → `primaryContainer`
   - nhãn → `labelMedium` (12sp), `maxLines = 1`

   Nếu API `itemColors` của phiên bản `material3-adaptive-navigation-suite` đang dùng (`1.5.0-alpha24`) khác tên, **đọc API thật rồi mới viết** — không đoán tên tham số của thư viện alpha.

3. Thêm tham số tùy chọn `alwaysShowLabel: Boolean = true` (style guide luôn hiện nhãn).

4. `DcNavigationItem` thêm `badge: String? = null` (cuối danh sách) để hiển thị số thông báo — chỉ khi cần cho màn hình tham chiếu; nếu không dùng ở Phase 5 thì **bỏ**, không thêm API chết (nguyên tắc §25.14 của style guide: không tạo lớp trừu tượng khi chưa có ca dùng thật).

---

## T3.2 — `DcNavigationDrawerContent` (mới, G-14)

**File mới:** `components/DcNavigationDrawer.kt`

Style guide §12: header có tên + email, các nhóm rõ ràng, item được chọn có nền primary container, nhãn ngắn.

```kotlin
/** Một mục trong drawer. [icon] bắt buộc để giữ nhịp căn lề của danh sách. */
data class DcDrawerItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String = label
)

/** Một nhóm mục, phân tách bằng divider. Tiêu đề nhóm là tùy chọn. */
data class DcDrawerSection(
    val title: String? = null,
    val items: List<DcDrawerItem>
)

/**
 * Nội dung của navigation drawer. Component không sở hữu trạng thái mở/đóng —
 * caller đặt nó vào ModalNavigationDrawer/PermanentNavigationDrawer của mình.
 */
@Composable
fun DcNavigationDrawerContent(
    sections: List<DcDrawerSection>,
    selectedItemId: String?,
    onItemClick: (DcDrawerItem) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null
)
```

Và một header dựng sẵn, tách riêng để không phình tham số (style guide §26: "Avoid giant components with dozens of parameters"):

```kotlin
@Composable
fun DcNavigationDrawerHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    avatar: (@Composable () -> Unit)? = null
)
```

Quy tắc cài đặt:
- Dùng `ModalDrawerSheet` của M3 làm vỏ để thừa hưởng inset và scrim; nếu không, `Surface(color = surface)`.
- Mỗi item dùng `NavigationDrawerItem` với `shape = MaterialTheme.shapes.medium`, `colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = primaryContainer, selectedTextColor = onPrimaryContainer, selectedIconColor = onPrimaryContainer)`.
- Khoảng cách: header padding `medium` (16dp), giữa các nhóm `DcDivider` + `component` (12dp), padding ngang danh sách `small` (8dp) theo chuẩn M3 drawer.
- Nhãn `bodyLarge`, `maxLines = 1`, `TextOverflow.Ellipsis`.
- Không đưa mọi hành động của app vào drawer — ghi vào KDoc.

**Business-neutral:** không hard-code "Driver App", "driver@example.com". Đó là dữ liệu của showcase ở Phase 5.

---

## T3.3 — Hệ thống dialog (G-15)

**File:** `components/DcOverlayComponents.kt`

Trước khi sửa, đọc lại chữ ký hiện tại của `DcConfirmDialog` và `DcAlertDialog` (dòng ~49 và ~84) rồi mới quyết định giữ/mở rộng.

### Anatomy bắt buộc (style guide §16)

```text
radius 20dp (shapes.extraLarge)   content padding 24dp
icon (tùy chọn) → title 12–16dp → body → 20–24dp → actions
title  titleLarge (20sp SemiBold)     body  bodyMedium (14sp)
action spacing 8–12dp                 button height 48dp
```

### `DcConfirmDialog` — mở rộng

```kotlin
@Composable
fun DcConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,          // mới
    destructive: Boolean = false        // mới
)
```

- `shape = MaterialTheme.shapes.extraLarge`, `containerColor = surface`.
- Hành động xác nhận: `DcButton(variant = if (destructive) Destructive else Primary)`.
- Hành động hủy: `DcButton(variant = Text)` — nhẹ hơn rõ rệt.
- Không thêm tham số `visible: Boolean`. Repo đang theo mẫu "caller quyết định có compose hay không"; giữ nhất quán, và ghi rõ trong KDoc kèm ví dụ:

```kotlin
if (state.showEndTripConfirm) {
    DcConfirmDialog(...)
}
```

(Prompt gốc §26 đề xuất `visible: Boolean`; ở đây chọn nhất quán với repo — ghi vào phần "khác biệt so với prompt gốc" trong báo cáo cuối.)

### Wrapper mỏng

```kotlin
@Composable fun DcInfoDialog(title: String, message: String, confirmText: String, onDismiss: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null)
@Composable fun DcErrorDialog(title: String, message: String, confirmText: String, onDismiss: () -> Unit, modifier: Modifier = Modifier)
```

- `DcInfoDialog`: một hành động duy nhất.
- `DcErrorDialog`: icon cảnh báo + màu error cho icon, **không** tô đỏ toàn bộ dialog; text lỗi phải là thông điệp người dùng hiểu được — ghi vào KDoc: không truyền exception thô (style guide §20).
- Không tạo `DcDestructiveDialog` riêng — đã có `destructive = true` (tránh trùng lặp API).

### Giới hạn nội dung
Ghi vào KDoc: dialog xác nhận tối đa 2 hành động; không nhồi form vào dialog — dùng bottom sheet hoặc màn hình riêng.

---

## T3.4 — `DcModalBottomSheet` (G-16)

**File:** `components/DcOverlayComponents.kt`

Style guide §17: bo góc trên 20dp, nền surface, padding 16–24dp, drag handle tùy chọn.

- `shape = MaterialTheme.shapes.extraLarge` — nếu cần bo **chỉ hai góc trên**, dùng `RoundedCornerShape(topStart = …, topEnd = …)` lấy giá trị từ `ShapeTokens.ExtraLarge`; không viết `20.dp` trực tiếp trong component.
- `containerColor = MaterialTheme.colorScheme.surface`, `contentColor = onSurface`.
- Thêm `showDragHandle: Boolean = true` (cuối tham số) → `dragHandle = if (showDragHandle) { { BottomSheetDefaults.DragHandle() } } else null`.
- Nội dung bọc `padding(horizontal = medium, vertical = component)` + `navigationBarsPadding()`.
- Ghi vào KDoc các ca dùng hợp lệ (chọn phương thức thanh toán, danh sách hành động, bộ lọc) và ca không hợp lệ (thay thế cả màn hình).

---

## Nghiệm thu Phase 3

```bash
./gradlew :ui:designsystem:compileDebugKotlin :ui:designsystem:testDebugUnitTest :app:assembleDebug
```

- [ ] `DcNavigationSuite` ném lỗi rõ ràng khi truyền 2 hoặc 6 mục
- [ ] Ở `widthDp = 1200`, navigation hiển thị dạng rail/drawer, không phải bar dính đáy
- [ ] Drawer: item được chọn có nền `primaryContainer`, chữ đọc rõ ở dark
- [ ] Dialog: góc 20dp, nút xác nhận nổi bật hơn nút hủy, biến thể destructive dùng màu error
- [ ] Bottom sheet: hai góc trên bo 20dp, không bị thanh điều hướng hệ thống che nội dung
- [ ] Không có `.dp` literal mới nào ngoài `tokens/`
