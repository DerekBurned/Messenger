package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.messengerTokens
import com.example.messenger.presentation.components.common.SegmentedToggle

enum class CallsFilter { ALL, MISSED }

@Composable
fun CallsScreenContent(
    modifier: Modifier = Modifier,
) {
    var filter by remember { mutableStateOf(CallsFilter.ALL) }
    val tokens = messengerTokens

    Column(modifier = modifier.fillMaxSize()) {
        SegmentedToggle(
            options = listOf("All", "Missed"),
            selectedIndex = if (filter == CallsFilter.ALL) 0 else 1,
            onSelect = { filter = if (it == 0) CallsFilter.ALL else CallsFilter.MISSED },
            selectedContentColors = listOf(Color(0xFF1C1C1E), tokens.danger),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (filter == CallsFilter.MISSED) {
                        Icons.Filled.CallMissed
                    } else {
                        Icons.Filled.Call
                    },
                    contentDescription = null,
                    tint = tokens.textPrimary.copy(alpha = 0.4f),
                    modifier = Modifier.size(72.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (filter == CallsFilter.MISSED) "No missed calls" else "No calls yet",
                    color = tokens.textPrimary.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Voice / video calls coming soon",
                    color = tokens.textPrimary.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CallsScreenContentPreview() {
    MessengerTheme {
        CallsScreenContent()
    }
}
