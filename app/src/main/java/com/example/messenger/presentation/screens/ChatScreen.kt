package com.example.messenger.presentation.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import com.example.messenger.domain.model.CallType
import com.example.messenger.domain.model.MediaItem
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.model.PresenceState
import com.example.messenger.presentation.components.AttachmentBar
import com.example.messenger.presentation.components.CallAwareTopBar
import com.example.messenger.presentation.components.FullscreenMediaPager
import com.example.messenger.presentation.components.MediaAlbumGrid
import com.example.messenger.presentation.components.MediaSource
import com.example.messenger.presentation.components.mediaCacheFile
import com.example.messenger.presentation.components.MessageStatusIcon
import com.example.messenger.presentation.components.PresenceIndicator
import com.example.messenger.presentation.components.TypingIndicator
import com.example.messenger.presentation.base.ObserveAsEvents
import com.example.messenger.presentation.effect.ChatEffect
import com.example.messenger.presentation.intent.ChatIntent
import com.example.messenger.presentation.notification.ChatNotifier
import com.example.messenger.presentation.notification.CurrentConversationHolder
import com.example.messenger.presentation.screens.ui.theme.BubbleReceived
import com.example.messenger.presentation.screens.ui.theme.BubbleReceivedText
import com.example.messenger.presentation.screens.ui.theme.BubbleSent
import com.example.messenger.presentation.screens.ui.theme.ChatBackground
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.OnlineGreen
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.state.ChatUiState
import com.example.messenger.presentation.state.GalleryItem
import com.example.messenger.presentation.viewmodel.ChatViewModel
import com.example.messenger.util.DateUtils

private fun mediaReadPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenWithNav(
    viewModel: ChatViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onCallClick: () -> Unit = {},
    onIntercultorProfileClick: () -> Unit = {}
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    var messageText by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current
    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            is ChatEffect.ShowError -> Toast.makeText(context, effect.message.asString(context), Toast.LENGTH_SHORT).show()
        }
    }

    var showPicker by remember { mutableStateOf(false) }
    var cameraGranted by remember { mutableStateOf(false) }
    var devicePreview by remember { mutableStateOf<Pair<List<GalleryItem>, Int>?>(null) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        cameraGranted = granted
        Log.d("ChatScreen", "camera permission granted=$granted")
    }

    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val anyGranted = result.values.any { it }
        Log.d("ChatScreen", "media permission result granted=$anyGranted")
        if (anyGranted) showPicker = true
    }

    val conversationId = viewModel.conversationId
    DisposableEffect(conversationId) {
        if (conversationId.isNotBlank()) {
            CurrentConversationHolder.setOpen(conversationId)
            ChatNotifier.clear(context, conversationId)
        }
        onDispose {
            if (conversationId.isNotBlank()) {
                CurrentConversationHolder.clear(conversationId)
            }
        }
    }

    ChatScreenContent(
        uiState = uiState,
        messageText = messageText,
        onMessageTextChange = { newText ->
            messageText = newText
            viewModel.dispatch(ChatIntent.TextChanged(newText))
        },
        onSendClick = {
            if (messageText.isNotBlank() || uiState.pendingAttachments.isNotEmpty()) {
                viewModel.dispatch(ChatIntent.SendMessage(messageText))
                messageText = ""
            }
        },
        onBackClick = onBackClick,
        onCopy = { text ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("message", text))
            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
        },
        onReply = { message -> viewModel.dispatch(ChatIntent.SetReplyTo(message)) },
        onReplyClick = { messageId -> viewModel.dispatch(ChatIntent.JumpToMessage(messageId)) },
        onDelete = { message ->
            if (message.senderId == uiState.currentUserId) {
                viewModel.dispatch(ChatIntent.DeleteMessage(message))
            }
        },
        onClearReply = { viewModel.dispatch(ChatIntent.ClearReply) },
        onAttachmentClick = {
            val perms = mediaReadPermissions()
            val granted = perms.any {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
            if (granted) {
                showPicker = true
            } else {
                mediaPermissionLauncher.launch(perms)
            }
        },
        onClearAttachments = { viewModel.dispatch(ChatIntent.ClearAttachments) },
        onIntercultorProfileClick = onIntercultorProfileClick,
        onCallClick = onCallClick,
        onMessagesSeen = { messages -> viewModel.dispatch(ChatIntent.MessagesSeen(messages)) },
        onLoadOlderMessages = { viewModel.dispatch(ChatIntent.LoadOlderMessages) },
        onDownload = { item -> viewModel.dispatch(ChatIntent.DownloadMedia(item)) },
        onCancelUpload = { messageId, itemId -> viewModel.dispatch(ChatIntent.CancelUpload(messageId, itemId)) },
        onCancelDownload = { itemId -> viewModel.dispatch(ChatIntent.CancelDownload(itemId)) },
        onRetryMedia = { messageId -> viewModel.dispatch(ChatIntent.RetryMedia(messageId)) },
    )

    if (showPicker) {
        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                cameraGranted = true
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
        MediaPickerBottomSheet(
            selected = uiState.pendingAttachments.map { it.uri }.toSet(),
            onToggle = { uri, kind -> viewModel.dispatch(ChatIntent.ToggleAttachment(uri, kind)) },
            onPreview = { items, index -> devicePreview = items to index },
            onCaptured = { uri, kind -> viewModel.dispatch(ChatIntent.AddAttachment(uri, kind)) },
            onDone = { showPicker = false },
            onDismiss = { showPicker = false },
            cameraEnabled = cameraGranted,
        )
    }

    devicePreview?.let { (items, index) ->
        val sources = remember(items) {
            items.map { gi ->
                MediaSource(
                    key = gi.id.toString(),
                    isVideo = gi.kind == MediaItem.VIDEO,
                    model = gi.uri,
                    playUri = gi.uri,
                )
            }
        }
        FullscreenMediaPager(
            sources = sources,
            startIndex = index,
            onDismiss = { devicePreview = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreenContent(
    uiState: ChatUiState,
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onBackClick: () -> Unit,
    onCallClick: () -> Unit,
    onIntercultorProfileClick: () -> Unit,
    onCopy: (String) -> Unit,
    onReply: (Message) -> Unit,
    onReplyClick: (messageId: String) -> Unit,
    onDelete: (Message) -> Unit,
    onClearReply: () -> Unit,
    onAttachmentClick: () -> Unit,
    onClearAttachments: () -> Unit,
    onMessagesSeen: (List<Message>) -> Unit,
    onLoadOlderMessages: () -> Unit,
    onDownload: (MediaItem) -> Unit,
    onCancelUpload: (messageId: String, itemId: String) -> Unit,
    onCancelDownload: (itemId: String) -> Unit,
    onRetryMedia: (messageId: String) -> Unit,
) {
    val context = LocalContext.current
    var chatPreviewIndex by remember { mutableStateOf<Int?>(null) }

    val presenceStatusText = when {
        uiState.isPartnerTyping -> "typing..."
        uiState.partnerPresence.state == PresenceState.ONLINE -> "Online"
        uiState.partnerPresence.state == PresenceState.AWAY -> "Away"
        else -> uiState.partnerLastSeenDisplay.ifBlank {
            DateUtils.formatLastSeen(uiState.partnerPresence.lastSeen)
        }
    }

    val statusColor = when {
        uiState.isPartnerTyping -> OnlineGreen
        uiState.partnerPresence.state == PresenceState.ONLINE -> OnlineGreen
        else -> Color.White.copy(alpha = 0.7f)
    }

    Scaffold(
        topBar = {
            CallAwareTopBar {
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
                        IconButton(onClick = onCallClick) {
                            Icon(
                                Icons.Default.Call,
                                contentDescription = "Voice call",
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
            }
        },
        containerColor = ChatBackground
    ) { padding ->
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current

        val isAtBottom by remember {
            derivedStateOf {
                val info = listState.layoutInfo
                val total = info.totalItemsCount
                total == 0 || (info.visibleItemsInfo.lastOrNull()?.index ?: -1) >= total - 1
            }
        }

        var unreadCount by rememberSaveable { mutableStateOf(0) }
        var didInitialScroll by remember { mutableStateOf(false) }
        var lastKnownNewestId by remember { mutableStateOf<String?>(null) }

        var dividerAnchorId by remember { mutableStateOf<String?>(null) }
        var seededReopenAnchor by remember { mutableStateOf(false) }

        if (!seededReopenAnchor && uiState.unreadAnchorResolved) {
            dividerAnchorId = uiState.firstUnreadMessageId
            seededReopenAnchor = true
        }

        val rows = remember(uiState.messages, dividerAnchorId) {
            buildList<ChatRow> {
                uiState.messages.forEach { message ->
                    if (message.id == dividerAnchorId) add(ChatRow.UnreadDivider)
                    add(ChatRow.MessageRow(message))
                }
            }
        }

        LaunchedEffect(rows, didInitialScroll) {
            if (didInitialScroll || rows.isEmpty()) return@LaunchedEffect
            val dividerIndex = rows.indexOfFirst { it is ChatRow.UnreadDivider }
            if (dividerIndex >= 0) {
                listState.scrollToItem(dividerIndex)
            } else {
                listState.scrollToItem(rows.size - 1)
            }
            didInitialScroll = true
        }

        LaunchedEffect(uiState.highlightedMessageId, rows) {
            val id = uiState.highlightedMessageId ?: return@LaunchedEffect
            val index = rows.indexOfFirst { it is ChatRow.MessageRow && it.message.id == id }
            if (index >= 0) listState.animateScrollToItem(index)
        }

        LaunchedEffect(uiState.messages, didInitialScroll) {
            if (!didInitialScroll) return@LaunchedEffect
            val newest = uiState.messages.lastOrNull() ?: return@LaunchedEffect
            if (newest.id == lastKnownNewestId) return@LaunchedEffect
            val prevNewestId = lastKnownNewestId
            lastKnownNewestId = newest.id
            if (prevNewestId == null) return@LaunchedEffect
            if (isAtBottom || newest.senderId == uiState.currentUserId) {
                listState.animateScrollToItem(rows.size - 1)
            } else {
                val newOnes = uiState.messages
                    .dropWhile { it.id != prevNewestId }
                    .drop(1)
                    .filter { it.senderId != uiState.currentUserId }
                unreadCount += newOnes.size
                if (dividerAnchorId == null) {
                    newOnes.firstOrNull()?.let { dividerAnchorId = it.id }
                }
            }
        }

        LaunchedEffect(isAtBottom) {
            if (isAtBottom) {
                unreadCount = 0
                
                dividerAnchorId = null
            }
        }

        val shouldLoadOlder by remember {
            derivedStateOf {
                val info = listState.layoutInfo
                val first = info.visibleItemsInfo.firstOrNull()?.index ?: 0
                info.totalItemsCount > 0 && first <= 2
            }
        }
        LaunchedEffect(shouldLoadOlder, didInitialScroll) {
            if (didInitialScroll && shouldLoadOlder && uiState.hasMoreOlder && !uiState.isLoadingOlder) {
                onLoadOlderMessages()
            }
        }

        val messagesById = remember(uiState.messages) { uiState.messages.associateBy { it.id } }
        LaunchedEffect(listState, messagesById, uiState.currentUserId) {
            snapshotFlow {
                val info = listState.layoutInfo
                val viewportTop = info.viewportStartOffset
                val viewportBottom = info.viewportEndOffset
                info.visibleItemsInfo
                    .asSequence()
                    .filter { item ->
                        val size = item.size
                        if (size <= 0) return@filter false
                        val top = item.offset
                        val bottom = top + size
                        val visiblePx = (minOf(bottom, viewportBottom) - maxOf(top, viewportTop))
                            .coerceAtLeast(0)
                        visiblePx.toFloat() / size >= 1f / 4f
                    }
                    .mapNotNull { item -> messagesById[item.key as? String] }
                    .filter { it.senderId != uiState.currentUserId && it.status != MessageStatus.READ }
                    .toList()
            }
                .distinctUntilChanged { old, new ->
                    old.mapTo(HashSet()) { it.id } == new.mapTo(HashSet()) { it.id }
                }
                .collect { messages -> if (messages.isNotEmpty()) onMessagesSeen(messages) }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            when {
                uiState.isLoading && uiState.messages.isEmpty() -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                uiState.error != null && uiState.messages.isEmpty() -> {
                    val errorMessage = uiState.error.asString()
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = errorMessage, color = Color.Red)
                    }
                }
                uiState.messages.isEmpty() -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No messages yet — say hi 👋",
                            color = Color.Gray,
                            fontSize = 14.sp,
                        )
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
                            items(
                                items = rows,
                                key = { row ->
                                    when (row) {
                                        is ChatRow.MessageRow -> row.message.id
                                        ChatRow.UnreadDivider -> "unread_divider"
                                    }
                                },
                            ) { row ->
                                when (row) {
                                    ChatRow.UnreadDivider -> UnreadMessagesDivider()
                                    is ChatRow.MessageRow -> {
                                        when (val msg = row.message) {
                                        is Message.Call -> when (msg.callType) {
                                            CallType.MISSED -> MissedCallCard(
                                                onCall = onCallClick,
                                                timestamp = msg.timestamp,
                                                isMe = msg.senderId == uiState.currentUserId,
                                                status = msg.status,
                                            )
                                            CallType.UNREACHED -> MissedCallCard(
                                                onCall = onCallClick,
                                                timestamp = msg.timestamp,
                                                isMe = msg.senderId == uiState.currentUserId,
                                                title = "Unreached call",
                                                status = msg.status,
                                            )
                                            CallType.ENDED -> EndedCallCard(
                                                durationSeconds = msg.durationSeconds,
                                                timestamp = msg.timestamp,
                                                isMe = msg.senderId == uiState.currentUserId,
                                            )
                                        }
                                        is Message.Media -> MediaAlbumGrid(
                                            message = msg,
                                            isMe = msg.senderId == uiState.currentUserId,
                                            transfers = uiState.transfers,
                                            onOpen = { item ->
                                                if (msg.status == MessageStatus.FAILED) {
                                                    onRetryMedia(msg.id)
                                                } else {
                                                    val idx = uiState.conversationMedia
                                                        .indexOfFirst { it.id == item.id }
                                                    if (idx >= 0) chatPreviewIndex = idx
                                                }
                                            },
                                            onDownload = { onDownload(it) },
                                            onCancelUpload = { messageId, itemId -> onCancelUpload(messageId, itemId) },
                                            onCancelDownload = { onCancelDownload(it) },
                                        )
                                        is Message.Text -> {
                                            val chatMessage = ChatMessage(
                                                text = msg.text,
                                                isMe = msg.senderId == uiState.currentUserId,
                                                status = msg.status,
                                                timestamp = msg.timestamp,
                                                replyToMessageId = msg.replyToMessageId,
                                                replyToText = msg.replyToText,
                                                replyToSenderLabel = msg.replyToMessageId?.let {
                                                    if (msg.replyToSenderId == uiState.currentUserId) "You"
                                                    else uiState.partnerUsername.ifBlank { "User" }
                                                },
                                            )
                                            MessageWithContextMenu(
                                                message = chatMessage,
                                                highlighted = msg.id == uiState.highlightedMessageId,
                                                onCopy = { onCopy(msg.text) },
                                                onReply = { onReply(msg) },
                                                onReplyClick = {
                                                    msg.replyToMessageId?.let(onReplyClick)
                                                },
                                                onEdit = { },
                                                onPin = { },
                                                onForward = { },
                                                onDelete = { onDelete(msg) },
                                            )
                                        }
                                    }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }

                        if (uiState.isLoadingOlder) {
                            CircularProgressIndicator(
                                color = PrimaryBlue,
                                strokeWidth = 2.dp,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 8.dp)
                                    .size(28.dp),
                            )
                        }

                        if (unreadCount > 0) {
                            NewMessagesPill(
                                count = unreadCount,
                                onClick = {
                                    coroutineScope.launch {
                                        if (rows.isNotEmpty()) {
                                            listState.animateScrollToItem(rows.size - 1)
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

            AttachmentBar(
                attachments = uiState.pendingAttachments,
                onClear = onClearAttachments,
                modifier = Modifier.fillMaxWidth(),
            )

            val replyingTo = uiState.replyingTo
            if (replyingTo != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8EEF8))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (replyingTo) {
                            is Message.Text  -> replyingTo.text
                            is Message.Media -> replyingTo.caption.ifBlank { "Media" }
                            is Message.Call  -> "Call"
                        },
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
                        contentDescription = "Add attachment",
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

    chatPreviewIndex?.let { startIndex ->
        val media = uiState.conversationMedia
        val sources = remember(media) {
            media.map { item ->
                val file = mediaCacheFile(context, item)
                MediaSource(
                    key = item.id,
                    isVideo = item.kind == MediaItem.VIDEO,
                    model = file,
                    playUri = android.net.Uri.fromFile(file),
                    blurHash = item.blurHash,
                )
            }
        }
        FullscreenMediaPager(
            sources = sources,
            startIndex = startIndex,
            onDismiss = { chatPreviewIndex = null },
            onPageSettled = { idx ->
                media.getOrNull(idx)?.let { item ->
                    val file = mediaCacheFile(context, item)
                    if ((!file.exists() || file.length() == 0L) && item.url.isNotBlank()) {
                        onDownload(item)
                    }
                }
            },
        )
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
    val timestamp: Long = 0L,
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToSenderLabel: String? = null,
)

private sealed interface ChatRow {
    data class MessageRow(val message: Message) : ChatRow
    data object UnreadDivider : ChatRow
}

@Composable
private fun UnreadMessagesDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = PrimaryBlue.copy(alpha = 0.4f))
        Text(
            text = "Unread messages",
            color = PrimaryBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = PrimaryBlue.copy(alpha = 0.4f))
    }
}

@Composable
private fun MissedCallCard(
    onCall: () -> Unit,
    timestamp: Long,
    isMe: Boolean,
    modifier: Modifier = Modifier,
    title: String = "Missed Call",
    status: MessageStatus? = null,
) {

    val titleColor = if (isMe) Color.White else BubbleReceivedText
    val subtitleColor = if (isMe) Color.White.copy(alpha = 0.8f) else Color.Gray
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            onClick = onCall,
            shape = RoundedCornerShape(18.dp),
            color = if (isMe) BubbleSent else BubbleReceived,
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.CallMissed,
                    contentDescription = null,
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        color = titleColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Tap to call back · ${DateUtils.formatMessageTime(timestamp)}",
                            color = subtitleColor,
                            fontSize = 12.sp,
                        )
                        if (isMe && status != null) {
                            Spacer(Modifier.width(4.dp))
                            MessageStatusIcon(status = status)
                        }
                    }
                }
                Spacer(Modifier.width(14.dp))
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF34C759), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EndedCallCard(
    durationSeconds: Int,
    timestamp: Long,
    isMe: Boolean,
    modifier: Modifier = Modifier,
) {
    val titleColor = if (isMe) Color.White else BubbleReceivedText
    val subtitleColor = if (isMe) Color.White.copy(alpha = 0.8f) else Color.Gray
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isMe) BubbleSent else BubbleReceived,
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = Color(0xFF34C759),
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Call",
                        color = titleColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                    Text(
                        text = "${DateUtils.formatDuration(durationSeconds)} · ${DateUtils.formatMessageTime(timestamp)}",
                        color = subtitleColor,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

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
        Message.Text(id = "1", senderId = "me", text = "Hey there!", timestamp = 1_700_000_000_000L, status = MessageStatus.READ),
        Message.Text(id = "2", senderId = "partner", text = "Hi! How are you?", timestamp = 1_700_000_060_000L, status = MessageStatus.SENT),
        Message.Text(id = "3", senderId = "me", text = "All good, thanks!", timestamp = 1_700_000_120_000L, status = MessageStatus.SENT),
    )
    MessengerTheme {
        ChatScreenContent(
            uiState = ChatUiState(
                messages = fakeMessages,
                partnerUsername = "Alice",
                isLoading = false,
                currentUserId = "me",
            ),
            messageText = "",
            onMessageTextChange = {},
            onSendClick = {},
            onBackClick = {},
            onCallClick = {},
            onCopy = {},
            onReply = {},
            onReplyClick = {},
            onDelete = {},
            onClearReply = {},
            onAttachmentClick = {},
            onClearAttachments = {},
            onIntercultorProfileClick = {},
            onMessagesSeen = {},
            onLoadOlderMessages = {},
            onDownload = {},
            onCancelUpload = { _, _ -> },
            onCancelDownload = {},
            onRetryMedia = {},
        )
    }
}
