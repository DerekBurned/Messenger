package com.example.messenger.presentation.state

import com.example.messenger.domain.model.User

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isEditing: Boolean = false,
    val updateSuccess: Boolean = false
)
