package com.dc.android.foundation.designsystem.components

import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DcNavigationSuiteTest {
    @Test
    fun rejectsTooFewTopLevelDestinations() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            requireSupportedNavigationItemCount(2)
        }

        assertTrue(error.message.orEmpty().contains("3 to 5"))
    }

    @Test
    fun rejectsTooManyTopLevelDestinations() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            requireSupportedNavigationItemCount(6)
        }

        assertTrue(error.message.orEmpty().contains("3 to 5"))
    }

    @Test
    fun acceptsSupportedTopLevelDestinationCounts() {
        (3..5).forEach(::requireSupportedNavigationItemCount)
    }
}
