package com.example.messenger.presentation.state

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val username: String = "",
    val dob: String = "",
    val saveSuccess: Boolean = false,
    val error: String? = null,
)
