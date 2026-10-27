# Triển khai `:ui:designsystem`

Ngôn ngữ thị giác Compose. Chiều phụ thuộc: **Tokens → Theme → Components → Feature UI**.

## Khi nào dùng
Mọi UI feature. Dùng API ngữ nghĩa của DC thay cho màu/kích thước/typography/shape thô của Material.

## Thêm vào dự án
```kotlin
plugins { id("dc.android.library"); id("dc.compose"); id("dc.testing") }   // module này
dependencies { implementation(project(":ui:designsystem")) }                // module dùng
```

## Nối dây (một lần, ở `MainActivity`)
```kotlin
DcTheme(
    darkTheme = …, dynamicColor = false,
    uiScale = configuration.uiScale,   // áp dụng DUY NHẤT ở đây
) { /* NavHost + DcNavigationSuite */ }
```
Tuỳ biến bằng tham số `colorScheme`, `typography`, `shapes`, `extendedColors`, `spacing`; không sửa token trong feature.

## Cách dùng
```kotlin
DcScaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
    DcContentContainer(Modifier.padding(padding)) {
        DcText("Hello"); DcButton(onClick = onRefresh) { DcText("Refresh") }
    }
}
DcSnackbarEffect(message = state.userMessage, snackbarHostState = host, onMessageShown = { … })
```
Nhóm component (`components/`): action/button, text/link, input/form/select/dropdown, list/card/row, sheet/dialog/overlay/menu, state (loading/empty/error/offline), feedback/message, layout, navigation (`DcNavigationSuite`, drawer), `DcPullToRefreshBox`, `DcAsyncImage`.
Danh mục sống: `catalog/DesignSystemCatalog.kt`, `ExtendedCatalog.kt` (hiện trong điều hướng của `:app` khi build debug); preview dùng `preview/DcPreviews.kt`.

## Thêm component
1. Đặt trong file `Dc<Nhóm>Components.kt` phù hợp, API nhận state + callback, lấy giá trị từ theme/token.
2. Thêm `@Preview` bằng annotation DC và đưa vào catalog.
3. Thêm test policy (`src/test`) hoặc Compose test (`src/androidTest`); cập nhật `architecture/07-design-system/components.md`.

## Test
`./gradlew :ui:designsystem:testDebugUnitTest :ui:designsystem:assembleDebug` (Compose test cần thiết bị/emulator: `connectedDebugAndroidTest`).

## Lỗi hay gặp
- Import Coil ngoài `DcAsyncImage.kt`, hoặc để lộ kiểu Material adaptive ra API.
- Áp `DcUiScale` ở từng màn hình thay vì một lần ở `DcTheme`.
- Import feature/data vào design system.

Tài liệu: `architecture/07-design-system/` (đọc file con liên quan trước khi đổi API công khai).
