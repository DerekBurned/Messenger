package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.presentation.components.call.CallAwareTopBar
import com.example.messenger.presentation.components.profile.AvatarPhotoViewer
import com.example.messenger.presentation.components.profile.MediaTabsRow
import com.example.messenger.presentation.components.profile.ProfileHeader
import com.example.messenger.presentation.components.profile.ProfileInfoCard
import com.example.messenger.presentation.components.profile.ProfileMediaGrid
import com.example.messenger.presentation.components.common.WallpaperBackground
import com.example.messenger.domain.model.PhoneNumber
import com.example.messenger.domain.model.User
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.messengerTokens
import com.example.messenger.presentation.state.ChatUserProfileUiState
import com.example.messenger.presentation.state.MediaTab
import com.example.messenger.presentation.viewmodel.ChatUserProfileViewModel

@Composable
fun ChatUserProfileScreen(
    viewModel: ChatUserProfileViewModel = hiltViewModel(),
    sharedKeyUserId: String = "",
    onBackClick: () -> Unit = {},
    onCallClick: (partnerId: String, partnerName: String, partnerPhone: String) -> Unit = { _, _, _ -> },
    onEditClick: () -> Unit = {}
    ) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ChatUserProfileScreenContent(
        state = state,
        sharedKeyUserId = sharedKeyUserId,
        onBackClick = onBackClick,
        onCallClick = onCallClick,
        onEditClick = onEditClick,
        onMediaTabChange = viewModel::onMediaTabChange,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatUserProfileScreenContent(
    state: ChatUserProfileUiState,
    sharedKeyUserId: String = "",
    onBackClick: () -> Unit = {},
    onCallClick: (partnerId: String, partnerName: String, partnerPhone: String) -> Unit = { _, _, _ -> },
    onMediaTabChange: (MediaTab) -> Unit = {},
    onEditClick: () -> Unit = {},
) {
    val tokens = messengerTokens
    var showViewer by remember { mutableStateOf(false) }
    val viewerPhotos = state.avatarPhotos.ifEmpty {
        listOfNotNull(state.user?.avatarUrl).filter { it.isNotBlank() }
    }
    val chipFill = if (tokens.isDark) Color.Black.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.06f)
    WallpaperBackground {
    Scaffold(
        topBar = {
            CallAwareTopBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 10.dp, top = 12.dp, bottom = 8.dp, end = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(chipFill)
                            .clickable(onClick = onBackClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = tokens.textPrimary,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(chipFill)
                            .clickable(onClick = onEditClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = tokens.textPrimary,
                        )
                    }
                }
            }
        },
        containerColor = Color.Transparent,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item {
                ProfileHeader(
                    state = state,
                    sharedKeyUserId = sharedKeyUserId,
                    onAvatarClick = { if (viewerPhotos.isNotEmpty()) showViewer = true },
                    onCallClick = onCallClick,
                )
            }
            item { ProfileInfoCard(state) }
            item { MediaTabsRow(state.mediaTab, onMediaTabChange) }
            item { ProfileMediaGrid() }
        }
    }
    }
    if (showViewer && viewerPhotos.isNotEmpty()) {
        AvatarPhotoViewer(
            photos = viewerPhotos,
            onDismiss = { showViewer = false },
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatUserProfileScreenPreview() {
    MessengerTheme {
        ChatUserProfileScreenContent(
            state = ChatUserProfileUiState(
                user = User(
                    id = "u1",
                    username = "Alice",
                    email = "alice@example.com",
                    phoneNumber = PhoneNumber(countryCode = "+1", number = "5550100"),
                ),
                isOnline = true,
            ),
            onCallClick = { _, _, _ -> },
        )
    }
}
