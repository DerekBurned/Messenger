package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.state.CallUiState
import com.example.messenger.presentation.viewmodel.CallViewModel

@Composable
fun CallScreen(
    viewModel: CallViewModel = hiltViewModel(),
    onCallEnded: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CallScreenContent(
        state = state,
        onAccept = viewModel::acceptCall,
        onDecline = {
            viewModel.declineCall()
            onCallEnded()
        },
        onEnd = {
            viewModel.endCall()
            onCallEnded()
        },
        onToggleSpeaker = viewModel::toggleSpeaker,
        onToggleMute = viewModel::toggleMute,
    )
}

@Composable
private fun CallScreenContent(
    state: CallUiState,
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
    onEnd: () -> Unit = {},
    onToggleSpeaker: () -> Unit = {},
    onToggleMute: () -> Unit = {},
) {
    if (!state.isActive && !state.isIncoming) {
        Box(Modifier.fillMaxSize().background(PrimaryBlue), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(PrimaryBlue)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CallAvatar(name = state.partnerName)
                Spacer(Modifier.height(16.dp))
                Text(state.partnerName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(state.partnerPhone, color = Color.White.copy(0.7f), fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (state.isIncoming) "Incoming call..." else formatSeconds(state.seconds),
                    color = Color.White.copy(0.6f),
                    fontSize = 14.sp,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.isIncoming) {
                    CallControlButton(Icons.Default.CallEnd, "Decline", Color.Red, onClick = onDecline)
                    CallControlButton(Icons.Default.Call, "Accept", Color(0xFF34C759), onClick = onAccept)
                } else {
                    CallControlButton(
                        Icons.Default.VolumeUp,
                        "Speaker",
                        if (state.speakerOn) Color.White else Color.White.copy(0.2f),
                        iconTint = if (state.speakerOn) PrimaryBlue else Color.White,
                        onClick = onToggleSpeaker,
                    )
                    CallControlButton(Icons.Default.CallEnd, "End", Color.Red, size = 68.dp, onClick = onEnd)
                    CallControlButton(
                        Icons.Default.MicOff,
                        "Mute",
                        if (state.muted) Color.White else Color.White.copy(0.2f),
                        iconTint = if (state.muted) PrimaryBlue else Color.White,
                        onClick = onToggleMute,
                    )
                }
            }
        }
    }
}

@Composable
private fun CallAvatar(name: String) {
    Box(
        modifier = Modifier.size(120.dp).background(Color.White.copy(0.2f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        val initial = name.take(1).uppercase()
        if (initial.isNotBlank()) {
            Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 48.sp)
        } else {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
        }
    }
}

@Composable
private fun CallControlButton(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    iconTint: Color = Color.White,
    size: Dp = 58.dp,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(size).background(backgroundColor, CircleShape),
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = iconTint)
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = Color.White.copy(0.7f), fontSize = 12.sp)
    }
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
            ),
        )
    }
}
