package com.example.messenger.presentation.components.chat

import com.example.messenger.presentation.components.common.MessengerAvatar
import com.example.messenger.presentation.components.common.PresenceIndicator
import com.example.messenger.presentation.components.common.sharedElementKey
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.messenger.domain.model.PresenceState
import androidx.compose.ui.tooling.preview.Preview
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.messengerTokens

@Composable
fun ChatTopBar(
    partnerName: String,
    presenceStatusText: String,
    statusAccent: Boolean,
    presenceState: PresenceState,
    sharedKeyPartnerId: String,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    val tokens = messengerTokens
    val chipFill = if (tokens.isDark) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.4f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)

                .background(chipFill)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = tokens.textPrimary,
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(26.dp))
                .background(chipFill)
                .clickable(onClick = onProfileClick)
                .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = partnerName.ifBlank { "Chat" },
                    color = tokens.textPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.sharedElementKey("name-$sharedKeyPartnerId"),
                )
                Text(
                    text = presenceStatusText,
                    color = if (statusAccent) tokens.callAccept else tokens.textPrimary.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(contentAlignment = Alignment.BottomEnd) {
                MessengerAvatar(
                    name = partnerName,
                    size = 40.dp,
                    modifier = Modifier.sharedElementKey("avatar-$sharedKeyPartnerId"),
                )
                PresenceIndicator(
                    state = presenceState,
                    size = 12.dp,
                    modifier = Modifier.padding(end = 2.dp, bottom = 2.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ChatTopBarPreview() {
    MessengerTheme {
        ChatTopBar(
            partnerName = "Alice",
            presenceStatusText = "online",
            statusAccent = true,
            presenceState = PresenceState.ONLINE,
            sharedKeyPartnerId = "preview",
            onBackClick = {},
            onProfileClick = {},
        )
    }
}
