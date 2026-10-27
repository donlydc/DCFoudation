# Triển khai `:app`

Composition root: app shell, Hilt, điều hướng thật, cấu hình, và hai feature tham chiếu (`greeting`, `articles`).

## Khi nào sửa `:app`
- Thêm feature mới, binding Hilt cho module thư viện, route mới, worker/receiver cụ thể, adapter vendor (analytics/crash).
- Chính sách tái sử dụng được thì thuộc module thư viện sở hữu, không đặt ở đây.

## Khởi động (đã có sẵn)
`DcApplication` (`@HiltAndroidApp`, `Configuration.Provider` cho `HiltWorkerFactory`, lập lịch sync) → `MainActivity` (`DcTheme` + `NavHost` + `DcNavigationSuite`). Cấu hình giao diện/ngôn ngữ/môi trường nằm trong `config/`; ngôn ngữ chỉ đổi qua `AppLanguageController`.

## Thêm feature mới (checklist)
1. Sinh khung: `./gradlew :app:createFeature -PfeatureName=Profile [-Pnavigation=true -Pnetwork=true -Proom=true -Ppermission=true -PbackgroundWork=true]`.
2. Hoàn thiện theo mẫu, mỗi file một trách nhiệm:

| File | Vai trò |
|---|---|
| `ProfileUiState` / `ProfileUiAction` | data class trạng thái / sealed interface hành động; không chứa kiểu hạ tầng |
| `ProfileViewModel` | `@HiltViewModel`, `StateFlow<UiState>` + `onAction`; mở rộng `ViewModel()` trực tiếp |
| `ProfileScreen` | Composable không trạng thái `(state, onAction, modifier)`; không DI, không import ViewModel |
| `ProfileRoute` | nơi duy nhất gọi `hiltViewModel()`, `collectAsStateWithLifecycle()`, dựng `DcScaffold` |
| `ProfileRepository` (+ data source) | nối `:data:*`; map DTO/Entity → model miền |
| `ProfileModule` | chỉ khi cần `@Binds`/`@Provides`, đặt cùng package (không dùng `AppModule` khổng lồ) |

3. Điều hướng: thêm `Destination.Profile` ở `:platform:navigation`, đăng ký `composable<Destination.Profile>` ở `MainActivity`, thêm mục vào `DcNavigationSuite`.
4. Dữ liệu: dùng `safeApiCall` (network), DAO sau repository (database), `PreferencesDataSource` (cấu hình); lỗi trả về dạng `AppError`.
5. Nền: định nghĩa `@HiltWorker` kế thừa `DcWorker`, lập lịch bằng `DcScheduler`.
6. Test ViewModel với `MainDispatcherRule` + fake từ `:testing` (mẫu: `GreetingViewModelTest`, `ArticlesViewModelTest`).

## Thêm binding cho module thư viện
Mỗi capability một `@Module` trong `di/` (`ConcurrencyModule`, `DataStoreModule`, `DatabaseModule`, `NetworkModule`, `ObservabilityModule`, `SecurityModule`). Module thư viện không biết Hilt, nên `@Provides` luôn ở đây. Chi tiết từng binding: xem `IMPLEMENTATION.md` của module tương ứng.

## Mẫu tham chiếu
- `greeting/`: DataStore → repository → ViewModel (có `SavedStateHandle`) → Route/Screen.
- `articles/`: offline-first (Retrofit + Room + `ArticlesSyncWorker`), `UiState/Action/Effect`.
- `showcase/trip/`: màn hình trình diễn design system, không phải feature thật, không có nguồn dữ liệu.

## Kiểm tra
`./gradlew :app:testDebugUnitTest` → `:app:lintDebug` → `:app:assembleDebug` (CI chạy đúng chuỗi này).

## Lỗi hay gặp
- Composable truy cập Retrofit/DAO/DataStore/repository trực tiếp.
- Feature import phần private của feature khác.
- Áp cấu hình giao diện ngoài `DcTheme` ở `MainActivity`.
- Tạo effect cho thông báo người dùng (dùng nullable state + action xác nhận).

Tài liệu: `architecture/04-app-bootstrap-and-lifecycle.md`, `05-presentation-udf.md`, `08-dependency-injection-hilt.md`, `20-feature-template.md`.
