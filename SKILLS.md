# Skills đang dùng

## Graphify

- Dùng `graphify-out/graph.json` làm bản đồ kiến trúc sẵn có trước khi tìm kiếm rộng trong mã nguồn.
- Với câu hỏi về kho mã, ưu tiên `graphify query`, `path`, hoặc `explain`; chỉ kiểm tra mã nguồn đích để xác thực kết quả đồ thị.
- Sau thay đổi làm ảnh hưởng cấu trúc, chạy `graphify update .` để đồng bộ đồ thị.
- Không suy diễn quan hệ không có trong đồ thị hoặc mã nguồn đã xác thực.

## CodeGraph (BẮT BUỘC mỗi phiên)

- Mọi câu hỏi về mã nguồn (vị trí, cách hoạt động, phạm vi ảnh hưởng của thay đổi) **phải** dùng CodeGraph trước `Grep`, `Glob`, `find` hoặc mở tệp.
- Công cụ MCP (ưu tiên): `codegraph_explore` với câu hỏi tự nhiên hoặc tên symbol/tệp; kết quả đã chứa mã nguồn có số dòng nên không đọc lại tệp đó.
- Shell dự phòng: `codegraph explore "<symbol hoặc câu hỏi>"`; tra cứu hẹp: `codegraph node`, `callers`, `callees`, `impact`, `affected`.
- Thứ tự công cụ: CodeGraph → Graphify (bản đồ kiến trúc) → Grep/Read có mục tiêu. Chỉ dùng Grep/Read trực tiếp cho tệp không phải mã (tài liệu, Gradle, XML) hoặc khi CodeGraph không trả về kết quả liên quan.
- Đầu phiên: nếu thiếu lệnh, cài `npm install -g @colbymchenry/codegraph`; nếu thiếu `.codegraph/`, chạy `codegraph init -y`; nếu đã có, chạy `codegraph sync`. `.codegraph/` là cục bộ và đã được git-ignore.
- Sau thay đổi lớn chạy `codegraph sync`.
- Áp dụng cùng quy tắc trong prompt của mọi subagent có khám phá mã.
- Cấu hình dự án: `.mcp.json` (MCP server `codegraph`), `.claude/settings.json` (quyền `mcp__codegraph__*` và hook `UserPromptSubmit`).

## Agents và kế hoạch triển khai

- Kế hoạch R0–R9 chia theo agent: `plan/AGENT_EXECUTION_PLAN.md` (nguồn yêu cầu: `plan/FOUNDATION_MASTER_PLAN.md`, chỉ trích mục cần bằng `sed`).
- Agent: `.claude/agents/` — `architecture-reader`, `architecture-writer`, `implementation-worker`, `consistency-checker`, `wp-reviewer` (đều Sonnet 5.5 — ưu tiên mặc định); `critical-architecture-auditor` (Opus, chỉ leo thang). Mọi WP được `wp-reviewer` (Sonnet) review.
- Bàn giao giữa agent qua `reports/wp/<WP-ID>.md`, không qua log thô.

## AgentMemory

- Tra cứu bộ nhớ theo ngữ cảnh dự án trước khi bắt đầu một tác vụ không tầm thường.
- Chỉ lưu các quyết định đã chốt, lý do của quyết định, hoặc ràng buộc khó thấy; không lưu bí mật, trạng thái tạm thời, hay nội dung đã đọc được trực tiếp từ mã nguồn.
- Khi người dùng sửa cách làm, ghi thành *lesson* thay vì một ghi nhớ thông thường.

## Trạng thái phiên này

- Đã nạp hướng dẫn của Graphify và AgentMemory.
- Các công cụ AgentMemory (`memory_smart_search`, `memory_save`) hiện chưa được phiên này cung cấp, nên chưa thể truy vấn hoặc lưu một bản ghi dài hạn.
