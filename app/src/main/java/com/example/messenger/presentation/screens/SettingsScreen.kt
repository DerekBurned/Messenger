package com.example.messenger.presentation.screens

import android.text.Layout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.domain.model.PresenceState
import com.example.messenger.domain.model.User
import com.example.messenger.presentation.components.PresenceIndicator
import com.example.messenger.presentation.screens.ui.theme.DangerRed
import com.example.messenger.presentation.screens.ui.theme.LightGray
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.OnSurface
import com.example.messenger.presentation.screens.ui.theme.OnSurfaceMuted
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.state.ProfileUiState
import com.example.messenger.presentation.viewmodel.ProfileViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ProfileViewModel = hiltViewModel<ProfileViewModel>(),
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
){
val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreenContent(
        uiState = uiState,
        onProfileClick = onProfileClick,
        onLogoutClick = onLogoutClick
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    uiState: State<ProfileUiState>,
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},

    ) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(elevation = 4.dp),
                title = {
                    Text(
                        text = "Settings",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PrimaryBlue,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            ProfileBaseInfo(uiState.value.user)
            SettingsRow(Icons.Filled.Person, "Profile", onClick = onProfileClick)
            SettingsRow(Icons.Filled.Notifications, "Notifications", onClick = {})
            SettingsRow(Icons.Filled.Lock, "Privacy", onClick = {})
            SettingsRow(Icons.Filled.Info, "About", onClick = {})



            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogoutClick() }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(DangerRed.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    "Logout",
                    color = DangerRed,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(PrimaryBlue.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PrimaryBlue,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = OnSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = OnSurfaceMuted,
        )
    }
    HorizontalDivider(
        color = LightGray.copy(alpha = 0.5f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(start = 76.dp),
    )
}

@Composable
fun ProfileBaseInfo(user: User?){
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(LightGray, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val initial = user?.username?.take(1)?.uppercase().orEmpty()
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

    Row(){Text(
        text = user?.username ?: "User Name",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = OnSurface,
    )
    }

    val subtitle = user?.email
        ?:user?.phoneNumber?.getFullNumber()
        ?: ""

        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically){
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = OnSurfaceMuted,
            )
            if(subtitle.isNotBlank()){Text(text = " ⋅ ")}
            Text(
                text = "@${user?.username ?: "User Name"}",
                fontSize = 14.sp,
                color = OnSurfaceMuted,
            )
        }

}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    MessengerTheme {
        SettingsScreenContent(
            uiState = androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(ProfileUiState(
                user = com.example.messenger.domain.model.User(
                    id = "preview-uid",
                    username = "Jane Doe",
                    email = "jane@example.com",
                ),
            )) }
        )
    }
}
