package com.dc.android.foundation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * The only Design System surface backed by Coil. Replacing the image loader later is contained to
 * this file; feature call sites keep using the DC API.
 */
@Composable
fun DcAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

/** Circular avatar with an initial fallback visible while an image is unavailable. */
@Composable
fun DcAvatar(
    name: String,
    modifier: Modifier = Modifier,
    imageModel: Any? = null,
    contentDescription: String? = name,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        DcText(
            text = name.trim().firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center
        )
        if (imageModel != null) {
            DcAsyncImage(
                model = imageModel,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}
