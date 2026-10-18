package com.dc.android.foundation.designsystem.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/** Light/dark preview pair for every public Design System component. */
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class DcThemePreviews

/** Font-scale preview matrix used to catch clipped/overlapping semantic text. */
@Preview(name = "Font 1.0x", fontScale = 1.0f)
@Preview(name = "Font 1.5x", fontScale = 1.5f)
@Preview(name = "Font 2.0x", fontScale = 2.0f)
annotation class DcFontScalePreviews

/** Window-width preview matrix for compact, medium and expanded layouts. */
@Preview(name = "Compact", widthDp = 360)
@Preview(name = "Medium", widthDp = 700)
@Preview(name = "Expanded", widthDp = 1200)
annotation class DcWindowPreviews
