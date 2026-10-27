# Trạng thái triển khai — DC Android Foundation

> Tài liệu tổng hợp chi tiết các phần **đã có trong mã nguồn**. Nguồn: `settings.gradle.kts`, cây
> mã nguồn từng module, `architecture/22-known-deviations.md`, `architecture/23-decision-log.md`,
> `HANDOVER.md` và chỉ mục CodeGraph (202 tệp, 3.098 node, 5.163 cạnh).
> Khi lệch với mã nguồn hoặc `architecture/`, hai nguồn đó là chuẩn.
>
> Chưa chạy lại build/test khi biên soạn tài liệu này. Kết quả xác minh gần nhất được ghi lại
> (4 module, 7/7 test đạt, 0 lỗi lint) đã cũ so với 13 module hiện tại.

## 1. Tổng quan

- Namespace chính: `com.dc.android.foundation`.
- 13 module Gradle + `build-logic` (included build chứa convention plugin).
- Ngôn ngữ: Kotlin (193/202 tệp được lập chỉ mục), Compose + Material 3, Hilt, Coroutines/Flow,
  Room, DataStore, WorkManager, Gradle Kotlin DSL + Version Catalog.
- Số tệp Kotlin / tệp test theo module:

| Module | Kotlin | Test |
|---|---:|---:|
| `:foundation` | 3 | 1 |
| `:ui:designsystem` | 48 | 7 |
| `:runtime:concurrency` | 12 | 4 |
| `:runtime:scheduling` | 18 | 3 |
| `:data:preferences` | 5 | 1 |
| `:data:database` | 7 | 1 |
| `:data:network` | 7 | 2 |
| `:platform:navigation` | 1 | 0 |
| `:platform:permission` | 2 | 0 |
| `:security` | 3 | 1 |
| `:observability` | 6 | 0 |
| `:testing` | 7 | 2 |
| `:app` | 45 | 6 |
| `build-logic` | 12 | 2 |

## 2. Chi tiết từng module

### `:foundation`
Primitive dùng chung, không mang nghiệp vụ.
- `core/AppError.kt`: `sealed interface AppError` với `Connectivity`, `Http`, `Serialization`,
  `Storage`, `Unknown` (mang `cause`); lỗi hạ tầng được ánh xạ về đây trước khi tới UI.
- `core/Retryable.kt`: contract đánh dấu khả năng thử lại.
- Test: `AppErrorTest`.

### `:runtime:concurrency`
- `DispatcherProvider` + `DefaultDispatcherProvider`, `DispatcherShortcuts`: dispatcher được
  inject, không hard-code (`GlobalScope` bị cấm).
- `AppCoroutineScope`: scope cấp ứng dụng.
- `JobRegistry`: quản lý Job theo khóa trong một scope.
- `SingleFlight<K, V>`: gộp các lời gọi đồng thời cùng khóa thành một lần chạy.
- `RetryPolicy`: chính sách retry/backoff cho coroutine.
- `FlowSharing`: tiện ích chia sẻ Flow.
- Test: `FlowSharingTest`, `JobRegistryTest`, `RetryPolicyTest`, `SingleFlightTest`.

### `:runtime:scheduling`
Lập lịch nền, tách hai nhánh.
- Cấp cao: `DcScheduler`, `NextOccurrence` (tính lần xảy ra kế tiếp).
- `work/` (WorkManager): `DcJobSpec`, `DcJobState`, `DcConstraints`, `DcRetryPolicy`,
  `DcWorkScheduler`, `DcWorker`.
- `alarm/` (AlarmManager): `DcAlarm`, `AndroidDcAlarmScheduler`, `DcAlarmCodec`,
  `DcAlarmReceiver`, `DcAlarmRestorer` (khôi phục sau reboot), `DcAlarmStore`,
  `DcExactAlarmPermission`.
- Test: `NextOccurrenceTest`, `DcAlarmCodecTest`, `DcWorkPolicyTest`.

### `:data:preferences`
- `PreferencesDataSource` (contract), `DataStorePreferencesDataSource`, `PreferenceKey` (khóa
  có kiểu), `PreferencesMigration`.
- Test: `DataStorePreferencesDataSourceTest`.

### `:data:database`
- Room: `DcDatabase`, `RoomDatabaseFactory`, `ArticleEntity`, `ArticleDao`,
  `RoomTransactionRunner` (chạy giao dịch qua abstraction), `StalenessPolicy` (chính sách dữ liệu cũ).
- Test: 1 tệp.

### `:data:network`
- `NetworkClientFactory`, `NetworkClientConfig`, `NetworkHeadersInterceptor`,
  `NetworkResult`, `HttpErrorBodyParser`.
- Test: 2 tệp.

### `:platform:navigation`
- Mới có `Destination.kt` (contract điểm đến). Chưa có test.

### `:platform:permission`
- `PermissionState`, `NotificationPermission`. Chưa có test.

### `:security`
- `SecureStorage` (contract) + `EncryptedSharedPreferencesSecureStorage`. Có 1 test.
- Lưu ý KD-04: đã cài đặt nhưng chưa có thông tin xác thực thật đi qua.

### `:observability`
- `Logger`/`AndroidLogger`, `AnalyticsTracker`/`LoggingAnalyticsTracker`,
  `CrashReporter`/`LoggingCrashReporter`. Chưa có test.
- Lưu ý KD-05: backend chỉ ghi log cục bộ, chưa chọn SDK nhà cung cấp.

### `:testing`
- `FakeDispatcherProvider`, `FakeScheduler`, `InMemoryPreferencesDataSource`,
  `MainDispatcherRule`, `ObservabilityFakes`. Test: `TestingKitTest`,
  `InMemoryPreferencesDataSourceTest`.

### `:ui:designsystem`
Chuỗi phụ thuộc: Primitive Tokens → Semantic Theme → Component DC → Feature UI.
- `tokens/`: `ColorTokens`, `SpacingTokens`/`DcSpacing`, `ShapeTokens`, `MotionTokens`,
  `SurfaceTokens`, `AccessibilityTokens`.
- `theme/`: `DcTheme`, `DcColors`, `DcTypography`, `DcShapes`, `DcUiScale` (tỉ lệ giao diện
  ở mức theme, giữ nguyên ngưỡng vùng chạm tối thiểu và font scale hệ thống).
- `components/` (nhóm chính): Button, Text/TextLink, Input/Form/Dropdown/Selection/ToggleRow,
  Search bar, List/ListRow, Menu, Badge, Feedback/Message/State (`DcContentState`),
  Layout/Screen/Sheet/Overlay, Navigation drawer/suite, PullToRefreshBox, AsyncImage, `DcModels`
  (kiểu như `DcOption<T>`, `DcMenuItem`, `DcSnackbarMessage`).
- `catalog/`: `DesignSystemCatalog`, `ExtendedCatalog` (màn hình catalog thành phần);
  `preview/DcPreviews`.
- Nhãn mặc định nằm trong `res/values/strings.xml` (`dc_*`) và có thể ghi đè (DEC-08).
- Test: 7 tệp.

### `:app` (ứng dụng tham chiếu)
- `DcApplication`, `MainActivity`.
- `di/`: `ConcurrencyModule`, `DataStoreModule`, `DatabaseModule`, `NetworkModule`,
  `ObservabilityModule`, `SecurityModule` (mỗi module sở hữu binding của mình, không có `AppModule` chung).
- `config/`: `AppConfiguration`, `AppConfigurationRepository`, `AppEnvironment`,
  `AppEnvironmentProvider`, `AppLanguageController`, `AppConfigurationViewModel`,
  `AppConfigurationScreen` — lưu theme/kích thước giao diện/màu động; ngôn ngữ do Android quản lý
  (`AppCompatDelegate`) (DEC-07). Có `AppConfigurationRepositoryTest`.
- `greeting/` — feature mẫu chuẩn: Route / ViewModel / UiState / UiAction / Screen, `GreetingModule`,
  `GreetingPreferencesRepository` (DataStore dưới `SavedStateHandle`), `GreetingProvider`. Test:
  `GreetingViewModelTest`, `GreetingScreenTest` (Compose UI).
- `articles/` — feature tham chiếu thứ hai (DEC-04): `ArticlesApiService`, `ArticleDto`,
  `ArticlesNetworkDataSource`, `ArticlesRepository`, `ArticlesSyncWorker`, `ArticlesModule`,
  Route/ViewModel/UiState/UiAction/UiEffect/Screen. Test: `ArticlesViewModelTest`.
- `showcase/trip/`: màn trình diễn (`TripDetailsShowcaseScreen`, `TripHistoryShowcaseScreen`,
  `TripShowcaseHost`, `TripShowcaseData`) — thành phần ổn định đã được nâng lên design system
  (DEC-06), phần còn lại giữ ở lớp showcase.

### `build-logic`
- Convention plugin: `DcAndroidApplication`, `DcAndroidLibrary`, `DcKotlinJvm`, `DcCompose`,
  `DcHilt`, `DcRoom`, `DcTesting` (+ `ConventionHelpers` giữ compile/min SDK).
- Trình sinh khung feature: `DcFeatureGeneratorPlugin`, `CreateFeatureTask`,
  `FeatureScaffoldGenerator` (test: `FeatureScaffoldGeneratorTest`).

## 3. CI và chất lượng
- `.github/workflows/ci.yml`: chạy khi push `main`/`master` và pull request; JDK 21 (Temurin);
  `testDebugUnitTest` → `lintDebug` → `assembleDebug`; tải lên kết quả test.
- Chưa chạy `connectedAndroidTest` trên CI.

## 4. Tài liệu kiến trúc (`architecture/`)
Mục lục `00-index.md`; bất biến và phân tầng (01–03); bootstrap/lifecycle (04); UDF (05);
điều hướng (06); design system (07); Hilt (08); concurrency (09); dữ liệu (10); lỗi (11);
bảo mật/phiên (12); quyền (13); tác vụ nền (14); observability (15); cấu hình (16); testing (17);
build-logic (18); CI/quality gate (19); mẫu feature (20); quy tắc code (21); known deviations (22);
decision log (23); hướng dẫn đọc cho AI (24).

Các quyết định đã ghi: DEC-01 (sửa namespace trong `CLAUDE.md`), DEC-02 (không tạo tài liệu rỗng),
DEC-03 (tra phiên bản qua Maven metadata), DEC-04 (thêm feature `articles` thay vì wrapper suy đoán),
DEC-05 (áp dụng UI style guide như delta), DEC-06 (chỉ nâng primitive UI ổn định), DEC-07 (tách
lưu trữ giao diện khỏi locale), DEC-08 (mở rộng bộ widget DS).

## 5. Công cụ hỗ trợ agent
- **CodeGraph**: `.mcp.json` (MCP server), quyền `mcp__codegraph__*` và hook `UserPromptSubmit`
  trong `.claude/settings.json`; quy tắc bắt buộc dùng mỗi phiên nằm trong `SKILLS.md` và
  `CLAUDE.md` §3a. Chỉ mục `.codegraph/` cục bộ, đã git-ignore. Chưa có hook `SessionStart` tự khởi tạo.
- **Graphify**: hook trong `.claude/settings.json` đang trỏ đường dẫn Windows nên không chạy trên Linux.

## 6. Khoảng trống và lệch chuẩn đã biết
| Mục | Mô tả |
|---|---|
| KD-02 | Ranh giới module chưa được thực thi cưỡng chế tự động. |
| KD-03 | Vẫn cần `android.disallowKotlinSourceSets=false` cho KSP trên AGP 9; gỡ khi KSP hỗ trợ AGP 9. |
| KD-04 | `SecureStorage` đã cài nhưng chưa có thông tin xác thực thật đi qua. |
| KD-05 | Analytics/crash chỉ ghi log cục bộ, chưa có SDK. |
| Test | `GreetingScreenTest` mới được biên dịch, chưa chạy (không có emulator). |
| Test | `:platform:navigation`, `:platform:permission`, `:observability` chưa có test. |
| CI | Workflow chưa được xác minh trên hạ tầng GitHub Actions thật. |
| Lint | 12 cảnh báo (lần ghi nhận trước) chưa được phân loại. |
| Tài liệu | `HANDOVER.md` còn mô tả cũ (4 module) và cần viết lại trước lần bàn giao tới. |
