package com.example.messenger.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.domain.model.User
import com.example.messenger.presentation.components.call.LocalCallBarInset
import com.example.messenger.presentation.components.common.CardDivider
import com.example.messenger.presentation.components.common.MessengerAvatar
import com.example.messenger.presentation.components.common.PillButton
import com.example.messenger.presentation.components.common.PillButtonStyle
import com.example.messenger.presentation.components.common.RoundedCard
import com.example.messenger.presentation.components.common.SettingsRow
import com.example.messenger.presentation.components.common.WallpaperBackground
import com.example.messenger.presentation.screens.ui.theme.Dimens
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.messengerTokens
import com.example.messenger.presentation.state.ProfileUiState
import com.example.messenger.presentation.viewmodel.ProfileViewModel

@Composable
fun SettingsScreen(
    viewModel: ProfileViewModel = hiltViewModel<ProfileViewModel>(),
    onProfileClick: () -> Unit = {},
    onSwitchAccountClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onAppearanceClick: () -> Unit = {},
    onSecurityClick: () -> Unit = {},
    onDataStorageClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreenContent(
        user = uiState.user,
        photoUrl = uiState.photos.firstOrNull() ?: uiState.user?.avatarUrl,
        onProfileClick = onProfileClick,
        onSwitchAccountClick = onSwitchAccountClick,
        onPrivacyClick = onPrivacyClick,
        onNotificationsClick = onNotificationsClick,
        onAppearanceClick = onAppearanceClick,
        onSecurityClick = onSecurityClick,
        onDataStorageClick = onDataStorageClick,
        onLanguageClick = onLanguageClick,
        onLogoutClick = onLogoutClick,
    )
}

@Composable
fun SettingsScreenContent(
    user: User?,
    photoUrl: String? = null,
    onProfileClick: () -> Unit = {},
    onSwitchAccountClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onAppearanceClick: () -> Unit = {},
    onSecurityClick: () -> Unit = {},
    onDataStorageClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    val tokens = messengerTokens
    WallpaperBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenPadding)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(72.dp + LocalCallBarInset.current))
            MessengerAvatar(name = user?.username.orEmpty(),
                photoUrl = photoUrl,
                size = Dimens.avatarLarge)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = user?.username ?: "User",
                color = tokens.textPrimary,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "@${user?.username ?: "user"}",
                color = tokens.textPrimary.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(22.dp))
            RoundedCard {
                SettingsRow(title = "My profile", onClick = onProfileClick)
                CardDivider()
                SettingsRow(title = "Switch account", onClick = onSwitchAccountClick)
                CardDivider()
                SettingsRow(title = "Privacy", onClick = onPrivacyClick)
                CardDivider()
                SettingsRow(title = "Notifications & Sounds", onClick = onNotificationsClick)
                CardDivider()
                SettingsRow(title = "Appearance", onClick = onAppearanceClick)
                CardDivider()
                SettingsRow(title = "Security", onClick = onSecurityClick)
                CardDivider()
                SettingsRow(title = "Data & Storage", onClick = onDataStorageClick)
                CardDivider()
                SettingsRow(title = "Language", onClick = onLanguageClick)
            }
            Spacer(modifier = Modifier.height(24.dp))
            PillButton(
                text = "Log out",
                onClick = onLogoutClick,
                style = PillButtonStyle.Danger,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    MessengerTheme {
        SettingsScreenContent(
            user = User(
                id = "preview-uid",
                username = "Jane Doe",
                email = "jane@example.com",
            ),
        )
    }
}
