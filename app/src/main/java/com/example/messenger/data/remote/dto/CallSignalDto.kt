package com.example.messenger.data.remote.dto

import com.example.messenger.domain.model.CallSignal
import com.example.messenger.domain.model.CallStatus

data class CallSignalDto(
    val callId: String = "",
    val callerId: String = "",
    val calleeId: String = "",
    val channelName: String = "",
    val status: String = "RINGING"
)

fun CallSignalDto.toCallSignal() = CallSignal(
    callId = callId,
    callerId = callerId,
    calleeId = calleeId,
    channelName = channelName,
    status = runCatching { CallStatus.valueOf(status) }.getOrDefault(CallStatus.RINGING)
)

fun CallSignal.toDto() = CallSignalDto(
    callId = callId,
    callerId = callerId,
    calleeId = calleeId,
    channelName = channelName,
    status = status.name
)
