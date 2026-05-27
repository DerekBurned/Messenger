package com.example.messenger.presentation.state

import com.example.messenger.domain.service.CallConnectionState

data class CallUiState(
    val partnerName: String = "",
    val partnerPhone: String = "",
    val channelName: String = "",
    val isIncoming: Boolean = false,
    val isActive: Boolean = false,
    val seconds: Int = 0,
    val speakerOn: Boolean = false,
    val muted: Boolean = false,
    val connectionState: CallConnectionState = CallConnectionState.CONNECTING,
    val error: String? = null,
)
