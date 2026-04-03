package com.example.messenger.presentation.state

import com.example.messenger.domain.model.Message

data class ChatUiState(
    val isLoading: Boolean = false,
    val messages: List<Message> = emptyList(),
    val error: String? = null,
    val isSending: Boolean = false
)
