# 06 — Kiểm tra, nghiệm thu và bàn giao

---

## 1. Lệnh kiểm tra

Chạy từ gốc repo. Trên Windows dùng `.\gradlew.bat`.

```bash
./gradlew :ui:designsystem:compileDebugKotlin
```

```bash
./gradlew :ui:designsystem:testDebugUnitTest :app:testDebugUnitTest
```

```bash
./gradlew :app:assembleDebug
```

```bash
./gradlew :ui:designsystem:lintDebug :app:lintDebug
```

```bash
./gradlew :ui:designsystem:connectedDebugAndroidTest
```

Ghi chú:
- Repo **không** cấu hình detekt/ktlint (đã kiểm tra `build-logic/`). Đừng đi tìm hay tự thêm.
- `connectedDebugAndroidTest` cần emulator/thiết bị. Nếu không có, ghi rõ "đã bỏ qua vì không có thiết bị" trong báo cáo — không báo là đã pass.
- Nếu bất kỳ lệnh nào đỏ: sửa trong cùng phase, không đẩy sang phase sau.

## 2. Ma trận kiểm tra thị giác

Kiểm ở **mỗi** tổ hợp dưới đây, dùng preview hoặc chạy app:

| Trục | Giá trị |
|---|---|
| Theme | light, dark |
| Font scale | 1.0×, 1.5×, 2.0× |
| Bề rộng | 360dp, 700dp, 1200dp |

Điểm cần soi:
- Chữ không bị cắt, không chồng lấn, không tràn ngang.
- Viền card còn nhìn thấy ở dark (không chìm vào nền).
- Chữ phụ (`onSurfaceVariant`) vẫn đọc được, không mờ quá.
- Ở 1200dp: form/nội dung không kéo giãn vô tận (style guide §24) — dùng `widthIn(max = …)` ở tầng màn hình nếu cần.

## 3. Kiểm tra tiếp cận (style guide §23)

- [ ] Mọi phần tử bấm được có vùng chạm ≥48dp (`DcStatusChip` là nhãn, không tính).
- [ ] Mọi icon có nghĩa đều có `contentDescription`; icon trang trí truyền `null`.
- [ ] Không có trạng thái nào chỉ truyền đạt bằng màu: chip có text, error state có text, offline banner có icon + text.
- [ ] Tương phản text/nền ≥ 4.5:1 cho text thường, ≥ 3:1 cho text lớn. Cặp cần kiểm kỹ:
  - `onSurfaceVariant #6B7280` trên `surface #FFFFFF` → 4.83:1 ✔
  - `onWarningContainer` trên `warningContainer` (light)
  - `onSurfaceVariant #9CA3AF` trên `surface #14171D` (dark)
- [ ] Loading không phải tín hiệu duy nhất cho thao tác đổi trạng thái lớn — có text kèm theo.

## 4. Đối chiếu Deliverables của style guide (§27)

| # | Yêu cầu | Nơi hoàn thành |
|---|---|---|
| 1 | Color token tập trung | `tokens/ColorTokens.kt`, `theme/DcTheme.kt`, `theme/DcColors.kt` |
| 2 | Typography token | `theme/DcTypography.kt` |
| 3 | Shape token | `tokens/ShapeTokens.kt`, `theme/DcShapes.kt` |
| 4 | Spacing / dimension token | `tokens/SpacingTokens.kt`, `tokens/DcSpacing.kt`, `tokens/SurfaceTokens.kt` |
| 5 | Button dùng lại | `DcButton`, `DcPrimaryActionButton` |
| 6 | Text field dùng lại | `DcTextField`, `DcPasswordTextField` |
| 7 | Top app bar | `DcTopAppBar` |
| 8 | Bottom navigation | `DcNavigationSuite` |
| 9 | Card / list item | `DcCard`, `DcListRow` |
| 10 | Status chip | `DcStatusChip` |
| 11 | Icon button | `DcIconButton` (3 variant) |
| 12 | Hệ dialog | `DcConfirmDialog`, `DcInfoDialog`, `DcErrorDialog`, `DcAlertDialog` |
| 13 | Loading state | `DcLoadingState` (Inline/Screen), `DcSkeleton` |
| 14 | Empty state | `DcEmptyState` |
| 15 | Error state | `DcErrorState` |
| 16 | ≥1 màn hình migrate hoàn chỉnh | `ArticlesScreen`, `GreetingScreen` |
| 17 | Preview cho component quan trọng | các file `components/*.kt` + `DesignSystemCatalog` |

Bổ sung ngoài style guide (điểm mạnh của foundation, ghi vào báo cáo): navigation adaptive (bar/rail/drawer), `DcContentState`, `DcOfflineBanner`, `DcPullToRefreshBox`, cơ chế override theme 4 cấp.

## 5. Cập nhật tài liệu kiến trúc

Chỉ cập nhật khi quy tắc **bền vững** thay đổi — đúng như vậy trong lần này:

| File | Nội dung cần sửa |
|---|---|
| `architecture/07-design-system/tokens.md` | Palette mới, elevation/stroke token, bước spacing 12dp |
| `architecture/07-design-system/typography.md` | Type scale đầy đủ + bảng "style nào dùng ở đâu" |
| `architecture/07-design-system/spacing-shapes-elevation.md` | Shape scale 4/8/12/16/20 + bảng ánh xạ thành phần → shape; chính sách viền-trước-bóng |
| `architecture/07-design-system/semantic-theme.md` | Vai trò màu light/dark, quy tắc override từ app |
| `architecture/07-design-system/components.md` | Component mới (`DcStatusChip`, `DcNavigationDrawerContent`, `DcPrimaryActionButton`, wrapper dialog) + variant mới |
| `architecture/07-design-system/adaptive-accessibility.md` | Ngoại lệ DS-02 cho chip (vùng chạm ≠ kích thước vẽ) |
| `architecture/07-design-system/previews-and-catalog.md` | Cấu trúc catalog 14 mục |
| `architecture/23-decision-log.md` | Một mục ghi quyết định D-01…D-12 kèm ngày và lý do |

Không viết lại tài liệu cho vui. Sửa đúng chỗ đã lệch với source.

## 6. Graphify

Chạy **một lần duy nhất**, ở cuối, sau khi tất cả code đã merge:

```bash
graphify update .
```

Không chạy `graphify .` (regenerate toàn bộ).

## 7. AgentMemory

Theo `AGENTS.md` §5, lưu ngay khi một quyết định không hiển nhiên được chốt. Với kế hoạch này, đáng lưu:

- Vì sao `dynamicColor` giữ `false` trong một foundation có thương hiệu.
- Vì sao chip không ép 48dp mà vẫn thỏa DS-02.
- Vì sao màn hình Trip nằm ở `:app` chứ không ở `:ui:designsystem`.
- Cạm bẫy: `ShapeTokens.Small/Medium` đổi giá trị → phải audit mọi `MaterialTheme.shapes.*`.

Không lưu lại nội dung đã có trong tài liệu này.

## 8. Báo cáo cuối (bắt buộc, theo style guide §27)

Nộp một bản tóm tắt gồm đúng 5 mục:

1. **Files added** — đường dẫn đầy đủ.
2. **Files modified** — đường dẫn + một dòng nói đã đổi gì.
3. **Main design decisions** — kèm những chỗ **khác với prompt gốc** và lý do (tối thiểu: giữ tiền tố `Dc`, không tạo module `core/design-system`, dialog không có tham số `visible`, navigation adaptive thay vì `NavigationBar` cứng, dark scheme tự định nghĩa).
4. **UI behavior intentionally kept unchanged** — ví dụ: luồng UDF của Articles, thứ tự gọi API, nội dung chuỗi hiển thị.
5. **Remaining screens / việc còn tồn** — ví dụ: tách showcase sang build debug, màn hình chưa migrate, `connectedAndroidTest` chưa chạy.

## 9. Definition of done (theo `AGENTS.md` §8)

- [ ] Ranh giới kiến trúc còn nguyên: `:ui:designsystem` không phụ thuộc feature, không chứa domain nghiệp vụ
- [ ] Code biên dịch được
- [ ] Unit test liên quan xanh; androidTest xanh hoặc ghi rõ lý do bỏ qua
- [ ] Android lint không phát sinh cảnh báo mới
- [ ] Feature UI dùng DS thay vì tự style (đã kiểm bằng `rg` ở T5.5)
- [ ] Hilt binding không đổi (phase này không đụng DI)
- [ ] Không đổi quyền sở hữu coroutine/dispatcher
- [ ] Không commit secret hay cấu hình máy cá nhân
- [ ] Tài liệu kiến trúc đã cập nhật (mục 5)
- [ ] `graphify update .` đã chạy
- [ ] Báo cáo 5 mục đã nộp
