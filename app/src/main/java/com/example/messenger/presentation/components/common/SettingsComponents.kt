package com.example.messenger.presentation.components.common
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Surface
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.messenger.presentation.screens.ui.theme.Dimens
import com.example.messenger.presentation.screens.ui.theme.MessengerShapes
import com.example.messenger.presentation.screens.ui.theme.messengerTokens

@Composable
fun NavHeaderPill(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = messengerTokens
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = Dimens.screenPadding),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(Dimens.iconButton)
                .clip(CircleShape)
                .background(tokens.pillFill)
                .clickable { onBack() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = "Back",
                tint = tokens.textPrimary,
                modifier = Modifier.size(24.dp),
            )
        }
        Box(
            modifier = Modifier
                .clip(MessengerShapes.button)
                .background(tokens.pillFill)
                .padding(horizontal = 24.dp, vertical = 9.dp),
        ) {
            Text(
                text = title,
                color = tokens.textPrimary,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Composable
fun RoundedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tokens = messengerTokens
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MessengerShapes.card)
            .background(tokens.cardFill),
        content = content,
    )
}

@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.padding(start = 14.dp, top = 18.dp, bottom = 6.dp),
        color = messengerTokens.textMuted,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailingText: String? = null,
    titleColor: Color? = null,
    showChevron: Boolean = true,
) {
    val tokens = messengerTokens
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.rowHeight)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = title,
            color = titleColor ?: tokens.textOnField,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                color = tokens.textMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = tokens.textMuted.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = messengerTokens
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.rowHeight)
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = tokens.textOnField,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = tokens.accent,
                checkedBorderColor = tokens.accent,
            ),
        )
    }
}

@Composable
fun CardDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(start = 18.dp),
        thickness = 0.5.dp,
        color = messengerTokens.divider,
    )
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsComponentsPreview() {
    MessengerTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                NavHeaderPill(title = "Settings", onBack = {})
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(text = "Visibility")
                    RoundedCard {
                        SettingsRow(title = "Privacy", onClick = {})
                        CardDivider()
                        ToggleRow(title = "Dark theme", checked = true, onCheckedChange = {})
                    }
                }
            }
        }
    }
}
