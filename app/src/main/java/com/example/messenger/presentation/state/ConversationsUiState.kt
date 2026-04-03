package com.example.messenger.presentation.state

import com.example.messenger.domain.model.Conversation

data class ConversationsUiState(
    val isLoading: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val error: String? = null
)
