package com.example.messenger.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import com.example.messenger.presentation.components.common.WallpaperBackground
import com.example.messenger.presentation.components.common.MessengerAvatar
import com.example.messenger.presentation.components.call.CallControlButton
import com.example.messenger.presentation.screens.ui.theme.messengerTokens
import com.example.messenger.presentation.screens.ui.theme.MessengerShapes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.domain.service.CallConnectionState
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.state.CallUiState
import com.example.messenger.presentation.viewmodel.CallExit
import com.example.messenger.presentation.viewmodel.CallViewModel

@Composable
fun CallScreen(
    viewModel: CallViewModel = hiltViewModel(),
    onCallEnded: () -> Unit = {},
    onOpenChat: (conversationId: String, partnerId: String, partnerName: String) -> Unit = { _, _, _ -> },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingMicAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var micDenied by remember { mutableStateOf(false) }

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            micDenied = false
            pendingMicAction?.invoke()
        } else {
            micDenied = true
        }
        pendingMicAction = null
    }

    fun withMic(action: () -> Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            micDenied = false
            action()
        } else {
            pendingMicAction = action
            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(viewModel) {
        if (viewModel.needsOutgoingStart) withMic { viewModel.startOutgoing() }
    }

    LaunchedEffect(viewModel) {
        viewModel.callExit.collect { exit ->
            when (exit) {
                CallExit.Pop -> onCallEnded()
                is CallExit.OpenChat -> onOpenChat(exit.conversationId, exit.partnerId, exit.partnerName)
            }
        }
    }

    CallScreenContent(
        state = state,
        micDenied = micDenied,
        onAccept = { withMic { viewModel.acceptCall() } },
        onDecline = { viewModel.declineCall() },
        onEnd = { viewModel.endCall() },
        onToggleSpeaker = viewModel::toggleSpeaker,
        onToggleMute = viewModel::toggleMute,
    )
}

@Composable
private fun CallScreenContent(
    state: CallUiState,
    micDenied: Boolean = false,
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
    onEnd: () -> Unit = {},
    onToggleSpeaker: () -> Unit = {},
    onToggleMute: () -> Unit = {},
) {
    val tokens = messengerTokens
    WallpaperBackground {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CallAvatar(name = state.partnerName)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = state.partnerName.ifBlank { "Calling…" },
                    color = tokens.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (state.partnerPhone.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(state.partnerPhone, color = tokens.textPrimary.copy(0.7f), fontSize = 16.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = callStatusText(state),
                    color = if (state.isActive && state.remotePresent) tokens.callAccept
                    else tokens.textPrimary.copy(0.7f),
                    fontSize = 16.sp,
                )
                if (micDenied) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Microphone permission is required to talk.",
                        color = tokens.danger,
                        fontSize = 13.sp,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(MessengerShapes.card)
                    .background(tokens.pillFill)
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.isIncoming) {
                    CallControlButton(Icons.Default.CallEnd, "Decline", tokens.danger, onClick = onDecline)
                    CallControlButton(Icons.Default.Call, "Accept", tokens.callAccept, onClick = onAccept)
                } else {
                    CallControlButton(
                        Icons.Default.VolumeUp,
                        "Speaker",
                        if (state.speakerOn) tokens.accent else tokens.neutralButtonFill,
                        iconTint = if (state.speakerOn) tokens.onAccent else tokens.textPrimary,
                        onClick = onToggleSpeaker,
                    )
                    CallControlButton(Icons.Default.CallEnd, "End", tokens.danger, size = 68.dp, onClick = onEnd)
                    CallControlButton(
                        Icons.Default.MicOff,
                        "Mute",
                        if (state.muted) tokens.accent else tokens.neutralButtonFill,
                        iconTint = if (state.muted) tokens.onAccent else tokens.textPrimary,
                        onClick = onToggleMute,
                    )
                }
            }
        }
    }
}

internal fun callStatusText(state: CallUiState): String = when {
    state.callEnded -> "Call Ended"
    state.isIncoming -> "Incoming call…"
    state.isActive && state.connectionState == CallConnectionState.CONNECTED && state.remotePresent ->
        formatSeconds(state.seconds)
    state.isActive -> "Connecting…"
    state.connectionState == CallConnectionState.FAILED -> "Call failed"
    state.connectionState == CallConnectionState.DISCONNECTED -> "Disconnected"
    state.remoteRinging -> "Ringing…"
    else -> "Requesting…"
}

@Composable
private fun CallAvatar(name: String) {
    MessengerAvatar(name = name, size = 121.dp)
}


private fun formatSeconds(s: Int): String {
    val m = s / 60
    val sec = s % 60
    return "$m:${sec.toString().padStart(2, '0')}"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CallScreenIncomingPreview() {
    MessengerTheme {
        CallScreenContent(
            state = CallUiState(
                partnerName = "Alice",
                partnerPhone = "+1 555 0100",
                isIncoming = true,
            ),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CallScreenActivePreview() {
    MessengerTheme {
        CallScreenContent(
            state = CallUiState(
                partnerName = "Alice",
                partnerPhone = "+1 555 0100",
                isActive = true,
                seconds = 73,
                connectionState = CallConnectionState.CONNECTED,
            ),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CallScreenRingingPreview() {
    MessengerTheme {
        CallScreenContent(
            state = CallUiState(
                partnerName = "Alice",
                partnerPhone = "+1 555 0100",
                remoteRinging = true,
                connectionState = CallConnectionState.CONNECTED,
            ),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CallScreenRequestingPreview() {
    MessengerTheme {
        CallScreenContent(
            state = CallUiState(
                partnerName = "Alice",
                partnerPhone = "+1 555 0100",
                connectionState = CallConnectionState.CONNECTED,
            ),
        )
    }
}
