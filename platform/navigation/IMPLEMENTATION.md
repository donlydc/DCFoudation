# Triển khai `:platform:navigation`

Hợp đồng điểm đến không phụ thuộc engine: `Destination` (`@Serializable` sealed interface). `NavController`/`NavHost` thật nằm ở `:app`.

## Khi nào dùng
Khai báo màn hình cấp cao nhất để feature và app dùng chung kiểu route.

## Thêm vào dự án
```kotlin
dependencies { implementation(project(":platform:navigation")) }
```
Cần `kotlinx.serialization` (đã có trong module); **không** thêm `androidx.navigation` vào đây.

## Cách dùng
1. Thêm điểm đến: `@Serializable data object Profile : Destination` (hoặc `data class` có tham số).
2. Chỉ thêm khi đã có màn hình thật; đăng ký ở composition root:
```kotlin
NavHost(navController, startDestination = Destination.Greeting) {
    composable<Destination.Greeting> { GreetingRoute() }
    composable<Destination.Profile>  { ProfileRoute() }
}
```
3. Thanh điều hướng dùng `DcNavigationSuite` từ design system (xem `MainActivity`).

## Test
Không có test riêng. Kiểm tra: `./gradlew :platform:navigation:compileDebugKotlin :app:assembleDebug`.

## Lỗi hay gặp
- Feature import `androidx.navigation.*`.
- Thêm wrapper `Navigator` khi chưa có hành động điều hướng nào cần (xem ghi chú trong `Destination.kt`).

Tài liệu: `architecture/06-navigation.md`.
