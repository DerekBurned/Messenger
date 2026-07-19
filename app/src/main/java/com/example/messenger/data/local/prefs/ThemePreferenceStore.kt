package com.example.messenger.data.local.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.messenger.presentation.screens.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

@Singleton
class ThemePreferenceStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val backgroundKey = stringPreferencesKey("app_background")
    private val intensityKey = floatPreferencesKey("theme_intensity")

    val themeMode: Flow<ThemeMode> = context.themeDataStore.data.map { prefs ->
        prefs[themeModeKey]
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM
    }

    val background: Flow<String> = context.themeDataStore.data.map { prefs ->
        prefs[backgroundKey] ?: "neutral"
    }

    private val liveIntensity = MutableStateFlow<Float?>(null)

    val intensity: Flow<Float> = combine(
        context.themeDataStore.data.map { prefs -> prefs[intensityKey] ?: 1f },
        liveIntensity,
    ) { stored, live -> live ?: stored }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { prefs -> prefs[themeModeKey] = mode.name }
    }

    suspend fun setBackground(key: String) {
        context.themeDataStore.edit { prefs -> prefs[backgroundKey] = key }
    }

    fun previewIntensity(value: Float) {
        liveIntensity.value = value.coerceIn(0f, 1f)
    }

    suspend fun setIntensity(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        liveIntensity.value = clamped
        context.themeDataStore.edit { prefs -> prefs[intensityKey] = clamped }
    }
}
