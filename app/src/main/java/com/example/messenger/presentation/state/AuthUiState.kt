package com.example.messenger.presentation.state

import com.example.messenger.domain.model.User

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val codeSent: Boolean = false,
    val loginSuccess: Boolean = false,
    val registerSuccess: Boolean = false
)
