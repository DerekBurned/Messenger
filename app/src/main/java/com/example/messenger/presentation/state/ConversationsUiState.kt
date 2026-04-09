package com.example.messenger.presentation.state

import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.UserPresence

data class ConversationsUiState(
    val isLoading: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val error: String? = null,
    val presenceMap: Map<String, UserPresence> = emptyMap(),
    val isRefreshing: Boolean = false
)
