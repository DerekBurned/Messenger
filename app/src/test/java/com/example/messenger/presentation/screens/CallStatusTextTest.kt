package com.example.messenger.presentation.screens

import com.example.messenger.domain.service.CallConnectionState
import com.example.messenger.presentation.state.CallUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class  CallStatusTextTest {

    @Test
    fun `incoming call shows incoming regardless of other flags`() {
        val state = CallUiState(
            isIncoming = true,
            remoteRinging = true,
            connectionState = CallConnectionState.CONNECTED,
        )
        assertEquals("Incoming call…", callStatusText(state))
    }

    @Test
    fun `connected but no ringing ack is still Requesting`() {

        val state = CallUiState(connectionState = CallConnectionState.CONNECTED)
        assertEquals("Requesting…", callStatusText(state))
    }

    @Test
    fun `still connecting socket with no ack is Requesting`() {
        val state = CallUiState(connectionState = CallConnectionState.CONNECTING)
        assertEquals("Requesting…", callStatusText(state))
    }

    @Test
    fun `ringing ack flips the label to Ringing`() {
        val state = CallUiState(
            remoteRinging = true,
            connectionState = CallConnectionState.CONNECTED,
        )
        assertEquals("Ringing…", callStatusText(state))
    }

    @Test
    fun `accepted but media not yet flowing is Connecting`() {
        val state = CallUiState(
            isActive = true,
            remoteRinging = true,
            connectionState = CallConnectionState.CONNECTING,
        )
        assertEquals("Connecting…", callStatusText(state))
    }

    @Test
    fun `active and connected shows the elapsed timer`() {
        val state = CallUiState(
            isActive = true,
            seconds = 73,
            connectionState = CallConnectionState.CONNECTED,
        )
        assertEquals("1:13", callStatusText(state))
    }

    @Test
    fun `failed connection shows call failed`() {
        val state = CallUiState(connectionState = CallConnectionState.FAILED)
        assertEquals("Call failed", callStatusText(state))
    }

    @Test
    fun `disconnected shows disconnected`() {
        val state = CallUiState(connectionState = CallConnectionState.DISCONNECTED)
        assertEquals("Disconnected", callStatusText(state))
    }
}
