package com.dc.android.foundation.designsystem.theme

import com.dc.android.foundation.designsystem.tokens.DcSpacing
import org.junit.Assert.assertEquals
import org.junit.Test

class DcUiScaleTest {

    @Test
    fun `standard scale preserves semantic values`() {
        assertEquals(DcSpacing.Default, DcUiScale.Standard.applyTo(DcSpacing.Default))
        assertEquals(DcTypography, DcUiScale.Standard.applyTo(DcTypography))
    }

    @Test
    fun `compact and comfortable scale spacing predictably`() {
        val compact = DcUiScale.Compact.applyTo(DcSpacing.Default)
        val comfortable = DcUiScale.Comfortable.applyTo(DcSpacing.Default)

        assertEquals(14.4f, compact.medium.value, 0.001f)
        assertEquals(17.6f, comfortable.medium.value, 0.001f)
        assertEquals(0f, compact.none.value, 0f)
    }

    @Test
    fun `ui scale changes typography without changing weight`() {
        val comfortable = DcUiScale.Comfortable.applyTo(DcTypography)

        assertEquals(17.6f, comfortable.bodyLarge.fontSize.value, 0.001f)
        assertEquals(DcTypography.bodyLarge.fontWeight, comfortable.bodyLarge.fontWeight)
    }
}
