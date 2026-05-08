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
import com.example.messenger.presentation.screens.ui.theme.LightGray
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue

enum class CallsFilter { ALL, MISSED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreenContent(
    modifier: Modifier = Modifier,
) {
    var filter by remember { mutableStateOf(CallsFilter.ALL) }

    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            SegmentedButton(
                selected = filter == CallsFilter.ALL,
                onClick = { filter = CallsFilter.ALL },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) {
                Text("All")
            }
            SegmentedButton(
                selected = filter == CallsFilter.MISSED,
                onClick = { filter = CallsFilter.MISSED },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) {
                Text("Missed")
            }
        }

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
                    tint = LightGray,
                    modifier = Modifier.size(72.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (filter == CallsFilter.MISSED) {
                        "No missed calls"
                    } else {
                        "No calls yet"
                    },
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Voice / video calls coming soon",
                    color = Color.Gray,
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
