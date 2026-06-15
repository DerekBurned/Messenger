package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.presentation.components.CallAwareTopBar
import com.example.messenger.domain.model.PresenceState
import com.example.messenger.presentation.components.PresenceIndicator
import com.example.messenger.presentation.screens.ui.theme.DangerRed
import com.example.messenger.presentation.screens.ui.theme.LightGray
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.OnSurface
import com.example.messenger.presentation.screens.ui.theme.OnSurfaceMuted
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
        onLogoutClick = {
            viewModel.logout()
            onLogoutClick()
        },
        onStartEditing = onStartEditing,
        onCancelEditing = { viewModel.cancelEditing() },
        onSaveProfile = { editedUsername -> viewModel.updateProfile(editedUsername) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreenContent(
    uiState: com.example.messenger.presentation.state.ProfileUiState,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onSaveProfile: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            CallAwareTopBar {
                CenterAlignedTopAppBar(
                    modifier = Modifier.shadow(elevation = 4.dp),
                    title = {
                        Text(
                            text = "Profile",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue),
                    actions = {
                        IconButton(onClick = onStartEditing) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White,
                            )
                        }
                    },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(LightGray, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    val initial = uiState.user?.username?.take(1)?.uppercase().orEmpty()
                    if (initial.isNotBlank()) {
                        Text(
                            text = initial,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(64.dp),
                            tint = PrimaryBlue,
                        )
                    }
                }
                PresenceIndicator(
                    state = PresenceState.ONLINE,
                    size = 20.dp,
                    borderWidth = 3.dp,
                    modifier = Modifier.padding(end = 6.dp, bottom = 6.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = uiState.user?.username ?: "User Name",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
            )

            val subtitle = uiState.user?.email
                ?: uiState.user?.phoneNumber?.getFullNumber()
                ?: ""
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = OnSurfaceMuted,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DangerRed),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log out", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    MessengerTheme {
        ProfileScreenContent(
            uiState = com.example.messenger.presentation.state.ProfileUiState(
                user = com.example.messenger.domain.model.User(
                    id = "preview-uid",
                    username = "Jane Doe",
                    email = "jane@example.com",
                ),
            ),
            onBackClick = {},
            onLogoutClick = {},
            onStartEditing = {},
            onCancelEditing = {},
            onSaveProfile = {},
        )
    }
}
