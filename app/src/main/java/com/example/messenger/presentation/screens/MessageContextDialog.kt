package com.example.messenger.presentation.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.messenger.presentation.components.MessageStatusIcon
import com.example.messenger.presentation.screens.ui.theme.BubbleReceived
import com.example.messenger.presentation.screens.ui.theme.BubbleReceivedText
import com.example.messenger.presentation.screens.ui.theme.BubbleSent
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.util.DateUtils

data class MessageAction(
    val title: String,
    val icon: ImageVector,
    val color: Color = Color.Black,
    val onClick: () -> Unit
)

@Composable
fun MessageWithContextMenu(
    message: ChatMessage,
    highlighted: Boolean = false,
    onCopy: () -> Unit = {},
    onReply: () -> Unit = {},
    onReplyClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onPin: () -> Unit = {},
    onForward: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    
    val highlightColor by animateColorAsState(
        targetValue = if (highlighted) Color(0x33FFC107) else Color.Transparent,
        label = "messageHighlight",
    )

    Box {
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(highlightColor)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            showMenu = true
                        }
                    )
                }
                .padding(vertical = 2.dp),
            horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
        ) {
            Column(horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start) {
                val bubbleShape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (message.isMe) 18.dp else 4.dp,
                    bottomEnd = if (message.isMe) 4.dp else 18.dp,
                )
                Box(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .shadow(elevation = 1.dp, shape = bubbleShape, clip = false)
                        .background(
                            color = if (message.isMe) BubbleSent else BubbleReceived,
                            shape = bubbleShape,
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {

                        if (message.replyToMessageId != null) {
                            val accent = if (message.isMe) Color.White else PrimaryBlue
                            Row(
                                modifier = Modifier
                                    .padding(bottom = 6.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (message.isMe) Color.White.copy(alpha = 0.15f)
                                        else Color.Black.copy(alpha = 0.05f),
                                    )
                                    .clickable(onClick = onReplyClick)
                                    .height(IntrinsicSize.Min)
                                    .padding(end = 8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .fillMaxHeight()
                                        .background(accent),
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.padding(vertical = 3.dp)) {
                                    Text(
                                        text = message.replyToSenderLabel ?: "Reply",
                                        color = accent,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = message.replyToText.orEmpty(),
                                        color = if (message.isMe) Color.White.copy(alpha = 0.85f)
                                        else BubbleReceivedText.copy(alpha = 0.7f),
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                        Text(
                            text = message.text,
                            color = if (message.isMe) Color.White else BubbleReceivedText,
                            fontSize = 15.sp
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                ) {
                    Text(
                        text = DateUtils.formatMessageTime(message.timestamp),
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    if (message.isMe) {
                        MessageStatusIcon(status = message.status)
                    }
                }
            }
        }

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
                            MessageAction("Copy", Icons.Default.ContentCopy, onClick = {
                                onCopy()
                                showMenu = false
                            }),
                            MessageAction("Reply", Icons.Default.Reply, onClick = {
                                onReply()
                                showMenu = false
                            }),
                            MessageAction("Edit", Icons.Default.Edit, onClick = {
                                onEdit()
                                showMenu = false
                            }),
                            MessageAction("Pin", Icons.Default.PushPin, onClick = {
                                onPin()
                                showMenu = false
                            }),
                            MessageAction("Delete", Icons.Default.Delete, color = Color.Red, onClick = {
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
                    MessageAction("Copy", Icons.Default.ContentCopy, onClick = {}),
                    MessageAction("Reply", Icons.Default.Reply, onClick = {}),
                    MessageAction("Edit", Icons.Default.Edit, onClick = {}),
                    MessageAction("Pin", Icons.Default.PushPin, onClick = {}),
                    MessageAction("Delete...", Icons.Default.Delete, color = Color.Red, onClick = {})
                )
            )
        }
    }
}