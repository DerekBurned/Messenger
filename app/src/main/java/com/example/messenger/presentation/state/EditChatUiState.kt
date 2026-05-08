package com.example.messenger.presentation.state

import com.example.messenger.domain.model.Conversation

data class EditChatUiState(
    val isLoading: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val error: String? = null,
)
