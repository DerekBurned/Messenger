package com.example.messenger.presentation.components.common

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.TextOnFieldDark
import com.example.messenger.presentation.screens.ui.theme.messengerTokens

enum class MainTab(val label: String) {
    CHATS("Chats"),
    CALLS("Calls"),
    SETTINGS("Settings"),
}

@Composable
fun FloatingTabBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    isSearching: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchOpen: () -> Unit,
    onSearchClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Crossfade(
            targetState = isSearching,
            animationSpec = spring(stiffness = 700f),
            label = "tabBarSearch",
        ) { searching ->
            if (searching) {
                SearchBar(query = query, onQueryChange = onQueryChange, onClose = onSearchClose)
            } else {
                SegmentedTabs(selected = selected, onSelect = onSelect, onSearch = onSearchOpen)
            }
        }
    }
}

@Composable
private fun SegmentedTabs(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    onSearch: () -> Unit,
) {
    val tokens = messengerTokens
    val barShape = RoundedCornerShape(24.dp)
    val pillColor = tokens.pillFillSelected
    val selectedIndex = MainTab.entries.indexOf(selected).coerceAtLeast(0)
    val indicatorPosition by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
        label = "tabIndicator",
    )

    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(barShape)
                .background(tokens.trackFill)
                .border(1.dp, tokens.panelBorder, barShape)
                .padding(5.dp)
                .drawBehind {
                    val segmentWidth = size.width / MainTab.entries.size
                    drawRoundRect(
                        color = pillColor,
                        topLeft = Offset(indicatorPosition * segmentWidth, 0f),
                        size = Size(segmentWidth, size.height),
                        cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                    )
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MainTab.entries.forEach { tab ->
                val isSelected = tab == selected
                val interaction = remember { MutableInteractionSource() }
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) TextOnFieldDark else tokens.textPrimary.copy(alpha = 0.72f),
                    animationSpec = spring(stiffness = 700f),
                    label = "tabTextColor",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pressScale(interaction)
                        .clip(RoundedCornerShape(19.dp))
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                        ) { onSelect(tab) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tab.label,
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        val searchInteraction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .size(48.dp)
                .pressScale(searchInteraction, pressedScale = 0.88f)
                .clip(CircleShape)
                .background(tokens.trackFill)
                .clickable(
                    interactionSource = searchInteraction,
                    indication = null,
                ) { onSearch() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = tokens.textPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    val tokens = messengerTokens
    val barShape = RoundedCornerShape(24.dp)
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(barShape)
            .background(tokens.trackFill)
            .border(1.dp, tokens.panelBorder, barShape)
            .padding(start = 18.dp, end = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = tokens.textMuted,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    text = "Search chats and messages",
                    color = tokens.textMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = tokens.textPrimary),
                cursorBrush = SolidColor(tokens.accent),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            )
        }
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(tokens.pillFill)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onClose() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close search",
                tint = tokens.textPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview
@Composable
private fun FloatingTabBarPreview() {
    MessengerTheme {
        Box(modifier = Modifier.padding(24.dp)) {
            FloatingTabBar(
                selected = MainTab.CHATS,
                onSelect = {},
                isSearching = false,
                query = "",
                onQueryChange = {},
                onSearchOpen = {},
                onSearchClose = {},
            )
        }
    }
}
