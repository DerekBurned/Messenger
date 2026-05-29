package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.UserPresence
import com.example.messenger.presentation.components.PresenceIndicator
import com.example.messenger.presentation.screens.ui.theme.LightGray
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.state.ConversationsUiState
import com.example.messenger.presentation.viewmodel.ConversationsViewModel
import com.example.messenger.util.DateUtils

enum class MainTab { CHATS, CALLS, CONTACTS, SETTINGS }

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
        onRefresh = viewModel::refresh,
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
            MainTopBar(
                title = selectedTab.title(),
                onLogoutClick = onLogoutClick,
                onSearchClick = onSearchClick,
                showSearch = selectedTab != MainTab.SETTINGS,
            )
        },
        bottomBar = {
            MainBottomBar(
                selected = selectedTab,
                onTabSelected = { tab ->
                    if (tab == MainTab.SETTINGS) onSettingsClick() else selectedTab = tab
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
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White),
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

private fun MainTab.title(): String = when (this) {
    MainTab.CHATS -> "Chats"
    MainTab.CALLS -> "Calls"
    MainTab.CONTACTS -> "Contacts"
    MainTab.SETTINGS -> "Settings"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatsTabContent(
    uiState: ConversationsUiState,
    onRefresh: () -> Unit,
    onChatClick: (String, String, String) -> Unit,
) {
    when {
        uiState.isLoading -> ChatsLoading()
        uiState.error != null -> ChatsError(message = uiState.error.asString(), onRetry = onRefresh)
        uiState.conversations.isEmpty() -> ChatsEmpty()
        else -> ChatsList(uiState = uiState, onRefresh = onRefresh, onChatClick = onChatClick)
    }
}

@Composable
private fun ChatsLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PrimaryBlue)
    }
}

@Composable
private fun ChatsError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = message, color = Color.Red)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
            Text("Retry")
        }
    }
}

@Composable
private fun ChatsEmpty() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No conversations yet", color = Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatsList(
    uiState: ConversationsUiState,
    onRefresh: () -> Unit,
    onChatClick: (String, String, String) -> Unit,
) {
    val currentUserId = uiState.currentUserId
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(Modifier.fillMaxSize()) {
            items(uiState.conversations) { conversation ->
                val otherIdx = conversation.participantIds.indexOfFirst { it != currentUserId }
                val otherUserId = conversation.participantIds.getOrNull(otherIdx) ?: ""
                val otherUserName = conversation.participantNames.getOrNull(otherIdx)
                    ?.takeIf { it.isNotBlank() }
                    ?: "Unknown"
                ConversationListItem(
                    conversation = conversation,
                    displayName = otherUserName,
                    presence = uiState.presenceMap[otherUserId],
                    onClick = { onChatClick(conversation.id, otherUserId, otherUserName) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    title: String,
    onLogoutClick: () -> Unit,
    onSearchClick: () -> Unit,
    showSearch: Boolean,
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.shadow(elevation = 4.dp),
        title = {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        navigationIcon = {
            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
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
        ),
    )
}

@Composable
private fun MainBottomBar(
    selected: MainTab,
    onTabSelected: (MainTab) -> Unit,
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
        MainBottomBarItem(
            tab = MainTab.CHATS,
            icon = Icons.Filled.Email,
            label = "Chats",
            selected = selected == MainTab.CHATS,
            onClick = onTabSelected,
            colors = itemColors,
        )
        MainBottomBarItem(
            tab = MainTab.CALLS,
            icon = Icons.Filled.Call,
            label = "Calls",
            selected = selected == MainTab.CALLS,
            onClick = onTabSelected,
            colors = itemColors,
        )
        MainBottomBarItem(
            tab = MainTab.CONTACTS,
            icon = Icons.Filled.Person,
            label = "Contacts",
            selected = selected == MainTab.CONTACTS,
            onClick = onTabSelected,
            colors = itemColors,
        )
        MainBottomBarItem(
            tab = MainTab.SETTINGS,
            icon = Icons.Filled.Settings,
            label = "Settings",
            selected = selected == MainTab.SETTINGS,
            onClick = onTabSelected,
            colors = itemColors,
        )
    }
}

@Composable
private fun RowScope.MainBottomBarItem(
    tab: MainTab,
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: (MainTab) -> Unit,
    colors: NavigationBarItemColors,
) {
    NavigationBarItem(
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, fontWeight = FontWeight.Medium) },
        selected = selected,
        onClick = { onClick(tab) },
        colors = colors,
    )
}

@Composable
private fun ConversationListItem(
    conversation: Conversation,
    displayName: String,
    presence: UserPresence?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ConversationAvatar(displayName = displayName, presence = presence)
        Spacer(modifier = Modifier.width(14.dp))
        ConversationPreview(
            displayName = displayName,
            lastMessage = conversation.lastMessage,
            hasUnread = conversation.unreadCount > 0,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        ConversationMeta(
            timestamp = conversation.lastMessageTimestamp,
            unreadCount = conversation.unreadCount,
        )
    }
    HorizontalDivider(
        color = LightGray.copy(alpha = 0.5f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(start = 82.dp),
    )
}

@Composable
private fun ConversationAvatar(displayName: String, presence: UserPresence?) {
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(LightGray),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = displayName.take(1).uppercase().ifBlank { "?" },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = PrimaryBlue,
            )
        }
        if (presence != null) {
            PresenceIndicator(state = presence.state, size = 14.dp)
        }
    }
}

@Composable
private fun ConversationPreview(
    displayName: String,
    lastMessage: String?,
    hasUnread: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = displayName,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color(0xFF1F2937),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = lastMessage ?: "No messages yet",
            color = if (hasUnread) Color(0xFF374151) else Color.Gray,
            fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ConversationMeta(timestamp: Long, unreadCount: Int) {
    val hasUnread = unreadCount > 0
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.height(40.dp),
    ) {
        Text(
            text = DateUtils.formatMessageTime(timestamp),
            color = if (hasUnread) PrimaryBlue else Color.Gray,
            fontSize = 12.sp,
            fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
        )
        if (hasUnread) {
            UnreadCountBadge(count = unreadCount)
        } else {
            Spacer(modifier = Modifier.height(22.dp))
        }
    }
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
