package com.example.messenger.presentation.state

import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.UserPresence
import com.example.messenger.presentation.base.UiState
import com.example.messenger.presentation.base.UiText

data class ChatUiState(
    val isLoading: Boolean = false,
    val messages: List<Message> = emptyList(),
    val error: UiText? = null,
    val isSending: Boolean = false,
    val partnerPresence: UserPresence = UserPresence(),
    val partnerUsername: String = "",
    val typingUsernames: List<String> = emptyList(),
    val isPartnerTyping: Boolean = false,
    val replyingTo: Message? = null,
    val forwardingMessage: Message? = null,
    val currentUserId: String = "",
    
    val partnerLastSeenDisplay: String = "",
) : UiState
