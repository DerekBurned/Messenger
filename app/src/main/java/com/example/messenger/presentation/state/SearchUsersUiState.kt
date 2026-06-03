package com.example.messenger.presentation.state

import com.example.messenger.domain.model.User
import com.example.messenger.presentation.base.UiState
import com.example.messenger.presentation.base.UiText

data class SearchUsersUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val error: UiText? = null,
    val isCreatingConversation: Boolean = false,
) : UiState
