package com.example.messenger.presentation.components.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.screens.ui.theme.messengerTokens
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.tooling.preview.Preview
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme

@Composable
fun UnreadMessagesDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = PrimaryBlue.copy(alpha = 0.4f))
        Text(
            text = "Unread messages",
            color = PrimaryBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = PrimaryBlue.copy(alpha = 0.4f))
    }
}

@Composable
fun DateDivider(label: String, modifier: Modifier = Modifier) {
    val tokens = messengerTokens
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = tokens.pillFill,
        ) {
            Text(
                text = label,
                color = tokens.textPrimary.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

@Preview
@Composable
private fun ChatDividersPreview() {
    MessengerTheme {
        Column {
            DateDivider(label = "Today")
            UnreadMessagesDivider()
        }
    }
}
