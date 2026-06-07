package com.example.messenger.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

enum class MainTab(val icon: ImageVector, val label: String) {
    CHATS(Icons.Filled.Email, "Chats"),
    CALLS(Icons.Filled.Call, "Calls"),
    CONTACTS(Icons.Filled.Person, "Contacts"),
    SETTINGS(Icons.Filled.Settings, "Settings")
}

private val ItemWidth = 60.dp
private val ItemHeight = 52.dp
private val BarPadding = 6.dp

@Composable
fun FloatingTabBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = MainTab.entries
    val scope = rememberCoroutineScope()
    val itemWidthPx = with(LocalDensity.current) { ItemWidth.toPx() }
    val maxOffsetPx = itemWidthPx * (tabs.lastIndex)

    val indicatorOffset = remember { Animatable(selected.ordinal * itemWidthPx) }

    LaunchedEffect(selected, itemWidthPx) {
        indicatorOffset.animateTo(
            targetValue = selected.ordinal * itemWidthPx,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        )
    }

    Surface(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(percent = 50),
        color = PrimaryBlue,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(BarPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.pointerInput(itemWidthPx) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                indicatorOffset.snapTo(
                                    (indicatorOffset.value + dragAmount).coerceIn(0f, maxOffsetPx),
                                )
                            }
                        },
                        onDragEnd = {
                            val index = (indicatorOffset.value / itemWidthPx)
                                .roundToInt()
                                .coerceIn(0, tabs.lastIndex)
                            scope.launch {
                                indicatorOffset.animateTo(
                                    index * itemWidthPx,
                                    spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                )
                            }
                            onSelect(tabs[index])
                        },
                    )
                },
            ) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(indicatorOffset.value.roundToInt(), 0) }
                        .size(width = ItemWidth, height = ItemHeight)
                        .background(
                            color = Color.White.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(percent = 50),
                        ),
                )
                Row {
                    tabs.forEach { tab ->
                        TabItem(
                            tab = tab,
                            selected = tab == selected,
                            onClick = {
                                scope.launch {
                                    indicatorOffset.animateTo(
                                        tab.ordinal * itemWidthPx,
                                        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                    )
                                }
                                onSelect(tab)
                            },
                        )
                    }
                }
            }

        }
    }
}

@Composable
private fun TabItem(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    TabIcon(
        icon = tab.icon,
        label = tab.label,
        tint = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
        onClick = onClick,
    )
}

@Composable
private fun TabIcon(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(width = ItemWidth, height = ItemHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint)
    }
}

@Preview
@Composable
private fun FloatingTabBarPreview() {
    MessengerTheme {
        Box(Modifier.padding(24.dp)) {
            FloatingTabBar(
                selected = MainTab.CALLS,
                onSelect = {}
            )
        }
    }
}
