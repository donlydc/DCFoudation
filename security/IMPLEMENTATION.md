# Triển khai `:security`

Lưu key/value nhạy cảm đã mã hoá. Chưa có luồng auth/session (cố ý).

## Khi nào dùng
Lưu token/khoá cục bộ. Cấu hình thường → `:data:preferences`.

## Thêm vào dự án
```kotlin
dependencies { implementation(project(":security")) }   // tự kéo :runtime:concurrency
```

## Nối dây (ở `:app`)
```kotlin
@Provides @Singleton
fun provideSecureStorage(@ApplicationContext c: Context, d: DispatcherProvider): SecureStorage =
    EncryptedSharedPreferencesSecureStorage(c, d)
```

## Cách dùng
```kotlin
interface SecureStorage {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun remove(key: String)
}
```
Các hàm đã main-safe (chạy qua `DispatcherProvider`). Khi Keystore/kho khoá không dùng được, module ném `SecureStorageUnavailableException`; repository bắt và map sang `AppError.Storage`.

## Test
`SecureStorageUnavailableExceptionTest`. Dùng fake `SecureStorage` trong test feature; test hành vi Keystore bằng instrumentation. Chạy: `./gradlew :security:testDebugUnitTest`.

## Lỗi hay gặp
- Log hoặc lưu bí mật ngoài ranh giới mã hoá.
- Tạo luồng đăng nhập giả chỉ để "dùng" module.

Tài liệu: `architecture/12-security-and-session.md`.
