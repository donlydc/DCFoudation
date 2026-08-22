package com.dc.android.foundation.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dc.android.foundation.designsystem.components.DcButton
import com.dc.android.foundation.designsystem.components.DcEmptyState
import com.dc.android.foundation.designsystem.components.DcErrorState
import com.dc.android.foundation.designsystem.components.DcListRow
import com.dc.android.foundation.designsystem.components.DcLoadingState
import com.dc.android.foundation.designsystem.components.DcOfflineBanner
import com.dc.android.foundation.designsystem.components.DcPasswordTextField
import com.dc.android.foundation.designsystem.components.DcText
import com.dc.android.foundation.designsystem.components.DcTextField
import com.dc.android.foundation.designsystem.theme.DcTheme
import com.dc.android.foundation.designsystem.tokens.SpacingTokens

/**
 * Manual preview catalog (Gate C requirement): one screen exercising every current
 * token/component combination so a visual regression is obvious. Grows as Phase C+
 * adds components — do not let it drift out of sync with what actually exists.
 */
@Composable
fun DesignSystemCatalog(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(SpacingTokens.Medium),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Small)
    ) {
        DcText("DC Design System catalog")
        DcButton(text = "Primary action", onClick = {})
        DcOfflineBanner(message = "You are offline. Changes will sync when connected.")
        DcLoadingState(label = "Loading content")
        DcEmptyState(
            title = "No saved items",
            description = "Items you save will appear here."
        )
        DcErrorState(
            title = "Could not load items",
            message = "Check your connection and try again.",
            retryLabel = "Retry",
            onRetry = {}
        )
        DcTextField(
            label = "Display name",
            value = "Donly",
            onValueChange = {},
            supportingText = "Shown to other people"
        )
        DcPasswordTextField(
            label = "Password",
            value = "password",
            onValueChange = {},
            showPasswordLabel = "Show password",
            hidePasswordLabel = "Hide password"
        )
        DcListRow(
            headline = "Account settings",
            supportingText = "Manage sign-in and security",
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Catalog - light")
@Composable
private fun DesignSystemCatalogLightPreview() {
    DcTheme(darkTheme = false, dynamicColor = false) {
        DesignSystemCatalog()
    }
}

@Preview(showBackground = true, name = "Catalog - dark")
@Composable
private fun DesignSystemCatalogDarkPreview() {
    DcTheme(darkTheme = true, dynamicColor = false) {
        DesignSystemCatalog()
    }
}
