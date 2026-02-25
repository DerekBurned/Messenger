package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme

// Данные для пункта меню
data class MessageAction(
    val title: String,
    val icon: ImageVector,
    val color: Color = Color.Black,
    val onClick: () -> Unit
)

@Composable
fun MessageWithContextMenu(
    message: ChatMessage,
    onCopy: () -> Unit = {},
    onReply: () -> Unit = {},
    onEdit: () -> Unit = {},
    onPin: () -> Unit = {},
    onForward: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        // Сообщение с обработкой долгого нажатия
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            showMenu = true
                        }
                    )
                },
            horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        color = Color(0xFF9DB4E8),
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (message.isMe) 20.dp else 4.dp,
                            bottomEnd = if (message.isMe) 4.dp else 20.dp
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 15.sp
                )
            }
        }

        // Контекстное меню
        if (showMenu) {
            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { showMenu = false }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { showMenu = false },
                    contentAlignment = Alignment.Center
                ) {
                    ContextMenuContent(
                        actions = listOf(
                            MessageAction("copy", Icons.Default.ContentCopy, onClick = {
                                onCopy()
                                showMenu = false
                            }),
                            MessageAction("reply", Icons.Default.Reply, onClick = {
                                onReply()
                                showMenu = false
                            }),
                            MessageAction("edit", Icons.Default.Edit, onClick = {
                                onEdit()
                                showMenu = false
                            }),
                            MessageAction("pin", Icons.Default.PushPin, onClick = {
                                onPin()
                                showMenu = false
                            }),
                            MessageAction("send", Icons.Default.Send, onClick = {
                                onForward()
                                showMenu = false
                            }),
                            MessageAction("delete...", Icons.Default.Delete, color = Color.Red, onClick = {
                                onDelete()
                                showMenu = false
                            })
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ContextMenuContent(actions: List<MessageAction>) {
    Surface(
        modifier = Modifier
            .width(280.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            actions.forEachIndexed { index, action ->
                MenuItemRow(
                    title = action.title,
                    icon = action.icon,
                    color = action.color,
                    onClick = action.onClick
                )

                // Разделитель (кроме последнего элемента)
                if (index < actions.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun MenuItemRow(
    title: String,
    icon: ImageVector,
    color: Color = Color.Black,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = color,
            fontSize = 16.sp
        )
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun ContextMenuPreview() {
    MessengerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            ContextMenuContent(
                actions = listOf(
                    MessageAction("copy", Icons.Default.ContentCopy, onClick = {}),
                    MessageAction("reply", Icons.Default.Reply, onClick = {}),
                    MessageAction("edit", Icons.Default.Edit, onClick = {}),
                    MessageAction("pin", Icons.Default.PushPin, onClick = {}),
                    MessageAction("send", Icons.Default.Send, onClick = {}),
                    MessageAction("delete...", Icons.Default.Delete, color = Color.Red, onClick = {})
                )
            )
        }
    }
}