# Triển khai `:observability`

Hợp đồng log, analytics, crash trung lập vendor; bản mặc định chỉ ghi qua `Logger`.

## Khi nào dùng
Mọi chỗ cần log/sự kiện/báo lỗi: inject interface, không gọi `android.util.Log` hay SDK vendor trực tiếp.

## Thêm vào dự án
```kotlin
dependencies { implementation(project(":observability")) }
```

## Nối dây (ở `:app`)
```kotlin
@Provides @Singleton fun logger(): Logger = AndroidLogger(isDebug = AppEnvironmentProvider.current == AppEnvironment.DEBUG)
@Provides @Singleton fun analytics(l: Logger): AnalyticsTracker = LoggingAnalyticsTracker(l)
@Provides @Singleton fun crash(l: Logger): CrashReporter = LoggingCrashReporter(l)
```
Khi chọn vendor (Firebase…): viết adapter implement `AnalyticsTracker`/`CrashReporter` **ở `:app`** và đổi `@Provides`; module này không phụ thuộc SDK.

## Cách dùng
```kotlin
logger.d("ArticlesRepo", "refresh started")
logger.e("ArticlesRepo", "refresh failed", throwable)
analytics.trackEvent("article_opened", mapOf("source" to "list"))
crash.recordException(throwable); crash.log("breadcrumb")
```

## Test
`RecordingLogger`, `NoOpLogger` và các fake khác trong `:testing`. Chạy test của module phụ thuộc.

## Lỗi hay gặp
- Đưa credential, token, secret, PII hay payload thô vào log/event (SEC-01).
- Tên sự kiện/thuộc tính không ổn định hoặc mang tính nghiệp vụ nội bộ.

Tài liệu: `architecture/15-observability/` và `architecture/12-security-and-session.md`.
