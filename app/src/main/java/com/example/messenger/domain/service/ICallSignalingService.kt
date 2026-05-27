package com.example.messenger.domain.service

import com.example.messenger.domain.model.CallSignal
import com.example.messenger.domain.model.CallStatus
import kotlinx.coroutines.flow.Flow

interface ICallSignalingService {
    
    suspend fun sendCallSignal(signal: CallSignal)

    fun observeIncomingCall(myUserId: String): Flow<CallSignal?>

    suspend fun updateCallStatus(calleeId: String, callId: String, status: CallStatus)
}
