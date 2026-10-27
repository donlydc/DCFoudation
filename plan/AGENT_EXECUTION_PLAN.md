# Agent Execution Plan — DC Android Foundation (R0–R9)

> Phân rã `plan/FOUNDATION_MASTER_PLAN.md` (MP) thành các **work package (WP)** gán cho từng agent,
> để triển khai đúng ranh giới kiến trúc với chi phí token thấp nhất.
> MP vẫn là nguồn yêu cầu; tài liệu này chỉ quy định **ai làm, đọc gì, sửa ở đâu, trả gì, theo thứ tự nào**.
> Agent đang hoạt động: `.claude/agents/*.md` (bản cập nhật của bộ agent gốc trong `plan/.claude/agents/`).
> Chính sách định tuyến model và điều kiện leo thang Opus: `plan/CLAUDE_IMPLEMENTATION_PLAN.md` §1.

---

## 1. Nguyên tắc tiết kiệm token

1. **Không agent nào đọc toàn bộ MP (86 KB).** Mỗi WP chỉ trích đúng mục cần:
   ```bash
   sed -n '/^### R3 /,/^### R4 /p'   plan/FOUNDATION_MASTER_PLAN.md   # một phase
   sed -n '/^### 4\.2\./,/^### 4\.3\./p' plan/FOUNDATION_MASTER_PLAN.md # một module backlog
   ```
2. **CodeGraph trước Read/Grep**: một `codegraph_explore` gom nhiều symbol, trả source + callers + blast radius.
3. **Đầu tư một lần ở R1, dùng lại mãi.** Sau R1 mọi agent đọc `architecture/modules/<m>.md` +
   `context-map.yaml` thay vì khám phá lại mã nguồn.
4. **Bàn giao bằng tệp nhỏ, không bằng hội thoại**: mỗi WP ghi `reports/wp/<WP-ID>.md` (mẫu §6, ≤ 120 dòng);
   agent kế tiếp chỉ đọc tệp đó. Context packet của `architecture-reader` ≤ 1.200 token.
5. **Định tuyến model** (theo yêu cầu người dùng, ưu tiên **Sonnet 5.5**): mọi agent mặc định Sonnet 5.5;
   **Opus chỉ khi gặp điều kiện leo thang** (§2.1). Haiku không dùng mặc định. Tiết kiệm token đến từ
   phạm vi đọc hẹp, CodeGraph và bàn giao bằng tệp, không từ hạ model.
6. **Log build không vào context nguyên văn**: lưu `reports/logs/` (git-ignore), chỉ chuyển lệnh, exit code,
   số test, lỗi gốc đầu tiên.
7. **Không spawn agent cho việc nhỏ** (≤ 3 tệp, một module): phiên chính tự làm.
8. **Song song chỉ khi không trùng module/public API**, mỗi nhánh dùng `isolation: worktree`.

## 2. Đội agent

| Agent | Vai trò MP §10.1 | Model | Quyền ghi | Dùng cho |
|---|---|---|---|---|
| `architecture-reader` | Discovery | Sonnet 5.5 | chỉ `reports/` | Kiểm kê, context packet, mock task kiểm tra tài liệu |
| `architecture-writer` | Architect (thường) + Documentation | Sonnet 5.5 | docs, `plan/`, `reports/` | Template, module/feature docs, context-map, HANDOVER, design delta |
| `implementation-worker` | Implementation | Sonnet 5.5 | đường dẫn của WP | Code + test + module doc delta |
| `consistency-checker` | Verification + gate review | Sonnet 5.5 | `reports/` | Chạy Gradle/docs-check/boundary-check, PASS/FAIL/NOT_RUN |
| `wp-reviewer` | Reviewer (mỗi WP) | Sonnet 5.5 | `reports/` | Review diff + báo cáo của **mọi** WP: APPROVE / CHANGES_REQUESTED |
| `critical-architecture-auditor` | Architect (leo thang) | Opus 5.5 | không | Quyết định cấp cao, chỉ theo §2.1 |
| *(phiên chính)* | Orchestrator | — | tất cả | Điều phối WP, việc nhỏ, commit/push, hỏi người dùng |

### 2.1. WP được phép gọi Opus (dự kiến trước)

| WP | Điều kiện leo thang (`CLAUDE_IMPLEMENTATION_PLAN.md` §1.1) |
|---|---|
| R3.0 cancellation/retry/ownership scope | (3) concurrency/lifecycle có nhiều thiết kế an toàn khác nhau |
| R5.0a D-01 ownership `Article*`/`DcDatabase` | (2) public API + (6) migration không đảo ngược |
| R5.0b D-04 provider SecureStorage | (3) security |
| R7.2 chính sách public API/publishing | (2) tương thích API xuyên module |

Ngoài bảng này, Opus chỉ được gọi khi phát sinh điều kiện (1), (4) hoặc (5). Mục tiêu: ≤ 1 lần gọi Opus mỗi phase.
Đầu vào cho Opus luôn là câu hỏi hẹp + evidence + tối đa 3 phương án do Sonnet chuẩn bị sẵn.

## 3. Luồng phase và wave song song

```text
Wave 0  R0  tuần tự
Wave 1  R1  R1.0 → {R1.1a | R1.1b | R1.1c | R1.1d | R1.2} song song → R1.3 → R1.4 → R1.5 → gate
Wave 2  R2  {R2.1 | R2.2} song song → R2.3 → R2.4 → gate
Wave 3  R3 ║ R4   hai luồng song song (worktree), không chạm chung public API
Wave 4  R5  R5.0 (ADR) → {R5.1 | R5.2 | R5.3} song song → R5.4
Wave 5  R6 → R7
Wave 6  R8, R9  chỉ khi người dùng xác nhận consumer thật
```

## 3a. Phân chia lại theo luồng song song (hiệu lực từ sau R1)

> Theo yêu cầu người dùng: các WP còn lại (R2–R7) chạy **song song theo luồng (lane)**, mỗi lane sở hữu một
> tập đường dẫn không giao nhau; **Sonnet review một lần cho mỗi lane** (`wp-reviewer`), và
> **`critical-architecture-auditor` (Opus) review toàn bộ một lượt khi mọi lane hoàn tất**.
> Các điểm leo thang Opus riêng lẻ ở §2.1 được gộp vào lượt review cuối này: quyết định thiết kế (R3.0, R5.0,
> R6.0, R7.2) do `architecture-writer` (Sonnet) soạn trong lane, ghi DEC ở trạng thái PROPOSED, Opus chốt ở lượt cuối.

### Wave A — chạy song song ngay sau gate R1

| Lane | WP gộp | Agent | Đường dẫn được sửa (độc quyền) |
|---|---|---|---|
| A1 Build & CI guard | R2.1 (KD-02), R2.3 | implementation-worker | `build-logic/**`, `.github/**`, `tools/**`, `architecture/dependency-rules.md`, `architecture/modules/build-logic.md`, `architecture/19-*` |
| A2 Platform & observability tests | R2.2 | implementation-worker | `platform/**`, `observability/**`, `architecture/modules/platform-*.md`, `architecture/modules/observability.md` |
| A3 Core & runtime | R3.0 → R3.1, R3.2, R3.3 | architecture-writer → implementation-worker | `foundation/**`, `runtime/**`, `testing/**`, `architecture/modules/{foundation,runtime-*,testing}.md`, `architecture/09-*`, `11-*`, `14-*` |
| A4 Design system | R4.1 → R4.5 | architecture-reader → architecture-writer → implementation-worker | `ui/designsystem/**`, `architecture/modules/ui-designsystem.md`, `architecture/07-design-system/**` |

### Wave B — sau Wave A (phụ thuộc hợp đồng `AppError`/concurrency của A3)

| Lane | WP gộp | Đường dẫn |
|---|---|---|
| B1 Network & preferences | R5.1 | `data/network/**`, `data/preferences/**`, docs tương ứng |
| B2 Security & observability | R5.2 | `security/**`, `observability/**`, docs tương ứng |
| B3 Database | R5.0 (D-01) → R5.3 | `data/database/**`, docs tương ứng, DEC PROPOSED |

### Wave C — sau Wave B

| Lane | WP gộp | Đường dẫn |
|---|---|---|
| C1 Navigation & permission | R6.0 → R6.1 | `platform/**`, docs tương ứng |
| C2 Reference app journeys | R6.2 | `app/**`, `architecture/modules/app.md`, `architecture/features/**` |
| C3 Developer experience | R7.1, R7.2, R7.3 | `build-logic/**` (generator), `README.md`, `CHANGELOG.md`, DEC PROPOSED (qua mục D) |

### Lượt triển khai gộp (single pass, Sonnet 5.5)

Theo yêu cầu người dùng, Wave B và Wave C chạy **cùng một lượt** (B1, B2, B3, C1, C2, C3 song song) cộng một agent
`DOC` hợp nhất tài liệu (là agent duy nhất được sửa `architecture/**`). Các lane đọc hợp đồng `:foundation`/`:runtime:*`
trong working tree (lane A3), không sửa `foundation/**`, `runtime/**`, `testing/**`. Lane C2 dùng API navigation hiện có;
C1 chỉ thêm API theo hướng tương thích. Mỗi lane → Sonnet review → commit; sau cùng Opus final review một lượt.

### Quy tắc lane

- Tệp dùng chung (`architecture/context-map.yaml`, `module-map.md`, `22-known-deviations.md`, `23-decision-log.md`,
  `HANDOVER.md`): lane **không sửa trực tiếp**, chỉ ghi đề xuất vào mục D của báo cáo lane; phiên chính hợp nhất
  sau mỗi wave (tránh xung đột khi chạy song song).
- Mỗi lane kết thúc: `reports/wp/<LANE>.md` → `wp-reviewer` (Sonnet) → `reports/wp/<LANE>.review.md` → commit khi APPROVE.
- Kết thúc mọi wave: `consistency-checker` chạy `tools/check_architecture.py` + kiểm tra ranh giới, rồi
  **Opus final review** → `reports/final-review.md`; các phát hiện blocking giao lại đúng lane để sửa.
- Build Android hiện **NOT_RUN** (mạng chặn `dl.google.com`, `api.foojay.io`): mọi thay đổi Kotlin ghi rõ chưa biên dịch;
  lane không được tuyên bố PASS. Khi môi trường mở mạng, chạy lại `consistency-checker` toàn bộ trước khi merge.

## 4. Work packages

"Đọc" là danh sách tối đa; ngoài ra agent chỉ mở thêm tệp khi CodeGraph chỉ ra phụ thuộc.
**Mọi WP kết thúc bằng `wp-reviewer` (Sonnet)** → `reports/wp/<WP-ID>.review.md`. CHANGES_REQUESTED → agent
thực hiện WP sửa đúng các mục blocking (tối đa 2 vòng; vòng thứ 3 chuyển phiên chính quyết định).
Gate phase: `consistency-checker` (Sonnet) + phiên chính đối chiếu; không có review Opus định kỳ.
WP liên quan (cùng phase, cùng agent, cùng loại tệp) có thể gộp một lần review để giảm token.

### R0 — Baseline & risk closure (P0)

| WP | Agent | Đọc | Việc | Đầu ra |
|---|---|---|---|---|
| R0.1 | architecture-reader | MP §1.3, §7 R0; `settings.gradle.kts`, `gradle/libs.versions.toml`, `build-logic/` | SHA, phiên bản (AGP/Kotlin/KSP/SDK), module graph, inventory test, `codegraph status` | `reports/baseline-verification.md` phần A |
| R0.2 | consistency-checker | R0.1 | Liệt kê task Gradle thật; chạy test/lint/assemble toàn module; `connectedAndroidTest` nếu có thiết bị, nếu không thì NOT_RUN | phần B |
| R0.3 | architecture-writer | R0.1–R0.2, `architecture/22-*` | Phân loại lint (fix/false positive/debt); trạng thái KD-02..05 có evidence; viết lại `HANDOVER.md` (13 module) | `HANDOVER.md`, `reports/wp/R0.3.md` |
| R0.4 | phiên chính | MP §12 RK-15 | Hook Graphify portable hoặc optional; hook `SessionStart` CodeGraph (cần người dùng cho phép sửa settings) | `.claude/settings.json` |

### R1 — Architecture-as-Context & dependency guard (P0) — phase đầu tư token lớn nhất

| WP | Agent | Đọc | Việc | Đầu ra |
|---|---|---|---|---|
| R1.0 | architecture-writer | MP §3; `architecture/00-index.md`, `01-*`, `02-*` | Template module/feature, schema `context-map.yaml`, `context-protocol.md`, `dependency-rules.md`, `module-map.md` | 2 TEMPLATE + 4 tệp bản đồ |
| R1.1a | architecture-writer | TEMPLATE + MP §4.1–4.3, §4.11 | `foundation`, `runtime-concurrency`, `runtime-scheduling`, `testing` | 4 module docs |
| R1.1b | architecture-writer | TEMPLATE + MP §4.4–4.6, §4.9 | `data-preferences`, `data-database`, `data-network`, `security` | 4 module docs |
| R1.1c | architecture-writer | TEMPLATE + MP §4.7, §4.8, §4.10, §4.14 | `platform-navigation`, `platform-permission`, `observability`, `build-logic` | 4 module docs |
| R1.1d | architecture-writer | TEMPLATE + MP §4.12, §4.13, §5 | `ui-designsystem`, `app` | 2 module docs |
| R1.2 | architecture-writer | features/TEMPLATE + MP §6.1 | `greeting`, `articles`, `showcase` (+ `config` trong `app.md`) | 3 feature docs |
| R1.3 | implementation-worker | MP §3.4, §10.5; schema R1.0 | `context-map.yaml` + `tools/check_architecture.py` (coverage module, link, heading, entry_symbols, TESTED cần evidence) | script + report |
| R1.4 | architecture-reader | **chỉ** `context-protocol.md` + docs R1 | Giải 2 mock task (PERM-01, NET-05) chỉ bằng tài liệu; ghi chỗ thiếu | `reports/wp/R1.4.md` |
| R1.5 | architecture-writer | R1.4 | Vá chỗ thiếu; liên kết từ `00-index.md`, `CLAUDE.md`, `SKILLS.md` | docs delta |

**Gate R1**: `check_architecture.py` xanh + R1.4 tìm đúng file/test. Mock task là phép thử: agent chỉ được đọc tài liệu; nếu định vị đúng tệp/test thì tài liệu đạt, và mọi WP sau tiêu ít token hơn.

### R2 — Quality & safety net (P0)

| WP | Agent | Đọc | Việc | Đầu ra |
|---|---|---|---|---|
| R2.1 | implementation-worker | `dependency-rules.md`, `modules/build-logic.md` | KD-02: task Gradle kiểm forbidden edges trên Gradle graph + test edge cấm | task + test |
| R2.2 | implementation-worker | `modules/platform-*.md`, `observability.md`, `testing.md` | Test còn thiếu: NAV-01, PERM-01, fakes/contract observability | tests |
| R2.3 | implementation-worker | MP §9.2, `.github/workflows/ci.yml` | CI thêm docs-check, boundary-check, báo cáo test; job emulator tách riêng | `ci.yml` |
| R2.4 | consistency-checker | `reports/wp/R2.1–R2.3` | Chạy CI-equivalent cục bộ; instrumentation NOT_RUN nếu không có runner | `reports/wp/R2.4.md` |

### R3 ║ R4 — chạy song song (worktree riêng)

**Luồng R3 — Core/Runtime hardening**

| WP | Agent | Đọc | Việc |
|---|---|---|---|
| R3.0 | architecture-writer → **critical-architecture-auditor** | `modules/foundation.md`, `runtime-*.md`, MP §7 R3 | Sonnet soạn ≤ 3 phương án cho cancellation/retry/ownership `AppCoroutineScope`, Worker/Alarm; Opus chốt một lần |
| R3.1 | implementation-worker | R3.0 + `modules/foundation.md` | CORE-01, CORE-03 |
| R3.2 | implementation-worker | R3.0 + `modules/runtime-concurrency.md` | CON-01..04, CON-07 (virtual time) |
| R3.3 | implementation-worker | R3.0 + `modules/runtime-scheduling.md` | SCH-01..03, SCH-06 |
| R3.4 | consistency-checker | diff + reports | Verify + gate |

**Luồng R4 — Design System**

| WP | Agent | Đọc | Việc |
|---|---|---|---|
| R4.1 | architecture-reader | `modules/ui-designsystem.md` | UI-01 inventory: component, tệp, catalog entry, test |
| R4.2 | architecture-writer | R4.1, DEC-05/06/08 | `COMPONENTS.md` registry, phân loại trùng lặp, D-03 (stack screenshot test) |
| R4.3 | implementation-worker | R4.2 | UI-02, UI-03 |
| R4.4 | implementation-worker | R4.2 | UI-04, UI-05, UI-10 (UI-06 chỉ khi có consumer) |
| R4.5 | consistency-checker | diff | UI-09 trong giới hạn môi trường; NOT_RUN có lý do |

### R5 — Data, security & observability (P0/P1)

| WP | Agent | Đọc | Việc |
|---|---|---|---|
| R5.0 | architecture-writer → **critical-architecture-auditor** | `modules/data-database.md`, `security.md`, MP §12.1 D-01, D-04 | ADR ownership `Article*`/`DcDatabase`; provider SecureStorage |
| R5.1 | implementation-worker | `modules/data-network.md`, `data-preferences.md` | NET-01, NET-05, PREF-01, PREF-03 |
| R5.2 | implementation-worker | `modules/security.md`, `observability.md` | SEC-01, SEC-04, OBS-01, OBS-02 (redaction) |
| R5.3 | implementation-worker | R5.0 + `modules/data-database.md` | DB-01, DB-02 |
| R5.4 | consistency-checker | diff | Verify + kiểm tra không lộ secret trong log/fixture |

### R6 — Navigation, permission & reference app (P0/P1)

| WP | Agent | Đọc | Việc |
|---|---|---|---|
| R6.0 | architecture-writer | `modules/platform-navigation.md`, D-02 | Độ sâu abstraction navigation tối thiểu (leo thang Opus chỉ khi đổi public API xuyên module) |
| R6.1 | implementation-worker | R6.0 + platform docs | NAV-01..04, PERM-01..04 theo nhu cầu consumer |
| R6.2 | implementation-worker | `modules/app.md`, `features/*` | 2 journey xuyên module (data flow + platform) |
| R6.3 | consistency-checker | diff | Compose UI test hoặc NOT_RUN + blocker; kiểm DEC-07/08 |

### R7 — Developer experience & release (P1)

| WP | Agent | Đọc | Việc |
|---|---|---|---|
| R7.1 | implementation-worker | `modules/build-logic.md`, `features/TEMPLATE.md` | BLD-02: generator sinh code + test + architecture doc khung |
| R7.2 | architecture-writer → **critical-architecture-auditor** | D-05, MP §11 | Publishing, API compatibility, deprecation |
| R7.3 | architecture-writer | R7.1–R7.2 | README Quickstart, changelog |
| R7.4 | architecture-reader | chỉ context-map + docs | Sinh feature thử, kiểm tra agent tìm được nó qua context map |

### R8 / R9 — chỉ khi người dùng xác nhận consumer thật

Không điều phối tự động. Khi có yêu cầu: `architecture-writer` soạn ADR (R8: chọn một extension; R9: adoption
audit), leo thang Opus nếu thuộc §2.1, rồi áp dụng lại mẫu WP như R5.

## 5. Mẫu prompt khi spawn một WP

Phiên chính gửi prompt ngắn, **không dán nội dung plan**:

```text
WP R3.2 — plan/AGENT_EXECUTION_PLAN.md §4.
Đọc: reports/wp/R3.0.md, architecture/modules/runtime-concurrency.md,
     sed -n '/^### 4\.2\./,/^### 4\.3\./p' plan/FOUNDATION_MASTER_PLAN.md
Được sửa: runtime/concurrency/**, testing/**, architecture/modules/runtime-concurrency.md
Đầu ra: reports/wp/R3.2.md theo mẫu §6.
```

## 6. Mẫu báo cáo `reports/wp/<WP-ID>.md` (≤ 120 dòng)

```markdown
# <WP-ID> — <tiêu đề>
- Agent / model:
- Commit gốc → commit kết quả:
- CodeGraph: đã truy vấn? index khớp commit?
## A. As-is evidence     (file:line, symbol, callers chính)
## B. Thay đổi           (tệp, API delta, DEC/KD liên quan)
## C. Kiểm chứng         (lệnh → kết quả; NOT_RUN + lý do)
## D. Drift tài liệu     (chỗ docs ≠ source)
## E. Rủi ro / việc tiếp theo
```

## 7. Quy tắc dừng và leo thang

- Source ≠ docs/plan → ghi mục D, không tự sửa kiến trúc; phiên chính quyết định (writer, Opus hoặc hỏi người dùng).
- Implementer cần chạm tệp ngoài phạm vi WP → dừng, báo phiên chính.
- Cùng lỗi thất bại 2 lần → không retry; nếu là lỗi hướng kiến trúc thì đủ điều kiện (5) để gọi Opus.
- WP đổi public API, dependency, DI, lifecycle → cập nhật module doc trong cùng WP (MP §10.4).
- WP chỉ được commit khi review APPROVE (hoặc phiên chính ghi rõ lý do chấp nhận rủi ro).
- Sau mỗi wave: `codegraph sync`; `graphify update .` nếu topology đổi; commit theo WP.

## 8. Thứ tự khởi động (MP §13.2)

1. R0.1 → R0.2 → R0.3 (R0.4 khi người dùng cho phép sửa settings)
2. R1.0 → {R1.1a, R1.1b, R1.1c, R1.1d, R1.2} → R1.3 → R1.4 → R1.5
3. {R2.1, R2.2} → R2.3 → R2.4
4. R3 ║ R4
