# Triển khai `:testing`

Rule và fake dùng chung, chỉ cho cấu hình test.

## Thêm vào dự án
```kotlin
dependencies { testImplementation(project(":testing")) }   // không bao giờ implementation
```
Module phơi `api` tới `:runtime:concurrency`, `:data:preferences`, `:observability`, `:runtime:scheduling` vì chữ ký fake cần các kiểu này.

## Thành phần
| Thành phần | Dùng để |
|---|---|
| `MainDispatcherRule(testDispatcher)` | đặt `Dispatchers.Main` cho test ViewModel |
| `FakeDispatcherProvider(dispatcher)` | cả 4 dispatcher trỏ về dispatcher test |
| `InMemoryPreferencesDataSource` | thay `PreferencesDataSource` |
| `FakeScheduler` | ghi `scheduleCommands`/`cancelledIds`, đặt `nextOutcome` |
| `NoOpLogger`/`RecordingLogger`, `NoOpAnalyticsTracker`/`RecordingAnalyticsTracker`, `NoOpCrashReporter`/`RecordingCrashReporter` | bỏ qua hoặc kiểm tra log/sự kiện/lỗi |

## Mẫu test ViewModel
```kotlin
class GreetingViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val dispatchers = FakeDispatcherProvider(Dispatchers.Main) // Main đã được rule thay bằng dispatcher test

    private fun createViewModel() = GreetingViewModel(
        FakeGreetingProvider(),
        GreetingPreferencesRepository(InMemoryPreferencesDataSource()),
        dispatchers, NoOpLogger, SavedStateHandle(),
    )

    @Test fun `Refresh advances to the next name`() = runTest {
        val vm = createViewModel()
        vm.onAction(GreetingUiAction.Refresh); advanceUntilIdle()
        assertEquals("Hello World!", vm.uiState.value.message)
    }
}
```
Mẫu thật: `app/src/test/.../GreetingViewModelTest.kt`, `ArticlesViewModelTest.kt`.

## Quy tắc
Fake xác định, tối thiểu, không chứa hành vi production, không kèm thư viện mocking. Chỉ thêm tiện ích khi ≥ 2 module dùng.

Chạy: `./gradlew :testing:testDebugUnitTest` và một module tiêu thụ. Tài liệu: `architecture/17-testing.md`; wiring chi tiết: `testing/README.md`.
