package com.example.messenger.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.data.*
import com.example.messenger.presentation.components.Avatar
import com.example.messenger.presentation.screens.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * data-figma-name: chat_inside
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatInsideScreen(
    chat: Chat?,
    contact: Contact?,
    messages: List<Message>,
    onBackClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onSendMessage: (text: String, replyToId: String?) -> Unit,
    onDeleteMessage: (messageId: String) -> Unit,
    onEditMessage: (messageId: String, newText: String) -> Unit
) {
    if (chat == null || contact == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Chat not found", color = TextSecondary)
                TextButton(onClick = onBackClick) { Text("Back") }
            }
        }
        return
    }

    var input       by remember { mutableStateOf("") }
    var replyTo     by remember { mutableStateOf<Message?>(null) }
    var editingMsg  by remember { mutableStateOf<Message?>(null) }
    var selectedMsg by remember { mutableStateOf<Message?>(null) }
    var pinnedMsg   by remember { mutableStateOf<Message?>(null) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    val listState  = rememberLazyListState()
    val scope      = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(messages.size - 1) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAvatarClick() },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(contact.name, color = Color.White, fontSize = 15.sp)
                        Text(
                            text = if (chat.isTyping) "typing..." else
                                if (contact.status == ContactStatus.ONLINE) "online"
                                else contact.lastSeen.ifBlank { "offline" },
                            color    = Color.White.copy(0.7f),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            Text("back", color = Color.White, fontSize = 13.sp)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onAvatarClick) {
                        Avatar(name = contact.name, size = 34.dp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        },
        bottomBar = {
            Column {
                // Reply / Edit preview
                if (replyTo != null || editingMsg != null) {
                    val previewMsg = replyTo ?: editingMsg
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .width(3.dp)
                                .height(36.dp)
                                .background(BluePrimary, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = if (editingMsg != null) "Editing" else "Replying to",
                                color = BluePrimary, fontSize = 11.sp
                            )
                            Text(
                                text     = previewMsg?.text ?: "",
                                color    = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                        IconButton(onClick = {
                            replyTo    = null
                            editingMsg = null
                            input      = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = TextSecondary)
                        }
                    }
                    HorizontalDivider(color = GrayDivider)
                }

                // Input bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BluePrimary)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* file picker */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Attach", tint = Color.White)
                    }

                    TextField(
                        value         = input,
                        onValueChange = { input = it },
                        placeholder   = { Text("Message...", color = Color.White.copy(0.6f)) },
                        modifier      = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor        = Color.White,
                            unfocusedTextColor      = Color.White,
                            focusedContainerColor   = Color.White.copy(0.2f),
                            unfocusedContainerColor = Color.White.copy(0.2f),
                            focusedIndicatorColor   = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor             = Color.White
                        ),
                        shape = CircleShape,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (input.isNotBlank()) {
                                if (editingMsg != null) {
                                    onEditMessage(editingMsg!!.id, input.trim())
                                    editingMsg = null
                                } else {
                                    onSendMessage(input.trim(), replyTo?.id)
                                    replyTo = null
                                }
                                input = ""
                            }
                        })
                    )

                    Spacer(Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(0.25f))
                            .clickable {
                                if (input.isNotBlank()) {
                                    if (editingMsg != null) {
                                        onEditMessage(editingMsg!!.id, input.trim())
                                        editingMsg = null
                                    } else {
                                        onSendMessage(input.trim(), replyTo?.id)
                                        replyTo = null
                                    }
                                    input = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (input.isNotBlank()) Icons.Default.Send else Icons.Default.Mic,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(GrayBg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val replyOriginal = msg.replyToId?.let { id -> messages.find { it.id == id } }
                MessageBubble(
                    message       = msg,
                    replyOriginal = replyOriginal,
                    contactName   = contact.name,
                    onLongClick   = { selectedMsg = msg }
                )
            }
        }
    }

    // ── Message Action Dialog ──────────────────────────────────────────────────
    if (selectedMsg != null) {
        val msg = selectedMsg!!
        AlertDialog(
            onDismissRequest = { selectedMsg = null },
            title            = { Text("Message") },
            text             = { Text(msg.text) },
            confirmButton    = {},
            dismissButton    = {},
            // Custom content for actions
            shape = RoundedCornerShape(16.dp)
        )
        // Better: use a ModalBottomSheet with action buttons
        MessageActionsSheet(
            onDismiss = { selectedMsg = null },
            onReply   = { replyTo = msg; selectedMsg = null },
            onEdit    = { editingMsg = msg; input = msg.text; selectedMsg = null },
            onDelete  = { pendingDelete = msg.id; selectedMsg = null },
            onPin     = { pinnedMsg = msg; selectedMsg = null },
            onCopy    = { /* copy to clipboard */ selectedMsg = null },
            isOwn     = msg.isOwn
        )
    }

    // ── Delete Confirmation ────────────────────────────────────────────────────
    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title            = { Text("Delete message?") },
            confirmButton    = {
                TextButton(onClick = { onDeleteMessage(pendingDelete!!); pendingDelete = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton    = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: Message,
    replyOriginal: Message?,
    contactName: String,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isOwn) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isOwn) {
            Avatar(name = contactName, size = 28.dp)
            Spacer(Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart    = 16.dp,
                        topEnd      = 16.dp,
                        bottomStart = if (message.isOwn) 16.dp else 4.dp,
                        bottomEnd   = if (message.isOwn) 4.dp else 16.dp
                    )
                )
                .background(if (message.isOwn) BluePrimary else Color.White)
                .combinedClickable(
                    onClick     = {},
                    onLongClick = onLongClick
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                // Reply preview
                if (replyOriginal != null) {
                    Row(modifier = Modifier.padding(bottom = 4.dp)) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .height(28.dp)
                                .background(
                                    if (message.isOwn) Color.White.copy(0.6f) else BluePrimary,
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text     = replyOriginal.text,
                            color    = if (message.isOwn) Color.White.copy(0.7f) else BluePrimary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                // Message text
                Text(
                    text     = message.text,
                    color    = if (message.isOwn) Color.White else TextPrimary,
                    fontSize = 14.sp
                )

                // Time + status
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.isEdited) {
                        Text(
                            text     = "edited ",
                            color    = if (message.isOwn) Color.White.copy(0.7f) else TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text     = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp),
                        color    = if (message.isOwn) Color.White.copy(0.7f) else TextSecondary,
                        fontSize = 10.sp
                    )
                    if (message.isOwn) {
                        Spacer(Modifier.width(3.dp))
                        StatusIcon(message.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusIcon(status: MessageStatus) {
    val tint = when (status) {
        MessageStatus.READ      -> BluePrimary
        MessageStatus.DELIVERED -> TextSecondary
        else                    -> TextSecondary.copy(0.5f)
    }
    Icon(
        imageVector = if (status == MessageStatus.SENDING) Icons.Default.RadioButtonUnchecked
        else Icons.Default.DoneAll,
        contentDescription = null,
        tint     = tint,
        modifier = Modifier.size(14.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageActionsSheet(
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit,
    onCopy: () -> Unit,
    isOwn: Boolean
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            ActionItem(Icons.AutoMirrored.Filled.Reply, "Reply", onReply)
            ActionItem(Icons.Default.ContentCopy, "Copy", onCopy)
            ActionItem(Icons.Default.PushPin, "Pin", onPin)
            if (isOwn) {
                ActionItem(Icons.Default.Edit, "Edit", onEdit)
            }
            ActionItem(Icons.Default.Delete, "Delete", onDelete, isDestructive = true)
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else TextPrimary
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text  = label,
            color = if (isDestructive) MaterialTheme.colorScheme.error else TextPrimary,
            fontSize = 16.sp
        )
    }
}
