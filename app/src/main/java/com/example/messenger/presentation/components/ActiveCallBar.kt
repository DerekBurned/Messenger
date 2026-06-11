package com.example.messenger.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.data.remote.call.ActiveCallHolder
import com.example.messenger.domain.service.CallConnectionState

private val ActiveBarGreen = Color(0xFF2E7D32)

val LocalOpenActiveCall = staticCompositionLocalOf<() -> Unit> { {} }

@Composable
fun CallAwareTopBar(topBar: @Composable () -> Unit) {
    Column {
        topBar()
        ActiveCallBar(onClick = LocalOpenActiveCall.current)
    }
}

@Composable
fun ActiveCallBar(onClick: () -> Unit) {
    val active by ActiveCallHolder.state.collectAsStateWithLifecycle()
    val call = active ?: return
    if (!call.isActive) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(ActiveBarGreen)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = call.partnerName.ifBlank { "On call" },
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = formatHms(call.seconds),
            color = Color.White.copy(0.9f),
            fontSize = 13.sp,
        )
        Spacer(Modifier.weight(1f))
        ConnectionQualityBadge(call.connectionState)
    }
}

@Composable
private fun ConnectionQualityBadge(state: CallConnectionState) {
    val (label, dotColor) = when (state) {
        CallConnectionState.CONNECTED -> "Good" to Color(0xFFB9F6CA)
        CallConnectionState.CONNECTING -> "Connecting" to Color(0xFFFFF59D)
        CallConnectionState.RECONNECTING -> "Reconnecting" to Color(0xFFFFCC80)
        CallConnectionState.FAILED -> "Failed" to Color(0xFFFF8A80)
        CallConnectionState.DISCONNECTED -> "Disconnected" to Color(0xFFFF8A80)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, shape = androidx.compose.foundation.shape.CircleShape),
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White, fontSize = 12.sp)
    }
}

private fun formatHms(s: Int): String {
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) {
        "%d:%02d:%02d".format(h, m, sec)
    } else {
        "%d:%02d".format(m, sec)
    }
}
