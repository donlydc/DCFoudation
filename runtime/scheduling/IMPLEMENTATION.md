# Triển khai `:runtime:scheduling`

Facade `DcScheduler`: việc bền/retry → WorkManager; mốc giờ chính xác → AlarmManager. Không có Hilt.

## Khi nào dùng
- Đồng bộ định kỳ, upload, việc nền có ràng buộc → `DcTrigger.After` / `Every` + `DcScheduleRequest.work`.
- Nhắc giờ cố định/hằng ngày/hằng tuần → `DcTrigger.ExactAt` / `DailyAt` / `WeeklyAt` + `DcScheduleRequest.alarm`.
- Không gọi `WorkManager`/`AlarmManager` trực tiếp trong app/feature.

## Thêm vào dự án
```kotlin
dependencies {
    implementation(project(":runtime:scheduling"))
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work); ksp(libs.androidx.hilt.compiler) // nếu worker dùng Hilt
}
```
Manifest của module tự gộp `DcAlarmRestorer` (khôi phục báo thức sau reboot/đổi giờ). App tự khai báo quyền `SCHEDULE_EXACT_ALARM`/`RECEIVE_BOOT_COMPLETED` khi cần.

## Nối dây (ở `:app`)
1. Tắt khởi tạo mặc định WorkManager trong manifest (`tools:node="remove"` cho `androidx.work.WorkManagerInitializer`) và để `DcApplication` implement `Configuration.Provider` với `HiltWorkerFactory`.
2. Tạo `AndroidDcScheduler(context, dispatchers)` ở composition root, chỉ phơi `DcScheduler` ra ngoài.

## Cách dùng — việc nền
```kotlin
@HiltWorker
class ArticlesSyncWorker @AssistedInject constructor(
    @Assisted context: Context, @Assisted params: WorkerParameters, private val repo: ArticlesRepository,
) : DcWorker<Unit>(context, params) {
    override suspend fun run(input: Unit?): DcJobResult = /* Success | Retry | Failure(AppError) */
}

val spec = DcJobSpec<Unit>(name = "articles-sync", worker = ArticlesSyncWorker::class,
    constraints = DcConstraints.Network, retry = DcRetryPolicy.Default)
scheduler.schedule("articles-sync", DcTrigger.Every(15.minutes), DcScheduleRequest.work(spec))
```
- `DcRetryPolicy`: tối thiểu `initialBackoff` 10s; `NoRetry` = 1 lần. `DcConstraints`: `None`, `Network`, `Sync` hoặc tự đặt.
- Input có kiểu cần `inputSerializer` (kotlinx.serialization).

## Cách dùng — báo thức
```kotlin
class ReminderReceiver : DcAlarmReceiver<ReminderPayload>(ReminderPayload.serializer()) {
    override suspend fun onAlarm(context: Context, payload: ReminderPayload?) = DcAlarmOutcome.Handled
    // hoặc DcAlarmOutcome.HandOff(spec) để chuyển việc nặng sang WorkManager
}
val outcome = scheduler.schedule("reminder-1", DcTrigger.DailyAt(LocalTime.of(8, 0)),
    DcScheduleRequest.alarm(ReminderReceiver::class, payload, ReminderPayload.serializer()))
```
`DcScheduleOutcome`: `Scheduled`, `NeedsExactAlarmPermission` (mở `DcExactAlarmPermission.settingsIntent(context)`), `Rejected`. Lệch loại trigger/request → `Rejected`. Khai báo receiver trong manifest `:app`. `cancel(id)`, `scheduled()`, `observe(id)` để quản lý/theo dõi.

## Test
Dùng `FakeScheduler` (`:testing`) kiểm `scheduleCommands`/`cancelledIds`. Chạy: `./gradlew :runtime:scheduling:testDebugUnitTest`, sau đó `:app:assembleDebug`.

## Lỗi hay gặp
- Dùng WorkManager làm báo thức chính xác, hoặc tự hạ cấp báo thức âm thầm.
- Làm việc nghiệp vụ nặng trong receiver (hãy `HandOff`).
- `@HiltWorker`/receiver cụ thể đặt vào module này (phải ở `:app`).

Tài liệu: `architecture/14-background-work.md`.
