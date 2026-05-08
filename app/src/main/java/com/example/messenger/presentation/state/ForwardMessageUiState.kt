package com.example.messenger.presentation.state

import com.example.messenger.domain.model.Conversation

data class ForwardMessageUiState(
    val isLoading: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val query: String = "",
    val searchActive: Boolean = false,
    val error: String? = null,
) {
    val filteredConversations: List<Conversation>
        get() = if (query.isBlank()) conversations
        else conversations.filter { conv ->
            conv.participantNames.any { it.contains(query, ignoreCase = true) }
        }
}
