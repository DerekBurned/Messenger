package com.example.messenger.presentation.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.messenger.presentation.components.common.RoundedCard
import com.example.messenger.presentation.components.common.SectionHeader
import com.example.messenger.presentation.components.common.ToggleRow
import com.example.messenger.presentation.screens.ui.theme.AppBackgrounds
import com.example.messenger.presentation.screens.ui.theme.CUSTOM_BACKGROUND_PREFIX
import com.example.messenger.presentation.screens.ui.theme.ThemeMode
import com.example.messenger.presentation.screens.ui.theme.customWallpaperFile
import com.example.messenger.presentation.screens.ui.theme.messengerTokens
import com.example.messenger.presentation.viewmodel.AppearanceViewModel
import kotlin.math.roundToInt

@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    viewModel: AppearanceViewModel = hiltViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val background by viewModel.background.collectAsStateWithLifecycle()
    val intensity by viewModel.intensity.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
    }
    val pickWallpaper = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.importWallpaper(uri)
    }

    SettingsSubScaffold(title = "Appearance", onBack = onBack) {
        RoundedCard {
            ToggleRow(
                title = "Dark theme",
                checked = isDark,
                onCheckedChange = { viewModel.setDarkTheme(it) },
            )
        }
        SectionHeader(text = "Background")
        BackgroundPicker(
            selectedKey = background,
            isDark = isDark,
            onSelect = { viewModel.setBackground(it) },
            onImport = {
                pickWallpaper.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
        )
        SectionHeader(text = "Intensity")
        IntensityCard(
            intensity = intensity,
            onPreview = { viewModel.previewIntensity(it) },
            onCommit = { viewModel.commitIntensity(it) },
        )
    }
}

@Composable
private fun IntensityCard(
    intensity: Float,
    onPreview: (Float) -> Unit,
    onCommit: (Float) -> Unit,
) {
    val tokens = messengerTokens
    var dragging by remember { mutableStateOf(false) }
    var localValue by remember { mutableStateOf(intensity) }
    if (!dragging) localValue = intensity
    RoundedCard {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Background intensity",
                    color = tokens.textPrimary,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${(localValue * 100).roundToInt()}%",
                    color = tokens.textMuted,
                    fontSize = 13.sp,
                )
            }
            Slider(
                value = localValue,
                onValueChange = {
                    dragging = true
                    localValue = it
                    onPreview(it)
                },
                onValueChangeFinished = {
                    onCommit(localValue)
                    dragging = false
                },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = tokens.accent,
                    activeTrackColor = tokens.accent,
                    inactiveTrackColor = tokens.pillFill,
                ),
            )
            Text(
                text = "Lower values soften the wallpaper toward a flat background",
                color = tokens.textMuted,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun BackgroundPicker(
    selectedKey: String,
    isDark: Boolean,
    onSelect: (String) -> Unit,
    onImport: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppBackgrounds.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { preset ->
                    BackgroundTile(
                        selected = preset.key == selectedKey,
                        label = preset.label,
                        onClick = { onSelect(preset.key) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        if (isDark) preset.darkColors else preset.lightColors,
                                    ),
                                ),
                        )
                    }
                }
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val tokens = messengerTokens
            val context = LocalContext.current
            val customSelected = selectedKey.startsWith(CUSTOM_BACKGROUND_PREFIX)
            val customFile = remember(selectedKey) {
                customWallpaperFile(context, selectedKey)?.takeIf { it.exists() }
            }
            BackgroundTile(
                selected = customSelected,
                label = "From device",
                onClick = onImport,
                modifier = Modifier.weight(1f),
            ) {
                if (customFile != null) {
                    AsyncImage(
                        model = customFile,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(tokens.pillFill),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddPhotoAlternate,
                            contentDescription = "Import wallpaper",
                            tint = tokens.textMuted,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
            }
            repeat(2) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BackgroundTile(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    preview: @Composable () -> Unit,
) {
    val tokens = messengerTokens
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 2.5.dp,
                    color = if (selected) tokens.accent else tokens.panelBorder,
                    shape = RoundedCornerShape(16.dp),
                )
                .clickable { onClick() },
        ) {
            preview()
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(tokens.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = tokens.textPrimary,
            fontSize = 13.sp,
        )
    }
}
