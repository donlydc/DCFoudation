# MODULE_GUIDES.md — Mục lục hướng dẫn triển khai từng module

Mỗi module có một tệp `IMPLEMENTATION.md` ngắn, cùng bố cục: **Khi nào dùng → Thêm vào dự án → Nối dây → Cách dùng → Test → Lỗi hay gặp**. Đọc đúng tệp của module cần dùng, không cần đọc cả bộ.

Thứ tự đọc cho một module: `STRUCTURE.md` (vai trò, ranh giới) → `IMPLEMENTATION.md` (cách làm) → tài liệu `architecture/` được trỏ tới. Bản đồ toàn repo: [`STRUCTURE.md`](STRUCTURE.md). Khi mâu thuẫn, `architecture/` và mã nguồn là chuẩn.

## Chọn tệp theo việc cần làm

| Tôi cần… | Đọc |
|---|---|
| Thêm feature/màn hình, binding Hilt, route | [`app/IMPLEMENTATION.md`](app/IMPLEMENTATION.md) |
| Dùng/định nghĩa lỗi chung | [`foundation/IMPLEMENTATION.md`](foundation/IMPLEMENTATION.md) |
| Dispatcher, retry, SingleFlight, Flow cho UI | [`runtime/concurrency/IMPLEMENTATION.md`](runtime/concurrency/IMPLEMENTATION.md) |
| Việc nền định kỳ, báo thức, nhắc giờ | [`runtime/scheduling/IMPLEMENTATION.md`](runtime/scheduling/IMPLEMENTATION.md) |
| Lưu cấu hình nhỏ (DataStore) | [`data/preferences/IMPLEMENTATION.md`](data/preferences/IMPLEMENTATION.md) |
| Gọi HTTP | [`data/network/IMPLEMENTATION.md`](data/network/IMPLEMENTATION.md) |
| Lưu dữ liệu có cấu trúc / offline | [`data/database/IMPLEMENTATION.md`](data/database/IMPLEMENTATION.md) |
| Màn hình, component, theme | [`ui/designsystem/IMPLEMENTATION.md`](ui/designsystem/IMPLEMENTATION.md) |
| Điểm đến điều hướng | [`platform/navigation/IMPLEMENTATION.md`](platform/navigation/IMPLEMENTATION.md) |
| Xin quyền runtime | [`platform/permission/IMPLEMENTATION.md`](platform/permission/IMPLEMENTATION.md) |
| Lưu bí mật | [`security/IMPLEMENTATION.md`](security/IMPLEMENTATION.md) |
| Log / analytics / crash | [`observability/IMPLEMENTATION.md`](observability/IMPLEMENTATION.md) |
| Viết test (rule, fake) | [`testing/IMPLEMENTATION.md`](testing/IMPLEMENTATION.md) |
| Module Gradle mới, plugin `dc.*`, sinh feature | [`build-logic/IMPLEMENTATION.md`](build-logic/IMPLEMENTATION.md) |

## Thứ tự dựng một app mới (gợi ý)

1. `build-logic` — chọn plugin, tạo module (nếu cần).
2. `foundation`, `runtime/concurrency` — lỗi và dispatcher.
3. `observability`, `security` — hạ tầng cắt ngang.
4. `data/preferences`, `data/network`, `data/database` — nguồn dữ liệu.
5. `ui/designsystem`, `platform/navigation`, `platform/permission` — UI và nền tảng.
6. `runtime/scheduling` — tác vụ nền (khi cần).
7. `app` — nối Hilt, điều hướng, rồi thêm feature; `testing` song song với mỗi bước.

## Bảo trì

Đổi API công khai hoặc cách nối dây của một module → cập nhật `IMPLEMENTATION.md` của module đó trong cùng thay đổi. Thêm module mới → thêm tệp này vào bảng trên và vào `STRUCTURE.md` gốc.
