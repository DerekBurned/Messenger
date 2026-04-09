package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.model.ReceiptInfo
import com.example.messenger.domain.usecase.message.GetMessagesUseCase
import com.example.messenger.domain.usecase.message.MarkMessageAsReadUseCase
import com.example.messenger.domain.usecase.message.SendMessageUseCase
import com.example.messenger.domain.usecase.presence.ObserveUserPresenceUseCase
import com.example.messenger.domain.usecase.receipt.ObserveReceiptsUseCase
import com.example.messenger.domain.usecase.typing.ObserveTypingUseCase
import com.example.messenger.domain.service.ITypingService
import com.example.messenger.domain.service.IReceiptService
import com.example.messenger.presentation.state.ChatUiState
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
    private val markMessageAsReadUseCase: MarkMessageAsReadUseCase,
    private val observeUserPresenceUseCase: ObserveUserPresenceUseCase,
    private val observeTypingUseCase: ObserveTypingUseCase,
    private val observeReceiptsUseCase: ObserveReceiptsUseCase,
    private val typingService: ITypingService,
    private val receiptService: IReceiptService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val conversationId: String = savedStateHandle.get<String>("conversationId") ?: ""
    private val partnerId: String = savedStateHandle.get<String>("partnerId") ?: ""
    private val partnerName: String = savedStateHandle.get<String>("partnerName") ?: ""

    private var typingJob: Job? = null
    private var typingClearJob: Job? = null
    private companion object {
        const val TYPING_DEBOUNCE_MS = 500L
        const val TYPING_TTL_MS = 5000L
    }

    init {
        if (conversationId.isNotBlank()) {
            loadMessages()
            if (partnerId.isNotBlank()) {
                observePartnerPresence()
            }
            observeTyping()
            observeReceipts()
        }
        if (partnerName.isNotBlank()) {
            _uiState.update { it.copy(partnerUsername = partnerName) }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            getMessagesUseCase(conversationId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        @Suppress("UNCHECKED_CAST")
                        val messages = resource.data as? List<Message> ?: emptyList()
                        _uiState.update { it.copy(isLoading = false, messages = messages, error = null) }
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

    private fun observePartnerPresence() {
        viewModelScope.launch {
            observeUserPresenceUseCase(partnerId)
                .catch { /* Ignore presence errors */ }
                .collect { presence ->
                    _uiState.update { it.copy(partnerPresence = presence) }
                }
        }
    }

    private fun observeTyping() {
        viewModelScope.launch {
            observeTypingUseCase(conversationId)
                .catch { /* Ignore typing errors */ }
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
                .catch { /* Ignore receipt errors */ }
                .collect { receipts ->
                    updateMessageStatuses(receipts)
                }
        }
    }

    private fun updateMessageStatuses(receipts: Map<String, ReceiptInfo>) {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { message ->
                if (message.senderId == partnerId) return@map message
                val partnerReceipt = receipts[partnerId] ?: return@map message
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
            state.copy(messages = updatedMessages)
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

    fun sendReadReceipt(latestTimestamp: Long) {
        viewModelScope.launch {
            try { receiptService.sendReadReceipt(conversationId, latestTimestamp) } catch (_: Exception) {}
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        // Best-effort clear typing when leaving chat
        typingJob?.cancel()
        typingClearJob?.cancel()
    }
}
