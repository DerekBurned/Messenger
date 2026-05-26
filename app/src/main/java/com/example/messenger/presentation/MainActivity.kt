package com.example.messenger.presentation

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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.util.DateUtils
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.UserPresence
import com.example.messenger.presentation.components.PresenceIndicator
import com.example.messenger.presentation.navigation.AppNavigation
import com.example.messenger.presentation.screens.CallsScreenContent
import com.example.messenger.presentation.screens.ContactsScreenContent
import com.example.messenger.presentation.screens.ui.theme.LightGray
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.state.ConversationsUiState
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

enum class MainTab { CHATS, CALLS, CONTACTS, SETTINGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithNav(
    viewModel: ConversationsViewModel = hiltViewModel(),
    onChatClick: (String, String, String) -> Unit = { _, _, _ -> },
    onLogoutClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MainScreenContent(
        uiState = uiState,
        onChatClick = onChatClick,
        onRefresh = { viewModel.refresh() },
        onSearchClick = onSearchClick,
        onLogoutClick = onLogoutClick,
        onProfileClick = onProfileClick,
        onSettingsClick = onSettingsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenContent(
    uiState: ConversationsUiState,
    onChatClick: (String, String, String) -> Unit,
    onChatHold: (String) -> Unit = {},
    onRefresh: () -> Unit,
    onSearchClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(MainTab.CHATS) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBarContentM3(
                title = when (selectedTab) {
                    MainTab.CHATS -> "Chats"
                    MainTab.CALLS -> "Calls"
                    MainTab.CONTACTS -> "Contacts"
                    MainTab.SETTINGS -> "Settings"
                },
                onLogoutClick = onLogoutClick,
                onSearchClick = onSearchClick,
                showSearch = selectedTab != MainTab.SETTINGS,
            )
        },
        bottomBar = {
            BottomNavBarM3(
                selected = selectedTab,
                onTabSelected = { tab ->
                    if (tab == MainTab.SETTINGS) {
                        onSettingsClick()
                    } else {
                        selectedTab = tab
                    }
                },
            )
        },
        floatingActionButton = {
            if (selectedTab == MainTab.CHATS) {
                FloatingActionButton(
                    onClick = onSearchClick,
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Chat")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            when (selectedTab) {
                MainTab.CHATS -> ChatsTabContent(
                    uiState = uiState,
                    onRefresh = onRefresh,
                    onChatClick = onChatClick,
                )
                MainTab.CALLS -> CallsScreenContent()
                MainTab.CONTACTS -> ContactsScreenContent(onContactClick = { /* TODO navigate to user profile */ })
                MainTab.SETTINGS -> Unit 
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatsTabContent(
    uiState: ConversationsUiState,
    onRefresh: () -> Unit,
    onChatClick: (String, String, String) -> Unit,
) {
    when {
        uiState.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }
        uiState.error != null -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = uiState.error!!, color = Color.Red)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onRefresh, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                    Text("Retry")
                }
            }
        }
        uiState.conversations.isEmpty() -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No conversations yet", color = Color.Gray)
            }
        }
        else -> {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(uiState.conversations) { conversation ->
                        val otherIdx = conversation.participantIds
                            .indexOfFirst { it != currentUserId }
                        val otherUserId = conversation.participantIds
                            .getOrNull(otherIdx) ?: ""
                        val otherUserName = conversation.participantNames
                            .getOrNull(otherIdx)
                            ?.takeIf { it.isNotBlank() }
                            ?: "Unknown"
                        val presence = uiState.presenceMap[otherUserId]
                        ChatListItemM3(
                            conversation = conversation,
                            displayName = otherUserName,
                            presence = presence,
                            onClick = { onChatClick(conversation.id, otherUserId, otherUserName) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarContentM3(
    title: String = "Chats",
    onLogoutClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    showSearch: Boolean = true,
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.shadow(elevation = 4.dp),
        title = {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        navigationIcon = {
            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    tint = Color.White,
                    contentDescription = "Logout",
                )
            }
        },
        actions = {
            if (showSearch) {
                IconButton(onClick = onSearchClick) {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = PrimaryBlue,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White,
        )
    )
}

@Composable
fun ChatListItemM3(
    conversation: Conversation = Conversation(),
    displayName: String = conversation.participantNames.firstOrNull() ?: "Unknown",
    presence: UserPresence? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.take(1).uppercase().ifBlank { "?" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
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
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF1F2937),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = conversation.lastMessage ?: "No messages yet",
                color = if (conversation.unreadCount > 0) Color(0xFF374151) else Color.Gray,
                fontWeight = if (conversation.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.height(40.dp),
        ) {
            Text(
                text = DateUtils.formatMessageTime(conversation.lastMessageTimestamp),
                color = if (conversation.unreadCount > 0) PrimaryBlue else Color.Gray,
                fontSize = 12.sp,
                fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
            )
            if (conversation.unreadCount > 0) {
                UnreadCountBadge(count = conversation.unreadCount)
            } else {
                Spacer(modifier = Modifier.height(22.dp))
            }
        }
    }
    HorizontalDivider(
        color = LightGray.copy(alpha = 0.5f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(start = 82.dp)
    )
}

@Composable
private fun UnreadCountBadge(count: Int) {
    val label = if (count > 99) "99+" else count.toString()
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
            .clip(CircleShape)
            .background(PrimaryBlue)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun BottomNavBarM3(
    selected: MainTab = MainTab.CHATS,
    onTabSelected: (MainTab) -> Unit = {},
) {
    NavigationBar(
        containerColor = PrimaryBlue,
        contentColor = Color.White,
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            selectedTextColor = Color.White,
            unselectedIconColor = Color.White.copy(alpha = 0.6f),
            unselectedTextColor = Color.White.copy(alpha = 0.6f),
            indicatorColor = PrimaryBlue,
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Email, contentDescription = "Chats") },
            label = { Text("Chats", fontWeight = FontWeight.Medium) },
            selected = selected == MainTab.CHATS,
            onClick = { onTabSelected(MainTab.CHATS) },
            colors = itemColors,
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Call, contentDescription = "Calls") },
            label = { Text("Calls", fontWeight = FontWeight.Medium) },
            selected = selected == MainTab.CALLS,
            onClick = { onTabSelected(MainTab.CALLS) },
            colors = itemColors,
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Contacts") },
            label = { Text("Contacts", fontWeight = FontWeight.Medium) },
            selected = selected == MainTab.CONTACTS,
            onClick = { onTabSelected(MainTab.CONTACTS) },
            colors = itemColors,
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontWeight = FontWeight.Medium) },
            selected = selected == MainTab.SETTINGS,
            onClick = { onTabSelected(MainTab.SETTINGS) },
            colors = itemColors,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenPreview() {
    MessengerTheme {
        MainScreenContent(
            uiState = ConversationsUiState(
                conversations = listOf(
                    Conversation(
                        id = "conv1",
                        participantIds = listOf("uid1", "uid2"),
                        participantNames = listOf("Alice"),
                        lastMessage = "Hey, how are you?",
                        lastMessageTimestamp = 1_700_000_000_000L,
                        unreadCount = 2,
                    ),
                    Conversation(
                        id = "conv2",
                        participantIds = listOf("uid1", "uid3"),
                        participantNames = listOf("Bob"),
                        lastMessage = "Let's catch up soon!",
                        lastMessageTimestamp = 1_700_000_060_000L,
                        unreadCount = 0,
                    ),
                ),
                isLoading = false,
            ),
            onChatClick = { _, _, _ -> },
            onRefresh = {},
            onSearchClick = {},
            onLogoutClick = {},
            onProfileClick = {},
            onSettingsClick = {},
        )
    }
}
