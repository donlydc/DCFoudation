# Phase 5 — Màn hình tham chiếu và migrate màn hình thật

**Mục tiêu:** chứng minh design system dùng được trên màn hình thật, và cung cấp hai màn hình mẫu đúng như style guide §21–§22.
**Phụ thuộc:** Phase 4 đã merge.
**Ranh giới:** mọi thứ trong phase này nằm ở `:app`. Không thêm gì vào `:ui:designsystem` (D-06).

---

## T5.1 — Màn hình showcase Trip Details (G-20)

**Thư mục mới:** `app/src/main/java/com/dc/android/foundation/showcase/trip/`

```text
showcase/trip/
├── TripShowcaseData.kt        dữ liệu tĩnh, immutable, chỉ dùng để trình bày
├── TripDetailsShowcaseScreen.kt
└── TripHistoryShowcaseScreen.kt
```

Quy tắc cho toàn bộ thư mục `showcase/`:
- Không ViewModel, không Hilt, không repository, không network, không DataStore.
- State cục bộ bằng `rememberSaveable` là đủ (mở dialog, chọn filter).
- Dữ liệu là `val` hằng số trong `TripShowcaseData.kt`.
- KDoc đầu mỗi file ghi rõ: *"Màn hình trình diễn design system. Không phải feature thật; không có nguồn dữ liệu."*

### Bố cục Trip Details (style guide §21)

```text
DcScaffold
├── topBar     DcTopAppBar(title = "Trip details", onNavigateBack = …, actions = { DcIconButton(MoreVert) })
├── bottomBar  DcNavigationSuite hoặc DcBottomActionBar (xem ghi chú dưới)
└── content    Column(verticalScroll, padding ngang = medium)
    ├── DcSectionHeader("Trip information")
    ├── DcCard { 3 dòng label–value: Distance / Duration / Waiting time }
    ├── DcSectionHeader("Fare details")
    ├── DcCard { Base fare / Waiting fee / DcDivider / Total (nhấn mạnh) }
    └── DcPrimaryActionButton("End Trip", destructive = true) → mở DcConfirmDialog(destructive = true)
```

Yêu cầu chi tiết:
- **Dòng label–value** lặp lại nhiều lần → tạo một composable private trong file: `TripDetailRow(label: String, value: String, emphasized: Boolean = false)`. Không tạo component này trong DS: chưa có ca dùng thứ hai ngoài showcase (style guide §25.14).
- Label `bodyMedium` màu `onSurfaceVariant`, value `bodyLarge` màu `onSurface`; dòng Total dùng `titleMedium` cho cả hai phía.
- Số căn phải, label căn trái, dùng `Modifier.weight(1f)` cho label — không dùng khoảng trắng để căn.
- Khoảng cách trong card: `verticalArrangement = Arrangement.spacedBy(component)` (12dp).
- Nút "End Trip" mở `DcConfirmDialog` với đúng nội dung style guide §16: title "End trip?", message "Are you sure you want to end this trip? This action cannot be undone.", confirm "End Trip", dismiss "Cancel", `destructive = true`.
- Bottom bar: dùng `DcNavigationSuite` bọc ngoài cả hai màn showcase (một host chung), **không** đặt navigation bar riêng trong từng màn.

### Bố cục Trip History (style guide §22)

```text
DcScaffold
├── topBar   DcTopAppBar(title = "Trip History", actions = { DcIconButton(Search) })
└── content  Column
    ├── hàng DcChip filter: All / Completed / Pending / Cancelled  (LazyRow, spacedBy(small))
    └── LazyColumn(spacedBy(component), contentPadding = medium)
        └── DcCard(onClick = …) cho từng chuyến:
            ├── hàng 1: DcText("Trip #10232", bodyLarge) ······ DcText("245,000 đ", titleMedium)
            ├── hàng 2: DcText("Dec 12, 2024 · 10:24", bodyMedium, onSurfaceVariant) ··· "12.6 km · 24 min"
            └── DcStatusChip(status.label, tone)
```

- Trạng thái chuyến ánh xạ sang tone: Completed → `Success`, Pending → `Warning`, Cancelled → `Error`, Active → `Info`.
- Khi filter cho ra danh sách rỗng → `DcEmptyState("No trips yet", description = …, action = { DcButton("Start Trip", …) })`. Đây là cách chứng minh empty state hoạt động thật, không chỉ nằm trong catalog.
- Thêm một công tắc trong showcase để xem trạng thái `DcErrorState` và `DcLoadingState` (ví dụ overflow menu "Preview error state") — hoặc đơn giản hơn: hiển thị chúng ở một tab riêng của showcase. Chọn cách ít code hơn.

### Định dạng số và ngày
Dùng `NumberFormat`/`DateTimeFormatter` với `Locale` mặc định, hoặc chuỗi tĩnh trong `TripShowcaseData`. Không tự viết hàm format tiền tệ mới trong `:app`.

---

## T5.2 — Điểm vào showcase

Xác minh trước bằng cách đọc `app/src/main/java/com/dc/android/foundation/MainActivity.kt` và module `:platform:navigation` xem `DesignSystemCatalog` hiện được mở bằng cách nào.

- Nếu đã có một destination catalog: thêm showcase như destination anh em, cùng cơ chế.
- Nếu chưa: thêm một `DcNavigationSuite` host trong showcase với 4 mục *Home / History / Report / More* đúng style guide §11, trong đó Home → Trip Details, History → Trip History, hai mục còn lại → `DcEmptyState("Coming soon")`.
- Showcase chỉ nên xuất hiện ở build debug. Nếu repo chưa có cơ chế tách debug/release cho catalog, **không tự dựng cơ chế mới** — đặt vào cùng chỗ với catalog hiện tại và ghi vào báo cáo như việc còn tồn.

---

## T5.3 — Migrate `GreetingScreen`

**File:** `app/src/main/java/com/dc/android/foundation/greeting/GreetingScreen.kt`

Đọc file trước. Thay mọi lệnh gọi Material trực tiếp bằng component DS tương ứng, giữ nguyên state/action/effect:

| Nếu thấy | Thay bằng |
|---|---|
| `Button(...)` / `OutlinedButton(...)` | `DcButton(variant = …)` |
| `Text(...)` | `DcText(...)` |
| `TextField/OutlinedTextField` | `DcTextField(...)` |
| `Scaffold`, `TopAppBar` | `DcScaffold`, `DcTopAppBar` |
| `.padding(16.dp)` | `.padding(LocalDcSpacing.current.medium)` |
| `Color(...)`, `MaterialTheme.colorScheme` thô trong feature | vai trò semantic đúng, không màu literal |

**Không** đổi ViewModel, `GreetingUiAction`, luồng state, hay chuỗi hiển thị.

---

## T5.4 — Migrate `ArticlesScreen`

**File:** `app/src/main/java/com/dc/android/foundation/articles/ArticlesScreen.kt`

Tương tự T5.3, cộng thêm:
- Danh sách bài viết → `DcCard(onClick = …)` hoặc `DcListRow`, chọn một kiểu và dùng nhất quán.
- Trạng thái loading/empty/error → `DcContentState` hoặc `DcLoadingState`/`DcEmptyState`/`DcErrorState`, **không** tự vẽ `CircularProgressIndicator` trong màn hình.
- Trạng thái offline → `DcOfflineBanner`.
- Pull-to-refresh (nếu có) → `DcPullToRefreshBox`.

Đây là màn hình "migrate hoàn chỉnh" mà style guide §27 yêu cầu — nó có dữ liệu thật, nên nó là bằng chứng mạnh hơn showcase.

---

## T5.5 — Kiểm tra rò rỉ style vào feature

Sau khi migrate, chạy trên thư mục `app/src/main/`:

```bash
rg -n "\b\d+\.dp\b" app/src/main/java --glob '!**/showcase/**'
rg -n "Color\(0x" app/src/main/java
rg -n "\bButton\(|\bOutlinedButton\(|\bTextField\(|\bTopAppBar\(" app/src/main/java
```

Mỗi hit còn lại phải hoặc được sửa, hoặc được ghi chú lý do trong báo cáo cuối (ví dụ: một layout bắt buộc theo ràng buộc hình học). Không được im lặng bỏ qua.

---

## Nghiệm thu Phase 5

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Chạy app trên emulator và kiểm tra bằng mắt:

- [ ] Trip Details: hệ thống thông tin rõ, số căn thẳng cột, nút chính nổi bật, màn hình không chật
- [ ] Trip History: các card đồng nhất, chip trạng thái đọc được, danh sách rỗng ra empty state đúng
- [ ] Dialog "End trip?" đúng anatomy, nút xác nhận màu error
- [ ] Greeting + Articles trông cùng một hệ với showcase (không giống hai app khác nhau)
- [ ] Xoay ngang và font 2.0x không vỡ layout
- [ ] Dark mode: không có mảng trắng lạc lõng, không chữ chìm
- [ ] Không có hành vi nào của Articles/Greeting thay đổi (số lần gọi API, thứ tự state)
