package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
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
import com.example.messenger.presentation.state.ChatUiState
import com.example.messenger.util.DateUtils
import com.example.messenger.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val conversationId: String = savedStateHandle.get<String>("conversationId") ?: ""
    private val partnerId: String = savedStateHandle.get<String>("partnerId") ?: ""
    private val partnerName: String = savedStateHandle.get<String>("partnerName") ?: ""

    private var typingJob: Job? = null
    private var typingClearJob: Job? = null
    private var lastSentReceiptTimestamp: Long = 0L

    private var latestReceipts: Map<String, ReceiptInfo> = emptyMap()

    private companion object {
        const val TYPING_DEBOUNCE_MS = 500L
        const val TYPING_TTL_MS = 5000L
        const val LAST_SEEN_TICK_MS = 60_000L
    }

    init {
        val currentUserId = firebaseAuthService.getCurrentUserId().orEmpty()
        _uiState.update {
            it.copy(
                currentUserId = currentUserId,
                partnerUsername = if (partnerName.isNotBlank()) partnerName else it.partnerUsername,
                partnerLastSeenDisplay = DateUtils.formatLastSeen(it.partnerPresence.lastSeen),
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
            startLastSeenTicker()
        }
    }

    private fun syncMessages() {
        viewModelScope.launch {
            try {
                syncMessagesUseCase(conversationId).collect { }
            } catch (_: Exception) { }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            getMessagesUseCase(conversationId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        @Suppress("UNCHECKED_CAST")
                        val messages = (resource.data as? List<*>)?.filterIsInstance<Message>() ?: emptyList()
                        val decorated = applyReceiptStatuses(messages, latestReceipts)
                        _uiState.update { it.copy(isLoading = false, messages = decorated, error = null) }
                        maybeSendReadReceiptForLatestReceived(messages)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, error = resource.message)
                    }
                    is Resource.Failure -> _uiState.update {
                        it.copy(isLoading = false, error = resource.exception.message)
                    }
                }
            }
        }
    }

    private fun maybeSendReadReceiptForLatestReceived(messages: List<Message>) {
        val me = _uiState.value.currentUserId
        if (me.isBlank()) return
        val latest = messages
            .asSequence()
            .filter { it.senderId != me }
            .maxOfOrNull { it.timestamp } ?: return
        if (latest <= lastSentReceiptTimestamp) return
        lastSentReceiptTimestamp = latest
        viewModelScope.launch {
            try {
                receiptService.sendReadReceipt(conversationId, latest)
            } catch (_: Exception) {  }
        }
    }

    private fun observePartnerPresence() {
        viewModelScope.launch {
            observeUserPresenceUseCase(partnerId)
                .catch {  }
                .collect { presence ->
                    _uiState.update {
                        it.copy(
                            partnerPresence = presence,
                            partnerLastSeenDisplay = DateUtils.formatLastSeen(presence.lastSeen),
                        )
                    }
                }
        }
    }

    private fun startLastSeenTicker() {
        viewModelScope.launch {
            while (true) {
                delay(LAST_SEEN_TICK_MS)
                val presence = _uiState.value.partnerPresence
                _uiState.update {
                    it.copy(partnerLastSeenDisplay = DateUtils.formatLastSeen(presence.lastSeen))
                }
            }
        }
    }

    private fun observeTyping() {
        viewModelScope.launch {
            observeTypingUseCase(conversationId)
                .catch {  }
                .collect { typingUserIds ->
                    _uiState.update {
                        it.copy(
                            typingUsernames = typingUserIds.toList(),
                            isPartnerTyping = typingUserIds.isNotEmpty()
                        )
                    }
                }
        }
    }

    private fun observeReceipts() {
        viewModelScope.launch {
            observeReceiptsUseCase(conversationId)
                .catch {  }
                .collect { receipts ->
                    latestReceipts = receipts
                    _uiState.update { state ->
                        state.copy(messages = applyReceiptStatuses(state.messages, receipts))
                    }
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
            val newStatus = when {
                message.timestamp <= partnerReceipt.lastReadTimestamp -> MessageStatus.READ
                message.timestamp <= partnerReceipt.lastDeliveredTimestamp -> MessageStatus.DELIVERED
                else -> message.status
            }
            if (newStatus.ordinal > message.status.ordinal) {
                message.copy(status = newStatus)
            } else {
                message
            }
        }
    }

    fun onTextChanged(text: String) {
        typingJob?.cancel()
        typingClearJob?.cancel()

        if (text.isBlank()) {
            viewModelScope.launch {
                try { typingService.clearTyping(conversationId) } catch (_: Exception) {}
            }
            return
        }

        typingJob = viewModelScope.launch {
            delay(TYPING_DEBOUNCE_MS)
            try { typingService.setTyping(conversationId) } catch (_: Exception) {}
        }

        typingClearJob = viewModelScope.launch {
            delay(TYPING_TTL_MS)
            try { typingService.clearTyping(conversationId) } catch (_: Exception) {}
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            typingService.clearTyping(conversationId)
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            sendMessageUseCase(conversationId, text).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> _uiState.update { it.copy(isSending = false, error = null) }
                    is Resource.Error -> _uiState.update { it.copy(isSending = false, error = resource.message) }
                    is Resource.Failure -> _uiState.update { it.copy(isSending = false, error = resource.exception.message) }
                }
            }
        }
    }

    fun markAsRead(message: Message) {
        viewModelScope.launch {
            markMessageAsReadUseCase(message).collect { }
        }
    }

    fun deleteMessage(message: Message) {
        viewModelScope.launch {
            deleteMessageUseCase(message).collect { resource ->
                when (resource) {
                    is Resource.Error -> _uiState.update { it.copy(error = resource.message) }
                    is Resource.Failure -> _uiState.update { it.copy(error = resource.exception.message) }
                    else -> {}
                }
            }
        }
    }

    fun setReplyTo(message: Message) {
        _uiState.update { it.copy(replyingTo = message) }
    }

    fun clearReply() {
        _uiState.update { it.copy(replyingTo = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        
        typingJob?.cancel()
        typingClearJob?.cancel()
    }
}
