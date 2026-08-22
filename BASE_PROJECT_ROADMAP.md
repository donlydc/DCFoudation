# DC Android Foundation — danh mục tính năng và kiến trúc mục tiêu

## 1. Mục tiêu

Xây dựng một nền tảng Android có thể tái sử dụng để khi tạo ứng dụng hoặc feature mới, phần lớn thời gian chỉ dành cho nghiệp vụ riêng. Những phần lặp lại như Gradle, DI, navigation, trạng thái màn hình, lỗi, loading, network, cache, permission, logging và kiểm thử phải có sẵn hoặc tạo được từ template.

Mục tiêu thực tế cho một feature CRUD/list-detail mới:

- lập trình viên chỉ viết model nghiệp vụ, API/DAO đặc thù, mapping, `UiState`, hành động và UI riêng;
- foundation cung cấp app shell, theme, component, trạng thái dùng chung, lifecycle collection, dispatcher, network/database setup, error mapping, logging, permission và test utilities;
- không dùng `BaseViewModel`, `BaseRepository` hoặc generic framework quá sâu chỉ để giảm số dòng code;
- giảm boilerplate bằng convention plugin, template/generator và component có API ổn định.

## 2. Trạng thái hiện tại đã xác minh

Repository hiện có 13 module trong `settings.gradle.kts`:

```text
:foundation
:app
├─ :ui:designsystem
├─ :runtime:concurrency
├─ :runtime:background
├─ :data:preferences
├─ :data:network
├─ :data:database
├─ :platform:navigation
├─ :platform:permission
├─ :security
├─ :observability
└─ :testing
```

Reference feature `articles` đã chứng minh được luồng xuyên suốt:

```text
Compose Screen
  → Route + lifecycle collection
  → ViewModel + StateFlow + action
  → Repository (Room là source of truth)
  ├─ Retrofit remote data source
  └─ Room DAO

WorkManager → cùng Repository → notification có kiểm tra permission
```

| Năng lực | Trạng thái thật | Việc còn thiếu để dùng rộng rãi |
|---|---|---|
| Compose + Material 3 | Có | adaptive UI, pull-to-refresh và app shell dùng lại |
| Design System | Có tokens, theme, state/input/list components và preview catalog | tiếp tục actions/selection/overlay components; thiếu icon/resource strategy và visual regression test |
| UDF/ViewModel/Hilt | Có hai reference feature; user message nằm trong state bền vững | chỉ dùng one-shot effect cho framework integration tức thời |
| Navigation | Có type-safe Navigation Compose cho hai top-level destination | deep link, nested flow, result passing, adaptive navigation; đánh giá Navigation 3 |
| Coroutines/Flow | Có `DispatcherProvider` và fake | Flow utilities, retry policy, app/process scope có owner rõ ràng |
| Preferences | Có typed DataStore abstraction, schema migration và in-memory fake | thêm policy khi app có use case thực tế mới |
| Network | Có Retrofit/OkHttp factory, serialization và `NetworkResult` ánh xạ `AppError` | timeout policy, auth/token refresh, connectivity, error body mapping, request ID, test fixtures |
| Database/cache | Có Room và offline-first `ArticlesRepository` | migration/schema export, transaction policy, database test kit; tách model mẫu khỏi hạ tầng tái sử dụng |
| Background work | Có periodic scheduler và Hilt worker mẫu | one-time work, progress/cancellation, charging/battery policy, worker test utilities |
| Permission | Có notification permission | rationale/settings flow và coordinator dùng lại cho các permission khác |
| Security | Có `SecureStorage` và encrypted implementation | session/auth lifecycle, logout/expiry, biometric gate nếu có use case |
| Observability | Có Logger/Analytics/Crash contracts | backend production, consent, common properties, performance tracing |
| Environment/config | Có debug/release và một base URL | staging, typed feature flags, secret injection và remote config adapter |
| Testing/CI | Có `:testing`, unit test, Compose test và CI test/lint/assemble | repository builders, integration/screenshot test, coverage và performance gate |
| Build tooling | Có version catalog, `build-logic` với 7 convention plugins và `createFeature` an toàn | mở rộng generator chỉ khi optional boundary có contract thực tế mới |

Lưu ý: `graphify-out/` có thể chậm hơn source khi CLI Graphify chưa được cài trong môi trường; source, build files và tài liệu `architecture/` đã xác minh mới là authority.

## 3. Kiến trúc mục tiêu

### 3.1 Luồng lớp

```text
UI (stateless Compose)
  ↓ state / ↑ action
Presentation (Route, ViewModel, UiState)
  ↓
Domain/use case [chỉ thêm khi logic được dùng lại hoặc ViewModel quá phức tạp]
  ↓
Repository (policy + mapping + source of truth)
  ↓
Data sources (network, Room, DataStore, device APIs)
```

Quy tắc chính:

- `:app` là composition root: tạo graph DI, app shell và gắn navigation engine.
- Feature không gọi Retrofit, DAO, DataStore hoặc Android permission API trực tiếp.
- Repository sở hữu chính sách cache/offline và mapping giữa DTO/entity/model.
- Data source chịu trách nhiệm main-safe và chuyển exception hạ tầng thành contract ổn định.
- Domain layer là tùy chọn, không tạo một use case chỉ để chuyển tiếp một lời gọi repository.
- Các module foundation phải business-neutral. `articles` nên được coi là sample/reference; khi foundation được publish như library, DTO/entity/DAO của sample không thuộc public foundation API.

### 3.2 Mô hình phân phối nên chọn

Khuyến nghị dùng mô hình lai:

```text
Business-neutral reusable libraries
  ui, runtime, platform, security, observability, data infrastructure

Starter/template repository
  app shell, DI composition, sample feature, environment và product-specific wiring
```

Library giúp sửa lỗi và nâng cấp một lần cho nhiều app. Template giúp tạo app mới nhanh nhưng vẫn cho phép thay đổi schema, navigation và branding. Không nên ép toàn bộ app phụ thuộc vào một module foundation khổng lồ.

## 4. Backlog ưu tiên theo lượng công sức tiết kiệm

### P0 — làm trước, ROI cao nhất

| Hạng mục | Nội dung | Vì sao tiết kiệm code |
|---|---|---|
| Đồng bộ tài liệu + Graphify | sửa trạng thái module/implemented, sau đó `graphify update .` | tránh lập kế hoạch dựa trên kiến trúc cũ |
| Gradle convention plugins | `dc.android.application`, `dc.android.library`, `dc.compose`, `dc.hilt`, `dc.room`, `dc.testing` | loại bỏ cấu hình SDK, Java/Kotlin, Compose, lint và test lặp lại ở mọi module |
| Feature template/generator | tạo `UiState`, action, ViewModel, Route, Screen, preview, test và optional API/DAO/module | giảm việc tạo thủ công 7–12 file nhưng vẫn giữ code tường minh |
| UI state kit | `DcLoading`, `DcEmptyState`, `DcErrorState`, `DcOfflineBanner`, `DcPullToRefresh`, `DcSnackbarHost` | gần như app nào cũng lặp lại các trạng thái này |
| Form/component kit | text field, password field, checkbox, radio, switch, top app bar, card, list item, dialog, bottom sheet, icon button | giảm styling và accessibility lặp ở từng màn hình |
| Error contract + mapper | `AppError`/`UiMessage`, retryability, HTTP/domain/storage mapping | ViewModel không tự viết lại `try/catch` và message mapping |
| Testing kit | `MainDispatcherRule`, fake dispatcher, fake logger/analytics/crash, repository builders, Flow/Turbine helpers | mỗi feature có test nhanh mà không tự dựng fixture |
| Quyết định UI event | rà soát `Channel<UiEffect>`; ưu tiên state bền vững cho message/navigation quan trọng, chỉ giữ effect cho integration UI thực sự tức thời | tránh mất event khi lifecycle/configuration thay đổi |

Trạng thái batch P0 ngày 2026-08-22:

| Hạng mục | Trạng thái |
|---|---|
| Tài liệu + graph | tài liệu đã đồng bộ; graph update bị chặn vì CLI `graphify` không có trong PATH |
| Gradle conventions | hoàn thành và áp dụng cho toàn bộ module |
| Feature generator | hoàn thành core + flags + validation/no-overwrite + unit tests |
| UI state/form/list kit | hoàn thành state, text/password và list row lõi; pull-to-refresh/snackbar host và selection/overlay components tiếp tục theo use case |
| Testing kit | hoàn thành module dùng chung và migrate hai ViewModel test |
| UI event | hoàn thành: message bền vững trong state, effect chỉ giữ cho permission launcher |
| Error contract + mapper | còn mở có chủ đích; thực hiện cùng network hardening để tránh tạo hierarchy không có failure case thật |

### P1 — nền tảng cho app production

| Hạng mục | Nội dung |
|---|---|
| Adaptive app shell | `NavigationSuiteScaffold`, list-detail/supporting pane, window size/posture, keyboard/mouse support |
| Navigation hoàn chỉnh | Navigation 3 evaluation/migration, deep links/App Links, nested auth/onboarding flow, typed arguments, state restoration và navigation tests |
| Network hardening | timeout, standardized headers/request ID, connectivity monitor, error-body parser, authentication interceptor, single-flight token refresh, MockWebServer fixtures |
| Offline-first toolkit | sync metadata, stale policy, refresh policy, conflict strategy, transaction boundary, Paging 3 + RemoteMediator khi có use case |
| Database quality | Room schema export, migrations và migration tests, in-memory database factory, destructive migration chỉ ở debug/test |
| Session/auth | credential storage, authenticated session state, refresh/expiry/logout, route guard; biometric chỉ thêm khi có yêu cầu |
| Feature flags/environments | local typed flags, debug override screen, staging/release config; remote provider nằm sau interface |
| Observability production | adapters cho analytics/crash/performance vendor, consent/privacy filter, correlation ID, release-only enablement |
| Common product features | splash/bootstrap, onboarding, sign-in/session, settings/theme/language, search/filter/sort, notification settings, offline indicator |

### P2 — chất lượng, vận hành và khả năng mở rộng

| Hạng mục | Nội dung |
|---|---|
| Screenshot/golden tests | kiểm tra light/dark, font scale và component catalog |
| Accessibility gates | semantics, content descriptions, contrast, minimum touch target, TalkBack và large-font checks |
| Localization | string resources, plural, locale switching, RTL previews/tests; không hard-code user-facing text |
| Performance | Macrobenchmark, Baseline Profile và critical user journeys cho startup/list scroll/navigation |
| Release engineering | signing qua secret store, build variants, R8 rules, versioning/changelog và artifact provenance |
| Static quality | detekt/ktlint hoặc bộ rule tương đương, dependency analysis, binary/public API validation nếu publish library |
| Developer tooling | sample app/catalog, dependency update automation, project bootstrap command và tài liệu “new app/new feature” |

## 5. UI backlog chi tiết

### Foundation của Design System

- color roles cho success/warning/info/offline ngoài Material defaults;
- typography có line height, font scaling và fallback rõ ràng;
- spacing, shape, elevation, motion duration/easing;
- light/dark, dynamic color theo policy, high-contrast policy;
- icon và shared resource strategy;
- adaptive breakpoint/window strategy;
- preview matrix: light/dark × compact/expanded × font scale.

### Component dùng thường xuyên

- Actions: primary/secondary/tertiary/destructive button, icon button, FAB.
- Inputs: text/password/search field, dropdown, checkbox, radio, switch, date/time picker.
- Containers: app bar, card, list row, section header, divider, scaffold.
- Feedback: progress, skeleton, snackbar, banner, badge, error/empty/offline state.
- Overlays: alert dialog, confirmation dialog, modal bottom sheet.
- Lists: pull-to-refresh, paged list, selectable list, swipe action nếu có use case.
- Navigation: adaptive bottom bar/rail/drawer và list-detail scaffold.

Mỗi component chỉ được đưa vào Design System khi nó khóa được ít nhất một policy chung như theme, spacing, accessibility, interaction hoặc analytics. Không wrap Material component chỉ để đổi tên.

## 6. Feature template đề xuất

```text
<feature>/
├─ <Feature>UiState.kt
├─ <Feature>UiAction.kt
├─ <Feature>ViewModel.kt
├─ <Feature>Route.kt
├─ <Feature>Screen.kt
├─ <Feature>Repository.kt           # khi có data
├─ remote/                          # optional
├─ local/                           # optional
├─ di/<Feature>Module.kt            # chỉ khi có binding
└─ test/<Feature>ViewModelTest.kt
```

Generator nên nhận tùy chọn thay vì tạo file rỗng:

```text
createFeature(name, navigation, network, room, permission, backgroundWork)
```

Không tạo `UiEffect`, use case, repository, API hoặc DAO nếu feature không cần.

## 7. Thứ tự triển khai khuyến nghị

1. Đồng bộ tài liệu/graph với module hiện hữu và chốt foundation là library, template hay mô hình lai.
2. Tạo `build-logic` và `testing` để giảm boilerplate ngay lập tức.
3. Mở rộng Design System bằng state kit + form/component kit; thêm catalog và UI tests.
4. Chốt error contract và UI-event policy, rồi cập nhật feature template/generator.
5. Hardening network/Room/offline và thêm integration tests.
6. Xây adaptive app shell, deep links và quyết định Navigation 3.
7. Hoàn thiện session, feature flags, observability production và privacy controls.
8. Thêm screenshot, accessibility, localization, Baseline Profile và release gates.

## 8. Definition of done cho foundation

- Một app mới build được sau khi chỉ đổi namespace, branding và environment.
- Một feature list/detail cơ bản được scaffold trong vài phút, không copy/paste từ feature cũ.
- Feature UI không chứa raw brand values, Retrofit/DAO/DataStore hoặc DI resolution.
- Offline/error/loading/empty state có hành vi và component thống nhất.
- Mỗi module có trách nhiệm, dependency direction và public API rõ ràng.
- Có unit/integration/UI tests mẫu; CI chạy test, lint, assemble và quality gates.
- Không log secret/PII; cấu hình release không chứa credential trong repository.
- Tài liệu kiến trúc luôn phân biệt “đã triển khai” và “mục tiêu”.

## 9. Tài liệu tham chiếu

- Nội bộ: `architecture/00-index.md`, `architecture/01-overview-and-invariants.md`, `architecture/20-feature-template.md`.
- Android Developers: [Architecture recommendations](https://developer.android.com/topic/architecture/recommendations), [Guide to app architecture](https://developer.android.com/topic/architecture), [Adaptive apps](https://developer.android.com/develop/adaptive-apps/guides/get-started-with-adaptive-apps), [Navigation 3](https://developer.android.com/guide/navigation/navigation-3), [Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles/create-baselineprofile).
