package com.example.messenger.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.messenger.presentation.screens.ui.theme.OnlineGreen

@Composable
fun TypingIndicator(
    usernames: List<String>,
    modifier: Modifier = Modifier
) {
    if (usernames.isEmpty()) return

    val text = when {
        usernames.size == 1 -> "${usernames[0]} is typing"
        usernames.size == 2 -> "${usernames[0]} and ${usernames[1]} are typing"
        else -> "${usernames[0]} and ${usernames.size - 1} others are typing"
    }

    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = OnlineGreen
        )
        BouncingDots()
    }
}

@Composable
private fun BouncingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { index ->
            val delay = index * 200
            val offset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, delayMillis = delay, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .offset(y = offset.dp)
                    .background(OnlineGreen, CircleShape)
            )
        }
    }
}
