# STRUCTURE.md — Bản đồ kiến trúc theo thư mục

Mô tả vai trò kiến trúc của **từng thư mục** trong repo. Nội dung được đối chiếu với `settings.gradle.kts`, các `build.gradle.kts` và mã nguồn hiện tại (không chép từ kế hoạch).

> Thẩm quyền: khi mâu thuẫn, `architecture/` > file này. File này là **bản đồ điều hướng**, không đặt luật mới. Luật phụ thuộc: [`architecture/02-layers-and-dependencies.md`](architecture/02-layers-and-dependencies.md). Danh sách module chuẩn: [`architecture/03-project-structure.md`](architecture/03-project-structure.md).
> Mỗi module có thêm một thẻ ngắn `STRUCTURE.md` (vai trò, sở hữu gì, hướng dẫn cho AI) — file này là phần tổng hợp phía trên chúng.

- Namespace gốc: `com.dc.android.foundation` · Root project: `DC Android Foundation`
- Android: compile/target SDK 37, min SDK 24, Java 11, Kotlin DSL + Version Catalog + convention plugins.
- 13 module Gradle + 1 included build (`build-logic`).

---

## 1. Cây thư mục tổng quan

```text
DCFoudation/
├── app/                     [Gradle] composition root + reference features
├── foundation/              [Gradle] primitive Kotlin thuần (AppError…)
├── ui/
│   └── designsystem/        [Gradle] tokens → theme → components → catalog
├── runtime/
│   ├── concurrency/         [Gradle] dispatcher + Flow/Job helper (Kotlin/JVM thuần)
│   └── scheduling/          [Gradle] DcScheduler: WorkManager + AlarmManager
├── data/
│   ├── preferences/         [Gradle] DataStore boundary
│   ├── network/             [Gradle] Retrofit/OkHttp + NetworkResult
│   └── database/            [Gradle] Room + schema tham chiếu Articles
├── platform/
│   ├── navigation/          [Gradle] Destination (engine-neutral)
│   └── permission/          [Gradle] PermissionState + notification permission
├── security/                [Gradle] SecureStorage (encrypted key/value)
├── observability/           [Gradle] Logger / AnalyticsTracker / CrashReporter
├── testing/                 [Gradle] rule + fake dùng chung cho test
├── build-logic/             [included build] convention plugins `dc.*`
├── gradle/                  version catalog, wrapper, daemon JVM
├── architecture/            tài liệu kiến trúc có thẩm quyền (living docs)
├── plan/                    gói kế hoạch ban đầu (lịch sử, không phải authority)
├── .github/workflows/       CI
├── .claude/                 cấu hình Claude Code (hook graphify)
└── (file gốc)               build.gradle.kts, settings.gradle.kts, gradle.properties,
                             CLAUDE.md, AGENTS.md, README.md, HANDOVER.md, PLAN_REPORT.md,
                             BASE_PROJECT_ROADMAP.md, SKILLS.md, STRUCTURE.md
```

Thư mục sinh ra, **không commit**: `graphify-out/`, `.codegraph/`, `**/build/`, `.gradle/`, `.idea/`.

---

## 2. Hướng phụ thuộc giữa các module

Theo các dòng `project(...)` trong `build.gradle.kts` (xác minh từ nguồn):

```text
                          ┌────────────┐
                          │    :app    │  (composition root, Hilt, NavHost)
                          └─────┬──────┘
   implementation tới mọi module bên dưới; :testing chỉ qua testImplementation
        │
        ├── :ui:designsystem          (độc lập, không phụ thuộc module DC nào)
        ├── :platform:navigation      (độc lập)
        ├── :platform:permission      (độc lập)
        ├── :data:preferences         (độc lập)
        ├── :observability            (độc lập)
        ├── :foundation               (độc lập)
        ├── :data:network     ──► :foundation, :observability
        ├── :data:database    ──► :runtime:concurrency
        ├── :security         ──► :runtime:concurrency
        ├── :runtime:scheduling ─► :foundation, :runtime:concurrency
        └── :runtime:concurrency      (độc lập, Kotlin/JVM thuần)

   :testing ──api──► :runtime:concurrency, :data:preferences,
                     :observability, :runtime:scheduling
```

Quy tắc cốt lõi:

- Chỉ `:app` dùng Hilt (`dc.hilt`), NavHost thật, WorkManager worker/receiver cụ thể của feature.
- Module thư viện **không** phụ thuộc `:app` hay lẫn nhau ngoài đồ thị trên; không có vòng.
- Không có module `dc-hilt`: Hilt chỉ là cơ chế triển khai.
- Luồng lớp: `UI → Presentation → [Domain] → Repository → DataSource` (xem `architecture/02`).

---

## 3. Mô tả từng thư mục

### `app/` — `:app`

**Vai trò:** composition root, app shell, hai feature tham chiếu. Plugin: `dc.android.application`, `dc.compose`, `dc.hilt`, `dc.room`, `dc.testing`, `dc.feature.generator`.

```text
app/src/main/java/com/dc/android/foundation/
├── DcApplication.kt, MainActivity.kt   entry point, DcTheme, NavHost + DcNavigationSuite
├── di/            ConcurrencyModule, DataStoreModule, DatabaseModule,
│                  NetworkModule, ObservabilityModule, SecurityModule
│                  (mỗi module Hilt gần capability sở hữu; không có AppModule khổng lồ)
├── config/        AppConfiguration(+Repository/ViewModel/Screen), AppEnvironment(+Provider),
│                  AppLanguageController — môi trường, giao diện, ngôn ngữ per-app
├── greeting/      feature tham chiếu: DataStore → Repository → ViewModel → Route/Screen
├── articles/      feature tham chiếu offline-first: Retrofit + Room + SyncWorker
│                  (ArticleDto, ApiService, NetworkDataSource, Repository, UiState/Action/Effect)
└── showcase/trip/ màn hình trình diễn design system (không phải feature thật)
app/src/{test,androidTest}   ViewModel/repository tests, Compose UI test
app/src/main/res             tài nguyên (values, values-night, values-vi, xml, mipmap)
```

Mẫu mỗi feature: `Route` (nối ViewModel, framework glue) → `Screen` (Composable thuần nhận state + callback) → `ViewModel` (`StateFlow`) → `Repository` → data source. Feature không import private của feature khác. Tạo feature mới bằng `./gradlew :app:createFeature` (xem `build-logic`).

### `foundation/` — `:foundation`

**Vai trò:** primitive Kotlin thuần, không Android/Hilt/UI. Plugin `dc.kotlin.jvm`.
**Chứa:** `core/AppError.kt` (lỗi hạ tầng đã chuẩn hoá), `core/Retryable.kt` (`AppError.isRetryable`).
Chỉ thêm khi ≥ 2 module cần cùng một policy. Không giữ resource ID hay message.

### `ui/designsystem/` — `:ui:designsystem`

**Vai trò:** ngôn ngữ thị giác Compose. Chiều phụ thuộc: **Tokens → Theme → Components → Feature UI**.

```text
src/main/java/com/dc/android/foundation/designsystem/
├── tokens/      Color, Spacing, Shape, Surface, Motion, Accessibility tokens
├── theme/       DcTheme, DcColors, DcTypography, DcShapes, DcUiScale (semantic theme)
├── components/  ~30 file Dc*: Button, Text, Input/Form, Dropdown, List/ListRow, Search,
│                Sheet/Overlay/Menu, State/Feedback/Message, NavigationSuite/Drawer,
│                PullToRefreshBox, AsyncImage (Coil bị cô lập ở đây), Models…
├── preview/     DcPreviews (annotation preview)
└── catalog/     DesignSystemCatalog, ExtendedCatalog
src/test, src/androidTest   policy/token test + Compose test
```

Không import feature/data. Coil chỉ trong `DcAsyncImage.kt`; adaptive chỉ lộ qua `DcNavigationSuite`. Tài liệu: `architecture/07-design-system/`.

### `runtime/concurrency/` — `:runtime:concurrency`

**Vai trò:** hợp đồng & helper coroutine, **Kotlin/JVM thuần** (`dc.kotlin.jvm`).
**Chứa:** `DispatcherProvider`/`DefaultDispatcherProvider`, `AppCoroutineScope` (thay `GlobalScope`), `DispatcherShortcuts` (`onIo`/`onDefault`/`flowOnIo`), `RetryPolicy`+`retrying`, `SingleFlight`, `JobRegistry`, `FlowSharing` (`stateInWhileSubscribed`, `throttleFirst`).
Mọi helper là tuỳ chọn; không nuốt `CancellationException`. Tài liệu: `architecture/09`.

### `runtime/scheduling/` — `:runtime:scheduling`

**Vai trò:** facade lập lịch trung lập nghiệp vụ — việc bền/retry → WorkManager, kích hoạt chính xác theo người dùng → AlarmManager.

```text
src/main/java/.../runtime/scheduling/
├── DcScheduler.kt, NextOccurrence.kt   facade + tính lần xảy ra tiếp theo (an toàn DST)
├── work/    DcJobSpec, DcConstraints, DcRetryPolicy, DcJobState, DcWorkScheduler, DcWorker
└── alarm/   DcAlarm, DcAlarmStore, DcAlarmCodec, AndroidDcAlarmScheduler,
             DcAlarmReceiver, DcAlarmRestorer, DcExactAlarmPermission
```

Không có Hilt. Worker/receiver cụ thể của feature nằm ở `:app`. Không gọi `WorkManager`/`AlarmManager` trực tiếp ngoài module này. Tài liệu: `architecture/14`.

### `data/preferences/` — `:data:preferences`

DataStore Preferences boundary generic: `PreferencesDataSource`, `DataStorePreferencesDataSource`, `PreferenceKey`, `PreferencesMigration`. Từ vựng/mapping của feature thuộc repository của feature. Tài liệu: `architecture/10-data/preferences-datastore.md`.

### `data/network/` — `:data:network`

Hạ tầng HTTP tái sử dụng: `NetworkClientFactory`, `NetworkClientConfig`, `NetworkHeadersInterceptor` (request-id), `HttpErrorBodyParser`, `NetworkResult` + `safeApiCall`. DTO/service thuộc feature. Không rò exception Retrofit/OkHttp lên UI; log không chứa secret/PII. Tài liệu: `architecture/10-data/network.md`, `11`.

### `data/database/` — `:data:database`

Hạ tầng Room + schema tham chiếu: `DcDatabase`, `RoomDatabaseFactory`, `RoomTransactionRunner`, `StalenessPolicy`, `ArticleEntity`, `ArticleDao`. `schemas/` chứa schema JSON đã export (phải commit và cập nhật khi đổi cấu trúc DB). Entity/DAO không vượt qua ranh giới repository. Tài liệu: `architecture/10-data/persistence-room.md`, `cache-and-offline.md`.

### `platform/navigation/` — `:platform:navigation`

Chỉ có `Destination` (`@Serializable` sealed interface: `Greeting`, `Articles`). Không phụ thuộc `androidx.navigation`; `NavController`/`NavHost` thật nằm ở `:app`. Tài liệu: `architecture/06`.

### `platform/permission/` — `:platform:permission`

`PermissionState` + `NotificationPermission` adapter (plugin `dc.compose`). Feature dùng state/action, không dùng `ActivityCompat`/launcher. Tài liệu: `architecture/13`.

### `security/` — `:security`

`SecureStorage` (hợp đồng) + `EncryptedSharedPreferencesSecureStorage`, lỗi storage riêng. Thao tác blocking đi qua `DispatcherProvider`. Chưa có luồng auth/session (cố ý). Tài liệu: `architecture/12`.

### `observability/` — `:observability`

Hợp đồng trung lập vendor: `Logger`/`AndroidLogger`, `AnalyticsTracker`/`LoggingAnalyticsTracker`, `CrashReporter`/`LoggingCrashReporter`. Binding SDK vendor đặt ở `:app`. Tuân thủ SEC-01 (không log credential/token/PII). Tài liệu: `architecture/15-observability/`.

### `testing/` — `:testing`

Công cụ test dùng chung (chỉ qua `testImplementation`): `MainDispatcherRule`, `FakeDispatcherProvider`, `FakeScheduler`, `InMemoryPreferencesDataSource`, `ObservabilityFakes`. Có thêm `README.md` hướng dẫn wiring. Phơi `api` các module mà chữ ký fake cần. Tài liệu: `architecture/17`.

### `build-logic/` — included build

Convention plugin (đăng ký trong `build-logic/build.gradle.kts`):

| Plugin ID | Lớp | Việc làm |
|---|---|---|
| `dc.android.application` | `DcAndroidApplicationConventionPlugin` | cấu hình app Android |
| `dc.android.library` | `DcAndroidLibraryConventionPlugin` | cấu hình library Android |
| `dc.kotlin.jvm` | `DcKotlinJvmConventionPlugin` | module Kotlin/JVM thuần |
| `dc.compose` | `DcComposeConventionPlugin` | Compose compiler + deps |
| `dc.hilt` | `DcHiltConventionPlugin` | Hilt + KSP |
| `dc.room` | `DcRoomConventionPlugin` | Room + KSP + `schemas/` export |
| `dc.testing` | `DcTestingConventionPlugin` | test deps theo loại module |
| `dc.feature.generator` | `DcFeatureGeneratorPlugin` | task `createFeature` (`CreateFeatureTask`, `FeatureScaffoldGenerator`) |

`ConventionHelpers.kt` giữ tiện ích dùng chung; `src/test` kiểm thử bộ sinh feature. Tài liệu: `architecture/18`, `20`.

### `gradle/`

`libs.versions.toml` (Version Catalog: `[versions]`, `[libraries]`, `[plugins]` — nguồn duy nhất cho phiên bản), `wrapper/`, `gradle-daemon-jvm.properties`.

### `architecture/`

Tài liệu kiến trúc **có thẩm quyền**, đánh số 00–24 (+ thư mục con `07-design-system/`, `10-data/`, `15-observability/`). Bắt đầu từ `00-index.md` (định tuyến theo chủ đề), `01-overview-and-invariants.md`, `24-ai-reading-guide.md`. `22-known-deviations.md` và `23-decision-log.md` là tài liệu sống.

### `plan/`

Gói kế hoạch thực thi ban đầu (lịch sử): `CLAUDE_IMPLEMENTATION_PLAN.md`, `MODULE_IMPLEMENTATION_PLAN.md`, `DESIGN_SYSTEM_UI_PLAN.md`, `SCHEDULING_MODULE_PLAN.md`, `TARGET_ARCHITECTURE_TREE.md`, bản sao seed `plan/architecture/`, `ui-style-guide/`, `references/`. **Không phải authority** — dùng `architecture/` và mã nguồn hiện tại.

### `.github/workflows/`

`ci.yml`: JDK 21 → `testDebugUnitTest` → `lintDebug` → `assembleDebug` → upload kết quả test. Xem `architecture/19`.

### `.claude/`

`CLAUDE.md` (kích hoạt `/graphify`) và `settings.json` (hook `graphify hook-guard` trước Bash/Grep/Read/Glob). Lưu ý: đường dẫn hook đang là đường dẫn Windows cục bộ — chỉ chạy được trên máy gốc.

### File gốc

| File | Mục đích |
|---|---|
| `CLAUDE.md`, `AGENTS.md` | chỉ dẫn cho AI agent (quy tắc, workflow, thứ tự thẩm quyền) |
| `README.md` | giới thiệu + lệnh build |
| `BASE_PROJECT_ROADMAP.md` | danh mục tính năng & backlog P0–P2 |
| `PLAN_REPORT.md` | nhật ký thực thi theo phase |
| `HANDOVER.md` | bàn giao (ghi chú: snapshot lịch sử, cần viết lại) |
| `SKILLS.md` | skill/công cụ đang dùng (Graphify, AgentMemory) |
| `settings.gradle.kts` | danh sách `include(...)` — nguồn chuẩn của module |
| `build.gradle.kts`, `gradle.properties` | cấu hình build gốc |

---

## 4. Duy trì file này

- Thêm/xoá module: cập nhật `settings.gradle.kts`, `architecture/03-project-structure.md` (STR-01), mục §1–§3 ở đây và thẻ `STRUCTURE.md` của module.
- Đổi package/file public trong module: cập nhật phần "Chứa" tương ứng.
- Kiểm tra nhanh: `grep -o 'include(":[^"]*")' settings.gradle.kts` phải khớp với danh sách module ở §1.
