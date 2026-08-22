# Skills đang dùng

## Graphify

- Dùng `graphify-out/graph.json` làm bản đồ kiến trúc sẵn có trước khi tìm kiếm rộng trong mã nguồn.
- Với câu hỏi về kho mã, ưu tiên `graphify query`, `path`, hoặc `explain`; chỉ kiểm tra mã nguồn đích để xác thực kết quả đồ thị.
- Sau thay đổi làm ảnh hưởng cấu trúc, chạy `graphify update .` để đồng bộ đồ thị.
- Không suy diễn quan hệ không có trong đồ thị hoặc mã nguồn đã xác thực.

## AgentMemory

- Tra cứu bộ nhớ theo ngữ cảnh dự án trước khi bắt đầu một tác vụ không tầm thường.
- Chỉ lưu các quyết định đã chốt, lý do của quyết định, hoặc ràng buộc khó thấy; không lưu bí mật, trạng thái tạm thời, hay nội dung đã đọc được trực tiếp từ mã nguồn.
- Khi người dùng sửa cách làm, ghi thành *lesson* thay vì một ghi nhớ thông thường.

## Trạng thái phiên này

- Đã nạp hướng dẫn của Graphify và AgentMemory.
- Các công cụ AgentMemory (`memory_smart_search`, `memory_save`) hiện chưa được phiên này cung cấp, nên chưa thể truy vấn hoặc lưu một bản ghi dài hạn.
