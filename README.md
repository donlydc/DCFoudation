# DC Android Foundation

Nền tảng Android tái sử dụng (Kotlin, Jetpack Compose + Material 3) cho các dự án DC trong tương lai — không phải một ứng dụng cụ thể, mà là kiến trúc, design system, và các quy ước hạ tầng dùng chung.

- Quy ước kiến trúc/kỹ thuật chi tiết: xem [`CLAUDE.md`](CLAUDE.md)
- Kiến trúc: [`architecture/00-index.md`](architecture/00-index.md); trạng thái bàn giao: [`HANDOVER.md`](HANDOVER.md)
- Lộ trình triển khai theo phase: xem [`plan/`](plan/)
- Thay đổi: [`CHANGELOG.md`](CHANGELOG.md)

> Trạng thái xác minh: build Gradle/test **NOT_RUN** trong các phiên gần đây (xem `HANDOVER.md`). Các lệnh dưới đây là quy trình thiết kế, chưa được chứng nhận bằng kết quả chạy.

## Quickstart

### Prerequisites (đúng như cấu hình trong repo)

| Thành phần | Giá trị | Nguồn |
|---|---|---|
| JDK chạy Gradle daemon | 25 (tự tải qua foojay toolchain resolver) | `gradle/gradle-daemon-jvm.properties` (`toolchainVersion=25`) |
| Kotlin/Java toolchain biên dịch module | 11 | `DC_JAVA_VERSION` trong `build-logic/.../ConventionHelpers.kt` |
| `compileSdk` / `targetSdk` | 37 | `ConventionHelpers.kt` (`DC_COMPILE_SDK`, `DC_TARGET_SDK`) |
| `minSdk` | 24 | `ConventionHelpers.kt` (`DC_MIN_SDK`) |
| AGP / Kotlin / Hilt | 9.3.1 / 2.2.10 / 2.60.1 | `gradle/libs.versions.toml` |

Cần Android SDK (platform 37) và đường dẫn `sdk.dir` trong `local.properties` (không commit). Python 3 (+ `pyyaml`) cho kiểm tra kiến trúc.

### Sử dụng convention plugins

`build-logic` được include qua `pluginManagement { includeBuild("build-logic") }` trong `settings.gradle.kts`. Trong `build.gradle.kts` của module:

```kotlin
plugins {
    id("dc.android.library")   // hoặc dc.android.application | dc.kotlin.jvm
    id("dc.compose")           // tùy chọn: Compose
    id("dc.hilt")              // tùy chọn: Hilt + KSP
    id("dc.room")              // tùy chọn: Room
    id("dc.testing")           // test dependencies dùng chung
}
android { namespace = "com.dc.android.foundation.<module>" }
```

Dùng `dc.kotlin.jvm` cho module JVM thuần (task test là `test`). Mọi cạnh `project(...)` phải nằm trong `architecture/dependency-rules.md`. Chưa có artifact publish; project ngoài repo dùng composite build — xem `reports/wp/C3-design.md` (D-05, PROPOSED).

### Tạo feature mới

```bash
./gradlew :app:createFeature -PfeatureName=Profile
# tùy chọn: -Pnavigation=true -Pnetwork=true -Proom=true -Ppermission=true -PbackgroundWork=true -PfeaturePackage=com.example.app
```

Sinh UiState/UiAction/ViewModel/Route/Screen/Preview, `ProfileViewModelTest` và trang kiến trúc `architecture/features/profile.md` (Status DRAFT, Test status NOT_RUN). Task không ghi đè tệp có sẵn. Sau đó thêm feature vào `architecture/context-map.yaml` và `architecture/module-map.md` để AI/checker tìm thấy. Chi tiết: `architecture/20-feature-template.md`.

### Chạy kiểm tra

```bash
python3 tools/check_architecture.py --boundaries-only    # gate cạnh module (KD-02)
python3 tools/check_architecture.py                       # drift docs/map (đủ: --md/--json)
python3 -m unittest discover -s tools -p 'test_*.py'      # self-test của checker
./gradlew testDebugUnitTest                               # unit test module Android + :app
./gradlew :foundation:test :runtime:concurrency:test      # module dc.kotlin.jvm
./gradlew -p build-logic test                             # test của build-logic
./gradlew lintDebug
./gradlew assembleDebug
```

Bộ lệnh đầy đủ và CI: `architecture/19-ci-release-quality-gates.md`.
