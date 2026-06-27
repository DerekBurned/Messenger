package com.example.messenger.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.messenger.presentation.components.common.CardDivider
import com.example.messenger.presentation.components.common.MessengerAvatar
import com.example.messenger.presentation.components.common.NavHeaderPill
import com.example.messenger.presentation.components.common.PillButton
import com.example.messenger.presentation.components.common.RoundedCard
import com.example.messenger.presentation.components.common.WallpaperBackground
import com.example.messenger.presentation.screens.ui.theme.Dimens
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.messengerTokens
import com.example.messenger.presentation.state.ProfileUiState
import com.example.messenger.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onStartEditing: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreenContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditClick = onStartEditing,
    )
}

@Composable
private fun ProfileScreenContent(
    uiState: ProfileUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    val tokens = messengerTokens
    val user = uiState.user
    WallpaperBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            NavHeaderPill(title = "Profile", onBack = onBackClick)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.screenPadding)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                MessengerAvatar(
                    name = user?.username.orEmpty(),
                    photoUrl = user?.avatarUrl,
                    size = Dimens.avatarLarge,
                )
                Spacer(modifier = Modifier.height(24.dp))
                RoundedCard {
                    ProfileDisplayRow(
                        label = "Phone number",
                        value = user?.phoneNumber?.getFullNumber().orEmpty(),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                RoundedCard {
                    ProfileDisplayRow(label = "Name", value = user?.username.orEmpty())
                    CardDivider()
                    ProfileDisplayRow(
                        label = "Username",
                        value = user?.username?.let { "@$it" }.orEmpty(),
                    )
                    CardDivider()
                    ProfileDisplayRow(label = "Email", value = user?.email.orEmpty())
                }
                Spacer(modifier = Modifier.height(28.dp))
                PillButton(text = "Edit profile", onClick = onEditClick)
            }
        }
    }
}

@Composable
private fun ProfileDisplayRow(label: String, value: String) {
    val tokens = messengerTokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            color = tokens.textMuted,
            style = MaterialTheme.typography.labelSmall,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value.ifBlank { "—" },
            color = tokens.textOnField,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    MessengerTheme {
        ProfileScreenContent(
            uiState = ProfileUiState(
                user = User(
                    id = "preview-uid",
                    username = "Jane Doe",
                    email = "jane@example.com",
                ),
            ),
            onBackClick = {},
            onEditClick = {},
        )
    }
}
