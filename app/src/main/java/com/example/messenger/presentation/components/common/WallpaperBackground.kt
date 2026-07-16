package com.example.messenger.presentation.components.common
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.messenger.R
import com.example.messenger.presentation.screens.ui.theme.messengerTokens

/**
 * The single app-wide wallpaper. Drawn once as a static layer behind the
 * navigation host so screen transitions animate only screen content over it.
 */
@Composable
fun WallpaperBackdrop(modifier: Modifier = Modifier) {
    val tokens = messengerTokens
    Box(modifier = modifier.fillMaxSize()) {
        if (tokens.isDark) {
            Image(
                painter = painterResource(R.drawable.wallpaper_doodles),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.05f),
                                Color.Black.copy(alpha = 0.32f),
                            ),
                        ),
                    ),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(tokens.backgroundTop, tokens.backgroundBottom),
                        ),
                    ),
            )
        }
    }
}

/**
 * Wallpaper with content on top, for surfaces rendered outside the main
 * navigation host (overlays, bubble activity, previews) that must be opaque.
 */
@Composable
fun WallpaperBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        WallpaperBackdrop()
        content()
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WallpaperBackgroundPreview() {
    MessengerTheme {
        WallpaperBackground {
            Text(
                "Wallpaper",
                modifier = Modifier.padding(24.dp),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
