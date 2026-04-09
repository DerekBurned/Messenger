package com.example.messenger.presentation.state

import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.User

data class SearchUsersUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val error: String? = null,
    val createdConversation: Conversation? = null,
    val isCreatingConversation: Boolean = false
)
