# Triển khai `:platform:permission`

Trạng thái quyền do DC sở hữu (`PermissionState`) và adapter cho `POST_NOTIFICATIONS`.

## Khi nào dùng
Feature cần biết/xin quyền. Chỉ thêm capability khi manifest khai báo quyền thật.

## Thêm vào dự án
```kotlin
dependencies { implementation(project(":platform:permission")) }
```
`:app` khai báo `<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>`.

## Cách dùng
```kotlin
// ViewModel/state chỉ giữ PermissionState
val initial = NotificationPermission.currentState(context)   // API < 33 → Granted

// Route (framework glue) mới chạm launcher
val launcher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
    viewModel.onAction(PermissionResult(NotificationPermission.fromRequestResult(granted)))
}
launcher.launch(NotificationPermission.permissionName)
```
`PermissionState`: `NotRequested`, `Granted`, `Denied`. Generator `-Ppermission=true` tạo thêm `UiEffect` cho luồng xin quyền.

## Test
Test ViewModel với `PermissionState` thuần, không cần launcher. Kiểm tra: `./gradlew :platform:permission:compileDebugKotlin` + test của feature.

## Lỗi hay gặp
- Gọi `ActivityCompat`/launcher từ ViewModel hoặc Composable lá.
- Quên rằng "Denied" gộp cả "chưa hỏi" lẫn "từ chối" khi chỉ đọc `currentState` (dùng `NotRequested` ở state feature để phân biệt).

Tài liệu: `architecture/13-permissions.md`.
