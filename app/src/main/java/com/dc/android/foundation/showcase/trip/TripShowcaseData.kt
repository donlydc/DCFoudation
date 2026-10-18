package com.dc.android.foundation.showcase.trip

/**
 * Màn hình trình diễn design system. Không phải feature thật; không có nguồn dữ liệu.
 */
internal object TripShowcaseData {
    val trips = listOf(
        TripShowcaseItem("Trip #10232", "Dec 12, 2024 · 10:24", "12.6 km · 24 min", "245,000 đ", TripStatus.Completed),
        TripShowcaseItem("Trip #10231", "Dec 11, 2024 · 18:05", "8.4 km · 19 min", "176,000 đ", TripStatus.Pending),
        TripShowcaseItem("Trip #10230", "Dec 10, 2024 · 08:42", "3.2 km · 11 min", "92,000 đ", TripStatus.Cancelled),
        TripShowcaseItem("Trip #10229", "Dec 9, 2024 · 14:18", "15.1 km · 31 min", "310,000 đ", TripStatus.Active)
    )
}

internal data class TripShowcaseItem(
    val id: String,
    val dateTime: String,
    val summary: String,
    val fare: String,
    val status: TripStatus
)

internal enum class TripStatus(val label: String) {
    Active("Active"),
    Completed("Completed"),
    Pending("Pending"),
    Cancelled("Cancelled")
}
