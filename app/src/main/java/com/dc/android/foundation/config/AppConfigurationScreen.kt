package com.dc.android.foundation.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dc.android.foundation.R
import com.dc.android.foundation.designsystem.components.DcButton
import com.dc.android.foundation.designsystem.components.DcButtonVariant
import com.dc.android.foundation.designsystem.components.DcDropdownField
import com.dc.android.foundation.designsystem.components.DcScaffold
import com.dc.android.foundation.designsystem.components.DcSectionHeader
import com.dc.android.foundation.designsystem.components.DcText
import com.dc.android.foundation.designsystem.components.DcToggleControl
import com.dc.android.foundation.designsystem.components.DcToggleRow
import com.dc.android.foundation.designsystem.components.DcTopAppBar
import com.dc.android.foundation.designsystem.theme.DcUiScale
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing

private data class LabeledOption<T>(val value: T, val label: String)

/** Business-neutral settings UI and usage example for the common configuration functions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppConfigurationScreen(
    configuration: AppConfiguration,
    onAction: (AppConfigurationAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var themeExpanded by rememberSaveable { mutableStateOf(false) }
    var sizeExpanded by rememberSaveable { mutableStateOf(false) }
    var languageExpanded by rememberSaveable { mutableStateOf(false) }
    val spacing = LocalDcSpacing.current

    val themeOptions = listOf(
        LabeledOption(AppThemeMode.System, stringResource(R.string.config_theme_system)),
        LabeledOption(AppThemeMode.Light, stringResource(R.string.config_theme_light)),
        LabeledOption(AppThemeMode.Dark, stringResource(R.string.config_theme_dark))
    )
    val sizeOptions = listOf(
        LabeledOption(DcUiScale.Compact, stringResource(R.string.config_size_compact)),
        LabeledOption(DcUiScale.Standard, stringResource(R.string.config_size_standard)),
        LabeledOption(DcUiScale.Comfortable, stringResource(R.string.config_size_comfortable))
    )
    val languageOptions = listOf(
        LabeledOption<String?>(null, stringResource(R.string.config_language_system)),
        LabeledOption("en", stringResource(R.string.config_language_english)),
        LabeledOption("vi", stringResource(R.string.config_language_vietnamese))
    )
    val currentLanguageTag = AppLanguageController.currentLanguageTag()
    val selectedLanguage = languageOptions.firstOrNull { option ->
        option.value == null && currentLanguageTag == null ||
            option.value != null && currentLanguageTag?.startsWith(option.value) == true
    } ?: languageOptions.first()

    DcScaffold(
        modifier = modifier,
        topBar = { DcTopAppBar(stringResource(R.string.config_title)) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.medium)
                .padding(bottom = spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.component)
        ) {
            DcSectionHeader(
                stringResource(R.string.config_appearance),
                contentPadding = PaddingValues(top = spacing.large, bottom = spacing.small)
            )
            DcDropdownField(
                label = stringResource(R.string.config_theme),
                selectedLabel = themeOptions.first { it.value == configuration.themeMode }.label,
                expanded = themeExpanded,
                onExpandedChange = { themeExpanded = it },
                options = themeOptions.map { it.label },
                onOptionSelected = { label ->
                    onAction(AppConfigurationAction.SetThemeMode(themeOptions.first { it.label == label }.value))
                }
            )
            DcDropdownField(
                label = stringResource(R.string.config_ui_size),
                selectedLabel = sizeOptions.first { it.value == configuration.uiScale }.label,
                expanded = sizeExpanded,
                onExpandedChange = { sizeExpanded = it },
                options = sizeOptions.map { it.label },
                onOptionSelected = { label ->
                    onAction(AppConfigurationAction.SetUiScale(sizeOptions.first { it.label == label }.value))
                }
            )
            DcText(stringResource(R.string.config_ui_size_description))
            DcToggleRow(
                label = stringResource(R.string.config_dynamic_color),
                checked = configuration.dynamicColor,
                onCheckedChange = { onAction(AppConfigurationAction.SetDynamicColor(it)) },
                supportingText = stringResource(R.string.config_dynamic_color_description),
                control = DcToggleControl.Switch
            )

            DcSectionHeader(
                stringResource(R.string.config_language_section),
                contentPadding = PaddingValues(top = spacing.large, bottom = spacing.small)
            )
            DcDropdownField(
                label = stringResource(R.string.config_language),
                selectedLabel = selectedLanguage.label,
                expanded = languageExpanded,
                onExpandedChange = { languageExpanded = it },
                options = languageOptions.map { it.label },
                onOptionSelected = { label ->
                    AppLanguageController.setApplicationLanguage(
                        languageOptions.first { it.label == label }.value
                    )
                }
            )
            DcText(stringResource(R.string.config_language_description))

            DcButton(
                text = stringResource(R.string.config_reset),
                onClick = { onAction(AppConfigurationAction.Reset) },
                modifier = Modifier.fillMaxWidth(),
                variant = DcButtonVariant.Secondary
            )
        }
    }
}
