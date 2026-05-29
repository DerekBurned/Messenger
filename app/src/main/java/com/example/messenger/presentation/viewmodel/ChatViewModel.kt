package com.example.messenger.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.messenger.data.remote.auth.FirebaseAuthService
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.model.ReceiptInfo
import com.example.messenger.domain.usecase.message.DeleteMessageUseCase
import com.example.messenger.domain.usecase.message.GetMessagesUseCase
import com.example.messenger.domain.usecase.message.MarkMessageAsReadUseCase
import com.example.messenger.domain.usecase.message.SendMessageUseCase
import com.example.messenger.domain.usecase.message.SyncMessagesUseCase
import com.example.messenger.domain.usecase.presence.ObserveUserPresenceUseCase
import com.example.messenger.domain.usecase.receipt.ObserveReceiptsUseCase
import com.example.messenger.domain.usecase.typing.ObserveTypingUseCase
import com.example.messenger.domain.service.ITypingService
import com.example.messenger.domain.service.IReceiptService
import com.example.messenger.R
import com.example.messenger.presentation.base.MviViewModel
import com.example.messenger.presentation.base.UiText
import com.example.messenger.presentation.base.toUiText
import com.example.messenger.presentation.effect.ChatEffect
import com.example.messenger.presentation.intent.ChatIntent
import com.example.messenger.presentation.state.ChatUiState
import com.example.messenger.util.DateUtils
import com.example.messenger.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val syncMessagesUseCase: SyncMessagesUseCase,
    private val markMessageAsReadUseCase: MarkMessageAsReadUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val observeUserPresenceUseCase: ObserveUserPresenceUseCase,
    private val observeTypingUseCase: ObserveTypingUseCase,
    private val observeReceiptsUseCase: ObserveReceiptsUseCase,
    private val typingService: ITypingService,
    private val receiptService: IReceiptService,
    firebaseAuthService: FirebaseAuthService,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<ChatUiState, ChatIntent, ChatEffect>(initialState = ChatUiState()) {

    val conversationId: String = savedStateHandle.get<String>("conversationId") ?: ""
    private val partnerId: String = savedStateHandle.get<String>("partnerId") ?: ""
    private val partnerName: String = savedStateHandle.get<String>("partnerName") ?: ""

    private var typingJob: Job? = null
    private var typingClearJob: Job? = null
    private var lastSentReadTimestamp: Long = 0L

    private var latestReceipts: Map<String, ReceiptInfo> = emptyMap()

    private companion object {
        const val TAG = "ChatViewModel"
        const val TYPING_DEBOUNCE_MS = 500L
        const val TYPING_TTL_MS = 5000L
        const val LAST_SEEN_TICK_MS = 60_000L
    }

    init {
        val currentUserId = firebaseAuthService.getCurrentUserId().orEmpty()
        setState {
            copy(
                currentUserId = currentUserId,
                partnerUsername = partnerName.ifBlank { partnerUsername },
                partnerLastSeenDisplay = DateUtils.formatLastSeen(partnerPresence.lastSeen),
            )
        }
        if (conversationId.isNotBlank()) {
            loadMessages()
            syncMessages()
            if (partnerId.isNotBlank()) {
                observePartnerPresence()
            }
            observeTyping()
            observeReceipts()
        }
    }

    override fun handleIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.TextChanged -> onTextChanged(intent.text)
            is ChatIntent.SendMessage -> sendMessage(intent.text)
            is ChatIntent.MarkAsRead -> markAsRead(intent.message)
            is ChatIntent.MessagesSeen -> onMessagesSeen(intent.upToTimestamp)
            is ChatIntent.DeleteMessage -> deleteMessage(intent.message)
            is ChatIntent.SetReplyTo -> setState { copy(replyingTo = intent.message) }
            ChatIntent.ClearReply -> setState { copy(replyingTo = null) }
            is ChatIntent.Forward -> setState { copy(forwardingMessage = intent.message) }
            ChatIntent.ClearForward -> setState { copy(forwardingMessage = null) }
            ChatIntent.ClearError -> setState { copy(error = null) }
        }
    }

    private fun syncMessages() {
        viewModelScope.launch {
            try {
                syncMessagesUseCase(conversationId).collect { }
            } catch (e: Exception) {
                Log.w(TAG, "Message sync stream errored", e)
            }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            getMessagesUseCase(conversationId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> setState { copy(isLoading = true) }
                    is Resource.Success -> {
                        val messages = resource.data
                        val decorated = applyReceiptStatuses(messages, latestReceipts)
                        setState { copy(isLoading = false, messages = decorated, error = null) }
                    }
                    is Resource.Error -> setState { copy(isLoading = false, error = resource.message.toUiText()) }
                    is Resource.Failure -> setState { copy(isLoading = false, error = resource.exception.message?.toUiText()) }
                }
            }
        }
    }

    private fun onMessagesSeen(upToTimestamp: Long) {
        val me = currentState.currentUserId
        if (me.isBlank()) {
            Log.w(TAG, "Skip read receipt: currentUserId is blank (auth race?)")
            return
        }
        if (upToTimestamp <= lastSentReadTimestamp) return
        lastSentReadTimestamp = upToTimestamp
        viewModelScope.launch {
            try {
                receiptService.sendReadReceipt(conversationId, upToTimestamp)
                Log.d(TAG, "Sent read receipt for ts=$upToTimestamp")
            } catch (e: Exception) {
                Log.w(TAG, "Read receipt send failed (best-effort)", e)
            }
        }
    }

    private fun observePartnerPresence() {
        viewModelScope.launch {
            val presence = observeUserPresenceUseCase(partnerId)
                .catch { e -> Log.w(TAG, "Presence observer errored", e) }
            combine(presence, tickerFlow(LAST_SEEN_TICK_MS)) { p, _ -> p }
                .collect { p ->
                    setState {
                        copy(
                            partnerPresence = p,
                            partnerLastSeenDisplay = DateUtils.formatLastSeen(p.lastSeen),
                        )
                    }
                }
        }
    }

    private fun tickerFlow(periodMs: Long) = flow {
        emit(Unit)
        while (true) {
            delay(periodMs)
            emit(Unit)
        }
    }

    private fun observeTyping() {
        viewModelScope.launch {
            observeTypingUseCase(conversationId)
                .catch { e -> Log.w(TAG, "Typing observer errored", e) }
                .collect { typingUserIds ->
                    setState {
                        copy(
                            typingUsernames = typingUserIds.toList(),
                            isPartnerTyping = typingUserIds.isNotEmpty(),
                        )
                    }
                }
        }
    }

    private fun observeReceipts() {
        viewModelScope.launch {
            observeReceiptsUseCase(conversationId)
                .catch { e -> Log.w(TAG, "Receipt observer errored", e) }
                .collect { receipts ->
                    Log.d(
                        TAG,
                        "Receipts update: ${receipts.size} entries; partner($partnerId)=" +
                            (receipts[partnerId]?.let {
                                "read=${it.lastReadTimestamp}"
                            } ?: "<none>"),
                    )
                    latestReceipts = receipts
                    setState { copy(messages = applyReceiptStatuses(messages, receipts)) }
                }
        }
    }

    private fun applyReceiptStatuses(
        messages: List<Message>,
        receipts: Map<String, ReceiptInfo>,
    ): List<Message> {
        val partnerReceipt = receipts[partnerId] ?: return messages
        return messages.map { message ->
            if (message.senderId == partnerId) return@map message
            if (message.timestamp <= partnerReceipt.lastReadTimestamp &&
                MessageStatus.READ.ordinal > message.status.ordinal
            ) {
                message.copy(status = MessageStatus.READ)
            } else {
                message
            }
        }
    }

    private fun onTextChanged(text: String) {
        typingJob?.cancel()
        typingClearJob?.cancel()

        if (text.isBlank()) {
            viewModelScope.launch {
                try { typingService.clearTyping(conversationId) } catch (e: Exception) {
                    Log.w(TAG, "Typing clear failed", e)
                }
            }
            return
        }

        typingJob = viewModelScope.launch {
            delay(TYPING_DEBOUNCE_MS)
            try { typingService.setTyping(conversationId) } catch (e: Exception) {
                Log.w(TAG, "Typing set failed", e)
            }
        }

        typingClearJob = viewModelScope.launch {
            delay(TYPING_TTL_MS)
            try { typingService.clearTyping(conversationId) } catch (e: Exception) {
                Log.w(TAG, "Typing clear (TTL) failed", e)
            }
        }
    }

    private fun sendMessage(text: String) {
        viewModelScope.launch {
            typingService.clearTyping(conversationId)
        }
        viewModelScope.launch {
            setState { copy(isSending = true) }
            sendMessageUseCase(conversationId, text).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> setState { copy(isSending = false, error = null) }
                    is Resource.Error -> setState { copy(isSending = false, error = resource.message.toUiText()) }
                    is Resource.Failure -> setState { copy(isSending = false, error = resource.exception.message?.toUiText()) }
                }
            }
        }
    }

    private fun markAsRead(message: Message) {
        viewModelScope.launch {
            markMessageAsReadUseCase(message).collect { }
        }
    }

    private fun deleteMessage(message: Message) {
        viewModelScope.launch {
            deleteMessageUseCase(message).collect { resource ->
                when (resource) {
                    is Resource.Error -> emitEffect(
                        ChatEffect.ShowError(resource.message.toUiText()),
                    )
                    is Resource.Failure -> emitEffect(
                        ChatEffect.ShowError(
                            resource.exception.message?.toUiText()
                                ?: UiText.StringResource(R.string.chat_error_delete_failed),
                        ),
                    )
                    else -> {}
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        
        typingJob?.cancel()
        typingClearJob?.cancel()
    }
}
