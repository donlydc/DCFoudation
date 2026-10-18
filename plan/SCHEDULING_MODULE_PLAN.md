# Kế hoạch module `:runtime:scheduling` — đơn giản hóa WorkManager + nền tảng cho app alarm/reminder

**Ngày:** 2026-08-23
**Thay thế:** `:runtime:background` (hiện có 1 file, 54 dòng, 1 consumer duy nhất là `DcApplication`)
**Bối cảnh mục tiêu:** ngay sau khi có module này sẽ triển khai một app alarm hoặc reminder.

Mọi trạng thái hiện tại dưới đây đã xác minh trên source, không lấy từ `graphify-out/`.

---

## 0. Vấn đề phải nói trước: WorkManager KHÔNG làm được alarm

Đây là điều quan trọng nhất trong tài liệu này. Nếu xây app alarm/reminder trên WorkManager, app sẽ chạy sai và lỗi chỉ lộ ra khi đã lên tay người dùng.

| Ràng buộc của WorkManager | Hệ quả cho app nhắc nhở |
|---|---|
| Chu kỳ tối thiểu **15 phút** | Không đặt được nhắc nhở "sau 5 phút" |
| `setInitialDelay` là **độ trễ tối thiểu**, không phải mốc chính xác | Nhắc 07:00 có thể nổ 07:00, cũng có thể 07:40 |
| Công việc bị hoãn trong **Doze** | Điện thoại để yên qua đêm → báo thức không kêu |
| Ràng buộc (mạng, pin, sạc) có thể chặn thực thi | Nhắc uống thuốc bị hoãn vì pin yếu |

Công cụ đúng cho "phải nổ đúng giờ" là **`AlarmManager`** (`setAlarmClock` / `setExactAndAllowWhileIdle`), không phải WorkManager.

**Vì vậy module này không chỉ bọc WorkManager.** Nó là một lớp lập lịch thống nhất, tự chọn đúng engine theo loại kích hoạt — và đó cũng chính là phần "đơn giản hóa" có giá trị nhất: lập trình viên khai báo *khi nào cần chạy*, module quyết định *chạy bằng gì*.

```text
                    DcScheduler.schedule(id, trigger, payload)
                                    │
                 ┌──────────────────┴──────────────────┐
        "phải nổ đúng giờ"                   "chạy lúc nào cũng được"
                 │                                     │
          AlarmManager                            WorkManager
    ExactAt · DailyAt · WeeklyAt            After · Every · WhenIdle
```

---

## 1. Quyết định module

**Đổi tên `:runtime:background` → `:runtime:scheduling`.**

Lý do: `PeriodicSyncScheduler` là file duy nhất trong module và chỉ có **một** consumer thật (`DcApplication.kt:37`). Đổi tên bây giờ tốn vài phút; sau khi có 10 app dùng thì không đổi được nữa. Tên "background" cũng sai nghĩa cho báo thức — một alarm clock là thứ người dùng nhìn thấy và nghe thấy, không phải việc chạy ngầm.

```text
runtime/scheduling/
├─ DcScheduler.kt              facade + DcTrigger (điểm vào duy nhất)
├─ work/                       WorkManager
│  ├─ DcWorkScheduler.kt       enqueue/cancel/observe
│  ├─ DcJobSpec.kt             định nghĩa job có kiểu
│  ├─ DcWorker.kt              base class thay CoroutineWorker
│  ├─ DcConstraints.kt         ràng buộc
│  └─ DcRetryPolicy.kt         backoff + trần số lần thử
├─ alarm/                      AlarmManager
│  ├─ DcAlarmScheduler.kt      đặt/huỷ/liệt kê
│  ├─ DcAlarm.kt               mô hình alarm + lịch lặp
│  ├─ DcAlarmStore.kt          lưu bền (bắt buộc — xem §4.2)
│  ├─ DcAlarmReceiver.kt       base receiver cho app
│  ├─ DcAlarmRestorer.kt       khôi phục sau reboot/update/đổi múi giờ
│  └─ DcExactAlarmPermission.kt
└─ NextOccurrence.kt           tính mốc kế tiếp có xử lý DST
```

Giữ nguyên nguyên tắc đang có của `:runtime:background`: **module không import Hilt** (INV-03), `@HiltWorker` vẫn nằm ở `:app`.

---

## 2. Điều kiện tiên quyết: bật core library desugaring

Đã xác minh `build-logic/src/main/kotlin/ConventionHelpers.kt`: `minSdk = 24`, và **không** có `isCoreLibraryDesugaringEnabled`. Nghĩa là `java.time` (`LocalTime`, `ZonedDateTime`, `ZoneId`, `Instant`) **không dùng được** trên API 24–25.

Một app nhắc nhở về bản chất là toán học ngày giờ: "7 giờ sáng mỗi ngày", "thứ Hai và thứ Năm", "sau 30 phút", cộng xử lý đổi múi giờ và DST. Làm việc đó bằng `Calendar` và cộng trừ mili-giây là con đường chắc chắn dẫn tới lỗi lệch một giờ hai lần mỗi năm.

**Việc cần làm trước tiên** — trong `DcAndroidLibraryConventionPlugin` và `DcAndroidApplicationConventionPlugin`:

```kotlin
extension.compileOptions.isCoreLibraryDesugaringEnabled = true
// + dependencies.add("coreLibraryDesugaring", libs.findLibrary("desugar-jdk-libs").get())
```

Phương án thay thế là nâng `minSdk` lên 26, nhưng bỏ API 24–25 chỉ để tránh một dòng cấu hình là đánh đổi tệ hơn.

---

## 3. Lớp A — Đơn giản hóa WorkManager

### 3.1 Những chỗ WorkManager gây phiền

| Phiền toái | Cách module xử lý |
|---|---|
| `Data.Builder().putString("key", ...)` — không kiểu, gõ sai key thì im lặng | Input có kiểu qua `kotlinx.serialization` |
| Tên unique work là chuỗi rải rác | `DcJobSpec.name` khai báo một chỗ |
| `Constraints.Builder()` dài dòng, lặp ở mọi nơi | `DcConstraints` với giá trị mặc định |
| `Result.retry()` không có trần → thử lại vô hạn | `DcRetryPolicy.maxAttempts`, tự chuyển thành thất bại khi vượt |
| Chu kỳ < 15 phút bị **âm thầm** nâng lên 15 | Ném lỗi ngay khi khai báo, kèm thông điệp rõ |
| `WorkInfo` → trạng thái UI phải tự map | `Flow<DcJobState>` |
| Test cần `WorkManagerTestInitHelper` + emulator | `FakeWorkScheduler` trong `:testing` |

Dòng "âm thầm nâng lên 15 phút" đáng chú ý: `PeriodicSyncScheduler` hiện tại chỉ ghi chú điều đó trong KDoc và để WorkManager tự clamp. Người viết feature đặt 5 phút sẽ tin là 5 phút. Thất bại to tiếng tốt hơn.

### 3.2 API

```kotlin
data class DcJobSpec<I : Any>(
    val name: String,
    val worker: KClass<out ListenableWorker>,
    val input: I? = null,
    val inputSerializer: KSerializer<I>? = null,
    val constraints: DcConstraints = DcConstraints.None,
    val retry: DcRetryPolicy = DcRetryPolicy.Default,
    val initialDelay: Duration = Duration.ZERO,
    val expedited: Boolean = false,
    val tags: Set<String> = emptySet(),
)

data class DcConstraints(
    val network: NetworkRequirement = NetworkRequirement.None,   // None/Connected/Unmetered
    val requiresCharging: Boolean = false,
    val requiresBatteryNotLow: Boolean = false,
    val requiresStorageNotLow: Boolean = false,
    val requiresDeviceIdle: Boolean = false,
) {
    companion object {
        val None = DcConstraints()
        val Network = DcConstraints(network = NetworkRequirement.Connected)
        /** Đồng bộ nặng: chỉ chạy khi cắm sạc và Wi-Fi. */
        val Sync = DcConstraints(network = NetworkRequirement.Unmetered, requiresCharging = true)
    }
}

data class DcRetryPolicy(
    val maxAttempts: Int = 5,
    val initialBackoff: Duration = 30.seconds,
    val exponential: Boolean = true,
) { companion object { val Default = DcRetryPolicy(); val NoRetry = DcRetryPolicy(maxAttempts = 1) } }

interface DcWorkScheduler {
    fun <I : Any> enqueueOnce(spec: DcJobSpec<I>, replaceExisting: Boolean = false)
    fun <I : Any> enqueuePeriodic(spec: DcJobSpec<I>, every: Duration, flex: Duration? = null, keepExisting: Boolean = true)
    fun cancel(name: String)
    fun cancelByTag(tag: String)
    fun observe(name: String): Flow<DcJobState>
}

sealed interface DcJobState {
    data object Enqueued : DcJobState
    data class Running(val progress: Float?) : DcJobState
    data object Succeeded : DcJobState
    data class Failed(val error: AppError?) : DcJobState      // dùng AppError của :foundation
    data object Cancelled : DcJobState
}
```

### 3.3 `DcWorker` — base class thay `CoroutineWorker`

```kotlin
abstract class DcWorker<I : Any>(
    context: Context,
    params: WorkerParameters,
    private val serializer: KSerializer<I>? = null,
) : CoroutineWorker(context, params) {

    abstract suspend fun run(input: I?): DcJobResult

    suspend fun reportProgress(fraction: Float)

    final override suspend fun doWork(): Result = /* giải mã input, gọi run(), map kết quả,
        áp trần retry theo runAttemptCount, bắt Throwable → AppError */
}

sealed interface DcJobResult {
    data object Success : DcJobResult
    data object Retry : DcJobResult
    data class Failure(val error: AppError) : DcJobResult
}
```

`DcJobResult` thay `ListenableWorker.Result` vì hai lý do có thật: `Result.retry()` không có trần nên một job hỏng vĩnh viễn sẽ thử lại mãi mãi, và `Result.failure()` không mang được nguyên nhân — tầng trên không biết vì sao hỏng. `DcWorker` đọc `runAttemptCount`, so với `retry.maxAttempts`, và tự chuyển `Retry` thành `Failure` khi hết lượt.

So sánh `ArticlesSyncWorker` hiện tại: nó trả `Result.retry()` ở cả nhánh lỗi mạng lẫn nhánh `catch (t: Throwable)` — một lỗi lập trình (NPE chẳng hạn) sẽ được thử lại vô hạn theo backoff. Đây là lỗi thật mà `DcWorker` đóng lại được ở một chỗ.

---

## 4. Lớp B — Alarm / Reminder

### 4.1 Mô hình

```kotlin
data class DcAlarm(
    val id: String,
    val trigger: DcTrigger,
    val payloadJson: String? = null,
    val exactness: DcExactness = DcExactness.Exact,
    val receiver: String,                      // tên lớp BroadcastReceiver của app
)

enum class DcExactness {
    /** setAlarmClock — miễn trừ Doze, hiện icon báo thức. Cho app báo thức thật. */
    AlarmClock,
    /** setExactAndAllowWhileIdle — nổ đúng giờ kể cả trong Doze, nhưng bị giới hạn tần suất. */
    Exact,
    /** setAndAllowWhileIdle — hệ thống được phép gom nhóm. Cho nhắc nhở không gấp. */
    Inexact,
}

sealed interface DcAlarmRepeat {
    data object None : DcAlarmRepeat
    data class Daily(val time: LocalTime, val zone: ZoneId) : DcAlarmRepeat
    data class Weekly(val time: LocalTime, val days: Set<DayOfWeek>, val zone: ZoneId) : DcAlarmRepeat
    data class EveryInterval(val interval: Duration) : DcAlarmRepeat
}
```

### 4.2 Lưu bền là **bắt buộc**, không phải tùy chọn

Alarm đã đặt bị hệ thống xóa sạch trong bốn tình huống: khởi động lại máy, cập nhật app, người dùng force-stop, và (một phần) khi app bị gỡ khỏi bộ nhớ ở một số ROM. Nếu không lưu lại, mọi nhắc nhở của người dùng biến mất sau lần khởi động máy đầu tiên — đây là lỗi #1 của app reminder tự viết.

```kotlin
interface DcAlarmStore {
    suspend fun put(alarm: DcAlarm)
    suspend fun remove(id: String)
    suspend fun all(): List<DcAlarm>
}
```

Implementation mặc định: **một file JSON trong `context.filesDir`**, ghi qua `dispatchers.io`. Cố ý không dùng Room hay DataStore — không muốn `:runtime:scheduling` kéo theo `:data:database` hoặc `:data:preferences`. Khoảng 60 dòng. App nào cần truy vấn phức tạp (danh sách nhắc nhở có phân trang, tìm kiếm) thì cài `DcAlarmStore` bằng Room của riêng nó — interface đã có sẵn cho việc đó.

### 4.3 Khôi phục sau khởi động — `DcAlarmRestorer`

Đăng ký một receiver duy nhất, lắng nghe bốn action:

| Action | Vì sao cần |
|---|---|
| `BOOT_COMPLETED` | Alarm bị xóa khi tắt máy (cần `RECEIVE_BOOT_COMPLETED`) |
| `MY_PACKAGE_REPLACED` | Alarm bị xóa khi cập nhật app |
| `TIMEZONE_CHANGED` | "7 giờ sáng" ở múi giờ mới là mốc tuyệt đối khác |
| `TIME_SET` | Người dùng chỉnh đồng hồ thủ công |

Xử lý: đọc `DcAlarmStore`, tính lại mốc kế tiếp cho từng alarm, đặt lại. Alarm đã quá hạn trong lúc máy tắt → chính sách rõ ràng, không đoán: `DcMissedPolicy.FireOnce` (bắn ngay một lần) hoặc `Skip` (bỏ qua, chờ lần kế tiếp). Mặc định `Skip` cho lịch lặp, `FireOnce` cho alarm một lần.

### 4.4 Lặp lại: tính mốc kế tiếp, **không** dùng `setRepeating`

`AlarmManager.setRepeating()` là *inexact* kể từ API 19 — dùng nó cho báo thức là sai ngay từ đầu. Cách đúng: mỗi lần alarm nổ thì tính và đặt lần kế tiếp.

```kotlin
// NextOccurrence.kt
fun nextDaily(after: Instant, time: LocalTime, zone: ZoneId): Instant
fun nextWeekly(after: Instant, time: LocalTime, days: Set<DayOfWeek>, zone: ZoneId): Instant
```

Hai trường hợp bắt buộc phải xử lý và bắt buộc phải có test:

- **Mùa xuân đổi giờ:** 02:30 có thể *không tồn tại* trong ngày đó. Chính sách: dời tới thời điểm hợp lệ gần nhất sau đó.
- **Mùa thu đổi giờ:** 02:30 xuất hiện *hai lần*. Chính sách: lấy lần đầu tiên.

`ZonedDateTime` xử lý sẵn hai ca này nếu dùng đúng; cộng trừ mili-giây thì không.

### 4.5 Quyền exact alarm

```kotlin
object DcExactAlarmPermission {
    fun canScheduleExact(context: Context): Boolean       // canScheduleExactAlarms() từ API 31
    fun settingsIntent(context: Context): Intent          // ACTION_REQUEST_SCHEDULE_EXACT_ALARM
}
```

Tóm tắt tình hình (cần đối chiếu tài liệu Android mới nhất khi triển khai — repo đang `targetSdk = 37`, cao hơn mốc mà tài liệu này khẳng định được):

| Quyền | Từ API | Đặc điểm |
|---|---|---|
| `SCHEDULE_EXACT_ALARM` | 31 | Người dùng có thể thu hồi; từ API 33 không còn tự động cấp khi cài. Phải kiểm tra `canScheduleExactAlarms()` **mỗi lần đặt** |
| `USE_EXACT_ALARM` | 33 | Tự cấp khi cài, **nhưng** chính sách Google Play chỉ cho phép app mà chức năng cốt lõi là báo thức/lịch. Dùng sai → bị từ chối phát hành |
| `RECEIVE_BOOT_COMPLETED` | — | Cần cho khôi phục sau reboot |
| `POST_NOTIFICATIONS` | 33 | Đã có `NotificationPermission` ở `:platform:permission` |
| `USE_FULL_SCREEN_INTENT` | 29 | Từ API 34 bị siết: chỉ app gọi điện/báo thức được cấp sẵn; app khác phải kiểm tra `canUseFullScreenIntent()` và xin qua màn hình cài đặt |

Chọn quyền nào là quyết định sản phẩm: app báo thức thật → `USE_EXACT_ALARM`; app ghi chú có nhắc nhở → `SCHEDULE_EXACT_ALARM` kèm luồng xin quyền. Module hỗ trợ cả hai, **không** tự quyết hộ, và tài liệu ghi rõ rủi ro phát hành.

Khi không có quyền exact: module **không im lặng hạ cấp**. Nó trả về `DcScheduleOutcome.NeedsExactAlarmPermission` để app hỏi người dùng, hoặc app chủ động chọn `DcExactness.Inexact`. Hạ cấp âm thầm nghĩa là báo thức trễ 40 phút mà không ai biết vì sao.

### 4.6 Nhận alarm: receiver chỉ có ~10 giây

`BroadcastReceiver.onReceive` chạy trên main thread và bị giới hạn khoảng 10 giây. Không được đọc database, gọi mạng, hay làm I/O dài trong đó.

```kotlin
abstract class DcAlarmReceiver<P : Any>(
    private val serializer: KSerializer<P>? = null,
) : BroadcastReceiver() {

    /** Phải nhanh: bắn notification, phát âm thanh. Cần việc dài hơn thì trả về HandOff. */
    abstract suspend fun onAlarm(context: Context, payload: P?): DcAlarmOutcome

    final override fun onReceive(context: Context, intent: Intent) { /* goAsync() + timeout + đặt lần kế tiếp */ }
}

sealed interface DcAlarmOutcome {
    data object Handled : DcAlarmOutcome
    /** Chuyển phần việc dài sang WorkManager expedited — hai engine phối hợp ở đúng chỗ này. */
    data class HandOff(val spec: DcJobSpec<*>) : DcAlarmOutcome
}
```

`DcAlarmReceiver` tự làm ba việc mà app hay quên: `goAsync()` đúng cách kèm hạn mức thời gian, đặt lịch cho lần lặp kế tiếp **trước khi** xử lý (để lỗi trong `onAlarm` không làm mất chuỗi lặp), và cập nhật `DcAlarmStore`.

---

## 5. Lớp C — Facade `DcScheduler`, một điểm vào

```kotlin
sealed interface DcTrigger {
    /** → AlarmManager. Mốc tuyệt đối. */
    data class ExactAt(val instant: Instant) : DcTrigger
    /** → AlarmManager, tính lại mỗi lần nổ. Xử lý DST. */
    data class DailyAt(val time: LocalTime, val zone: ZoneId = ZoneId.systemDefault()) : DcTrigger
    data class WeeklyAt(val time: LocalTime, val days: Set<DayOfWeek>, val zone: ZoneId = ZoneId.systemDefault()) : DcTrigger

    /** → WorkManager one-time. Độ trễ tối thiểu, KHÔNG đảm bảo đúng giờ. */
    data class After(val delay: Duration) : DcTrigger
    /** → WorkManager periodic. Ném lỗi nếu interval < 15 phút. */
    data class Every(val interval: Duration, val flex: Duration? = null) : DcTrigger
}

interface DcScheduler {
    suspend fun schedule(id: String, trigger: DcTrigger, request: DcScheduleRequest): DcScheduleOutcome
    suspend fun cancel(id: String)
    suspend fun scheduled(): List<DcScheduledItem>
    fun observe(id: String): Flow<DcJobState>
}

sealed interface DcScheduleOutcome {
    data object Scheduled : DcScheduleOutcome
    data object NeedsExactAlarmPermission : DcScheduleOutcome
    data class Rejected(val reason: String) : DcScheduleOutcome    // vd: Every(5.minutes)
}
```

Bảng định tuyến — cũng là bảng tài liệu cho lập trình viên:

| Trigger | Engine | Đảm bảo | Dùng cho |
|---|---|---|---|
| `ExactAt` | AlarmManager | Đúng giờ (nếu có quyền) | Báo thức, hẹn giờ |
| `DailyAt` / `WeeklyAt` | AlarmManager | Đúng giờ, có DST | Nhắc uống thuốc, thói quen |
| `After` | WorkManager | Ít nhất sau X, có thể trễ | Thử gửi lại, dọn cache |
| `Every` | WorkManager | Chu kỳ ≥15 phút, có thể trễ | Đồng bộ nền |

---

## 6. Kịch bản: app reminder viết những gì?

Toàn bộ code phía app cho một nhắc nhở lặp hằng ngày:

```kotlin
// 1. Payload có kiểu
@Serializable
data class ReminderPayload(val reminderId: Long, val title: String)

// 2. Một receiver cho toàn app
@AndroidEntryPoint
class ReminderReceiver : DcAlarmReceiver<ReminderPayload>(ReminderPayload.serializer()) {
    @Inject lateinit var notifier: ReminderNotifier

    override suspend fun onAlarm(context: Context, payload: ReminderPayload?): DcAlarmOutcome {
        payload?.let { notifier.show(it.reminderId, it.title) }
        return DcAlarmOutcome.Handled
    }
}

// 3. Đặt nhắc nhở — trong ViewModel
scheduler.schedule(
    id = "reminder-${reminder.id}",
    trigger = DcTrigger.DailyAt(LocalTime.of(7, 0)),
    request = DcScheduleRequest.alarm(
        receiver = ReminderReceiver::class,
        payload = ReminderPayload(reminder.id, reminder.title),
        serializer = ReminderPayload.serializer(),
        exactness = DcExactness.Exact,
    ),
).let { outcome ->
    if (outcome is DcScheduleOutcome.NeedsExactAlarmPermission) {
        // UiState mang cờ này; Route mở màn hình cài đặt
    }
}

// 4. Huỷ
scheduler.cancel("reminder-${reminder.id}")
```

Module lo phần còn lại: lưu bền, khôi phục sau reboot/cập nhật/đổi múi giờ, tính mốc kế tiếp có DST, `goAsync()` trong receiver, `PendingIntent` với flag đúng, chống trùng, và kiểm tra quyền.

Ghép với các module đã có: `AppError` (`:foundation`) cho `DcJobState.Failed`; `DispatcherProvider` (`:runtime:concurrency`) cho I/O của store; `NotificationPermission` (`:platform:permission`) cho `POST_NOTIFICATIONS`; `Logger`/`CrashReporter` (`:observability`) do `:app` nối vào — module giữ nguyên nguyên tắc không phụ thuộc DI/observability.

---

## 7. Bẫy được khóa sẵn

| Bẫy | Hậu quả nếu tự viết | Module xử lý |
|---|---|---|
| Alarm mất sau reboot | Nhắc nhở im lặng biến mất | `DcAlarmRestorer` + `DcAlarmStore` |
| `setRepeating` inexact | Báo thức lệch hàng chục phút | Tính mốc kế tiếp mỗi lần nổ |
| Cộng 24h mili-giây cho lịch hằng ngày | Lệch 1 giờ hai lần mỗi năm | `ZonedDateTime` + test DST |
| Không kiểm tra `canScheduleExactAlarms()` | Ném `SecurityException` khi người dùng thu hồi quyền | Kiểm tra mỗi lần đặt, trả outcome rõ ràng |
| `PendingIntent` thiếu `FLAG_IMMUTABLE` | Crash trên API 31+ | Đặt flag ở một chỗ duy nhất |
| `PendingIntent` trùng requestCode | Alarm này ghi đè alarm kia | Sinh mã từ `id`, có test |
| I/O dài trong `onReceive` | ANR | `goAsync()` + hạn mức + `HandOff` |
| `Result.retry()` không trần | Job hỏng thử lại vĩnh viễn | `DcRetryPolicy.maxAttempts` |
| Chu kỳ < 15 phút bị clamp âm thầm | Lập trình viên tin là 5 phút | Ném lỗi khi khai báo |
| ROM tự tắt app nền (một số hãng) | Không sửa được bằng code | Cung cấp `isIgnoringBatteryOptimizations()` để app hướng dẫn người dùng, và ghi rõ giới hạn |

Dòng cuối là giới hạn thật: một số ROM sẽ giết app bất kể code đúng thế nào. Module phát hiện và để app giải thích cho người dùng, chứ không giả vờ đã giải quyết.

---

## 8. Manifest mà app cần khai báo

```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Chọn MỘT trong hai, theo loại app (xem §4.5) -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<!-- <uses-permission android:name="android.permission.USE_EXACT_ALARM" /> -->

<receiver android:name=".reminder.ReminderReceiver" android:exported="false" />

<receiver android:name="com.dc.android.foundation.runtime.scheduling.alarm.DcAlarmRestorer"
    android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
        <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
        <action android:name="android.intent.action.TIMEZONE_CHANGED" />
        <action android:name="android.intent.action.TIME_SET" />
    </intent-filter>
</receiver>
```

`DcAlarmRestorer` được khai báo trong manifest **của module** (manifest merger tự gộp), để app không phải nhớ. Các quyền thì để app tự khai báo — một thư viện tự thêm quyền vào app là hành vi xấu, và việc chọn `SCHEDULE_EXACT_ALARM` hay `USE_EXACT_ALARM` là quyết định của app.

---

## 9. Test

Phần lập lịch là chỗ dễ tin nhầm là đúng nhất, nên test nhắm vào logic thuần trước:

**Unit test (JVM, không cần thiết bị) — bắt buộc:**
1. `nextDaily` / `nextWeekly` — trường hợp thường, qua nửa đêm, qua tuần.
2. DST mùa xuân — giờ không tồn tại.
3. DST mùa thu — giờ lặp hai lần.
4. Đổi múi giờ — mốc tuyệt đối thay đổi đúng.
5. `DcRetryPolicy` — chuyển `Retry` thành `Failure` khi hết lượt.
6. `Every(interval < 15.minutes)` — bị từ chối, không bị clamp.
7. `DcAlarmStore` file JSON — ghi/đọc/xoá, file hỏng thì không làm sập app.
8. Sinh requestCode cho `PendingIntent` — không trùng giữa các `id` khác nhau.

**Instrumented (cần thiết bị/emulator):**
9. `WorkManagerTestInitHelper` + `TestDriver` cho một `DcWorker` thật.
10. `DcAlarmRestorer` khôi phục đúng sau khi giả lập `BOOT_COMPLETED`.

Mục 1–8 chạy được ngay trên CI hiện tại. Ghi rõ 9–10 là hạng mục cần thiết bị, giống cách `14-background-work.md` đang trung thực về khoảng trống test của `ArticlesSyncWorker`.

Thêm `FakeScheduler` vào `:testing` (ghi lại mọi lệnh đặt/huỷ) để ViewModel của feature reminder test được mà không đụng framework.

---

## 10. Thứ tự triển khai

| Bước | Nội dung | Ghi chú |
|---|---|---|
| 1 | Bật core library desugaring trong hai convention plugin | Chặn mọi thứ liên quan ngày giờ |
| 2 | Đổi tên module `:runtime:background` → `:runtime:scheduling`, cập nhật `settings.gradle.kts` và `DcApplication` | Rẻ nhất lúc này (1 consumer) |
| 3 | `NextOccurrence` + 4 test DST/múi giờ | Logic thuần, không phụ thuộc Android — làm trước để chắc phần khó nhất |
| 4 | Lớp A: `DcJobSpec`, `DcConstraints`, `DcRetryPolicy`, `DcWorkScheduler`, `DcWorker` | Thay `PeriodicSyncScheduler`; chuyển `ArticlesSyncWorker` sang `DcWorker` để có consumer thật |
| 5 | Lớp B: `DcAlarm`, `DcAlarmStore`, `DcAlarmScheduler`, `DcExactAlarmPermission` | |
| 6 | `DcAlarmReceiver` + `DcAlarmRestorer` + manifest của module | Phần dễ sai nhất, làm sau khi store đã ổn |
| 7 | Facade `DcScheduler` + `DcTrigger` | Ghép hai lớp lại |
| 8 | `FakeScheduler` trong `:testing`, cập nhật `architecture/14-background-work.md`, `graphify update .` | |

**Ước lượng:** ~900 dòng sản phẩm + ~450 dòng test.

Sau bước 7, app reminder có thể bắt đầu — nó chỉ cần viết payload, receiver, và màn hình.

---

## 11. Cố ý không làm

| Hạng mục | Lý do |
|---|---|
| Foreground service cho báo thức đang kêu | Thuộc app cụ thể (giao diện tắt/báo lại), không phải hạ tầng lập lịch |
| Quản lý notification channel tổng quát | Thuộc `:platform:notification` (module riêng, chưa tạo); alarm chỉ cần `HandOff` hoặc app tự bắn |
| Chuỗi công việc (`beginWith().then()`) | Chưa có use case; WorkManager API gốc đã đủ dùng khi cần |
| Đồng bộ alarm với server/lịch | Thuộc nghiệp vụ |
| Tự lách cơ chế tiết kiệm pin của ROM | Không làm được bằng code sạch, và làm sẽ khiến app bị đánh giá xấu |
| `AlarmManager.setRepeating` | Inexact từ API 19 — sai công cụ ngay từ đầu |

---

## 12. Definition of done

- `DcScheduler.schedule` là điểm vào duy nhất; app không import `WorkManager` hay `AlarmManager` trực tiếp.
- Module không import Hilt (giữ INV-03); `@HiltWorker` vẫn ở `:app`.
- Alarm sống sót qua reboot, cập nhật app, và đổi múi giờ — có test cho ít nhất reboot.
- Lịch hằng ngày/hằng tuần đúng qua cả hai mốc DST, có test.
- Không có đường hạ cấp âm thầm: thiếu quyền exact → outcome rõ ràng; chu kỳ quá ngắn → lỗi rõ ràng.
- `ArticlesSyncWorker` đã chuyển sang `DcWorker` (chứng minh lớp A dùng được thật), và nhánh `catch (Throwable)` không còn retry vô hạn.
- `FakeScheduler` có trong `:testing` cùng lúc với contract.
- Cập nhật `architecture/14-background-work.md` (BG-01 mở rộng sang alarm) và `STRUCTURE.md` của module.
- Chạy `graphify update .` — đổi tên module là thay đổi topology thật.
