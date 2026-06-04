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
import com.example.messenger.domain.repository.IConversationRepository
import com.example.messenger.presentation.base.toUiText
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

sealed interface CallExit {
    data object Pop : CallExit
    data class OpenChat(
        val conversationId: String,
        val partnerId: String,
        val partnerName: String,
    ) : CallExit
}

@HiltViewModel
class CallViewModel @Inject constructor(
    application: Application,
    private val auth: FirebaseAuth,
    private val conversationRepository: IConversationRepository,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    private val _callExit = Channel<CallExit>(Channel.BUFFERED)
    val callExit = _callExit.receiveAsFlow()

    private val context: Context get() = getApplication()
    private var hadActiveCall = false

    private var lastCall: ActiveCallHolder.ActiveCall? = null
    private var wasAnswered = false

    private val pendingPartnerId: String = savedStateHandle["partnerId"] ?: ""
    private val pendingPartnerName: String = savedStateHandle["partnerName"] ?: ""
    private val pendingPartnerPhone: String = savedStateHandle["partnerPhone"] ?: ""

    val needsOutgoingStart: Boolean
        get() = ActiveCallHolder.snapshot() == null &&
            pendingPartnerId.isNotBlank() &&
            pendingPartnerId != RESUME_PARTNER_ID

    init {
        if (ActiveCallHolder.snapshot() == null) {
            
            _uiState.update { it.copy(partnerName = pendingPartnerName, partnerPhone = pendingPartnerPhone) }
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
                        routeExit(lastCall, wasAnswered)
                        lastCall = null
                        wasAnswered = false
                    }
                    return@collectLatest
                }
                hadActiveCall = true
                lastCall = active
                if (active.isActive) wasAnswered = true
                _uiState.value = CallUiState(
                    partnerName = active.partnerName,
                    partnerPhone = active.partnerPhone,
                    channelName = active.channelName,
                    isIncoming = active.isIncoming,
                    isActive = active.isActive,
                    remoteRinging = active.remoteRinging,
                    seconds = active.seconds,
                    muted = active.muted,
                    speakerOn = active.speakerOn,
                    connectionState = active.connectionState,
                    error = active.error?.toUiText(),
                )
            }
        }
    }

    private fun routeExit(call: ActiveCallHolder.ActiveCall?, answered: Boolean) {
        val myUid = auth.currentUser?.uid.orEmpty()
        val partnerId = call?.callerId.orEmpty()
        if (call == null || !call.wasIncoming || !answered || myUid.isBlank() || partnerId.isBlank()) {
            _callExit.trySend(CallExit.Pop)
            return
        }
        viewModelScope.launch {
            val conversationId = runCatching {
                conversationRepository.createConversation(listOf(myUid, partnerId)).getOrNull()?.id
            }.getOrNull()
            if (conversationId.isNullOrBlank()) {
                _callExit.trySend(CallExit.Pop)
            } else {
                _callExit.trySend(CallExit.OpenChat(conversationId, partnerId, call.partnerName))
            }
        }
    }

    fun startOutgoing() {
        val myUserId = auth.currentUser?.uid.orEmpty()
        if (myUserId.isBlank()) {
            Log.w(TAG, "startOutgoing aborted: no signed-in user")
            return
        }
        if (pendingPartnerId.isBlank() || pendingPartnerId == RESUME_PARTNER_ID) {
            Log.w(TAG, "startOutgoing aborted: no real partner id (was '$pendingPartnerId')")
            return
        }
        val callId = UUID.randomUUID().toString()
        val channelName = "call-" + UUID.randomUUID().toString()
        Log.d(TAG, "startOutgoing")
        val intent = CallForegroundService.outgoingIntent(
            ctx = context,
            callId = callId,
            callerId = myUserId,
            calleeId = pendingPartnerId,
            channelName = channelName,
            partnerName = pendingPartnerName,
            partnerPhone = pendingPartnerPhone,
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
        runCatching { context.startService(intent) }
            .onFailure { Log.w(TAG, "sendAction $action failed", it) }
    }

    private companion object {
        const val TAG = "CallViewModel"

        const val RESUME_PARTNER_ID = "active"
    }
}
