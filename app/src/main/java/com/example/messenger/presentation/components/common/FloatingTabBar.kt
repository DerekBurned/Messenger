package com.example.messenger.presentation.components.common

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.messenger.presentation.screens.ui.theme.MessengerShapes
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.messengerTokens
import kotlinx.coroutines.launch

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
    val tokens = messengerTokens
    Box(
        modifier = modifier
            .springEnter()
            .shadow(elevation = 6.dp, shape = MessengerShapes.tab, clip = false)
            .clip(MessengerShapes.tab)
            .background(tokens.trackFill)
            .padding(horizontal = 5.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Crossfade(
            targetState = isSearching,
            animationSpec = spring(stiffness = 700f),
            label = "tabBarSearch",
        ) { searching ->
            if (searching) {
                SearchRow(query = query, onQueryChange = onQueryChange, onClose = onSearchClose)
            } else {
                PillsRow(selected = selected, onSelect = onSelect, onSearch = onSearchOpen)
            }
        }
    }
}

@Composable
private fun PillsRow(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    onSearch: () -> Unit,
) {
    val tokens = messengerTokens
    val indicatorColor = tokens.pillFillSelected
    val pillBounds = remember { mutableStateMapOf<MainTab, Rect>() }
    val indicatorX = remember { Animatable(0f) }
    val indicatorWidth = remember { Animatable(0f) }
    val selectedBounds = pillBounds[selected]

    LaunchedEffect(selectedBounds) {
        val target = selectedBounds ?: return@LaunchedEffect
        if (indicatorWidth.value == 0f) {
            indicatorX.snapTo(target.left)
            indicatorWidth.snapTo(target.width)
        } else {
            launch { indicatorX.animateTo(target.left, MotionSprings.settle) }
            launch { indicatorWidth.animateTo(target.width, MotionSprings.settle) }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 36.dp)
            .drawBehind {
                if (indicatorWidth.value > 0f) {
                    drawRoundRect(
                        color = indicatorColor,
                        topLeft = Offset(indicatorX.value, 0f),
                        size = Size(indicatorWidth.value, size.height),
                        cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                    )
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MainTab.entries.forEach { tab ->
            val isSelected = tab == selected
            val interaction = remember { MutableInteractionSource() }
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color(0xFF1C1C1E) else tokens.textPrimary.copy(alpha = 0.7f),
                animationSpec = spring(stiffness = 700f),
                label = "tabTextColor",
            )
            Box(
                modifier = Modifier
                    .onGloballyPositioned { pillBounds[tab] = it.boundsInParent() }
                    .pressScale(interaction)
                    .clip(MessengerShapes.tab)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                    ) { onSelect(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.label,
                    color = textColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        val searchInteraction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .size(36.dp)
                .pressScale(searchInteraction, pressedScale = 0.88f)
                .clip(MessengerShapes.tab)
                .clickable(
                    interactionSource = searchInteraction,
                    indication = null,
                ) { onSearch() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = tokens.textPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
    }
}

@Composable
private fun SearchRow(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    val tokens = messengerTokens
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 36.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = tokens.textPrimary.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    text = "Search users",
                    color = tokens.textPrimary.copy(alpha = 0.5f),
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
        val closeInteraction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .size(36.dp)
                .pressScale(closeInteraction, pressedScale = 0.88f)
                .clip(MessengerShapes.tab)
                .clickable(
                    interactionSource = closeInteraction,
                    indication = null,
                ) { onClose() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close search",
                tint = tokens.textPrimary.copy(alpha = 0.8f),
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
