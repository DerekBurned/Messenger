package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.data.local.prefs.ThemePreferenceStore
import com.example.messenger.presentation.screens.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val themePreferenceStore: ThemePreferenceStore,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themePreferenceStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            themePreferenceStore.setThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
        }
    }
}
