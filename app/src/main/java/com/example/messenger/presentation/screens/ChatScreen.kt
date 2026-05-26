package com.example.messenger.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.model.PresenceState
import com.example.messenger.presentation.components.MessageStatusIcon
import com.example.messenger.presentation.components.PresenceIndicator
import com.example.messenger.presentation.components.TypingIndicator
import com.example.messenger.presentation.screens.ui.theme.BubbleReceived
import com.example.messenger.presentation.screens.ui.theme.BubbleReceivedText
import com.example.messenger.presentation.screens.ui.theme.BubbleSent
import com.example.messenger.presentation.screens.ui.theme.ChatBackground
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.OnlineGreen
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.viewmodel.ChatViewModel
import com.example.messenger.util.DateUtils
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenWithNav(
    viewModel: ChatViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onCallClick: () -> Unit = {},
    onIntercultorProfileClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var messageText by remember { mutableStateOf("") }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val context = LocalContext.current
    val attachmentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            Toast.makeText(context, "Attachment selected: $uri (upload coming soon)", Toast.LENGTH_SHORT).show()
        }
    }
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

    LaunchedEffect(uiState.messages) {
        val latestReceivedTimestamp = uiState.messages
            .filter { it.senderId != currentUserId }
            .maxOfOrNull { it.timestamp }
        if (latestReceivedTimestamp != null) {
            viewModel.sendReadReceipt(latestReceivedTimestamp)
        }
    }

    ChatScreenContent(
        uiState = uiState,
        chatMessages = chatMessages,
        messageText = messageText,
        onMessageTextChange = { newText ->
            messageText = newText
            viewModel.onTextChanged(newText)
        },
        onSendClick = {
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
        onBackClick = onBackClick,
        onCopy = { text ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("message", text))
            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
        },
        onReply = { message -> viewModel.setReplyTo(message) },
        onDelete = { message ->
            if (message.senderId == currentUserId) {
                viewModel.deleteMessage(message)
            }
        },
        onClearReply = { viewModel.clearReply() },
        onAttachmentClick = { attachmentPicker.launch(arrayOf("*/*")) },
        onIntercultorProfileClick = onIntercultorProfileClick,
        onCallClick = onCallClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreenContent(
    uiState: com.example.messenger.presentation.state.ChatUiState,
    chatMessages: List<ChatMessage>,
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onBackClick: () -> Unit,
    onCallClick: () -> Unit,
    onIntercultorProfileClick: () -> Unit,
    onCopy: (String) -> Unit,
    onReply: (com.example.messenger.domain.model.Message) -> Unit,
    onDelete: (com.example.messenger.domain.model.Message) -> Unit,
    onClearReply: () -> Unit,
    onAttachmentClick: () -> Unit,
) {
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
                modifier = Modifier.shadow(elevation = 4.dp),
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
                    IconButton(onClick =  onCallClick ) {
                        Icon(Icons.Default.Call,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onIntercultorProfileClick() }
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
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
                    containerColor = PrimaryBlue
                )
            )
        },
        containerColor = ChatBackground
    ) { padding ->
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val isAtBottom by remember {
            derivedStateOf {
                val info = listState.layoutInfo
                val total = info.totalItemsCount
                total == 0 || (info.visibleItemsInfo.lastOrNull()?.index ?: -1) >= total - 1
            }
        }

        var unreadCount by remember { mutableStateOf(0) }
        var prevMessageCount by remember { mutableStateOf(0) }
        var didInitialScroll by remember { mutableStateOf(false) }

        LaunchedEffect(uiState.messages.size, isAtBottom) {
            val currentCount = uiState.messages.size
            if (currentCount == 0) {
                prevMessageCount = 0
                return@LaunchedEffect
            }
            if (!didInitialScroll) {
                listState.scrollToItem(currentCount - 1)
                didInitialScroll = true
                prevMessageCount = currentCount
                return@LaunchedEffect
            }
            if (currentCount > prevMessageCount) {
                val newOnes = uiState.messages.subList(prevMessageCount, currentCount)
                val lastIsMine = newOnes.lastOrNull()?.senderId == currentUserId
                if (isAtBottom || lastIsMine) {
                    listState.animateScrollToItem(currentCount - 1)
                } else {
                    unreadCount += newOnes.count { it.senderId != currentUserId }
                }
            }
            prevMessageCount = currentCount
        }

        LaunchedEffect(isAtBottom) {
            if (isAtBottom) unreadCount = 0
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            when {
                uiState.isLoading && chatMessages.isEmpty() -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
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
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                })
                            }
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            items(uiState.messages.size) { index ->
                                val originalMessage = uiState.messages[index]
                                val chatMessage = chatMessages[index]
                                MessageWithContextMenu(
                                    message = chatMessage,
                                    onCopy = { onCopy(originalMessage.text) },
                                    onReply = { onReply(originalMessage) },
                                    onEdit = {  },
                                    onPin = {  },
                                    onForward = {  },
                                    onDelete = { onDelete(originalMessage) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        if (unreadCount > 0) {
                            NewMessagesPill(
                                count = unreadCount,
                                onClick = {
                                    coroutineScope.launch {
                                        if (uiState.messages.isNotEmpty()) {
                                            listState.animateScrollToItem(uiState.messages.size - 1)
                                        }
                                        unreadCount = 0
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 16.dp, bottom = 12.dp)
                            )
                        }
                    }
                }
            }

            if (uiState.isPartnerTyping) {
                TypingIndicator(
                    usernames = uiState.typingUsernames.ifEmpty {
                        listOf(uiState.partnerUsername.ifBlank { "User" })
                    }
                )
            }

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
                        tint = PrimaryBlue,
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
                        onClick = onClearReply,
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryBlue)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onAttachmentClick,
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
                    onValueChange = onMessageTextChange,
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
                    onClick = onSendClick,
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
                        color = if (message.isMe) BubbleSent else BubbleReceived,
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (message.isMe) 18.dp else 4.dp,
                            bottomEnd = if (message.isMe) 4.dp else 18.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (message.isMe) Color.White else BubbleReceivedText,
                    fontSize = 15.sp
                )
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
}

data class ChatMessage(
    val text: String,
    val isMe: Boolean,
    val status: MessageStatus = MessageStatus.SENT,
    val timestamp: Long = 0L
)

@Composable
private fun NewMessagesPill(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = CircleShape,
        color = PrimaryBlue,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Scroll to newest",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (count == 1) "1 new message" else "$count new messages",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ChatScreenPreview() {
    val fakeMessages = listOf(
        com.example.messenger.domain.model.Message(
            id = "1", senderId = "me", text = "Hey there!", timestamp = 1_700_000_000_000L,
            status = MessageStatus.READ
        ),
        com.example.messenger.domain.model.Message(
            id = "2", senderId = "partner", text = "Hi! How are you?", timestamp = 1_700_000_060_000L,
            status = MessageStatus.SENT
        ),
        com.example.messenger.domain.model.Message(
            id = "3", senderId = "me", text = "All good, thanks!", timestamp = 1_700_000_120_000L,
            status = MessageStatus.DELIVERED
        ),
    )
    val fakeChatMessages = listOf(
        ChatMessage(text = "Hey there!", isMe = true, status = MessageStatus.READ, timestamp = 1_700_000_000_000L),
        ChatMessage(text = "Hi! How are you?", isMe = false, status = MessageStatus.SENT, timestamp = 1_700_000_060_000L),
        ChatMessage(text = "All good, thanks!", isMe = true, status = MessageStatus.DELIVERED, timestamp = 1_700_000_120_000L),
    )
    MessengerTheme {
        ChatScreenContent(
            uiState = com.example.messenger.presentation.state.ChatUiState(
                messages = fakeMessages,
                partnerUsername = "Alice",
                isLoading = false,
            ),
            chatMessages = fakeChatMessages,
            messageText = "",
            onMessageTextChange = {},
            onSendClick = {},
            onBackClick = {},
            onCallClick = {},
            onCopy = {},
            onReply = {},
            onDelete = {},
            onClearReply = {},
            onAttachmentClick = {},
            onIntercultorProfileClick = {}
        )
    }
}
