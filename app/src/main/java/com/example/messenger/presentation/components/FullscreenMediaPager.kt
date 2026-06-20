package com.example.messenger.presentation.components

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.example.messenger.data.media.BlurHash
import kotlin.math.abs
import kotlinx.coroutines.launch

data class MediaSource(
    val key: String,
    val isVideo: Boolean,
    val model: Any,
    val playUri: Uri,
    val blurHash: String? = null,
)

@Composable
fun FullscreenMediaPager(
    sources: List<MediaSource>,
    startIndex: Int,
    onDismiss: () -> Unit,
    onPageSettled: (Int) -> Unit = {},
) {
    if (sources.isEmpty()) return
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(androidx.compose.ui.platform.LocalDensity.current) {
        configuration.screenHeightDp.dp.toPx()
    }
    val dismissThreshold = screenHeightPx * 0.25f

    val pagerState = rememberPagerState(
        initialPage = startIndex.coerceIn(0, sources.lastIndex),
        pageCount = { sources.size },
    )
    val scope = rememberCoroutineScope()
    val dragOffsetY = remember { Animatable(0f) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply { playWhenReady = true }
    }
    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "release player")
            exoPlayer.release()
        }
    }

    LaunchedEffect(pagerState.currentPage, sources) {
        onPageSettled(pagerState.currentPage)
        val current = sources.getOrNull(pagerState.currentPage)
        if (current != null && current.isVideo) {
            Log.d(TAG, "bind video page=${pagerState.currentPage}")
            exoPlayer.setMediaItem(Media3Item.fromUri(current.playUri))
            exoPlayer.prepare()
            exoPlayer.play()
        } else {
            exoPlayer.pause()
            exoPlayer.clearMediaItems()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        ApplyBlurBehind()
        val dragProgress = (abs(dragOffsetY.value) / screenHeightPx).coerceIn(0f, 1f)
        val scrimAlpha = (1f - dragProgress) * 0.92f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha)),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val source = sources[page]
                val isCurrent = page == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (isCurrent) {
                                translationY = dragOffsetY.value
                                val scale = (1f - dragProgress * 0.3f).coerceIn(0.7f, 1f)
                                scaleX = scale
                                scaleY = scale
                            }
                        }
                        .pointerInput(page) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    if (abs(dragOffsetY.value) > dismissThreshold) {
                                        Log.d(TAG, "dismiss via drag")
                                        onDismiss()
                                    } else {
                                        scope.launch { dragOffsetY.animateTo(0f) }
                                    }
                                },
                                onVerticalDrag = { _, dragAmount ->
                                    scope.launch { dragOffsetY.snapTo(dragOffsetY.value + dragAmount) }
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (source.isVideo && isCurrent) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = true
                                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        val placeholder = remember(source.blurHash) {
                            source.blurHash?.let { BlurHash.decode(it, 32, 32) }
                                ?.let { BitmapPainter(it.asImageBitmap()) }
                        }
                        AsyncImage(
                            model = source.model,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            placeholder = placeholder,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

@Composable
private fun ApplyBlurBehind() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.parent as? DialogWindowProvider)?.window
        if (window != null) {
            window.setBackgroundDrawable(ColorDrawable(0x00000000))
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.attributes = window.attributes.apply { dimAmount = 0.55f }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window.attributes = window.attributes.apply { blurBehindRadius = 48 }
            }
        }
        onDispose { }
    }
}

private const val TAG = "FullscreenMediaPager"
