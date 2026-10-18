package com.dc.android.foundation.designsystem.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DcWidgetsTest {
    @get:Rule val composeRule = createComposeRule()

    private val options = listOf(DcOption("a", "Alpha"), DcOption("b", "Beta"), DcOption("c", "Gamma"))

    @Test
    fun overflowMenuOpensAndReportsItem() {
        var clicked: String? = null
        composeRule.setContent {
            DcOverflowMenu(
                items = listOf(DcMenuItem("share", "Share"), DcMenuItem("delete", "Delete", destructive = true)),
                onItemClick = { clicked = it.id },
                contentDescription = "More"
            )
        }
        composeRule.onNodeWithContentDescription("More").performClick()
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.runOnIdle { assertEquals("delete", clicked) }
    }

    @Test
    fun chipGroupSingleModeKeepsSelection() {
        composeRule.setContent {
            var selected by remember { mutableStateOf(setOf("a")) }
            DcChipGroup(options, selected, { selected = it })
        }
        composeRule.onNodeWithText("Beta").performClick()
        composeRule.onNodeWithText("Beta").assertIsSelected()
    }

    @Test
    fun radioGroupSelectsClickedOption() {
        var chosen: String? = null
        composeRule.setContent { DcRadioGroup(options, selected = null, onSelect = { chosen = it }) }
        composeRule.onNodeWithText("Gamma").performClick()
        composeRule.runOnIdle { assertEquals("c", chosen) }
    }

    @Test
    fun stepperDisablesDecrementAtMinimum() {
        composeRule.setContent { DcStepper(0, {}, 0..3, "Guests", increaseLabel = "More", decreaseLabel = "Less") }
        composeRule.onNodeWithContentDescription("Less").assertIsNotEnabled()
    }

    @Test
    fun stepperButtonsMeetTouchFloor() {
        composeRule.setContent { DcStepper(1, {}, 0..3, "Guests", increaseLabel = "More", decreaseLabel = "Less") }
        composeRule.onNodeWithContentDescription("More").assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun expandableSectionTogglesThroughCallback() {
        var expanded = false
        composeRule.setContent { DcExpandableSection("Details", expanded, { expanded = it }) { DcText("Body") } }
        composeRule.onNodeWithText("Details").performClick()
        composeRule.runOnIdle { assertEquals(true, expanded) }
    }

    @Test
    fun badgeShowsCappedCount() {
        composeRule.setContent { DcBadge(count = 150) }
        composeRule.onNodeWithText("99+").assertExists()
    }

    @Test
    fun inlineMessageDismissInvokesCallback() {
        var dismissed = false
        composeRule.setContent {
            DcInlineMessage("Heads up", DcStatusTone.Warning, onDismiss = { dismissed = true }, dismissLabel = "Dismiss")
        }
        composeRule.onNodeWithContentDescription("Dismiss").performClick()
        composeRule.runOnIdle { assertEquals(true, dismissed) }
    }
}
