# Phase 1 — Token và Theme

**Mục tiêu:** mọi giá trị thị giác của style guide tồn tại đúng một chỗ, và `DcTheme` phát chúng ra toàn app.
**Không đụng tới:** component, màn hình, business logic.
**Kết quả kiểm chứng được:** chạy `DesignSystemCatalog` ở light + dark thấy đổi tông sang xanh/nền xám nhạt, dù chưa sửa component nào.

---

## T1.1 — Thay `ColorTokens` bằng palette style guide

**File:** `ui/designsystem/src/main/java/com/dc/android/foundation/designsystem/tokens/ColorTokens.kt` (viết lại toàn bộ)

Xóa `Purple40/80`, `PurpleGrey40/80`, `Pink40/80`. Token nguyên thủy, **không** dùng trực tiếp trong feature UI (DS-01).

```kotlin
package com.dc.android.foundation.designsystem.tokens

import androidx.compose.ui.graphics.Color

/**
 * Primitive palette backing the DC default theme. Feature UI must never reference these
 * directly — read semantic colors from MaterialTheme/DcTheme instead (DS-01).
 */
object ColorTokens {
    // Brand
    val Blue600 = Color(0xFF2563EB)   // primary
    val Blue700 = Color(0xFF1D4ED8)   // primary pressed / dark
    val Blue800 = Color(0xFF1E40AF)
    val Blue900 = Color(0xFF1E3A8A)   // on primary container
    val Blue100 = Color(0xFFDBEAFE)   // primary container
    val Blue300 = Color(0xFF93C5FD)   // dark-theme primary

    // Neutral
    val White = Color(0xFFFFFFFF)
    val Grey50 = Color(0xFFF7F8FA)    // background (light)
    val Grey100 = Color(0xFFF1F3F5)   // surface variant
    val Grey200 = Color(0xFFE5E7EB)   // border
    val Grey300 = Color(0xFFD1D5DB)   // outline
    val Grey400 = Color(0xFF9CA3AF)   // text disabled
    val Grey500 = Color(0xFF6B7280)   // text secondary
    val Grey900 = Color(0xFF111827)   // text primary

    // Neutral (dark theme)
    val Ink900 = Color(0xFF0F1115)    // background
    val Ink800 = Color(0xFF14171D)    // surface
    val Ink700 = Color(0xFF1F242C)    // surface variant
    val Ink600 = Color(0xFF2A303A)    // outline variant
    val Ink500 = Color(0xFF3A414B)    // outline

    // Status
    val Green600 = Color(0xFF16A34A)
    val Green100 = Color(0xFFDCFCE7)
    val Green900 = Color(0xFF14532D)
    val Green300 = Color(0xFF86EFAC)

    val Amber500 = Color(0xFFF59E0B)
    val Amber100 = Color(0xFFFEF3C7)
    val Amber900 = Color(0xFF78350F)
    val Amber300 = Color(0xFFFCD34D)

    val Red600 = Color(0xFFDC2626)
    val Red100 = Color(0xFFFEE2E2)
    val Red900 = Color(0xFF7F1D1D)
    val Red300 = Color(0xFFFCA5A5)
}
```

> Kiểm tra sau khi sửa: `rg "Purple|Pink" ui/ app/` phải trống. Nếu còn, sửa nốt trong cùng commit.

## T1.2 — Khai báo ColorScheme đầy đủ cho light và dark

**File:** `theme/DcTheme.kt`

Thay hai `private val` 3-màu hiện tại. Toàn bộ vai trò dùng trong component phải được chỉ định rõ, không để rơi về default M3.

```kotlin
private val LightColorScheme = lightColorScheme(
    primary = ColorTokens.Blue600,
    onPrimary = ColorTokens.White,
    primaryContainer = ColorTokens.Blue100,
    onPrimaryContainer = ColorTokens.Blue900,

    secondary = ColorTokens.Grey500,
    onSecondary = ColorTokens.White,
    secondaryContainer = ColorTokens.Grey100,
    onSecondaryContainer = ColorTokens.Grey900,

    tertiary = ColorTokens.Blue700,
    onTertiary = ColorTokens.White,
    tertiaryContainer = ColorTokens.Blue100,
    onTertiaryContainer = ColorTokens.Blue900,

    background = ColorTokens.Grey50,
    onBackground = ColorTokens.Grey900,
    surface = ColorTokens.White,
    onSurface = ColorTokens.Grey900,
    surfaceVariant = ColorTokens.Grey100,
    onSurfaceVariant = ColorTokens.Grey500,

    surfaceContainerLowest = ColorTokens.White,
    surfaceContainerLow = ColorTokens.White,
    surfaceContainer = ColorTokens.Grey50,
    surfaceContainerHigh = ColorTokens.Grey100,
    surfaceContainerHighest = ColorTokens.Grey100,

    outline = ColorTokens.Grey300,
    outlineVariant = ColorTokens.Grey200,

    error = ColorTokens.Red600,
    onError = ColorTokens.White,
    errorContainer = ColorTokens.Red100,
    onErrorContainer = ColorTokens.Red900,

    inverseSurface = ColorTokens.Grey900,
    inverseOnSurface = ColorTokens.Grey50,
    inversePrimary = ColorTokens.Blue300,
    scrim = Color(0xFF000000)
)

private val DarkColorScheme = darkColorScheme(
    primary = ColorTokens.Blue300,
    onPrimary = ColorTokens.Blue900,
    primaryContainer = ColorTokens.Blue800,
    onPrimaryContainer = ColorTokens.Blue100,

    secondary = ColorTokens.Grey400,
    onSecondary = ColorTokens.Ink900,
    secondaryContainer = ColorTokens.Ink600,
    onSecondaryContainer = ColorTokens.Grey200,

    tertiary = ColorTokens.Blue300,
    onTertiary = ColorTokens.Blue900,
    tertiaryContainer = ColorTokens.Blue800,
    onTertiaryContainer = ColorTokens.Blue100,

    background = ColorTokens.Ink900,
    onBackground = ColorTokens.Grey200,
    surface = ColorTokens.Ink800,
    onSurface = ColorTokens.Grey200,
    surfaceVariant = ColorTokens.Ink700,
    onSurfaceVariant = ColorTokens.Grey400,

    surfaceContainerLowest = ColorTokens.Ink900,
    surfaceContainerLow = ColorTokens.Ink800,
    surfaceContainer = ColorTokens.Ink800,
    surfaceContainerHigh = ColorTokens.Ink700,
    surfaceContainerHighest = ColorTokens.Ink600,

    outline = ColorTokens.Ink500,
    outlineVariant = ColorTokens.Ink600,

    error = ColorTokens.Red300,
    onError = ColorTokens.Red900,
    errorContainer = ColorTokens.Red900,
    onErrorContainer = ColorTokens.Red100,

    inverseSurface = ColorTokens.Grey200,
    inverseOnSurface = ColorTokens.Ink900,
    inversePrimary = ColorTokens.Blue600,
    scrim = Color(0xFF000000)
)
```

Giữ nguyên chữ ký `DcTheme(...)` và `DcThemeDefaults.colorScheme(darkTheme, dynamicColor)`. Chỉ đổi phần thân.

**Dọn thêm (bắt buộc):** `DcTheme.kt` hiện dùng tên đầy đủ `androidx.compose.material3.ColorScheme`, `androidx.compose.ui.graphics.Color(...)` trong thân hàm. Chuyển sang import để đọc được — đây là file nhiều người sẽ mở nhất trong module.

## T1.3 — Mở rộng `DcColors` cho status container

**File:** `theme/DcColors.kt`

Chip trạng thái cần nền tint nhạt (style guide §15: "Use tinted light backgrounds"). Material `ColorScheme` không có success/warning → nằm ở `DcColors`. Thêm cặp container, giữ field cũ.

```kotlin
data class DcColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
    val offline: Color,
    val onOffline: Color
)
```

`DcThemeDefaults.extendedColors(darkTheme)`:

| Vai trò | Light | Dark |
|---|---|---|
| success / onSuccess | `Green600` / `White` | `Green300` / `Green900` |
| successContainer / on… | `Green100` / `Green900` | `Green900` / `Green100` |
| warning / onWarning | `Amber500` / `Grey900` | `Amber300` / `Amber900` |
| warningContainer / on… | `Amber100` / `Amber900` | `Amber900` / `Amber100` |
| info / onInfo | `Blue600` / `White` | `Blue300` / `Blue900` |
| infoContainer / on… | `Blue100` / `Blue900` | `Blue800` / `Blue100` |
| offline / onOffline | `Red600` / `White` | `Red300` / `Red900` |

> `onWarning = Grey900` chứ không phải trắng: chữ trắng trên `#F59E0B` không đạt tương phản 4.5:1.

**Breaking:** mọi nơi khởi tạo `DcColors(...)` phải cập nhật. Tìm bằng `rg "DcColors\("`.

## T1.4 — Type scale đầy đủ

**File:** `theme/DcTypography.kt` (viết lại)

```kotlin
private val Sans = FontFamily.Default

val DcTypography = Typography(
    displaySmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),

    titleLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),

    bodyLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),

    labelLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp)
)
```

Quy ước sử dụng (ghi vào KDoc của file, và vào `architecture/07-design-system/typography.md` ở bước tài liệu):

| Style | Dùng cho |
|---|---|
| `titleLarge` | tiêu đề màn hình / top app bar / tiêu đề dialog |
| `titleMedium` | section header, tiêu đề card |
| `bodyLarge` | nội dung chính, headline của list row |
| `bodyMedium` | text phụ, supporting text |
| `labelLarge` | nhãn nút |
| `bodySmall` / `labelMedium` | caption, helper text, nhãn bottom nav |

Bỏ `letterSpacing = 0.5.sp` của `bodyLarge` cũ — giãn chữ mặc định của M3 làm text 16sp trông rời rạc, trái "easy to read".

**Không** khai báo các style không dùng (`displayLarge`, `headlineLarge`…) chỉ để cho đủ; để chúng rơi về M3 default và ghi chú lý do.

## T1.5 — Shape scale 4/8/12/16/20

**File:** `tokens/ShapeTokens.kt`

```kotlin
object ShapeTokens {
    val None: CornerBasedShape = RoundedCornerShape(0.dp)
    val ExtraSmall: CornerBasedShape = RoundedCornerShape(4.dp)
    val Small: CornerBasedShape = RoundedCornerShape(8.dp)      // chip, control nhỏ
    val Medium: CornerBasedShape = RoundedCornerShape(12.dp)    // button, text field, icon container
    val Large: CornerBasedShape = RoundedCornerShape(16.dp)     // card
    val ExtraLarge: CornerBasedShape = RoundedCornerShape(20.dp) // dialog, bottom sheet
    val Full: CornerBasedShape = RoundedCornerShape(50)
}
```

**File:** `theme/DcShapes.kt`

```kotlin
val DcShapes = Shapes(
    extraSmall = ShapeTokens.ExtraSmall,
    small = ShapeTokens.Small,
    medium = ShapeTokens.Medium,
    large = ShapeTokens.Large,
    extraLarge = ShapeTokens.ExtraLarge
)
```

**Audit bắt buộc trong cùng commit** — `rg "MaterialTheme.shapes|ShapeTokens\." ui/ app/`. Với mỗi hit, quyết định theo bảng ánh xạ style guide §4:

| Thành phần | Shape đúng |
|---|---|
| Chip / status tag | `shapes.small` (8) |
| Button, TextField, icon container | `shapes.medium` (12) |
| Card, list item, banner | `shapes.large` (16) |
| Dialog, bottom sheet | `shapes.extraLarge` (20) |

Đã biết: `DcCard` đang dùng `shapes.medium` → phải đổi sang `shapes.large` ở Phase 2. `DcOfflineBanner` đang dùng `shapes.medium` → đổi sang `shapes.large`.

## T1.6 — Thêm bước 12dp vào spacing

**File:** `tokens/DcSpacing.kt` và `tokens/SpacingTokens.kt`

Không đổi tên field hiện có (`medium = 16.dp` đang được dùng làm padding màn hình, đúng style guide). Chỉ thêm bước 12dp, **đặt cuối danh sách tham số** (D-08):

```kotlin
data class DcSpacing(
    val none: Dp = 0.dp,
    val xSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val xLarge: Dp = 32.dp,
    val xxLarge: Dp = 48.dp,
    /** 12dp step — khoảng cách giữa các control liền kề trong cùng một khối. */
    val component: Dp = 12.dp
) {
    companion object {
        val Default = DcSpacing()
        val Compact = DcSpacing(small = 6.dp, medium = 12.dp, large = 20.dp, xLarge = 28.dp, component = 8.dp)
    }
}
```

`SpacingTokens` thêm `val Component: Dp = 12.dp`.

Bảng dùng (ghi vào KDoc):

```text
xSmall  4dp   khoảng cách giữa hai dòng text liên quan
small   8dp   nhãn → control
component 12dp giữa hai control cùng khối
medium  16dp  padding ngang màn hình, padding trong card, khối thường
large   24dp  giữa hai section
xLarge  32dp  section lớn
```

## T1.7 — Token elevation và stroke (mới)

**File mới:** `tokens/SurfaceTokens.kt`

```kotlin
package com.dc.android.foundation.designsystem.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Elevation scale. Style guide ưu tiên viền mảnh hơn đổ bóng: mặt phẳng mặc định là Level0,
 * Level1 chỉ dùng khi cần tách khỏi nền, Level2/3 dành cho lớp nổi (menu, FAB).
 */
object ElevationTokens {
    val Level0: Dp = 0.dp
    val Level1: Dp = 1.dp
    val Level2: Dp = 3.dp
    val Level3: Dp = 6.dp
}

/** Độ dày viền. Foundation chỉ dùng một giá trị; nhiều độ dày làm giao diện nhiễu. */
object StrokeTokens {
    val Hairline: Dp = 1.dp
}
```

## T1.8 — Đồng bộ theme tài nguyên Android của app

**Files:** `app/src/main/res/values/themes.xml`, `app/src/main/res/values/colors.xml` (tạo lại), `app/src/main/res/values-night/themes.xml` (mới)

`colors.xml` đã bị xóa ở working tree; `themes.xml` đang kế thừa `android:Theme.Material.Light.NoActionBar` với nền trắng hệ thống → chớp trắng trước khi Compose vẽ nền `#F7F8FA`, và ở dark mode thì chớp sáng.

```xml
<!-- values/colors.xml -->
<resources>
    <color name="dc_window_background">#F7F8FA</color>
</resources>

<!-- values-night/colors.xml -->
<resources>
    <color name="dc_window_background">#0F1115</color>
</resources>

<!-- values/themes.xml -->
<resources>
    <style name="Theme.DCAndroidFoundation" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:windowBackground">@color/dc_window_background</item>
    </style>
</resources>

<!-- values-night/themes.xml -->
<resources>
    <style name="Theme.DCAndroidFoundation" parent="android:Theme.Material.NoActionBar">
        <item name="android:windowBackground">@color/dc_window_background</item>
    </style>
</resources>
```

Kiểm tra `AndroidManifest.xml` đang trỏ đúng `Theme.DCAndroidFoundation`; nếu `MainActivity` bật `enableEdgeToEdge()` thì không thêm màu status bar cứng.

## Nghiệm thu Phase 1

```bash
./gradlew :ui:designsystem:compileDebugKotlin :app:assembleDebug
```

- [ ] `rg "Purple|Pink|0xFF6650A4" ui/ app/` trống
- [ ] `rg "DcColors\("` — mọi call site đã có đủ field container
- [ ] Preview `DesignSystemCatalog` ở light: nền xám nhạt, nút xanh `#2563EB`, card trắng
- [ ] Preview ở dark: không còn tông tím, chữ không bị chìm
- [ ] Không có `.dp`/`Color(` mới nào xuất hiện ngoài thư mục `tokens/`
- [ ] `DcTheme(...)` vẫn override được: viết thử một preview truyền `colorScheme` khác và thấy đổi
