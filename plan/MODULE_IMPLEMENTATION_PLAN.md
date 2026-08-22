# Kế hoạch triển khai theo module — 7 module đòn bẩy cao

**Ngày:** 2026-08-23
**Phạm vi:** `ui:designsystem`, `observability`, `runtime:concurrency`, `data:network`, `data:preferences`, `platform:navigation`, `data:database`.
**Nguyên tắc chọn lọc:** chỉ đưa vào những gì (a) lặp lại ở **mọi** app/feature, hoặc (b) khóa một policy mà kỷ luật con người không giữ nổi (an toàn, main-safety, ranh giới lớp). Mọi mục "cố ý không làm" đều có lý do — đó là phần quan trọng ngang phần "làm".

Trạng thái hiện tại dưới đây đã xác minh trực tiếp trên source, không lấy từ `graphify-out/` (graph đang stale).

---

## 0. Điều kiện tiên quyết: module `:foundation`

**Vấn đề:** `NetworkResult.Failure(val throwable: Throwable)` đang mang `Throwable` hạ tầng lên tầng trên — vi phạm invariant "infrastructure exceptions do not leak into UI contracts". `data:preferences` thì nuốt lỗi thành default. Hai module xử lý lỗi theo hai kiểu khác nhau, và không kiểu nào dùng được cho UI.

`ui:designsystem` không được phép phụ thuộc `data:*`, nên contract lỗi không thể sống trong `data:network`. Cần một module primitives thuần Kotlin (đúng ô `foundation/` trong target map của `CLAUDE.md`, hiện chưa tồn tại).

**Tạo `:foundation`** (Kotlin JVM, không dep Android, không dep DI):

```text
foundation/src/main/kotlin/com/dc/android/foundation/core/
├─ AppError.kt
└─ Retryable.kt
```

```kotlin
sealed interface AppError {
    val cause: Throwable?

    /** Không có kết nối, timeout, DNS. Người dùng thử lại được. */
    data class Connectivity(override val cause: Throwable?) : AppError

    /** Server trả non-2xx. [code] để tầng trên phân biệt 401/403/404/5xx. */
    data class Http(val code: Int, val serverMessage: String?, override val cause: Throwable?) : AppError

    /** Response không parse được — lỗi hợp đồng API, không phải lỗi người dùng. */
    data class Serialization(override val cause: Throwable?) : AppError

    /** Room/DataStore/file. */
    data class Storage(override val cause: Throwable?) : AppError

    data class Unknown(override val cause: Throwable?) : AppError
}

/** Chính sách retry tập trung một chỗ, ViewModel không tự đoán. */
val AppError.isRetryable: Boolean
    get() = when (this) {
        is AppError.Connectivity -> true
        is AppError.Http -> code >= 500 || code == 408 || code == 429
        else -> false
    }
```

**Cố ý không làm:** không đặt `UiMessage`/string resource id ở đây — ánh xạ `AppError` → text người dùng là việc của tầng presentation, để `:foundation` không dính `android.content.res`. Không tạo `Result<T, E>` riêng — dùng `NetworkResult` hiện có, đổi payload sang `AppError`.

**Chi phí đổi `NetworkResult`:** chỉ 3 import site trong toàn repo. Đổi ngay, đừng để tích nợ.

---

## 1. `ui:designsystem` — đòn bẩy lớn nhất

**Hiện tại (555 dòng, 14 file, 0 test):** tokens (Color/Spacing/Shape/Motion/Accessibility), `DcTheme`, `DcTypography`, `DcShapes`, và các component `DcButton`, `DcText`, `DcTextField`, `DcPasswordTextField`, `DcListRow`, `DcLoadingState`, `DcEmptyState`, `DcErrorState`, `DcOfflineBanner`, cộng `DesignSystemCatalog`.

### 1.1 `DcContentState` — hạng mục tiết kiệm code nhiều nhất toàn repo

Mọi màn hình list/detail đều viết lại đúng một khối `when` bốn nhánh: loading / lỗi / rỗng / nội dung. Gói nó lại:

```kotlin
// components/DcContentState.kt
@Composable
fun <T> DcContentState(
    value: T?,
    isLoading: Boolean,
    errorMessage: String?,
    isEmpty: (T) -> Boolean = { false },
    onRetry: (() -> Unit)? = null,
    emptyTitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
)
```

Quy tắc ưu tiên hiển thị nằm bên trong: có dữ liệu cũ + đang loading thì hiện dữ liệu cũ kèm indicator, không nháy sang spinner toàn màn hình. Đây chính là policy mà từng feature làm sai theo từng cách khác nhau.

### 1.2 `DcScaffold` + `DcTopAppBar`

Khóa: padding, `WindowInsets`, vị trí `SnackbarHost`, hành vi nút back, `scrollBehavior`. Không phải wrap cho đẹp — hiện mỗi màn hình tự nối `Scaffold` và sẽ lệch insets.

### 1.3 Snackbar gắn với `userMessage`-trong-state

Repo đã chốt hướng "message nằm trong state bền vững" (`ArticlesUiState.userMessage`). Chuẩn hóa nốt phía UI:

```kotlin
@Composable
fun DcSnackbarEffect(
    message: String?,
    hostState: SnackbarHostState,
    onMessageShown: () -> Unit,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
)
```

Không có nó thì mỗi màn hình tự viết `LaunchedEffect(message)` và sẽ quên gọi `onMessageShown`.

### 1.4 Semantic colors ngoài Material

`DcErrorState`/`DcOfflineBanner` hiện phải mượn role `error` của Material cho cả cảnh báo lẫn trạng thái offline. Thêm lớp bổ sung, **chỉ 4 role**:

```kotlin
// theme/DcColors.kt
@Immutable
data class DcColors(
    val success: Color, val onSuccess: Color,
    val warning: Color, val onWarning: Color,
    val info: Color, val onInfo: Color,
    val offline: Color, val onOffline: Color,
)

val LocalDcColors = staticCompositionLocalOf<DcColors> { error("DcTheme missing") }
```

Đồng thời **đổi `DcTheme(dynamicColor = true)` thành mặc định `false`**: dynamic color lấy màu từ wallpaper người dùng, phá vỡ cam kết thương hiệu và làm screenshot test không xác định. Để `true` là opt-in.

### 1.5 `DcPullToRefreshBox`, `DcDialog`, `DcModalBottomSheet`

Ba thứ gần như app nào cũng có, mỗi thứ có một cái bẫy riêng đáng khóa lại: pull-to-refresh phải đồng bộ với `isRefreshing` trong state; dialog cần chặn dismiss khi đang xử lý; bottom sheet cần xử lý insets bàn phím.

### 1.6 Preview matrix bằng multipreview annotation

```kotlin
// preview/DcPreviews.kt
@Preview(name = "Light")
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
annotation class DcThemePreviews

@Preview(fontScale = 1.0f)
@Preview(fontScale = 1.5f)
@Preview(fontScale = 2.0f)
annotation class DcFontScalePreviews
```

Một annotation thay 6 khối `@Preview` copy-paste ở mỗi component.

### 1.7 Test bắt buộc (module này đang 0 test)

Bốn Compose test, không hơn: nhánh ưu tiên của `DcContentState`; `DcSnackbarEffect` gọi `onMessageShown` đúng một lần; `DcTextField` có semantics lỗi cho TalkBack; mọi icon button đạt tối thiểu 48dp theo `AccessibilityTokens`.

### Cố ý không làm

Bộ icon riêng (dùng `material-icons-extended`; chỉ thêm `ui:resources` khi có icon thương hiệu thật); thư viện animation/motion (tokens đã đủ); chart/graph; screenshot test (chờ chốt công cụ ở P2); và **không wrap component Material chỉ để đổi tên** — `DcCheckbox` không khóa thêm policy nào so với `Checkbox` thì không đưa vào.

**Ước lượng:** ~500–600 dòng sản phẩm + ~150 dòng test. Gấp đôi kích thước module, và là chỗ duy nhất mà việc gấp đôi được hoàn vốn theo số màn hình.

---

## 2. `observability` — cross-cutting, mật độ import cao nhất

**Hiện tại (94 dòng, 6 file, 0 test):** `Logger`/`AndroidLogger`, `AnalyticsTracker`/`LoggingAnalyticsTracker`, `CrashReporter`/`LoggingCrashReporter`.

### 2.1 Redaction — hạng mục quan trọng nhất

Cả ba file impl hiện đều có comment dạng "không truyền secret/PII vào đây — người gọi chịu trách nhiệm". Một invariant an toàn dựa vào kỷ luật người gọi thì sớm muộn cũng vỡ. Ép nó vào kiểu dữ liệu:

```kotlin
fun interface Redactor { fun redact(text: String): String }

/** Deny-list: token, password, authorization, secret, email, phone, otp... */
class KeyDenyListRedactor(private val deniedKeys: Set<String> = DEFAULT_DENIED) : Redactor

class RedactingLogger(private val delegate: Logger, private val redactor: Redactor) : Logger
class RedactingAnalyticsTracker(...) : AnalyticsTracker   // lọc cả key lẫn value trong properties
```

Wiring: `:app` luôn bọc impl thật bằng decorator. Không đổi call site nào.

### 2.2 Lazy message

`logger.d(TAG, "state=$bigObject")` vẫn build chuỗi trong release dù `AndroidLogger.d` bỏ qua nó. Thêm overload inline:

```kotlin
inline fun Logger.d(tag: String, message: () -> String)
```

### 2.3 `ObservabilityContext` — thuộc tính chung tự động

```kotlin
interface ObservabilityContext {
    val sessionId: String
    val correlationId: String?     // nối log ↔ analytics ↔ crash ↔ request id của network
    val appVersion: String
    val environment: String        // debug/staging/release
    fun asProperties(): Map<String, String>
}
```

`CompositeAnalyticsTracker` tự merge vào mọi event. Không có nó thì mỗi call site tự truyền và sẽ truyền thiếu.

### 2.4 `AnalyticsEvent` có kiểu, thay magic string

```kotlin
interface AnalyticsEvent {
    val name: String
    val properties: Map<String, String> get() = emptyMap()
}

fun AnalyticsTracker.track(event: AnalyticsEvent)
```

Feature khai báo event của mình dưới dạng `data object`/`data class` — đổi tên event thì compiler bắt lỗi, không phải grep.

### 2.5 Consent gate

```kotlin
class ConsentGatedAnalyticsTracker(
    private val delegate: AnalyticsTracker,
    private val consent: () -> Boolean,
) : AnalyticsTracker
```

Bắt buộc với bất kỳ app nào phát hành ở EU. Rẻ, và không thể thêm sau khi call site đã rải khắp nơi.

### 2.6 Composite sink

`CompositeAnalyticsTracker(vararg trackers)` / `CompositeCrashReporter(...)`: debug đẩy ra logcat, release đẩy sang vendor, chạy song song được trong giai đoạn migrate vendor — không đổi call site.

### 2.7 Tracing tối giản

```kotlin
suspend fun <T> PerformanceTracer.trace(name: String, block: suspend () -> T): T
```

~25 dòng, dùng ngay được cho startup và mỗi lời gọi mạng.

### Cố ý không làm

Không tích hợp Firebase/Sentry/Datadog **bên trong** `observability` — adapter vendor phải nằm ở module riêng (`observability:firebase`) hoặc trong app, nếu không mọi app đều kéo theo SDK nó không dùng. Không ghi log ra file, không upload log từ xa, không OpenTelemetry đầy đủ.

**Ước lượng:** ~250 dòng + ~120 dòng test (redaction và consent phải có test — đây là hai thứ hỏng âm thầm).

---

## 3. `runtime:concurrency` — giữ mỏng, chỉ thêm 3 thứ

**Hiện tại (27 dòng):** `DispatcherProvider` + `DefaultDispatcherProvider`. Đúng như nó cần. **Không đụng vào hai file này.**

### 3.1 `AppCoroutineScope` — thay chỗ cho `GlobalScope`

`CLAUDE.md` cấm `GlobalScope` nhưng không cung cấp phương án thay thế, nên áp lực dùng nó vẫn còn (fire-and-forget khi ViewModel đã chết: ghi analytics, flush cache khi logout).

```kotlin
class AppCoroutineScope(
    dispatchers: DispatcherProvider,
    onUncaughtException: (Throwable) -> Unit,   // KHÔNG dep observability — app nối vào CrashReporter
) : CoroutineScope
```

Giữ INV-03: module không biết DI, không biết observability.

### 3.2 Hai Flow operator, không hơn

```kotlin
fun <T> Flow<T>.stateInWhileSubscribed(scope: CoroutineScope, initialValue: T, stopTimeoutMillis: Long = 5_000): StateFlow<T>

fun <T> Flow<T>.retryWithBackoff(
    maxAttempts: Int = 3,
    initialDelay: Duration = 500.milliseconds,
    factor: Double = 2.0,
    retryIf: (Throwable) -> Boolean,
): Flow<T>
```

Cái đầu lặp nguyên xi ở mọi ViewModel (kể cả hằng số 5000 dễ gõ sai). Cái thứ hai là chính sách retry — thuộc về một chỗ, không phải mỗi repository.

### Cố ý không làm

Không thêm dispatcher mới vào `DispatcherProvider`; không tạo `@IoDispatcher` qualifier (module phải DI-agnostic, qualifier thuộc `:app`); không viết lại operator đã có trong `kotlinx.coroutines`; không actor/channel framework.

**Ước lượng:** ~80 dòng + ~90 dòng test (`runTest` với virtual time cho backoff).

---

## 4. `data:network` — hardening

**Hiện tại (79 dòng):** `NetworkClientFactory` (OkHttp + Retrofit + `Json { ignoreUnknownKeys }`, logging BASIC chỉ ở debug) và `NetworkResult` + `safeApiCall`.

### 4.1 Đổi `Failure` sang `AppError`

```kotlin
data class Failure(val error: AppError) : NetworkResult<Nothing>

suspend fun <T> safeApiCall(
    errorBodyParser: ErrorBodyParser? = null,
    block: suspend () -> T,
): NetworkResult<T>
// IOException → Connectivity; HttpException → Http(code, parsed message); SerializationException → Serialization
```

`safeApiCall` hiện **chưa bắt `SerializationException`** — API đổi schema là app crash. Sửa cùng lúc.

### 4.2 Policy timeout + header — hiện chưa có gì

`createOkHttpClient` đang dùng toàn bộ mặc định OkHttp (10s cho cả ba). Thêm tham số có giá trị mặc định của DC:

```kotlin
fun createOkHttpClient(
    isDebug: Boolean,
    connectTimeout: Duration = 15.seconds,
    readTimeout: Duration = 30.seconds,
    writeTimeout: Duration = 30.seconds,
    interceptors: List<Interceptor> = emptyList(),
    authenticator: Authenticator? = null,
): OkHttpClient
```

Cộng `HeaderInterceptor`: `User-Agent` chuẩn, `Accept-Language` theo locale, và `X-Request-Id` (UUID) — request id chính là `correlationId` ở mục 2.3, cho phép nối log client với log server.

### 4.3 Auth + refresh token single-flight

```kotlin
interface TokenProvider {          // impl nằm ở :security / :app
    suspend fun accessToken(): String?
    suspend fun refresh(): String?
    suspend fun onRefreshFailed()
}

class AuthInterceptor(private val tokens: TokenProvider) : Interceptor
class TokenRefreshAuthenticator(private val tokens: TokenProvider) : Authenticator  // Mutex chống refresh đồng thời
```

Đây là thứ mọi app có đăng nhập đều phải viết, và gần như luôn viết sai lần đầu: N request 401 cùng lúc → N lần refresh → server thu hồi token.

### 4.4 `NetworkMonitor`

```kotlin
interface NetworkMonitor { val isOnline: Flow<Boolean> }
```

`DcOfflineBanner` đã tồn tại trong design system nhưng **chưa có nguồn dữ liệu nào** để bật nó. Đặt tạm ở đây (module đã là Android library); chuyển sang `platform:device` khi module đó ra đời.

### 4.5 Fixtures test — đặt ở `:testing`

`MockWebServerRule` + `enqueueJson(fileName)` + `assertRequest(path, method, headers)`. Đặt trong `:testing` để `data:network` không phải phụ thuộc ngược.

### Cố ý không làm

Không HTTP cache của OkHttp (Room đã là source of truth, hai lớp cache sẽ mâu thuẫn); không GraphQL/gRPC; không certificate pinning bật sẵn (chỉ mở tham số + ghi tài liệu — pin sai là chết app ngoài production); không multipart upload helper cho tới khi có use case thật.

**Ước lượng:** ~280 dòng + ~200 dòng test (MockWebServer cho refresh single-flight và error mapping).

---

## 5. `data:preferences` — mở khóa module đang không dùng được

**Hiện tại (48 dòng):** `PreferencesDataSource` chỉ có `getInt`/`setInt`. Bất kỳ app thật nào cũng phải sửa module foundation ngay ngày đầu — trái mục tiêu "app mới chỉ đổi namespace/branding/environment".

### 5.1 Typed key

```kotlin
sealed class PreferenceKey<T>(val name: String, val default: T) {
    class IntKey(name: String, default: Int) : PreferenceKey<Int>(name, default)
    class LongKey(name: String, default: Long) : PreferenceKey<Long>(name, default)
    class FloatKey(name: String, default: Float) : PreferenceKey<Float>(name, default)
    class BooleanKey(name: String, default: Boolean) : PreferenceKey<Boolean>(name, default)
    class StringKey(name: String, default: String) : PreferenceKey<String>(name, default)
    class StringSetKey(name: String, default: Set<String>) : PreferenceKey<Set<String>>(name, default)
    class EnumKey<E : Enum<E>>(name: String, default: E, val values: Array<E>) : PreferenceKey<E>(name, default)
    class JsonKey<T>(name: String, default: T, val serializer: KSerializer<T>) : PreferenceKey<T>(name, default)
}

interface PreferencesDataSource {
    fun <T> observe(key: PreferenceKey<T>): Flow<T>
    suspend fun <T> get(key: PreferenceKey<T>): T
    suspend fun <T> set(key: PreferenceKey<T>, value: T)
    suspend fun <T> remove(key: PreferenceKey<T>)
    suspend fun clear()
    /** Nhiều thao tác ghi trong MỘT transaction — tránh state nửa vời khi đổi theme + locale cùng lúc. */
    suspend fun edit(block: suspend PreferencesEditor.() -> Unit)
}
```

`EnumKey` và `JsonKey` là hai thứ giúp module này dùng được cho theme mode và onboarding state mà không phải tự encode chuỗi ở từng feature.

### 5.2 Migration có phiên bản

```kotlin
interface PreferencesMigration {
    val toVersion: Int
    suspend fun migrate(editor: PreferencesEditor)
}
```

Key đổi tên là chuyện chắc chắn xảy ra ở app sống lâu; không có đường migrate thì hoặc mất dữ liệu người dùng, hoặc phải giữ key rác vĩnh viễn.

### 5.3 `InMemoryPreferencesDataSource` trong `:testing`

Mọi test chạm preferences hiện phải dựng DataStore thật trên đĩa.

### Cố ý không làm

Không encrypted preferences (đã có `SecureStorage` ở `:security` — hai đường làm cùng một việc là mời gọi lưu token nhầm chỗ); không Proto DataStore (chỉ khi có schema lớn thật sự); không multi-process.

**Ước lượng:** ~220 dòng + ~150 dòng test.

---

## 6. `platform:navigation` — sửa ranh giới, thêm đúng một năng lực

**Hiện tại (29 dòng):** `sealed interface Destination` với hai `data object` `Greeting` và `Articles`.

### 6.1 Tách destination của sample ra khỏi module hạ tầng

Đây là **cùng một lỗi** với `data:database`: module nền tảng dùng chung đang hard-code destination của feature mẫu. App thứ hai kế thừa nguyên hai màn hình nó không có.

```kotlin
// platform:navigation — chỉ còn contract
interface Destination                    // marker; @Serializable do nơi khai báo tự gắn
interface DestinationRegistry { val startDestination: Destination }
```

Chuyển `Greeting`/`Articles` sang `:app` (hoặc feature module tương ứng khi đã chốt việc module hóa feature).

### 6.2 Deep link — use case đã tồn tại sẵn

`ArticlesSyncWorker` đã bắn notification, nhưng chạm vào notification hiện không mở được đúng màn hình. Đây là nhu cầu thật, không phải phòng xa:

```kotlin
interface DeepLinkResolver { fun resolve(uri: Uri): Destination? }
data class DeepLinkPattern(val pattern: String, val toDestination: (Map<String, String>) -> Destination?)
```

### 6.3 `Navigator` — ghi rõ điều kiện kích hoạt, chưa làm bây giờ

Comment trong `Destination.kt` đã lập luận đúng rằng chưa cần wrapper: hai destination hiện là peer, chỉ composition root chuyển giữa chúng. Giữ nguyên quyết định, nhưng ghi rõ mốc kích hoạt:

> Thêm `Navigator` khi xuất hiện feature đầu tiên cần tự điều hướng sang feature khác (list → detail nằm ở feature khác, hoặc flow đăng nhập → màn hình gốc). Khi đó contract là `interface Navigator { val events: Flow<NavigationEvent>; fun navigate(destination, options); fun back() }` cộng `NavOptions(popUpTo, inclusive, singleTop)`.

### Cố ý không làm

Không migrate Navigation 3 bây giờ (đánh giá riêng — và mục 6.1 khiến việc migrate rẻ hơn về sau); không tự quản back stack; không DSL nested graph.

**Ước lượng:** ~90 dòng + ~60 dòng test. Module vẫn phải mỏng — giá trị của nó là ranh giới, không phải số dòng.

---

## 7. `data:database` — tách sample rồi mới nói chuyện tái sử dụng

**Hiện tại (50 dòng, 3 file):** `DcDatabase`, `ArticleEntity`, `ArticleDao` — tức 2/3 module là model của feature mẫu, còn `@Database(entities = [ArticleEntity::class], version = 1, exportSchema = false)` hard-code danh sách entity. Ở trạng thái này module **không tái sử dụng được** cho app khác.

### 7.1 Chuyển `ArticleEntity`/`ArticleDao`/`DcDatabase` sang `:app`

Đúng theo nguyên tắc đã ghi trong roadmap: DTO/entity/DAO của sample không thuộc public foundation API. App sở hữu database của nó — đó là điều đúng, vì Room không cho phép ghép entity từ nhiều nguồn vào một `@Database` một cách động.

### 7.2 Phần còn lại của module là hạ tầng thật

```kotlin
// RoomDatabaseFactory.kt — builder chuẩn hoá cho mọi app
fun <T : RoomDatabase> buildDatabase(
    context: Context,
    klass: Class<T>,
    name: String,
    dispatchers: DispatcherProvider,
    migrations: List<Migration> = emptyList(),
    allowDestructiveMigrationInDebug: Boolean = false,
): T
```

Khóa được hai thứ: `setQueryExecutor`/`setTransactionExecutor` trỏ vào `dispatchers.io` (main-safety không còn phụ thuộc việc nhớ đánh dấu `suspend`), và destructive migration **chỉ** bật được ở debug.

```kotlin
// TransactionRunner.kt
interface TransactionRunner { suspend fun <T> inTransaction(block: suspend () -> T): T }
```

Để repository không phải import `androidx.room.withTransaction` — giữ DEP-04.

### 7.3 `exportSchema = true` + schema location trong convention plugin

Đặt `room.schemaLocation` một lần trong `DcRoomConventionPlugin`, không lặp ở từng module. Không có schema export thì không viết được migration test, và không có migration test thì mọi bản cập nhật là một canh bạc với dữ liệu người dùng.

### 7.4 `SyncMetadata` — phần tái sử dụng thật cho offline-first

```kotlin
@Entity(tableName = "dc_sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val key: String,
    val lastSyncAt: Long,
    val etag: String?,
)

interface SyncMetadataDao { /* observe / upsert / clear */ }

class StalenessPolicy(private val ttl: Duration) {
    fun isStale(lastSyncAt: Long, now: Long): Boolean
}
```

Hiện `ArticleEntity` tự mang cột `fetchedAt` — đúng ý tưởng nhưng nằm sai chỗ, mỗi entity mới lại phải tự nghĩ lại. Đây là mảnh offline-first duy nhất đáng tổng quát hóa ngay.

### 7.5 `MigrationTestHelper` wrapper trong `:testing`

### Cố ý không làm

Không Paging 3 / `RemoteMediator` cho tới khi có màn hình danh sách thật sự dài; không FTS; không hỗ trợ nhiều database; không repository generic (`BaseRepository` bị cấm rõ ràng).

**Ước lượng:** ~200 dòng + ~120 dòng test (một migration test thật, chạy trên schema đã export).

---

## 8. Thứ tự triển khai

| Bước | Nội dung | Vì sao trước |
|---|---|---|
| 1 | `:foundation` + `AppError`, đổi `NetworkResult.Failure` | Mọi module còn lại ánh xạ lỗi về đây; đổi lúc chỉ có 3 call site |
| 2 | `runtime:concurrency` (3 mục) | Rẻ, không phụ thuộc gì, mở khóa test cho các bước sau |
| 3 | `data:preferences` typed key | Đang chặn mọi app thật; độc lập với UI |
| 4 | `ui:designsystem` (1.1–1.7) | Đòn bẩy lớn nhất, nhưng `DcContentState`/`DcErrorState` cần `AppError` ở bước 1 |
| 5 | `observability` (redaction → context → consent → composite) | Redaction làm trước, ba mục sau nối vào |
| 6 | `data:network` hardening | Dùng `AppError` (b1), `correlationId` (b5), retry (b2) |
| 7 | `data:database` tách sample + sync metadata | Chạm nhiều file `:app`, làm sau khi các contract đã ổn định |
| 8 | `platform:navigation` tách destination + deep link | Phụ thuộc quyết định module hóa feature (xem review `BASE_PROJECT_ROADMAP.md`) |

Tổng ước lượng: **~1.700 dòng sản phẩm + ~900 dòng test**, đưa foundation từ ~1.250 lên ~3.000 dòng với độ phủ test thật ở 7/7 module — và mỗi dòng thêm vào đều trả lời một câu hỏi mà app tiếp theo chắc chắn sẽ hỏi.

---

## 9. Definition of done chung cho mọi module ở trên

- Mọi API public có KDoc nêu **policy được khóa**, không chỉ mô tả tham số.
- Không module nào chứa type của feature mẫu (`Article*`, `Greeting*`).
- Không module nào (trừ `:app`) import `androidx.navigation`, Retrofit, Room, DataStore ngoài module sở hữu.
- Mỗi module có ít nhất một test cho phần **policy** (retry, redaction, migration, staleness, ưu tiên hiển thị) — không tính test getter/setter.
- Bổ sung fake tương ứng vào `:testing` cùng lúc với contract, không để sau.
- Chạy `graphify update .` sau khi tách sample ra khỏi `data:database` và `platform:navigation` (thay đổi topology thật).
