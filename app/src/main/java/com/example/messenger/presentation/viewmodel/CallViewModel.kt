package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.domain.model.CallSignal
import com.example.messenger.domain.model.CallStatus
import com.example.messenger.domain.service.CallEventListener
import com.example.messenger.domain.service.ICallService
import com.example.messenger.domain.service.ICallSignalingService
import com.example.messenger.presentation.state.CallUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class CallViewModel @Inject constructor(
    private val callService: ICallService,
    private val signalingService: ICallSignalingService,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    private val myUserId: String get() = auth.currentUser?.uid.orEmpty()
    private val myUid: Int get() = abs(myUserId.hashCode())

    private var currentCallId: String = ""
    private var tickerJob: Job? = null

    init {
        val partnerId: String = savedStateHandle["partnerId"] ?: ""
        val partnerName: String = savedStateHandle["partnerName"] ?: ""
        val partnerPhone: String = savedStateHandle["partnerPhone"] ?: ""

        val channelName = listOf(myUserId, partnerId).sorted().joinToString("_")

        _uiState.update {
            it.copy(partnerName = partnerName, partnerPhone = partnerPhone, channelName = channelName)
        }

        setupAgoraListener()

        if (partnerId.isNotBlank()) {
            startOutgoingCall(partnerId, channelName)
        }
    }

    private fun setupAgoraListener() {
        callService.setEventListener(object : CallEventListener {
            override fun onRemoteUserJoined(uid: Int) {
                _uiState.update { it.copy(isActive = true, isIncoming = false) }
                startTicker()
            }

            override fun onRemoteUserLeft(uid: Int) {
                endCall()
            }

            override fun onError(code: Int) {
                _uiState.update { it.copy(error = "Call error: $code") }
            }
        })
    }

    private fun startOutgoingCall(partnerId: String, channelName: String) {
        currentCallId = UUID.randomUUID().toString()
        viewModelScope.launch {
            signalingService.sendCallSignal(
                CallSignal(
                    callId = currentCallId,
                    callerId = myUserId,
                    calleeId = partnerId,
                    channelName = channelName,
                    status = CallStatus.RINGING,
                )
            )
        }
        callService.joinChannel(channelName, myUid)
    }

    fun acceptCall() {
        val channelName = _uiState.value.channelName
        viewModelScope.launch {
            if (currentCallId.isNotBlank()) {
                signalingService.updateCallStatus(currentCallId, CallStatus.ACTIVE)
            }
        }
        callService.joinChannel(channelName, myUid)
        _uiState.update { it.copy(isIncoming = false, isActive = true, seconds = 0) }
        startTicker()
    }

    fun declineCall() {
        stopTicker()
        viewModelScope.launch {
            if (currentCallId.isNotBlank()) {
                signalingService.updateCallStatus(currentCallId, CallStatus.DECLINED)
            }
        }
        callService.leaveChannel()
        _uiState.update { CallUiState() }
    }

    fun endCall() {
        stopTicker()
        viewModelScope.launch {
            if (currentCallId.isNotBlank()) {
                signalingService.updateCallStatus(currentCallId, CallStatus.ENDED)
            }
        }
        callService.leaveChannel()
        _uiState.update { CallUiState() }
    }

    fun toggleMute() {
        val muted = !_uiState.value.muted
        callService.muteLocalAudio(muted)
        _uiState.update { it.copy(muted = muted) }
    }

    fun toggleSpeaker() {
        val speakerOn = !_uiState.value.speakerOn
        callService.setSpeakerphone(speakerOn)
        _uiState.update { it.copy(speakerOn = speakerOn) }
    }

    private fun startTicker() {
        stopTicker()
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(seconds = it.seconds + 1) }
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    override fun onCleared() {
        stopTicker()
        callService.leaveChannel()
        super.onCleared()
    }
}
