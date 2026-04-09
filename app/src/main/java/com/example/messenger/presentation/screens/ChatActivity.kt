package com.example.messenger.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.model.PresenceState
import com.example.messenger.presentation.components.MessageStatusIcon
import com.example.messenger.presentation.components.PresenceIndicator
import com.example.messenger.presentation.components.TypingIndicator
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.OnlineGreen
import com.example.messenger.presentation.viewmodel.ChatViewModel
import com.example.messenger.util.DateUtils
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenWithNav(
    viewModel: ChatViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var messageText by remember { mutableStateOf("") }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val context = LocalContext.current
    val chatMessages = remember(uiState.messages) {
        uiState.messages.map { msg ->
            ChatMessage(
                text = msg.text,
                isMe = msg.senderId == currentUserId,
                status = msg.status,
                timestamp = msg.timestamp
            )
        }
    }

    // Send read receipt when messages are loaded
    LaunchedEffect(uiState.messages) {
        val latestReceivedTimestamp = uiState.messages
            .filter { it.senderId != currentUserId }
            .maxOfOrNull { it.timestamp }
        if (latestReceivedTimestamp != null) {
            viewModel.sendReadReceipt(latestReceivedTimestamp)
        }
    }

    val presenceStatusText = when {
        uiState.isPartnerTyping -> "typing..."
        uiState.partnerPresence.state == PresenceState.ONLINE -> "Online"
        uiState.partnerPresence.state == PresenceState.AWAY -> "Away"
        else -> DateUtils.formatLastSeen(uiState.partnerPresence.lastSeen)
    }

    val statusColor = when {
        uiState.isPartnerTyping -> OnlineGreen
        uiState.partnerPresence.state == PresenceState.ONLINE -> OnlineGreen
        else -> Color.White.copy(alpha = 0.7f)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.partnerUsername.ifBlank { "Chat" },
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = presenceStatusText,
                            color = statusColor,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(8.dp)
                                .background(Color.White.copy(alpha = 0.3f), CircleShape)
                        )
                        PresenceIndicator(
                            state = uiState.partnerPresence.state,
                            size = 12.dp,
                            modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF5B8DEE)
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && chatMessages.isEmpty() -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF5B8DEE))
                    }
                }
                uiState.error != null && chatMessages.isEmpty() -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = uiState.error!!, color = Color.Red)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        items(uiState.messages.size) { index ->
                            val originalMessage = uiState.messages[index]
                            val chatMessage = chatMessages[index]
                            MessageWithContextMenu(
                                message = chatMessage,
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("message", originalMessage.text))
                                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                },
                                onReply = {
                                    viewModel.setReplyTo(originalMessage)
                                },
                                onEdit = { },
                                onPin = { },
                                onForward = { },
                                onDelete = {
                                    if (originalMessage.senderId == currentUserId) {
                                        viewModel.deleteMessage(originalMessage)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Typing indicator
            if (uiState.isPartnerTyping) {
                TypingIndicator(
                    usernames = uiState.typingUsernames.ifEmpty {
                        listOf(uiState.partnerUsername.ifBlank { "User" })
                    }
                )
            }

            // Reply preview
            if (uiState.replyingTo != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8EEF8))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null,
                        tint = Color(0xFF5B8DEE),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.replyingTo!!.text,
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.clearReply() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel reply",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF5B8DEE))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White
                    )
                }

                BasicTextField(
                    value = messageText,
                    onValueChange = { newText ->
                        messageText = newText
                        viewModel.onTextChanged(newText)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            val textToSend = if (uiState.replyingTo != null) {
                                "> ${uiState.replyingTo!!.text}\n\n$messageText"
                            } else {
                                messageText
                            }
                            viewModel.sendMessage(textToSend)
                            viewModel.clearReply()
                            messageText = ""
                        }
                    },
                    modifier = Modifier.size(40.dp),
                    enabled = !uiState.isSending
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start) {
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
            // Timestamp and status row
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
}

data class ChatMessage(
    val text: String,
    val isMe: Boolean,
    val status: MessageStatus = MessageStatus.SENT,
    val timestamp: Long = 0L
)

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    MessengerTheme {
        ChatScreenWithNav()
    }
}
