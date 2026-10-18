# Common screen patterns

[Back to design system](README.md)

## Mục tiêu

Tài liệu này hướng dẫn ghép các API `Dc*` thành giao diện trung tính, có thể dùng cho hồ sơ, đơn hàng, giao dịch, đặt chỗ, công việc hoặc ứng dụng dịch vụ. Các màn hình Trip trong `:app` chỉ là dữ liệu minh họa; model và từ ngữ nghiệp vụ không thuộc `:ui:designsystem`.

Luồng phụ thuộc luôn là:

```text
DcTheme/tokens → Dc components → screen layout → domain data and actions
```

Không tạo một `DcServiceScreen` khổng lồ. Design system sở hữu các quy tắc thị giác và tương tác ổn định; feature sở hữu bố cục màn hình, trạng thái và nghiệp vụ.

## Ánh xạ pattern sang nhiều miền

| Pattern trung tính | Trip | Ứng dụng khác |
|---|---|---|
| Detail summary | quãng đường, thời gian, cước | tổng đơn hàng, thông tin đặt chỗ, số dư, metadata hồ sơ |
| Record list | lịch sử chuyến đi | giao dịch, đơn hàng, phiên làm việc, thông báo |
| Status label | completed, pending | paid, processing, approved, failed |
| Primary/destructive action | kết thúc chuyến | lưu, gửi, hủy đơn, xóa tài khoản |
| Content state | loading/empty/error | áp dụng như nhau cho mọi dữ liệu bất đồng bộ |

## 1. Màn hình chi tiết hoặc tóm tắt

Dùng `DcScaffold` cho safe insets, `DcTopAppBar` cho điều hướng, `DcSectionHeader` để chia nhóm và `DcCard` + `DcKeyValueRow` cho dữ liệu nhãn–giá trị.

```kotlin
@Composable
fun OrderSummaryScreen(
    onBack: () -> Unit,
    onCancel: () -> Unit
) {
    val spacing = LocalDcSpacing.current
    DcScaffold(
        topBar = { DcTopAppBar("Order details", onNavigateBack = onBack) },
        bottomBar = {
            DcBottomActionBar {
                DcPrimaryActionButton(
                    text = "Cancel order",
                    onClick = onCancel,
                    destructive = true
                )
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .padding(horizontal = spacing.medium)
        ) {
            DcSectionHeader(
                "Payment",
                contentPadding = PaddingValues(
                    top = spacing.large,
                    bottom = spacing.small
                )
            )
            DcCard {
                DcKeyValueRow("Subtotal", "$120.00")
                DcKeyValueRow("Service fee", "$8.00")
                DcDivider()
                DcKeyValueRow("Total", "$128.00", emphasized = true)
            }
        }
    }
}
```

Quy ước:

- Dùng `emphasized = true` cho kết quả cuối như tổng tiền, số dư hoặc trạng thái kết luận; không dùng để nhấn mọi dòng.
- Đặt các dòng liên quan trong cùng một `DcCard`; tách nhóm bằng `DcSectionHeader`.
- Hành động phá hủy phải dùng style destructive và xác nhận bằng `DcConfirmDialog`.
- Feature cung cấp text đã định dạng và bản địa hóa. Design system không định dạng tiền, ngày tháng hoặc đơn vị nghiệp vụ.

## 2. Danh sách bản ghi

Kết hợp `DcCard` và `DcListRow` thay vì tạo card riêng cho mỗi miền. Dùng slot `trailingContent` cho giá trị và `DcStatusChip`.

```kotlin
DcCard(
    onClick = onOpen,
    contentPadding = PaddingValues.Zero
) {
    DcListRow(
        headline = record.title,
        supportingText = record.subtitle,
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                DcText(record.amount, style = MaterialTheme.typography.titleMedium)
                DcStatusChip(record.statusLabel, record.statusTone)
            }
        }
    )
}
```

`DcStatusTone` là ngữ nghĩa trình bày, không phải domain enum. Feature tự ánh xạ trạng thái nghiệp vụ:

```kotlin
private fun OrderStatus.toTone(): DcStatusTone = when (this) {
    OrderStatus.Paid -> DcStatusTone.Success
    OrderStatus.Processing -> DcStatusTone.Info
    OrderStatus.WaitingForPayment -> DcStatusTone.Warning
    OrderStatus.Cancelled -> DcStatusTone.Error
}
```

Luôn hiển thị text trạng thái; màu chỉ là tín hiệu bổ sung. Bộ lọc tương tác dùng `DcChip`, nhãn trạng thái không tương tác dùng `DcStatusChip`.

## 3. Loading, empty, error và dữ liệu cũ

State phải đến từ Route/ViewModel; không đặt nút giả lập loading/error trong màn hình sản phẩm. Dùng `DcContentState` để giữ chung thứ tự ưu tiên:

```kotlin
DcContentState(
    value = uiState.items,
    isLoading = uiState.isLoading,
    errorMessage = uiState.errorMessage,
    isEmpty = List<UiItem>::isEmpty,
    onRetry = onRetry,
    loadingLabel = "Loading orders",
    emptyTitle = "No orders yet"
) { items ->
    LazyColumn {
        items(items, key = UiItem::id) { item ->
            OrderRow(item = item, onClick = { onOpen(item.id) })
        }
    }
}
```

Khi đã có dữ liệu, `DcContentState` giữ nội dung cũ trên màn hình và hiển thị tiến trình/banner trong lúc refresh hoặc gặp lỗi. Chỉ dùng blocking loading/error khi chưa có nội dung.

## 4. Theme và khả năng tái sử dụng

App đổi thương hiệu tại `DcTheme`, không fork component:

```kotlin
DcTheme(
    colorScheme = brandColorScheme,
    typography = brandTypography,
    shapes = brandShapes,
    extendedColors = brandExtendedColors,
    spacing = brandSpacing
) {
    AppNavigation()
}
```

Feature đọc `MaterialTheme.*` và `LocalDcSpacing`; không hard-code màu, shape hoặc `.dp` khi đã có semantic token.

## 5. Checklist trước khi đưa vào feature

- UI chỉ nhận state và callback; không gọi repository/DAO/API trong Composable.
- Text, tiền tệ, ngày giờ và content description đã được bản địa hóa ở app.
- Nút bấm có nhãn rõ; status không truyền tải thông tin chỉ bằng màu.
- Kiểm tra light/dark, font scale lớn và compact/medium/expanded width.
- Danh sách có key ổn định; lỗi có retry khi hành động đó thực sự khả dụng.
- Component mới chỉ được thêm vào design system khi có chính sách dùng chung; bố cục hoặc model riêng tiếp tục nằm trong feature.

## Mẫu tham chiếu hiện có

- Chi tiết/tóm tắt: `app/src/main/java/com/dc/android/foundation/showcase/trip/TripDetailsShowcaseScreen.kt`
- Danh sách bản ghi + filter + status: `app/src/main/java/com/dc/android/foundation/showcase/trip/TripHistoryShowcaseScreen.kt`
- Catalog của mọi primitive: `ui/designsystem/src/main/java/com/dc/android/foundation/designsystem/catalog/DesignSystemCatalog.kt`
