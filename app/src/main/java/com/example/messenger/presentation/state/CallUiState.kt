package com.example.messenger.presentation.state

data class CallUiState(
    val partnerName: String = "",
    val partnerPhone: String = "",
    val isIncoming: Boolean = false,
    val isActive: Boolean = false,
    val seconds: Int = 0,
    val speakerOn: Boolean = false,
    val muted: Boolean = false,
)
