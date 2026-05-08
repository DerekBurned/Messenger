package com.example.messenger.presentation.state

data class EditContactDataUiState(
    val contactId: String = "",
    val name: String = "",
    val initialName: String = "",
    val isSaving: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val saveSuccess: Boolean = false,
    val deleted: Boolean = false,
    val error: String? = null,
)
