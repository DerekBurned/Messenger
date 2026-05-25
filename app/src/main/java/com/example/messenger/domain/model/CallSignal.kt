package com.example.messenger.domain.model

data class CallSignal(
    val callId: String = "",
    val callerId: String = "",
    val calleeId: String = "",
    val channelName: String = "",
    val status: CallStatus = CallStatus.RINGING
)

enum class CallStatus { RINGING, ACTIVE, DECLINED, ENDED }
