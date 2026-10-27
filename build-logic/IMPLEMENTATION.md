# Triển khai `build-logic`

Included build chứa convention plugin `dc.*`. Module áp plugin thay vì lặp cấu hình.

## Chọn plugin cho module mới
| Loại module | Plugin |
|---|---|
| Kotlin/JVM thuần | `dc.kotlin.jvm` (+ `dc.testing`) |
| Thư viện Android | `dc.android.library` (+ `dc.testing`) |
| UI Compose | thêm `dc.compose` |
| Dùng Room | thêm `dc.room` (xuất schema vào `schemas/`) |
| Dùng Hilt | thêm `dc.hilt` (chỉ `:app`) |
| Ứng dụng | `dc.android.application` (+ `dc.feature.generator`) |

## Tạo module Gradle mới
1. Chỉ tạo khi có ranh giới thật về phụ thuộc/sở hữu/tái sử dụng/test (STR-01).
2. Tạo `<đường dẫn>/build.gradle.kts` với plugin ở trên, đặt `namespace` dưới `com.dc.android.foundation`.
3. Thêm `include(":đường:dẫn")` vào `settings.gradle.kts`.
4. Thêm `STRUCTURE.md` + `IMPLEMENTATION.md`, cập nhật `architecture/03-project-structure.md` và `STRUCTURE.md` gốc.

## Tạo feature
```bash
./gradlew :app:createFeature -PfeatureName=Profile
./gradlew :app:createFeature -PfeatureName=Orders -Pnavigation=true -Pnetwork=true \
  -Proom=true -Ppermission=true -PbackgroundWork=true      # cờ tuỳ chọn, mặc định false
# -PfeaturePackage=com.example.app để đổi package suy ra từ namespace
```
Generator kiểm tra định danh Kotlin, lập kế hoạch toàn bộ file trước khi ghi, thất bại nếu file đích đã tồn tại.

## Sửa convention
- Phiên bản lấy từ `gradle/libs.versions.toml`, không hard-code trong plugin.
- Tiện ích dùng chung: `ConventionHelpers.kt`; đăng ký plugin ID trong `build-logic/build.gradle.kts`.
- Kiểm tra: `./gradlew -p build-logic test` rồi `./gradlew assembleDebug`.

Tài liệu: `architecture/18-build-logic-and-dependencies.md`, `architecture/20-feature-template.md`.
