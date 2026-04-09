package com.example.messenger.presentation.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.PresenceState
import com.example.messenger.domain.model.UserPresence
import com.example.messenger.presentation.components.PresenceIndicator
import com.example.messenger.presentation.navigation.AppNavigation
import com.example.messenger.presentation.screens.ui.theme.LightGray
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.viewmodel.ConversationsViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MessengerTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithNav(
    viewModel: ConversationsViewModel = hiltViewModel(),
    onChatClick: (String, String, String) -> Unit = { _, _, _ -> },
    onLogoutClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBarContentM3(onLogoutClick = onLogoutClick) },
        bottomBar = { BottomNavBarM3(onProfileClick = onProfileClick) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryBlue
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                uiState.conversations.isEmpty() -> {
                    Text(
                        text = "No conversations yet",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.conversations) { conversation ->
                            val otherUserId = conversation.participantIds
                                .firstOrNull { it != currentUserId } ?: ""
                            val otherUserName = conversation.participantNames
                                .firstOrNull() ?: "Unknown"
                            val presence = uiState.presenceMap[otherUserId]
                            ChatListItemM3(
                                conversation = conversation,
                                presence = presence,
                                onClick = { onChatClick(conversation.id, otherUserId, otherUserName) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarContentM3(onLogoutClick: () -> Unit = {}) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Chats",
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    tint = Color.White,
                    contentDescription = "Logout"
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Handle search click */ }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = PrimaryBlue,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

@Composable
fun ChatListItemM3(
    conversation: Conversation = Conversation(),
    presence: UserPresence? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.participantNames.firstOrNull()?.take(1)?.uppercase() ?: "?",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
            if (presence != null) {
                PresenceIndicator(
                    state = presence.state,
                    size = 14.dp
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.participantNames.firstOrNull() ?: "Unknown",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = conversation.lastMessage ?: "",
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (conversation.unreadCount > 0) {
            Badge(containerColor = PrimaryBlue) {
                Text(conversation.unreadCount.toString())
            }
        }
    }
    HorizontalDivider(color = LightGray, thickness = 1.dp, modifier = Modifier.padding(start = 80.dp))
}

@Composable
fun BottomNavBarM3(onProfileClick: () -> Unit = {}) {
    var selectedItem by remember { mutableStateOf(1) }

    NavigationBar(
        containerColor = PrimaryBlue,
        contentColor = Color.White
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            selectedTextColor = Color.White,
            unselectedIconColor = Color.White.copy(alpha = 0.6f),
            unselectedTextColor = Color.White.copy(alpha = 0.6f),
            indicatorColor = PrimaryBlue
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Call, contentDescription = "Calls") },
            label = { Text("calls") },
            selected = selectedItem == 0,
            onClick = { selectedItem = 0 },
            colors = itemColors
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Email, contentDescription = "Chats") },
            label = { Text("chats") },
            selected = selectedItem == 1,
            onClick = { selectedItem = 1 },
            colors = itemColors
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("profile") },
            selected = selectedItem == 2,
            onClick = {
                selectedItem = 2
                onProfileClick()
            },
            colors = itemColors
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MessengerTheme {
        MainScreenWithNav()
    }
}
