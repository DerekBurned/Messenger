package com.example.messenger.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.messenger.domain.model.PresenceState
import com.example.messenger.presentation.screens.ui.theme.AwayYellow
import com.example.messenger.presentation.screens.ui.theme.OnlineGreen

@Composable
fun PresenceIndicator(
    state: PresenceState,
    modifier: Modifier = Modifier,
    size: Dp = 12.dp,
    borderWidth: Dp = 2.dp
) {
    if (state == PresenceState.OFFLINE) return

    val color = when (state) {
        PresenceState.ONLINE -> OnlineGreen
        PresenceState.AWAY -> AwayYellow
        PresenceState.OFFLINE -> return
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(borderWidth, Color.White, CircleShape)
            .background(color, CircleShape)
    )
}
