# 00 — Trạng thái hiện tại và các quyết định đã chốt

Mọi thông tin trong tài liệu này lấy từ source thật (đọc file), không từ `graphify-out/`.

---

## 1. Những gì ĐÃ có trong `:ui:designsystem`

```text
ui/designsystem/src/main/java/com/dc/android/foundation/designsystem/
├── tokens/       AccessibilityTokens, ColorTokens, DcSpacing, MotionTokens, ShapeTokens, SpacingTokens
├── theme/        DcTheme (+DcThemeDefaults), DcColors, DcTypography, DcShapes
├── components/   19 file, ~40 API công khai
├── catalog/      DesignSystemCatalog
└── preview/      DcThemePreviews, DcFontScalePreviews, DcWindowPreviews
```

Component công khai hiện có:

| Nhóm | API |
|---|---|
| Text | `DcText`, `DcTextLink` |
| Action | `DcButton` (5 variant + overload slot), `DcIcon`, `DcIconButton`, `DcFab`, `DcExtendedFab` |
| Input | `DcTextField`, `DcPasswordTextField`, `DcDropdownField`, `DcSearchBar`, `DcToggleRow` |
| List/Surface | `DcCard`, `DcChip`, `DcDivider`, `DcListRow` (2 overload) |
| Screen | `DcScaffold`, `DcTopAppBar`, `DcSectionHeader`, `DcBottomActionBar` |
| Navigation | `DcNavigationSuite`, `DcNavigationItem` |
| Overlay | `DcModalBottomSheet`, `DcConfirmDialog`, `DcAlertDialog`, `DcSkeleton` |
| State | `DcLoadingState`, `DcEmptyState`, `DcErrorState`, `DcOfflineBanner`, `DcContentState`, `DcSnackbarEffect` |
| Media | `DcAsyncImage`, `DcAvatar`, `DcPullToRefreshBox` |

**Kết luận:** style guide yêu cầu 13 component; foundation đã có ~85% về mặt tồn tại. Việc cần làm chủ yếu là **chỉnh giá trị thị giác + bổ sung tham số**, không phải viết mới từ đầu.

## 2. Bảng gap — style guide vs source hiện tại

| # | Mục style guide | Hiện tại trong source | Gap | Phase |
|---|---|---|---|---|
| G-01 | Palette xanh `#2563EB` + nền `#F7F8FA` | `ColorTokens` còn nguyên Purple template (`Purple40/80`, `Pink40/80`) | Thay toàn bộ palette | 1 |
| G-02 | Semantic đầy đủ (background/surface/surfaceVariant/outline) | `lightColorScheme(primary, secondary, tertiary)` — 3 màu, còn lại là default M3 | Khai báo scheme đầy đủ, light + dark | 1 |
| G-03 | Type scale 6 style cố định | `DcTypography` chỉ set `bodyLarge`, còn lại rơi về M3 default | Định nghĩa full scale | 1 |
| G-04 | Radius 8/12/16/20 | `ShapeTokens.Small=4, Medium=8, Large=16`; `DcShapes` chỉ map 3 size | Remap 5 size + audit call site | 1 |
| G-05 | Spacing có bước 12dp | `DcSpacing`: 0/4/8/16/24/32/48 — thiếu 12 | Thêm 1 field | 1 |
| G-06 | Border 1dp `#E5E7EB`, elevation 0–1dp | Không có token elevation/stroke | Thêm `SurfaceTokens` | 1 |
| G-07 | Button loading không đổi kích thước | `DcButton` dùng `CircularProgressIndicator` mặc định (~40dp) → nút phình, chữ nhảy | Indicator 18dp + min height cố định | 2 |
| G-08 | TextField có leading icon, placeholder, error text | `DcTextField` không có `leadingIcon`, không có `placeholder`, error đi chung `supportingText` | Thêm tham số | 2 |
| G-09 | Card trắng + viền 1dp + radius 16 | `DcCard` dùng `shapes.medium` + `CardDefaults.cardElevation()` (filled, không viền) | Đổi sang outlined, radius large | 2 |
| G-10 | Status chip nền tint nhạt (Active/Completed/Pending/Cancelled) | Chỉ có `DcChip` = `FilterChip` (chọn được) | Thêm `DcStatusChip` | 2 |
| G-11 | Top bar có icon back 24dp | `DcTopAppBar` render nút back bằng `TextButton` chữ "Back" | Đổi sang `IconButton` + `ArrowBack` | 2 |
| G-12 | Icon button có container mềm cho hành động phá hủy | `DcIconButton` chỉ 1 kiểu | Thêm variant | 2 |
| G-13 | Bottom nav 3–5 mục, selected = primary | `DcNavigationSuite` adaptive, không ràng buộc số mục, không policy màu | Ràng buộc + policy | 3 |
| G-14 | Navigation drawer có header + nhóm + item chọn nền primaryContainer | Không có | Thêm `DcNavigationDrawerContent` | 3 |
| G-15 | Dialog anatomy: radius 20, padding 24, icon tùy chọn, destructive | `DcConfirmDialog`/`DcAlertDialog` có nhưng chưa theo anatomy | Bổ sung + wrapper | 3 |
| G-16 | Bottom sheet bo góc trên 20dp | `DcModalBottomSheet` dùng shape mặc định M3 | Chốt shape/padding | 3 |
| G-17 | Empty/Error state có icon tùy chọn | Chỉ text + action | Thêm `icon` | 4 |
| G-18 | Inline loading / button loading / page loading tách bạch | Chỉ `DcLoadingState` (full block) | Thêm inline | 4 |
| G-19 | Ít nhất một màn hình migrate hoàn chỉnh | `GreetingScreen`, `ArticlesScreen` chưa theo style guide | Migrate | 5 |
| G-20 | Màn Trip details / Trip history mẫu | Không có | Thêm ở `:app` (showcase) | 5 |
| G-21 | `windowBackground` khớp nền app | `themes.xml` dùng `android:Theme.Material.Light.NoActionBar`, `colors.xml` đã bị xóa | Đặt nền + bản `values-night` | 1 |

## 3. Quyết định đã chốt (D-01 → D-12)

Codex **không được** đảo các quyết định này. Nếu thấy quyết định sai, dừng lại và báo, không tự đổi.

### D-01 — Giữ tiền tố `Dc`, không dùng `App`
Prompt gốc đề xuất `AppPrimaryButton`, `AppTopBar`… Repo đã chuẩn hóa `Dc*` và có ~40 call site. Ánh xạ:

| Style guide | DC |
|---|---|
| `AppPrimaryButton` | `DcButton(variant = DcButtonVariant.Primary)` |
| `AppSecondaryButton` | `DcButton(variant = DcButtonVariant.Secondary)` |
| `AppTextButton` | `DcButton(variant = DcButtonVariant.Text)` |
| `AppDestructiveButton` | `DcButton(variant = DcButtonVariant.Destructive)` |
| `AppTextField` / `AppTopBar` / `AppCard` / `AppChip` | `DcTextField` / `DcTopAppBar` / `DcCard` / `DcStatusChip` |
| `AppConfirmDialog` / `AppInfoDialog` / `AppErrorDialog` / `AppDestructiveDialog` | `DcConfirmDialog` / `DcInfoDialog` / `DcErrorDialog` / `DcConfirmDialog(destructive = true)` |
| `AppLoading` / `AppEmptyState` / `AppErrorState` | `DcLoadingState` / `DcEmptyState` / `DcErrorState` |

### D-02 — Không tạo module mới
Prompt gốc đề xuất `core/design-system/`. Repo dùng `:ui:designsystem` với package `theme/`, `tokens/`, `components/`. Giữ nguyên. Không thêm entry vào `settings.gradle.kts`.

### D-03 — Style guide là **giá trị mặc định**, không phải khóa cứng
Palette/typography/shape mới nằm ở tầng default (`DcThemeDefaults`, `ColorTokens`, `DcTypography`, `DcShapes`). App dùng lại foundation vẫn phải đổi được toàn bộ qua `DcTheme(colorScheme = …, typography = …, shapes = …, extendedColors = …, spacing = …)`. Bất kỳ thay đổi nào làm mất khả năng này là sai.

### D-04 — Bắt buộc định nghĩa dark scheme
Style guide chỉ cho palette sáng. Foundation phải chạy được dark (preview `DcThemePreviews` đã bắt buộc cặp light/dark). Giá trị dark do kế hoạch này chỉ định ở Phase 1, không để rơi về M3 default (sẽ lệch tông tím).

### D-05 — `dynamicColor` giữ mặc định `false`
Đã đúng trong source. Không đổi. Dynamic color ghi đè màu thương hiệu và làm preview/screenshot không xác định.

### D-06 — Foundation business-neutral: màn hình Trip nằm ở `:app`
Trip details / Trip history là ví dụ nghiệp vụ. Đặt tại `app/src/main/java/com/dc/android/foundation/showcase/trip/`, dữ liệu tĩnh, không ViewModel/repository/network. Không đưa vào `:ui:designsystem` dưới bất kỳ hình thức nào.

### D-07 — Thay đổi phá vỡ phải migrate trong cùng commit
Ba thay đổi phá vỡ đã biết:
1. `ShapeTokens.Small` 4→8, `Medium` 8→12 → audit mọi call site `MaterialTheme.shapes.*` và `ShapeTokens.*`.
2. `DcColors` thêm field container → cập nhật `DcThemeDefaults.extendedColors` và mọi nơi khởi tạo `DcColors(...)`.
3. `DcCard` đổi từ filled sang outlined → kiểm tra thị giác ở catalog + màn hình đang dùng.

### D-08 — Tham số mới phải có default, thêm ở cuối danh sách
Mọi tham số bổ sung (`leadingIcon`, `placeholder`, `errorText`, `icon`, `variant`…) đều có giá trị mặc định giữ nguyên hành vi cũ, và đặt sau các tham số hiện có để không phá call site dùng đối số vị trí.

### D-09 — Không dựa vào màu để truyền trạng thái
Status chip phải có **text**, không chỉ màu nền. Error state phải có **text**, không chỉ viền đỏ. Chip có thêm chấm chỉ báo để phân biệt khi mù màu. (Style guide §23.)

### D-10 — Touch target 48dp giải quyết ở tầng chính sách, không nhồi vào mọi component
`DcChip` hiện ép `defaultMinSize(48.dp)` làm chip cao bất thường. Material3 chip đã tự áp `minimumInteractiveComponentSize()` cho vùng chạm. Bỏ ép 48dp về mặt **thị giác**, giữ vùng chạm ≥48dp. Ghi rõ trong tài liệu DS-02 rằng đây là ngoại lệ có kiểm soát.

### D-11 — Không thêm dependency mới
`material-icons-core` đã có sẵn (`ArrowBack`, `Menu`, `Search`, `Close`, `MoreVert`, `Check`, `Delete`, `Favorite`, `Settings`, `Home`… đều nằm trong core). Không thêm `material-icons-extended`. Nếu thiếu icon, dùng icon core gần nhất hoặc để caller truyền `ImageVector`.

### D-12 — Không đổi hành vi
Refactor này chỉ đụng lớp thị giác và chữ ký component. Không đổi luồng UDF, không đổi state, không đổi thứ tự gọi, không đổi nội dung chuỗi hiển thị của feature (trừ khi phải thêm `contentDescription`).

## 4. Ánh xạ cấu trúc thư mục: prompt gốc → repo

```text
prompt gốc                          repo thật
core/design-system/theme/Color.kt   → tokens/ColorTokens.kt + theme/DcTheme.kt (scheme) + theme/DcColors.kt
core/design-system/theme/Typography → theme/DcTypography.kt
core/design-system/theme/Shape.kt   → tokens/ShapeTokens.kt + theme/DcShapes.kt
core/design-system/theme/Dimension  → tokens/SpacingTokens.kt + tokens/DcSpacing.kt + tokens/SurfaceTokens.kt (mới)
core/design-system/theme/Theme.kt   → theme/DcTheme.kt
core/design-system/component/*      → components/Dc*.kt (gộp theo nhóm, không 1 file/1 component)
```

Giữ quy ước gộp file theo nhóm đang có (`DcListComponents.kt`, `DcScreenComponents.kt`…). Không tách 13 file mới.
