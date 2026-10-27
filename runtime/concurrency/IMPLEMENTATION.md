# Triển khai `:runtime:concurrency`

Hợp đồng dispatcher và helper coroutine/Flow. Kotlin/JVM thuần, mọi helper đều tuỳ chọn.

## Khi nào dùng
- Mọi nơi cần dispatcher: inject `DispatcherProvider` thay vì gọi `Dispatchers.*`.
- Cần scope vòng đời process (thay `GlobalScope`), retry, gộp request trùng, hoặc chia sẻ Flow cho UI.

## Thêm vào dự án
```kotlin
dependencies { implementation(project(":runtime:concurrency")) }
```

## Nối dây (ở `:app`, vì module không biết Hilt)
```kotlin
@Module @InstallIn(SingletonComponent::class)
object ConcurrencyModule {
    @Provides @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}
```
Scope cấp process: `AppCoroutineScope(dispatcherProvider) { t -> logger.e("Tag", "…", t) }` (xem `DcApplication`).

## Cách dùng
| Cần | API |
|---|---|
| Chạy trên IO/Default | `onIo(dispatchers) { … }`, `onDefault`, `flow.flowOnIo(dispatchers)` |
| Retry có backoff | `retrying(RetryPolicy.Default) { attempt -> … }` / `flow.retrying(policy)` |
| Gộp lời gọi cùng key | `SingleFlight<K, V>(scope).run(key) { … }` |
| Job đặt tên | `JobRegistry(scope).launchLatest(key) { … }` / `launchIfIdle` / `cancel` |
| Flow → StateFlow cho UI | `flow.stateInWhileSubscribed(scope, initial)` (dùng `WhileUiSubscribed`), `throttleFirst(window)` |

`RetryPolicy`: `None`, `Default` (3 lần), `Background` (5 lần, tối đa 60s); mặc định chỉ retry `IOException`.

## Test
Thay `DispatcherProvider` bằng `FakeDispatcherProvider(testDispatcher)` từ `:testing`. Chạy: `./gradlew :runtime:concurrency:test`.

## Lỗi hay gặp
- Nuốt `CancellationException` trong helper mới (phải ném lại).
- Chạy việc dùng chung của `SingleFlight`/`JobRegistry` trên coroutine của caller thay vì scope truyền vào constructor.
- Thêm phụ thuộc Android/Hilt vào module.

Tài liệu: `architecture/09-concurrency-and-threading.md`.
