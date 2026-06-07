package com.example.messenger.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.UserPresence
import com.example.messenger.presentation.components.PresenceIndicator
import com.example.messenger.presentation.screens.ui.theme.DangerRed
import com.example.messenger.presentation.screens.ui.theme.LightGray
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.state.ConversationsUiState
import com.example.messenger.presentation.viewmodel.ConversationsViewModel
import com.example.messenger.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    viewModel: ConversationsViewModel = hiltViewModel(),
    onChatClick: (String, String, String) -> Unit = { _, _, _ -> },
    onLogoutClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MainTopBar(
                title = "Chats",
                onLogoutClick = onLogoutClick,
                onSearchClick = onSearchClick,
                showSearch = true,
            )
        },

    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White),
        ) {
            ChatsTabContent(
                uiState = uiState,
                onRefresh = viewModel::refresh,
                onChatClick = onChatClick,
                onDeleteForMe = viewModel::deleteForMe,
                onDeleteForEveryone = viewModel::deleteForEveryone,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(
    onLogoutClick: () -> Unit = {},
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MainTopBar(
                title = "Calls",
                onLogoutClick = onLogoutClick,
                onSearchClick = {},
                showSearch = false,
            )
        },
    ) { innerPadding ->
        CallsScreenContent(modifier = Modifier.padding(innerPadding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onLogoutClick: () -> Unit = {},
    onContactClick: (String) -> Unit = {},
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MainTopBar(
                title = "Contacts",
                onLogoutClick = onLogoutClick,
                onSearchClick = {},
                showSearch = false,
            )
        },
    ) { innerPadding ->
        ContactsScreenContent(
            modifier = Modifier.padding(innerPadding),
            onContactClick = { user -> onContactClick(user.id) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatsTabContent(
    uiState: ConversationsUiState,
    onRefresh: () -> Unit,
    onChatClick: (String, String, String) -> Unit,
    onDeleteForMe: (String) -> Unit,
    onDeleteForEveryone: (String) -> Unit,
) {
    when {
        uiState.isLoading -> ChatsLoading()
        uiState.error != null -> ChatsError(message = uiState.error.asString(), onRetry = onRefresh)
        uiState.conversations.isEmpty() -> ChatsEmpty()
        else -> ChatsList(
            uiState = uiState,
            onRefresh = onRefresh,
            onChatClick = onChatClick,
            onDeleteForMe = onDeleteForMe,
            onDeleteForEveryone = onDeleteForEveryone,
        )
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
    onDeleteForMe: (String) -> Unit,
    onDeleteForEveryone: (String) -> Unit,
) {
    val currentUserId = uiState.currentUserId
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            items(uiState.conversations, key = { it.id }) { conversation ->
                val otherIdx = conversation.participantIds.indexOfFirst { it != currentUserId }
                val otherUserId = conversation.participantIds.getOrNull(otherIdx) ?: ""
                val otherUserName = conversation.participantNames.getOrNull(otherIdx)
                    ?.takeIf { it.isNotBlank() }
                    ?: "Unknown"
                SwipeableConversationRow(
                    displayName = otherUserName,
                    isOneOnOne = conversation.participantIds.size == 2,
                    onClick = { onChatClick(conversation.id, otherUserId, otherUserName) },
                    onDeleteForMe = { onDeleteForMe(conversation.id) },
                    onDeleteForEveryone = { onDeleteForEveryone(conversation.id) },
                ) { rowOnClick ->
                    ConversationListItem(
                        conversation = conversation,
                        displayName = otherUserName,
                        presence = uiState.presenceMap[otherUserId],
                        onClick = rowOnClick,
                    )
                }
            }
        }
    }
}

private val SwipeActionWidth = 84.dp

@Composable
private fun SwipeableConversationRow(
    displayName: String,
    isOneOnOne: Boolean,
    onClick: () -> Unit,
    onDeleteForMe: () -> Unit,
    onDeleteForEveryone: () -> Unit,
    content: @Composable (rowOnClick: () -> Unit) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val revealPx = with(LocalDensity.current) { (SwipeActionWidth * 2).toPx() }
    val offsetX = remember { Animatable(0f) }

    val close: () -> Unit = { scope.launch { offsetX.animateTo(0f) } }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.End,
        ) {
            ConversationActionButton(
                label = "Edit",
                background = LightGray,
                contentColor = Color(0xFF374151),
            ) { menuDismiss ->
                DropdownMenu(expanded = true, onDismissRequest = menuDismiss) {
                    listOf("Rename", "Mute", "Mark as unread", "Archive").forEach { item ->
                        DropdownMenuItem(text = { Text(item) }, onClick = menuDismiss)
                    }
                }
            }
            ConversationActionButton(
                label = "Delete",
                background = DangerRed,
                contentColor = Color.White,
            ) { menuDismiss ->
                DropdownMenu(expanded = true, onDismissRequest = menuDismiss) {
                    DropdownMenuItem(
                        text = { Text("Delete for me") },
                        onClick = { menuDismiss(); close(); onDeleteForMe() },
                    )
                    if (isOneOnOne) {
                        DropdownMenuItem(
                            text = { Text("Delete for $displayName too") },
                            onClick = { menuDismiss(); close(); onDeleteForEveryone() },
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .fillMaxWidth()
                .background(Color.White)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            offsetX.snapTo((offsetX.value + delta).coerceIn(-revealPx, 0f))
                        }
                    },
                    onDragStopped = {
                        val target = if (offsetX.value < -revealPx / 2f) -revealPx else 0f
                        offsetX.animateTo(target)
                    },
                ),
        ) {
            content {
                if (offsetX.value != 0f) close() else onClick()
            }
        }
    }
}

@Composable
private fun ConversationActionButton(
    label: String,
    background: Color,
    contentColor: Color,
    menu: @Composable (dismiss: () -> Unit) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .width(SwipeActionWidth)
            .fillMaxHeight()
            .background(background)
            .clickable { menuOpen = true },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, color = contentColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        if (menuOpen) {
            menu { menuOpen = false }
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
private fun ConversationsListPreview() {
    MessengerTheme {
        ChatsTabContent(
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
            onRefresh = {},
            onChatClick = { _, _, _ -> },
            onDeleteForMe = {},
            onDeleteForEveryone = {},
        )
    }
}
