package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.domain.model.PhoneNumber
import com.example.messenger.domain.model.User
import com.example.messenger.presentation.screens.ui.theme.LightGray
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.state.ChatUserProfileUiState
import com.example.messenger.presentation.state.MediaTab
import com.example.messenger.presentation.viewmodel.ChatUserProfileViewModel

@Composable
fun ChatUserProfileScreen(
    viewModel: ChatUserProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onCallClick: (partnerId: String, partnerName: String, partnerPhone: String) -> Unit = { _, _, _ -> },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ChatUserProfileScreenContent(
        state = state,
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        onCallClick = onCallClick,
        onMediaTabChange = viewModel::onMediaTabChange,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatUserProfileScreenContent(
    state: ChatUserProfileUiState,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onCallClick: (partnerId: String, partnerName: String, partnerPhone: String) -> Unit = { _, _, _ -> },
    onMediaTabChange: (MediaTab) -> Unit = {},
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = PrimaryBlue),
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            item { ProfileHeader(state, onCallClick) }
            item { ProfileInfoCard(state) }
            item { MediaTabsRow(state.mediaTab, onMediaTabChange) }
            item { MediaGrid() }
        }
    }
}

@Composable
private fun ProfileHeader(
    state: ChatUserProfileUiState,
    onCallClick: (partnerId: String, partnerName: String, partnerPhone: String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(96.dp).clip(CircleShape).background(LightGray),
            contentAlignment = Alignment.Center,
        ) {
            val initial = state.user?.username?.take(1)?.uppercase().orEmpty()
            if (initial.isNotBlank()) {
                Text(initial, color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 36.sp)
            } else {
                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(48.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(state.user?.username ?: "Unknown", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        Text(
            text = if (state.isOnline) "online" else state.lastSeen.ifBlank { "offline" },
            color = Color.Gray,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                val user = state.user ?: return@Button
                onCallClick(
                    user.id,
                    user.username.orEmpty(),
                    user.phoneNumber?.getFullNumber().orEmpty(),
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(50),
        ) {
            Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Call", fontSize = 14.sp)
        }
    }
}

@Composable
private fun ProfileInfoCard(state: ChatUserProfileUiState) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PrimaryBlue),
    ) {
        ContactInfoRow("Phone", state.user?.phoneNumber?.getFullNumber().orEmpty().ifBlank { "—" }, divider = true)
        ContactInfoRow("Username", "@${state.user?.username.orEmpty()}", divider = true)
        ContactInfoRow("Email", state.user?.email.orEmpty().ifBlank { "—" }, divider = false)
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun ContactInfoRow(label: String, value: String, divider: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(label, color = Color.White.copy(0.7f), fontSize = 11.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = Color.White, fontSize = 14.sp)
    }
    if (divider) HorizontalDivider(color = Color.White.copy(0.15f), thickness = 0.5.dp)
}

@Composable
private fun MediaTabsRow(active: MediaTab, onChange: (MediaTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MediaTab.values().forEach { tab ->
            val isActive = tab == active
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isActive) PrimaryBlue else LightGray)
                    .clickable { onChange(tab) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.name.lowercase(),
                    color = if (isActive) Color.White else Color.Gray,
                    fontSize = 12.sp,
                )
            }
        }
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun MediaGrid() {
    val placeholderTints = listOf(
        Color(0xFFA8D8EA), Color(0xFFB8E0D2), Color(0xFFFFD3B6),
        Color(0xFFD6E4F0), Color(0xFFF7D6E0), Color(0xFFE8D5C4),
    )
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        placeholderTints.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { color ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                    )
                }
                repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
    Spacer(Modifier.height(24.dp))
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
