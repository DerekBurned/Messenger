package com.example.messenger.presentation.screens.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.presentation.components.common.RoundedCard
import com.example.messenger.presentation.components.common.ToggleRow
import com.example.messenger.presentation.screens.ui.theme.ThemeMode
import com.example.messenger.presentation.viewmodel.AppearanceViewModel

@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    viewModel: AppearanceViewModel = hiltViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
    }

    SettingsSubScaffold(title = "Appearance", onBack = onBack) {
        RoundedCard {
            ToggleRow(
                title = "Dark theme",
                checked = isDark,
                onCheckedChange = { viewModel.setDarkTheme(it) },
            )
        }
    }
}
