package com.dc.android.foundation.designsystem.components

import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DcWidgetPolicyTest {
    @Test
    fun badgeCountCollapsesAboveMax() {
        assertEquals("7", formatBadgeCount(7))
        assertEquals("99", formatBadgeCount(99))
        assertEquals("99+", formatBadgeCount(100))
        assertEquals("9+", formatBadgeCount(10, maxCount = 9))
    }

    @Test
    fun singleSelectionAlwaysKeepsOne() {
        assertEquals(setOf("b"), toggleSelection(setOf("a"), "b", DcSelectionMode.Single))
        assertEquals(setOf("a"), toggleSelection(setOf("a"), "a", DcSelectionMode.Single))
    }

    @Test
    fun singleOptionalSelectionClearsOnSecondClick() {
        assertEquals(emptySet<String>(), toggleSelection(setOf("a"), "a", DcSelectionMode.SingleOptional))
        assertEquals(setOf("b"), toggleSelection(setOf("a"), "b", DcSelectionMode.SingleOptional))
    }

    @Test
    fun multipleSelectionTogglesMembership() {
        assertEquals(setOf("a", "b"), toggleSelection(setOf("a"), "b", DcSelectionMode.Multiple))
        assertEquals(setOf("a"), toggleSelection(setOf("a", "b"), "b", DcSelectionMode.Multiple))
    }

    @Test
    fun selectAllStateReflectsSelection() {
        val all = setOf(1, 2, 3)
        assertEquals(ToggleableState.Off, selectAllState(emptySet(), all))
        assertEquals(ToggleableState.Indeterminate, selectAllState(setOf(1), all))
        assertEquals(ToggleableState.On, selectAllState(all, all))
        assertEquals(ToggleableState.Off, selectAllState(setOf(1), emptySet<Int>()))
    }

    @Test
    fun stepperClampsToRange() {
        assertEquals(0, clampStep(-3, 0..5))
        assertEquals(5, clampStep(9, 0..5))
        assertEquals(3, clampStep(3, 0..5))
    }

    @Test
    fun otpKeepsOnlyDigitsUpToLength() {
        assertEquals("1234", filterOtpInput("1a2b3c4", 6))
        assertEquals("123456", filterOtpInput("1234567890", 6))
        assertEquals("", filterOtpInput("abc", 6))
    }

    @Test
    fun maxLengthTruncatesOnlyWhenSet() {
        assertEquals("abc", enforceMaxLength("abcdef", 3))
        assertEquals("abcdef", enforceMaxLength("abcdef", null))
        assertEquals("ab", enforceMaxLength("ab", 3))
    }

    @Test
    fun buttonRowStacksWhenNarrowOrLargeFont() {
        assertTrue(shouldStackButtons(280.dp, 320.dp, 1.0f))
        assertTrue(shouldStackButtons(500.dp, 320.dp, 1.5f))
        assertFalse(shouldStackButtons(500.dp, 320.dp, 1.0f))
    }

    @Test
    fun dateBoundsAreInclusive() {
        val min = LocalDate.of(2026, 1, 10)
        val max = LocalDate.of(2026, 1, 20)
        assertTrue(isDateSelectable(min, min, max))
        assertTrue(isDateSelectable(max, min, max))
        assertFalse(isDateSelectable(min.minusDays(1), min, max))
        assertFalse(isDateSelectable(max.plusDays(1), min, max))
        assertTrue(isDateSelectable(LocalDate.of(1990, 1, 1), null, null))
    }

    @Test
    fun utcMillisRoundTripsLocalDate() {
        val date = LocalDate.of(2026, 2, 28)
        assertEquals(date, utcMillisToLocalDate(date.toUtcMillis()))
    }

    @Test
    fun indefiniteSnackbarRequiresAction() {
        assertThrows(IllegalArgumentException::class.java) {
            DcSnackbarMessage("Oops", duration = DcMessageDuration.Indefinite)
        }
        DcSnackbarMessage("Oops", actionLabel = "Retry", duration = DcMessageDuration.Indefinite)
    }
}
