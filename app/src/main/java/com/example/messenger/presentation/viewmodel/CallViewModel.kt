package com.example.messenger.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.messenger.data.remote.call.ActiveCallHolder
import com.example.messenger.data.remote.call.CallForegroundService
import com.example.messenger.presentation.state.CallUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    application: Application,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    private val _callEnded = Channel<Unit>(Channel.BUFFERED)
    val callEnded = _callEnded.receiveAsFlow()

    private val context: Context get() = getApplication()
    private var hadActiveCall = false

    init {
        val partnerId: String = savedStateHandle["partnerId"] ?: ""
        val partnerName: String = savedStateHandle["partnerName"] ?: ""
        val partnerPhone: String = savedStateHandle["partnerPhone"] ?: ""

        val existing = ActiveCallHolder.snapshot()
        if (existing == null && partnerId.isNotBlank()) {
            startOutgoing(partnerId, partnerName, partnerPhone)
        } else if (existing == null) {
            
            _uiState.update { it.copy(partnerName = partnerName, partnerPhone = partnerPhone) }
        }

        observeActiveCall()
    }

    private fun observeActiveCall() {
        viewModelScope.launch {
            ActiveCallHolder.state.collectLatest { active ->
                if (active == null) {
                    
                    _uiState.value = CallUiState()
                    if (hadActiveCall) {
                        hadActiveCall = false
                        _callEnded.trySend(Unit)
                    }
                    return@collectLatest
                }
                hadActiveCall = true
                _uiState.value = CallUiState(
                    partnerName = active.partnerName,
                    partnerPhone = active.partnerPhone,
                    channelName = active.channelName,
                    isIncoming = active.isIncoming,
                    isActive = active.isActive,
                    seconds = active.seconds,
                    muted = active.muted,
                    speakerOn = active.speakerOn,
                    connectionState = active.connectionState,
                    error = active.error,
                )
            }
        }
    }

    private fun startOutgoing(partnerId: String, partnerName: String, partnerPhone: String) {
        val myUserId = auth.currentUser?.uid.orEmpty()
        if (myUserId.isBlank()) {
            Log.w(TAG, "startOutgoing aborted: no signed-in user")
            return
        }
        val callId = UUID.randomUUID().toString()
        val channelName = "call-" + UUID.randomUUID().toString()
        Log.d(TAG, "startOutgoing -> partnerId=$partnerId callId=$callId")
        val intent = CallForegroundService.outgoingIntent(
            ctx = context,
            callId = callId,
            callerId = myUserId,
            calleeId = partnerId,
            channelName = channelName,
            partnerName = partnerName,
            partnerPhone = partnerPhone,
        )
        ContextCompat.startForegroundService(context, intent)
    }

    fun acceptCall() = sendAction(CallForegroundService.ACTION_ACCEPT)
    fun declineCall() = sendAction(CallForegroundService.ACTION_DECLINE)
    fun endCall() = sendAction(CallForegroundService.ACTION_END)
    fun toggleMute() = sendAction(CallForegroundService.ACTION_TOGGLE_MUTE)
    fun toggleSpeaker() = sendAction(CallForegroundService.ACTION_TOGGLE_SPEAKER)

    private fun sendAction(action: String) {
        Log.d(TAG, "sendAction $action")
        val intent = android.content.Intent(context, CallForegroundService::class.java)
            .setAction(action)
        ContextCompat.startForegroundService(context, intent)
    }

    private companion object {
        const val TAG = "CallViewModel"
    }
}
