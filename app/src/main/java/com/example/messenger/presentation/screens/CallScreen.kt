package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.data.Contact
import com.example.messenger.presentation.components.Avatar
import com.example.messenger.presentation.screens.ui.theme.BluePrimary
import com.example.messenger.presentation.viewmodel.ActiveCall
import kotlinx.coroutines.delay

/**
 * Overlay shown during incoming / active calls
 * data-figma-name: incoming_call / active_call
 */
@Composable
fun CallScreen(
    activeCall: ActiveCall?,
    incomingCall: Contact?,
    onEndCall: () -> Unit,
    onAcceptCall: () -> Unit,
    onDeclineCall: () -> Unit
) {
    // Incoming call overlay
    if (incomingCall != null && activeCall == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BluePrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Avatar(name = incomingCall.name, size = 100.dp)
                    Spacer(Modifier.height(16.dp))
                    Text(text = incomingCall.name, color = Color.White, fontSize = 24.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(text = incomingCall.phone, color = Color.White.copy(0.7f), fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(text = "Incoming call...", color = Color.White.copy(0.6f), fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Decline
                    CallControlButton(
                        icon = Icons.Default.CallEnd,
                        label = "Decline",
                        backgroundColor = Color.Red,
                        onClick = onDeclineCall
                    )
                    // Accept
                    CallControlButton(
                        icon = Icons.Default.Call,
                        label = "Accept",
                        backgroundColor = Color(0xFF34C759),
                        onClick = onAcceptCall
                    )
                }
            }
        }
        return
    }

    // Active call
    if (activeCall == null) return

    var seconds by remember { mutableStateOf(0) }
    var speaker by remember { mutableStateOf(false) }
    var muted   by remember { mutableStateOf(false) }

    LaunchedEffect(activeCall) {
        while (true) {
            delay(1000)
            seconds++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BluePrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(name = activeCall.contact.name, size = 100.dp)
                Spacer(Modifier.height(16.dp))
                Text(text = activeCall.contact.name, color = Color.White, fontSize = 24.sp)
                Spacer(Modifier.height(4.dp))
                Text(text = activeCall.contact.phone, color = Color.White.copy(0.7f), fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = formatSeconds(seconds),
                    color = Color.White.copy(0.6f),
                    fontSize = 14.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speaker
                CallControlButton(
                    icon = Icons.Default.VolumeUp,
                    label = "Speaker",
                    backgroundColor = if (speaker) Color.White else Color.White.copy(0.2f),
                    iconTint = if (speaker) BluePrimary else Color.White,
                    onClick = { speaker = !speaker }
                )
                // End call
                CallControlButton(
                    icon = Icons.Default.CallEnd,
                    label = "End",
                    backgroundColor = Color.Red,
                    size = 68.dp,
                    onClick = onEndCall
                )
                // Mute
                CallControlButton(
                    icon = Icons.Default.MicOff,
                    label = "Mute",
                    backgroundColor = if (muted) Color.White else Color.White.copy(0.2f),
                    iconTint = if (muted) BluePrimary else Color.White,
                    onClick = { muted = !muted }
                )
            }
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
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(size)
                .background(backgroundColor, CircleShape)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = iconTint)
        }
        Spacer(Modifier.height(6.dp))
        Text(text = label, color = Color.White.copy(0.7f), fontSize = 12.sp)
    }
}

private fun formatSeconds(s: Int): String {
    val m = s / 60
    val sec = s % 60
    return "$m:${sec.toString().padStart(2, '0')}"
}
