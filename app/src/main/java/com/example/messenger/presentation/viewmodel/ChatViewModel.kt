package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.usecase.message.GetMessagesUseCase
import com.example.messenger.domain.usecase.message.MarkMessageAsReadUseCase
import com.example.messenger.domain.usecase.message.SendMessageUseCase
import com.example.messenger.presentation.state.ChatUiState
import com.example.messenger.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val markMessageAsReadUseCase: MarkMessageAsReadUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val conversationId: String = savedStateHandle.get<String>("conversationId") ?: ""

    init {
        if (conversationId.isNotBlank()) {
            loadMessages()
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

    fun sendMessage(text: String) {
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
