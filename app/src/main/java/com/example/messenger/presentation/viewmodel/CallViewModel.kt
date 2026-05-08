package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.presentation.state.CallUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null

    fun receiveIncomingCall(partnerName: String, partnerPhone: String) {
        _uiState.update {
            it.copy(partnerName = partnerName, partnerPhone = partnerPhone, isIncoming = true, isActive = false)
        }
    }

    fun startCall(partnerName: String, partnerPhone: String) {
        _uiState.update {
            it.copy(partnerName = partnerName, partnerPhone = partnerPhone, isIncoming = false, isActive = true, seconds = 0)
        }
        startTicker()
    }

    fun acceptCall() {
        _uiState.update { it.copy(isIncoming = false, isActive = true, seconds = 0) }
        startTicker()
    }

    fun declineCall() {
        stopTicker()
        _uiState.update { CallUiState() }
    }

    fun endCall() {
        stopTicker()
        _uiState.update { CallUiState() }
    }

    fun toggleSpeaker() = _uiState.update { it.copy(speakerOn = !it.speakerOn) }
    fun toggleMute() = _uiState.update { it.copy(muted = !it.muted) }

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
        super.onCleared()
    }
}
