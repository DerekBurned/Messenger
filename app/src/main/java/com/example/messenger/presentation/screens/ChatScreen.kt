package com.example.messenger.presentation.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.messenger.data.Chat
import com.example.messenger.data.Contact
import com.example.messenger.data.ContactStatus
import com.example.messenger.presentation.components.Avatar
import com.example.messenger.presentation.components.BottomNav
import com.example.messenger.presentation.screens.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * data-figma-name: chats
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    chats: List<Chat>,
    contacts: List<Contact>,
    totalUnread: Int,
    onChatClick: (chatId: String) -> Unit,
    onEditClick: () -> Unit,
    navController: NavController
) {
    var searchOpen  by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredChats = if (searchQuery.isNotBlank()) {
        chats.filter { chat ->
            contacts.find { it.id == chat.contactId }
                ?.name?.contains(searchQuery, ignoreCase = true) == true
        }
    } else chats

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (searchOpen) {
                        TextField(
                            value         = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder   = { Text("Search chats...", color = Color.White.copy(0.7f)) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor        = Color.White,
                                unfocusedTextColor      = Color.White,
                                focusedContainerColor   = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor   = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor             = Color.White
                            ),
                            singleLine = true
                        )
                    } else {
                        Text("Chats", color = Color.White)
                    }
                },
                navigationIcon = {
                    if (!searchOpen) {
                        TextButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Text("edit", color = Color.White, fontSize = 14.sp)
                        }
                    }
                },
                actions = {
                    if (searchOpen) {
                        IconButton(onClick = { searchOpen = false; searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    } else {
                        IconButton(onClick = { searchOpen = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        },
        bottomBar = { BottomNav(navController = navController, totalUnread = totalUnread) }
    ) { paddingValues ->
        if (filteredChats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Search, contentDescription = null,
                        modifier = Modifier.size(48.dp), tint = TextSecondary.copy(0.3f))
                    Spacer(Modifier.height(8.dp))
                    Text("No results found", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                items(filteredChats, key = { it.id }) { chat ->
                    val contact = contacts.find { it.id == chat.contactId } ?: return@items
                    ChatItem(
                        chat    = chat,
                        contact = contact,
                        onClick = { onChatClick(chat.id) }
                    )
                    HorizontalDivider(color = GrayDivider, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun ChatItem(
    chat: Chat,
    contact: Contact,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            name   = contact.name,
            size   = 50.dp,
            online = contact.status == ContactStatus.ONLINE
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text     = contact.name,
                    color    = TextPrimary,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                chat.lastMessageTime?.let {
                    Text(
                        text     = formatChatTime(it),
                        color    = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text     = if (chat.isTyping) "typing..." else chat.lastMessage,
                    color    = if (chat.isTyping) BluePrimary else TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .sizeIn(minWidth = 20.dp, minHeight = 20.dp)
                            .background(BluePrimary, CircleShape)
                            .padding(horizontal = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text     = chat.unreadCount.toString(),
                            color    = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatChatTime(date: Date): String {
    val now  = Date()
    val diff = now.time - date.time
    return when {
        diff < 86_400_000  -> SimpleDateFormat("HH:mm",  Locale.getDefault()).format(date)
        diff < 172_800_000 -> "Yesterday"
        else               -> SimpleDateFormat("d MMM",  Locale.getDefault()).format(date)
    }
}
