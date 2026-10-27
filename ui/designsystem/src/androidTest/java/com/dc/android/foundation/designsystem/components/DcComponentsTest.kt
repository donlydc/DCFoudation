package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DcComponentsTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun loadingButtonIsDisabled() {
        composeRule.setContent { DcButton(text = "Submit", loading = true, onClick = {}) }
        composeRule.onNodeWithText("Submit").assertIsNotEnabled()
    }

    @Test
    fun loadingButtonKeepsNormalHeight() {
        composeRule.setContent {
            Row {
                DcButton(text = "Normal", onClick = {})
                DcButton(text = "Loading", loading = true, onClick = {})
            }
        }
        val normalHeight = composeRule.onNodeWithText("Normal").fetchSemanticsNode().boundsInRoot.height
        val loadingHeight = composeRule.onNodeWithText("Loading").fetchSemanticsNode().boundsInRoot.height
        composeRule.runOnIdle { assertEquals(normalHeight, loadingHeight, 0.5f) }
    }

    @Suppress("DEPRECATION") // intentionally covers the legacy Material-typed overload (DEC-08)
    @Test
    fun snackbarAcknowledgesOneMessageOnce() {
        var acknowledgements = 0
        composeRule.setContent {
            val hostState = remember { SnackbarHostState() }
            DcSnackbarEffect(message = "Saved", snackbarHostState = hostState) { acknowledgements++ }
        }
        composeRule.runOnIdle { assertEquals(1, acknowledgements) }
    }

    @Test
    fun typedSnackbarAcknowledgesOneMessageOnce() {
        var acknowledgements = 0
        composeRule.setContent {
            val hostState = remember { DcSnackbarHostState() }
            DcSnackbarEffect(
                message = DcSnackbarMessage("Saved"),
                state = hostState,
                onMessageShown = { acknowledgements++ }
            )
        }
        composeRule.runOnIdle { assertEquals(1, acknowledgements) }
    }

    @Test
    fun switchToggleRowExposesSwitchRole() {
        composeRule.setContent {
            DcToggleRow("Wi-Fi", checked = true, onCheckedChange = {}, control = DcToggleControl.Switch)
        }
        composeRule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)).assertExists()
    }

    @Test
    fun toggleRowRespondsWhenLabelIsTapped() {
        var checked = false
        composeRule.setContent {
            DcToggleRow("Airplane mode", checked = checked, onCheckedChange = { checked = it })
        }
        composeRule.onNode(hasText("Airplane mode") and hasClickAction()).performClick()
        composeRule.runOnIdle { assertEquals(true, checked) }
    }

    @Test
    fun textFieldExposesErrorSemantics() {
        composeRule.setContent {
            DcTextField(label = "Name", value = "", onValueChange = {}, isError = true, supportingText = "Required")
        }
        composeRule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.Error, "Required")).assertExists()
    }

    @Test
    fun textFieldErrorTextSetsErrorStateAndSemantics() {
        composeRule.setContent {
            DcTextField(
                label = "Name",
                value = "",
                onValueChange = {},
                errorText = "Required"
            )
        }
        composeRule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.Error, "Required")).assertExists()
        composeRule.onNodeWithText("Required").assertExists()
    }

    @Test
    fun statusChipIsAReadableNonInteractiveLabel() {
        composeRule.setContent { DcStatusChip(text = "Completed", tone = DcStatusTone.Success) }
        composeRule.onNodeWithText("Completed").assertExists()
        composeRule.onNode(hasText("Completed") and hasClickAction()).assertDoesNotExist()
    }

    @Test
    fun keyValueRowExposesLabelAndValue() {
        composeRule.setContent { DcKeyValueRow(label = "Total", value = "$128.00", emphasized = true) }
        composeRule.onNodeWithText("Total").assertExists()
        composeRule.onNodeWithText("$128.00").assertExists()
    }

    @Test
    fun dropdownFieldOpensAndSelectsAnOption() {
        var selected = ""
        composeRule.setContent {
            var expanded by remember { mutableStateOf(false) }
            var current by remember { mutableStateOf("System") }
            DcDropdownField(
                label = "Theme",
                selectedLabel = current,
                expanded = expanded,
                onExpandedChange = { expanded = it },
                options = listOf("System", "Light", "Dark"),
                onOptionSelected = {
                    current = it
                    selected = it
                }
            )
        }

        composeRule.onNode(hasText("System") and hasClickAction()).performClick()
        composeRule.onNodeWithText("Dark").performClick()
        composeRule.runOnIdle { assertEquals("Dark", selected) }
    }

    @Test
    fun topAppBarBackIconUsesLocalizedDescription() {
        composeRule.setContent {
            DcTopAppBar(
                title = "Details",
                onNavigateBack = {},
                navigateBackLabel = "Navigate up"
            )
        }
        composeRule.onNodeWithContentDescription("Navigate up").assertExists()
    }

    @Test
    fun interactiveButtonMeetsTouchFloor() {
        composeRule.setContent { DcButton(text = "Save", onClick = {}) }
        val node = composeRule.onNodeWithText("Save").fetchSemanticsNode()
        composeRule.runOnIdle {
            val expected = with(composeRule.density) { 48.dp.toPx() }
            assert(node.boundsInRoot.height >= expected)
        }
    }
}
