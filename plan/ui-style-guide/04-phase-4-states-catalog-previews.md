# Phase 4 — Trạng thái, Catalog, Preview

**Mục tiêu:** loading/empty/error trở thành bộ dùng lại đầy đủ; catalog và preview phủ 100% component và mọi trạng thái, để một app đổi thương hiệu kiểm tra được bằng mắt trong một màn hình duy nhất (DS-04).
**Phụ thuộc:** Phase 2, 3 đã merge.

---

## T4.1 — Loading (G-18)

**File:** `components/DcStateComponents.kt`

Style guide §18 tách 4 kiểu: inline, trong nút, cả trang, skeleton. Hiện chỉ có `DcLoadingState` (khối căn giữa) và `DcSkeleton`.

1. Đổi `DcLoadingState` thành có kích thước khai báo được, thay vì luôn là khối lớn:

```kotlin
enum class DcLoadingSize {
    /** Trong một hàng/khối nội dung. Indicator 20dp, không chiếm hết chiều cao. */
    Inline,
    /** Cả màn hình hoặc cả vùng nội dung. Indicator 40dp, căn giữa. */
    Screen
}

@Composable
fun DcLoadingState(
    label: String,
    modifier: Modifier = Modifier,
    size: DcLoadingSize = DcLoadingSize.Screen   // giữ hành vi cũ làm mặc định
)
```

- `Inline`: `Row` ngang, indicator 20dp + `DcText(label, bodyMedium, onSurfaceVariant)`, padding `component`.
- `Screen`: giữ nguyên bố cục hiện tại.
- Cả hai giữ `semantics { contentDescription = label }` trên indicator.

2. Thay `SpacingTokens.*` cứng trong file này bằng `LocalDcSpacing.current.*` — hiện `DcLoadingState`/`DcEmptyState`/`DcErrorState`/`DcOfflineBanner` đang đọc `SpacingTokens` trực tiếp, nên app đổi mật độ qua `DcTheme(spacing = Compact)` không ảnh hưởng tới các state này. Đây là lỗi nhất quán, sửa trong phase này.

3. Không thêm loading toàn màn hình chặn tương tác. Style guide §18: chỉ dùng full-screen cho thao tác khởi tạo/chặn thật sự — đó là quyết định của feature, không phải của DS.

---

## T4.2 — Empty state và Error state (G-17)

**File:** `components/DcStateComponents.kt`

```kotlin
@Composable
fun DcEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    action: (@Composable () -> Unit)? = null,
    icon: ImageVector? = null            // mới, cuối danh sách
)

@Composable
fun DcErrorState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    retryLabel: String? = null,
    onRetry: (() -> Unit)? = null,
    icon: ImageVector? = null            // mới
)
```

Quy tắc icon (style guide §19: "Avoid decorative oversized illustrations"):
- Kích thước 40dp, màu `onSurfaceVariant` (empty) / `error` (error state).
- `contentDescription = null` — icon chỉ trang trí, tiêu đề đã mang nghĩa.
- Khoảng cách icon → title = `component` (12dp), title → description = `xSmall` (4dp), description → action = `large` (24dp).

Kiểu chữ: title `titleMedium`, description `bodyMedium` màu `onSurfaceVariant`.

Error state: nút thử lại dùng `DcButton(variant = Secondary)` — hành động khôi phục không phải hành động chính của màn hình. Ghi vào KDoc: không hiển thị exception thô.

---

## T4.3 — `DcOfflineBanner`

Đổi `shape` sang `MaterialTheme.shapes.large` (16dp) sau khi shape scale đổi ở Phase 1, và thêm icon cảnh báo nhỏ ở đầu để không chỉ dựa vào màu (D-09).

---

## T4.4 — `DcContentState`

**File:** `components/DcFeedbackComponents.kt`, test đi kèm: `src/test/.../DcContentStatePolicyTest.kt`

Đọc `resolveDcContentStateBranch` hiện tại trước khi sửa. Chỉ cần đảm bảo nhánh loading/empty/error gọi các API mới đúng tham số. **Không đổi luật ưu tiên nhánh** — đã có unit test khóa hành vi; nếu test đỏ nghĩa là đã đổi hành vi ngoài phạm vi, hoàn tác.

---

## T4.5 — Catalog

**File:** `catalog/DesignSystemCatalog.kt`

Catalog là công cụ nghiệm thu chính của toàn bộ kế hoạch này. Tổ chức lại thành các mục có tiêu đề (`DcSectionHeader`), theo đúng thứ tự style guide:

```text
1.  Color        ô màu: primary/primaryContainer/surface/background/surfaceVariant/outline/
                 error/errorContainer + success/warning/info (từ LocalDcColors), có nhãn tên vai trò
2.  Typography   mỗi style một dòng mẫu, kèm tên style + size
3.  Buttons      ma trận 4 variant × 4 trạng thái (normal/pressed-ghi chú/disabled/loading)
4.  Text fields  normal / có leading icon / error / disabled / read-only
5.  Top app bar  small / centered / large, có và không có back
6.  Bottom nav   4 mục mẫu
7.  Drawer       header + 2 nhóm + item được chọn
8.  Icon buttons standard / tonal / soft-destructive
9.  Cards        outlined / elevated / card có onClick
10. Chips        DcChip (filter, có chọn) + DcStatusChip 5 tone
11. Dialogs      confirm / destructive / info / error (nút mở từng cái)
12. Bottom sheet có và không có drag handle
13. States       loading inline / loading screen / skeleton / empty / error / offline banner
14. Spacing      thanh trực quan 4/8/12/16/24/32 + mẫu bo góc 8/12/16/20
```

Yêu cầu:
- Mục 1, 2, 14 là **mới** — chính chúng làm cho việc đổi thương hiệu kiểm tra được bằng mắt.
- Mọi component công khai phải xuất hiện đúng một lần. Sau khi sửa xong, đối chiếu danh sách `rg "^fun Dc" ui/designsystem/src/main/.../components/` với các mục trong catalog; thiếu component nào thì bổ sung.
- Catalog không được chứa dữ liệu nghiệp vụ (không "Trip #10232" ở đây — cái đó thuộc showcase ở `:app`, Phase 5). Dùng nhãn trung tính: "Primary action", "List item", "Status".

---

## T4.6 — Preview

**Files:** `preview/DcPreviews.kt` (annotation đã có, không đổi), các file component.

Yêu cầu tối thiểu, theo style guide §25.10:

| Component | Preview bắt buộc |
|---|---|
| `DcButton` | `@DcThemePreviews` ma trận variant × trạng thái |
| `DcTextField` | `@DcThemePreviews` normal/error/disabled + `@DcFontScalePreviews` |
| `DcCard`, `DcListRow` | `@DcThemePreviews` |
| `DcStatusChip` | `@DcThemePreviews` đủ 5 tone |
| `DcTopAppBar` | `@DcThemePreviews` 3 style |
| `DcNavigationDrawerContent` | `@DcThemePreviews` |
| `DcConfirmDialog` (+ destructive) | `@DcThemePreviews` |
| `DcEmptyState`, `DcErrorState`, `DcLoadingState` | `@DcThemePreviews` |
| `DesignSystemCatalog` | `@DcThemePreviews` + `@DcWindowPreviews` |

Preview đặt cuối file component tương ứng, `private`, đặt tên `<Component>Preview`, luôn bọc trong `DcTheme { }`.

Preview phải là **hàm private không tham số** để công cụ dựng được; dữ liệu mẫu khai báo tại chỗ.

---

## Nghiệm thu Phase 4

```bash
./gradlew :ui:designsystem:compileDebugKotlin :ui:designsystem:testDebugUnitTest
./gradlew :ui:designsystem:connectedDebugAndroidTest   # nếu có thiết bị/emulator
```

- [ ] Catalog cuộn hết một lượt ở light và dark, không có chỗ nào chữ chìm hoặc mất viền
- [ ] Ở `fontScale = 2.0f`, không component nào bị cắt chữ hay chồng lấn
- [ ] Ở `widthDp = 360 / 700 / 1200`, catalog không tràn ngang
- [ ] Mọi component công khai có ít nhất một preview
- [ ] `DcContentStatePolicyTest` xanh, không sửa assert
