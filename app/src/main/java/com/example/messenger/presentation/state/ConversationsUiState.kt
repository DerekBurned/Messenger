package com.example.messenger.presentation.state

import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.UserPresence
import com.example.messenger.presentation.base.UiState
import com.example.messenger.presentation.base.UiText

data class ConversationsUiState(
    val isLoading: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val error: UiText? = null,
    val presenceMap: Map<String, UserPresence> = emptyMap(),
    val aliases: Map<String, String> = emptyMap(),
    val isRefreshing: Boolean = false,
    val currentUserId: String = "",
) : UiState
