package com.dc.android.foundation.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dc.android.foundation.designsystem.components.DcButton
import com.dc.android.foundation.designsystem.components.DcText
import com.dc.android.foundation.designsystem.theme.DcTheme

/**
 * Manual preview catalog (Gate C requirement): one screen exercising every current
 * token/component combination so a visual regression is obvious. Grows as Phase C+
 * adds components — do not let it drift out of sync with what actually exists.
 */
@Composable
fun DesignSystemCatalog(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DcText("DC Design System catalog")
        DcButton(text = "Primary action", onClick = {})
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
