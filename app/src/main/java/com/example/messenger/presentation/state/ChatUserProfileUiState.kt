package com.example.messenger.presentation.state

import com.example.messenger.domain.model.User

enum class MediaTab { PHOTO, VIDEO, FILE }

data class ChatUserProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val isOnline: Boolean = false,
    val lastSeen: String = "",
    val mediaTab: MediaTab = MediaTab.PHOTO,
    val mediaItems: List<String> = emptyList(),
    val error: String? = null,
)
