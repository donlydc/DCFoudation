package com.dc.android.foundation.greeting

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.dc.android.foundation.designsystem.theme.DcTheme
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI test for the one screen this app has (17-testing.md). Requires a device/emulator
 * to run — not exercised by the unit-test-only verification this batch of phases relied on
 * (see PLAN_REPORT.md's consolidated-verification entry for what was actually run).
 */
class GreetingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysTheGivenMessage() {
        composeTestRule.setContent {
            DcTheme {
                GreetingScreen(state = GreetingUiState(message = "Hello Test!"), onAction = {})
            }
        }

        composeTestRule.onNodeWithText("Hello Test!").assertExists()
    }

    @Test
    fun refreshButtonSendsRefreshAction() {
        var receivedAction: GreetingUiAction? = null
        composeTestRule.setContent {
            DcTheme {
                GreetingScreen(
                    state = GreetingUiState(message = "Hello Test!"),
                    onAction = { receivedAction = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Refresh").performClick()

        assert(receivedAction == GreetingUiAction.Refresh)
    }
}
