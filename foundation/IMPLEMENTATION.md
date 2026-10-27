# Triển khai `:foundation`

Primitive Kotlin thuần (không Android/Hilt). Hiện chỉ có `AppError` và `AppError.isRetryable`.

## Khi nào dùng
- Cần một từ vựng lỗi hạ tầng chung giữa data và presentation.
- Chỉ **thêm primitive mới** khi ≥ 2 module cần cùng một policy.

## Thêm vào dự án
```kotlin
// build.gradle.kts của module dùng
dependencies { implementation(project(":foundation")) }
```
Module này dùng plugin `dc.kotlin.jvm` + `dc.testing`.

## Cách dùng
```kotlin
val error: AppError = AppError.Http(code = 503)
if (error.isRetryable) { /* Connectivity, HTTP 408/429/5xx → true */ }
```
- Biến thể: `Connectivity`, `Http(code, serverMessage)`, `Serialization`, `Storage`, `Unknown`; `cause` chỉ để chẩn đoán.
- Tầng presentation map `AppError` → chuỗi hiển thị (string resource nằm ở `:app`), không bao giờ render `cause.message`.

## Test
`foundation/src/test/.../AppErrorTest.kt`. Chạy: `./gradlew :foundation:test`.

## Lỗi hay gặp
- Đưa resource ID, message hay kiểu Android vào đây.
- Thêm biến thể `AppError` rồi quên cập nhật `isRetryable` (when là exhaustive nên compiler sẽ báo).

Tài liệu: `architecture/11-error-and-result-model.md`.
