package com.dc.android.foundation.designsystem.components

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/** A typed choice; [value] is the comparison key and [label] is already-localized text. */
@Immutable
data class DcOption<out T>(
    val value: T,
    val label: String,
    val enabled: Boolean = true,
    val icon: ImageVector? = null,
    val supportingText: String? = null
)

/** A menu or action-sheet entry. [destructive] only changes color, never behavior. */
@Immutable
data class DcMenuItem(
    val id: String,
    val label: String,
    val icon: ImageVector? = null,
    val enabled: Boolean = true,
    val destructive: Boolean = false
)

/** How a group of choices reacts to a click. */
enum class DcSelectionMode {
    /** Exactly one item stays selected; clicking the selected item is a no-op. */
    Single,

    /** At most one item; clicking the selected item clears it. */
    SingleOptional,

    /** Any number of items. */
    Multiple
}

/**
 * Pure selection policy shared by chip/segmented/checkbox groups so every group toggles the same
 * way and the behavior is unit-testable without Compose.
 */
fun <T> toggleSelection(current: Set<T>, clicked: T, mode: DcSelectionMode): Set<T> = when (mode) {
    DcSelectionMode.Single -> setOf(clicked)
    DcSelectionMode.SingleOptional -> if (clicked in current) emptySet() else setOf(clicked)
    DcSelectionMode.Multiple -> if (clicked in current) current - clicked else current + clicked
}
