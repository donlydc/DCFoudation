# Kế hoạch triển khai UI Style Guide — gói thực thi cho Codex

**Ngày lập:** 2026-09-05
**Người thực thi:** Codex (agent), tuân thủ `AGENTS.md` tại repo root
**Module chính:** `:ui:designsystem` — phụ: `:app`
**Nguồn yêu cầu:** `plan/ui-style-guide/references/android-ui-style-guide.md` (bản sao nguyên văn prompt gốc)
**Trạng thái source đã xác minh:** commit `f29195e` + working tree hiện tại (đọc trực tiếp source, không lấy từ `graphify-out/`)

---

## 1. Mục tiêu

Áp dụng style guide "Android App UI Style Guide" (Clean / Familiar / Rounded / Easy to read / Material 3 inspired) lên **design system đã có sẵn** của foundation, thay vì dựng một design system mới.

Kết quả cuối:

1. Bảng màu, typography, shape, spacing, elevation/border tập trung một chỗ, khớp style guide.
2. Bộ component `Dc*` hiện có được chỉnh để đúng spec (kích thước, bo góc, viền, trạng thái).
3. Bổ sung các component còn thiếu so với style guide (status chip, navigation drawer, dialog theo anatomy, icon button có variant).
4. Hai màn hình tham chiếu (Trip details, Trip history) + hai màn hình thật của app được migrate.
5. Catalog + Preview phủ toàn bộ component và toàn bộ trạng thái.

## 2. Điều kiện tiên quyết — đọc trước khi sửa dòng code đầu tiên

Đọc theo đúng thứ tự này, không đọc thừa:

1. `AGENTS.md` (root) — quy tắc làm việc, thẩm quyền kiến trúc, definition of done.
2. `plan/ui-style-guide/00-context-and-decisions.md` — trạng thái hiện tại + các quyết định đã chốt. **Bắt buộc.**
3. `architecture/07-design-system/README.md` — invariants DS-01 → DS-04.
4. `plan/ui-style-guide/references/android-ui-style-guide.md` — spec gốc, dùng để tra số liệu.
5. Tài liệu phase đang làm.

Không preload toàn bộ `architecture/`. Không preload toàn bộ `:ui:designsystem`.

## 3. Bản đồ phase

| Phase | Tài liệu | Phạm vi | Phụ thuộc |
|---|---|---|---|
| 1 | `01-phase-1-tokens-and-theme.md` | Token màu/typography/shape/spacing/elevation + `DcTheme` + resource theme của app | — |
| 2 | `02-phase-2-core-components.md` | Button, TextField, Card, Chip/StatusChip, IconButton, Text, TopAppBar, SectionHeader, ListRow | Phase 1 |
| 3 | `03-phase-3-navigation-and-overlays.md` | Bottom navigation, Navigation drawer, Dialog system, Bottom sheet | Phase 2 |
| 4 | `04-phase-4-states-catalog-previews.md` | Loading / Empty / Error / Skeleton, catalog, preview matrix | Phase 2, 3 |
| 5 | `05-phase-5-reference-screens.md` | Trip details + Trip history showcase, migrate Greeting + Articles | Phase 4 |
| ✔ | `06-verification-and-done.md` | Kiểm tra, tiêu chí nghiệm thu, cập nhật tài liệu, báo cáo | tất cả |

**Thực thi tuần tự.** Không nhảy phase. Sau mỗi phase: build + test + commit riêng.

## 4. Guardrails — những điều Codex KHÔNG được làm

| Cấm | Lý do |
|---|---|
| Tạo module `core/design-system` như trong prompt gốc | Repo đã có `:ui:designsystem`; xem D-02 |
| Đổi tên component `Dc*` → `App*` | Phá toàn bộ call site; xem D-01 |
| Hard-code `Color(0xFF2563EB)` trong feature UI | Vi phạm DS-01 |
| Dùng `.dp` literal trong feature UI | Vi phạm DS-03 |
| Đưa domain "trip / chuyến xe" vào `:ui:designsystem` | Foundation phải business-neutral (INV) |
| Xóa/đổi chữ ký công khai đang được dùng mà không có bước migrate trong cùng commit | Phá `:app` và catalog |
| Sửa business logic, ViewModel, repository, network, DB | Ngoài phạm vi refactor UI |
| Bật `dynamicColor = true` mặc định | Ghi đè màu thương hiệu; xem D-05 |
| Thêm gradient, glassmorphism, shadow nặng, icon trang trí cỡ lớn | Trái style guide §2 |
| Chạy `graphify .` (regenerate toàn bộ) | Chỉ chạy `graphify update .` ở cuối |

## 5. Quy ước commit

Mỗi phase một commit, prefix `feat(ds):` hoặc `refactor(ds):`:

```text
feat(ds): phase 1 — apply style-guide tokens and theme
feat(ds): phase 2 — align core components with style guide
feat(ds): phase 3 — navigation drawer, dialog system, bottom sheet
feat(ds): phase 4 — states, catalog and preview coverage
feat(app): phase 5 — reference screens and screen migration
docs(ds): update design-system architecture docs
```

Không commit khi build đỏ. Không gộp 5 phase thành một commit.

## 6. Cấu trúc thư mục kế hoạch

```text
plan/ui-style-guide/
├── README.md                              ← file này
├── 00-context-and-decisions.md            trạng thái đã verify + 12 quyết định chốt
├── 01-phase-1-tokens-and-theme.md
├── 02-phase-2-core-components.md
├── 03-phase-3-navigation-and-overlays.md
├── 04-phase-4-states-catalog-previews.md
├── 05-phase-5-reference-screens.md
├── 06-verification-and-done.md
└── references/
    └── android-ui-style-guide.md          bản sao nguyên văn prompt gốc
```

## 7. Quan hệ với kế hoạch đã có

`plan/DESIGN_SYSTEM_UI_PLAN.md` (2026-08-23) giải quyết **cơ chế tùy chỉnh** (4 cấp: token override → tham số/variant → slot → escape hatch). Phần lớn cơ chế đó đã nằm trong source: `DcTheme` đã nhận `colorScheme/typography/shapes/extendedColors/spacing`.

Kế hoạch này **không thay thế** kế hoạch đó. Nó điền **nội dung thị giác** vào cơ chế đã có: giá trị mặc định của foundation phải là style guide này, và mọi giá trị đó vẫn phải override được từ app qua `DcTheme(...)`. Nếu một thay đổi trong kế hoạch này làm mất khả năng override, thay đổi đó sai.
