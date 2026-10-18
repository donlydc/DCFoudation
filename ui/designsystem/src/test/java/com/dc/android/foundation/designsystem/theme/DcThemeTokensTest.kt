package com.dc.android.foundation.designsystem.theme

import com.dc.android.foundation.designsystem.tokens.ColorTokens
import com.dc.android.foundation.designsystem.tokens.DcSpacing
import com.dc.android.foundation.designsystem.tokens.ElevationTokens
import com.dc.android.foundation.designsystem.tokens.ShapeTokens
import com.dc.android.foundation.designsystem.tokens.SpacingTokens
import com.dc.android.foundation.designsystem.tokens.StrokeTokens
import org.junit.Assert.assertEquals
import org.junit.Test

class DcThemeTokensTest {
    @Test
    fun extendedColorsUseReadableStatusPairsInLightTheme() {
        val colors = DcThemeDefaults.extendedColors(darkTheme = false)

        assertEquals(ColorTokens.Green600, colors.success)
        assertEquals(ColorTokens.Green100, colors.successContainer)
        assertEquals(ColorTokens.Green900, colors.onSuccessContainer)
        assertEquals(ColorTokens.Grey900, colors.onWarning)
        assertEquals(ColorTokens.Amber100, colors.warningContainer)
        assertEquals(ColorTokens.Blue100, colors.infoContainer)
        assertEquals(ColorTokens.Red600, colors.offline)
    }

    @Test
    fun extendedColorsUseDarkStatusPairsInDarkTheme() {
        val colors = DcThemeDefaults.extendedColors(darkTheme = true)

        assertEquals(ColorTokens.Green300, colors.success)
        assertEquals(ColorTokens.Green900, colors.successContainer)
        assertEquals(ColorTokens.Amber300, colors.warning)
        assertEquals(ColorTokens.Amber900, colors.warningContainer)
        assertEquals(ColorTokens.Blue800, colors.infoContainer)
        assertEquals(ColorTokens.Red300, colors.offline)
    }

    @Test
    fun semanticScalesMapToPrimitiveTokens() {
        assertEquals(SpacingTokens.Component, DcSpacing.Default.component)
        assertEquals(ShapeTokens.ExtraSmall, DcShapes.extraSmall)
        assertEquals(ShapeTokens.Small, DcShapes.small)
        assertEquals(ShapeTokens.Medium, DcShapes.medium)
        assertEquals(ShapeTokens.Large, DcShapes.large)
        assertEquals(ShapeTokens.ExtraLarge, DcShapes.extraLarge)
        assertEquals(0f, ElevationTokens.Level0.value, 0f)
        assertEquals(1f, ElevationTokens.Level1.value, 0f)
        assertEquals(3f, ElevationTokens.Level2.value, 0f)
        assertEquals(6f, ElevationTokens.Level3.value, 0f)
        assertEquals(1f, StrokeTokens.Hairline.value, 0f)
    }

    @Test
    fun typographyUsesTheDcSemanticScale() {
        assertEquals(32f, DcTypography.displaySmall.fontSize.value, 0f)
        assertEquals(24f, DcTypography.headlineSmall.fontSize.value, 0f)
        assertEquals(20f, DcTypography.titleLarge.fontSize.value, 0f)
        assertEquals(16f, DcTypography.bodyLarge.fontSize.value, 0f)
        assertEquals(14f, DcTypography.bodyMedium.fontSize.value, 0f)
        assertEquals(11f, DcTypography.labelSmall.fontSize.value, 0f)
    }
}
