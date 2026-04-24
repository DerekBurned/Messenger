package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.data.Chat
import com.example.messenger.data.Contact
import com.example.messenger.data.ContactStatus
import com.example.messenger.presentation.components.Avatar
import com.example.messenger.presentation.screens.ui.theme.*

/**
 * data-figma-name: edit_chats
 * Экран выбора чатов для удаления или массового прочтения.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChatsScreen(
    chats: List<Chat>,
    contacts: List<Contact>,
    onBackClick: () -> Unit,
    onDeleteChats: (ids: List<String>) -> Unit,
    onMarkAllRead: () -> Unit
) {
    // По умолчанию все чаты выбраны (как в React-версии)
    val selectedIds = remember { mutableStateSetOf<String>().also { set -> chats.forEach { set.add(it.id) } } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("select chat", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                // Прочитать всё
                Button(
                    onClick = { onMarkAllRead(); onBackClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape  = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("read all", fontSize = 14.sp)
                }

                // Удалить выбранные
                Button(
                    onClick = {
                        onDeleteChats(selectedIds.toList())
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape  = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("delete", fontSize = 14.sp)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            items(chats, key = { it.id }) { chat ->
                val contact = contacts.find { it.id == chat.contactId } ?: return@items
                val isSelected = selectedIds.contains(chat.id)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isSelected) selectedIds.remove(chat.id)
                            else selectedIds.add(chat.id)
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(
                        name   = contact.name,
                        size   = 46.dp,
                        online = contact.status == ContactStatus.ONLINE
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text     = contact.name,
                        color    = TextPrimary,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )

                    // Чекбокс
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) BluePrimary else Color.Transparent)
                            .border(
                                width = 2.dp,
                                color = if (isSelected) BluePrimary else GrayDivider,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector   = Icons.Default.Check,
                                contentDescription = null,
                                tint     = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(color = GrayDivider, thickness = 0.5.dp)
            }
        }
    }
}

// Вспомогательная функция — создаём mutableStateSetOf через extension
private fun <T> mutableStateSetOf(): androidx.compose.runtime.snapshots.SnapshotStateList<T> =
    androidx.compose.runtime.mutableStateListOf()

// Перегрузка для Set-семантики
@Composable
private fun rememberSelectedSet(initial: List<String>): MutableSet<String> {
    return remember { initial.toMutableSet() }
}
