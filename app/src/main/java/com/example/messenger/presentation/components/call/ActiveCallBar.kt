package com.example.messenger.presentation.components.call

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.data.remote.call.ActiveCallHolder
import com.example.messenger.data.remote.call.CallForegroundService
import com.example.messenger.presentation.components.common.MessengerAvatar

private val BarBackground = Color(0xFF1F2A37)
private val RingGreen = Color(0xFF34C759)
private val EndRed = Color(0xFFE53935)

val LocalOpenActiveCall = staticCompositionLocalOf<() -> Unit> { {} }

@Composable
fun CallAwareTopBar(topBar: @Composable () -> Unit) {
    Column(modifier = Modifier.background(Color.Transparent)) {
        topBar()
        ActiveCallBar(onClick = LocalOpenActiveCall.current)
    }
}

@Composable
fun ActiveCallBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val active by ActiveCallHolder.state.collectAsStateWithLifecycle()
    val call = active ?: return
    if (!call.isActive) return
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(BarBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.Center) {
            MessengerAvatar(
                name = call.partnerName,
                photoUrl = null,
                size = 40.dp,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, RingGreen, CircleShape),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.partnerName.ifBlank { "On call" },
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CallWave(active = call.remotePresent)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = formatHms(call.seconds),
                    color = RingGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        CircleActionButton(
            icon = if (call.muted) Icons.Filled.MicOff else Icons.Filled.Mic,
            background = Color.White.copy(alpha = 0.12f),
            iconTint = Color.White,
            contentDescription = if (call.muted) "Unmute" else "Mute",
        ) { sendCallAction(context, CallForegroundService.ACTION_TOGGLE_MUTE) }
        Spacer(Modifier.width(10.dp))
        CircleActionButton(
            icon = Icons.Filled.CallEnd,
            background = EndRed,
            iconTint = Color.White,
            contentDescription = "End call",
        ) { sendCallAction(context, CallForegroundService.ACTION_END) }
    }
}

@Composable
private fun CircleActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    iconTint: Color,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun CallWave(active: Boolean) {
    val transition = rememberInfiniteTransition(label = "wave")
    val heights = (0 until 4).map { index ->
        transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 500 + index * 120),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "bar$index",
        )
    }
    Row(
        modifier = Modifier.height(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        heights.forEach { h ->
            val fraction = if (active) h.value else 0.25f
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(RingGreen),
            )
        }
    }
}

private fun sendCallAction(context: Context, action: String) {
    runCatching {
        context.startService(
            Intent(context, CallForegroundService::class.java).setAction(action),
        )
    }
}

private fun formatHms(s: Int): String {
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%d:%02d".format(m, sec)
}
