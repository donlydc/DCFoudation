package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Slider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dc.android.foundation.designsystem.R
import com.dc.android.foundation.designsystem.preview.DcThemePreviews
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.tokens.LocalDcSpacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Multi-line [DcTextField] with an IME action that inserts a new line instead of closing. */
@Composable
fun DcTextArea(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    errorText: String? = null,
    minLines: Int = 3,
    maxLines: Int = 8,
    maxLength: Int? = null
) {
    DcTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        supportingText = supportingText,
        errorText = errorText,
        singleLine = false,
        minLines = minLines,
        maxLines = maxLines,
        maxLength = maxLength,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
    )
}

/** Groups related form fields beneath an optional heading with standard spacing. */
@Composable
fun DcFormSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    supportingText: String? = null,
    content: @Composable () -> Unit
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(LocalDcSpacing.current.component)) {
        title?.let { DcSectionHeader(it, supportingText = supportingText, contentPadding = PaddingValues(0.dp)) }
        content()
    }
}

/** Clamps [value] into [range]; the stepper never emits out-of-range values. */
fun clampStep(value: Int, range: IntRange): Int = value.coerceIn(range.first, range.last)

private val MinusIcon: ImageVector by lazy {
    ImageVector.Builder("Minus", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(androidx.compose.ui.graphics.Color.Black)) {
            moveTo(5f, 11f); horizontalLineTo(19f); verticalLineTo(13f); horizontalLineTo(5f); close()
        }
    }.build()
}

/** Integer stepper with decrement/increment buttons; [label] names the quantity. */
@Composable
fun DcStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    label: String,
    modifier: Modifier = Modifier,
    increaseLabel: String = stringResource(R.string.dc_increase),
    decreaseLabel: String = stringResource(R.string.dc_decrease),
    enabled: Boolean = true
) {
    val spacing = LocalDcSpacing.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.small)
    ) {
        DcText(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        DcIconButton(
            MinusIcon, decreaseLabel,
            onClick = { onValueChange(clampStep(value - 1, range)) },
            enabled = enabled && value > range.first,
            variant = DcIconButtonVariant.Tonal
        )
        DcText(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(40.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
        )
        DcIconButton(
            Icons.Filled.Add, increaseLabel,
            onClick = { onValueChange(clampStep(value + 1, range)) },
            enabled = enabled && value < range.last,
            variant = DcIconButtonVariant.Tonal
        )
    }
}

/** Slider with a visible label/value; [valueText] also becomes the screen-reader state. */
@Composable
fun DcSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    valueText: (Float) -> String,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true
) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DcText(label, style = MaterialTheme.typography.bodyLarge)
            DcText(valueText(value), style = MaterialTheme.typography.labelLarge)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            modifier = Modifier.semantics {
                contentDescription = label
                stateDescription = valueText(value)
            }
        )
    }
}

/** Two-thumb variant of [DcSlider]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DcRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    label: String,
    valueText: (ClosedFloatingPointRange<Float>) -> String,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true
) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DcText(label, style = MaterialTheme.typography.bodyLarge)
            DcText(valueText(value), style = MaterialTheme.typography.labelLarge)
        }
        RangeSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            modifier = Modifier.semantics {
                contentDescription = label
                stateDescription = valueText(value)
            }
        )
    }
}

/** Keeps only digits and truncates to [length]. */
fun filterOtpInput(raw: String, length: Int): String = raw.filter(Char::isDigit).take(length)

/**
 * One-time-code entry rendered as boxes over a single text input, so TalkBack, paste and IME
 * autofill treat it as one field. [onComplete] fires when the value reaches [length] digits.
 */
@Composable
fun DcOtpField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    isError: Boolean = false,
    errorText: String? = null,
    enabled: Boolean = true,
    onComplete: (String) -> Unit = {}
) {
    val spacing = LocalDcSpacing.current
    val hasError = isError || errorText != null
    Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing.xSmall)) {
        BasicTextField(
            value = value,
            onValueChange = { raw ->
                val filtered = filterOtpInput(raw, length)
                onValueChange(filtered)
                if (filtered.length == length && filtered != value) onComplete(filtered)
            },
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            keyboardActions = KeyboardActions.Default,
            textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
                    repeat(length) { index ->
                        val focused = index == value.length.coerceAtMost(length - 1)
                        val borderColor = when {
                            hasError -> MaterialTheme.colorScheme.error
                            focused -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline
                        }
                        Box(
                            Modifier
                                .width(44.dp)
                                .height(56.dp)
                                .border(BorderStroke(if (focused) 2.dp else 1.dp, borderColor), MaterialTheme.shapes.medium)
                                .clearAndSetSemantics {},
                            contentAlignment = Alignment.Center
                        ) {
                            DcText(
                                value.getOrNull(index)?.toString().orEmpty(),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        )
        errorText?.let {
            DcText(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

internal fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

internal fun utcMillisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

/** Whether [date] lies within the inclusive [min]..[max] bounds (either may be absent). */
internal fun isDateSelectable(date: LocalDate, min: LocalDate?, max: LocalDate?): Boolean =
    (min == null || !date.isBefore(min)) && (max == null || !date.isAfter(max))

/** Date picker dialog; the caller owns visibility, formatting and locale. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DcDatePickerDialog(
    initialDate: LocalDate?,
    onConfirm: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = stringResource(R.string.dc_ok),
    dismissLabel: String = stringResource(R.string.dc_cancel),
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.toUtcMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                isDateSelectable(utcMillisToLocalDate(utcTimeMillis), minDate, maxDate)
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        confirmButton = {
            DcButton(
                text = confirmLabel,
                variant = DcButtonVariant.Text,
                enabled = state.selectedDateMillis != null,
                onClick = { state.selectedDateMillis?.let { onConfirm(utcMillisToLocalDate(it)) } }
            )
        },
        dismissButton = { DcButton(text = dismissLabel, variant = DcButtonVariant.Text, onClick = onDismissRequest) }
    ) {
        DatePicker(state = state)
    }
}

/**
 * Read-only field that opens [DcDatePickerDialog]. The feature supplies [formattedValue] so the
 * design system never formats dates or chooses a locale.
 */
@Composable
fun DcDateField(
    label: String,
    value: LocalDate?,
    onValueChange: (LocalDate) -> Unit,
    formattedValue: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    errorText: String? = null,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    Box(modifier) {
        DcTextField(
            label = label,
            value = if (value == null) "" else formattedValue,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            errorText = errorText,
            leadingIcon = Icons.Filled.DateRange
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable(enabled = enabled, role = Role.Button) { showDialog = true }
        )
    }
    if (showDialog) {
        DcDatePickerDialog(
            initialDate = value,
            onConfirm = {
                onValueChange(it)
                showDialog = false
            },
            onDismissRequest = { showDialog = false },
            minDate = minDate,
            maxDate = maxDate
        )
    }
}

@DcThemePreviews
@Composable
private fun DcFormPreview() {
    DcTheme {
        DcFormSection(title = "Profile") {
            DcTextArea("Bio", "Hello", {}, maxLength = 120)
            DcStepper(2, {}, 0..5, "Guests")
            DcSlider(0.4f, {}, "Volume", { "${(it * 100).toInt()}%" })
            DcOtpField("123", {}, length = 6)
            DcDateField("Birthday", LocalDate.of(2000, 1, 1), {}, "Jan 1, 2000")
        }
    }
}
