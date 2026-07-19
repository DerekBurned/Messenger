package com.example.messenger.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.data.local.prefs.ThemePreferenceStore
import com.example.messenger.presentation.screens.ui.theme.CUSTOM_BACKGROUND_PREFIX
import com.example.messenger.presentation.screens.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themePreferenceStore: ThemePreferenceStore,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themePreferenceStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val background: StateFlow<String> = themePreferenceStore.background
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "neutral")

    val intensity: StateFlow<Float> = themePreferenceStore.intensity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1f)

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            themePreferenceStore.setThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
        }
    }

    fun setBackground(key: String) {
        viewModelScope.launch {
            themePreferenceStore.setBackground(key)
        }
    }

    fun previewIntensity(value: Float) {
        themePreferenceStore.previewIntensity(value)
    }

    fun commitIntensity(value: Float) {
        viewModelScope.launch {
            themePreferenceStore.setIntensity(value)
        }
    }

    fun importWallpaper(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val dir = File(context.filesDir, "wallpapers").apply { mkdirs() }
                val timestamp = System.currentTimeMillis()
                val file = File(dir, "wallpaper_$timestamp.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                } ?: return@runCatching
                dir.listFiles()
                    ?.filter { it.name != file.name }
                    ?.forEach { it.delete() }
                themePreferenceStore.setBackground("$CUSTOM_BACKGROUND_PREFIX$timestamp")
            }
        }
    }
}
