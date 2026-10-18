package com.dc.android.foundation.designsystem.tokens

import androidx.compose.ui.graphics.Color

/**
 * Primitive palette backing the DC default theme. Feature UI must never reference these
 * directly — read semantic colors from MaterialTheme/DcTheme instead (DS-01).
 */
object ColorTokens {
    // Brand
    val Blue600 = Color(0xFF2563EB)
    val Blue700 = Color(0xFF1D4ED8)
    val Blue800 = Color(0xFF1E40AF)
    val Blue900 = Color(0xFF1E3A8A)
    val Blue100 = Color(0xFFDBEAFE)
    val Blue300 = Color(0xFF93C5FD)

    // Neutral
    val White = Color(0xFFFFFFFF)
    val Grey50 = Color(0xFFF7F8FA)
    val Grey100 = Color(0xFFF1F3F5)
    val Grey200 = Color(0xFFE5E7EB)
    val Grey300 = Color(0xFFD1D5DB)
    val Grey400 = Color(0xFF9CA3AF)
    val Grey500 = Color(0xFF6B7280)
    val Grey900 = Color(0xFF111827)

    // Neutral (dark theme)
    val Ink900 = Color(0xFF0F1115)
    val Ink800 = Color(0xFF14171D)
    val Ink700 = Color(0xFF1F242C)
    val Ink600 = Color(0xFF2A303A)
    val Ink500 = Color(0xFF3A414B)

    // Status
    val Green600 = Color(0xFF16A34A)
    val Green100 = Color(0xFFDCFCE7)
    val Green900 = Color(0xFF14532D)
    val Green300 = Color(0xFF86EFAC)

    val Amber500 = Color(0xFFF59E0B)
    val Amber100 = Color(0xFFFEF3C7)
    val Amber900 = Color(0xFF78350F)
    val Amber300 = Color(0xFFFCD34D)

    val Red600 = Color(0xFFDC2626)
    val Red100 = Color(0xFFFEE2E2)
    val Red900 = Color(0xFF7F1D1D)
    val Red300 = Color(0xFFFCA5A5)
}
