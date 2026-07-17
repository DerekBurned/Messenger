package com.example.messenger.presentation.components.common
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
    val visible = state != PresenceState.OFFLINE
    var displayColor by remember { mutableStateOf(OnlineGreen) }
    when (state) {
        PresenceState.ONLINE -> displayColor = OnlineGreen
        PresenceState.AWAY -> displayColor = AwayYellow
        PresenceState.OFFLINE -> Unit
    }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = MotionSprings.pop,
        label = "presencePop",
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .border(borderWidth, Color.White, CircleShape)
            .background(displayColor, CircleShape)
    )
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PresenceIndicatorPreview() {
    MessengerTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PresenceIndicator(state = PresenceState.ONLINE, size = 16.dp)
                PresenceIndicator(state = PresenceState.AWAY, size = 16.dp)
            }
        }
    }
}
