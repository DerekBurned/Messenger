package com.example.messenger.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.messenger.presentation.screens.ui.theme.*
import com.example.messenger.data.ContactStatus

// ─── Avatar ────────────────────────────────────────────────────────────────────

private val avatarColors = listOf(
    Color(0xFF4B7BEC), Color(0xFF45AAF2), Color(0xFF26DE81),
    Color(0xFFFD9644), Color(0xFFA55EEA), Color(0xFFFC5C65),
    Color(0xFF2BCBBA), Color(0xFF45AAF2)
)

@Composable
fun Avatar(
    name: String,
    size: Dp = 46.dp,
    online: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colorIndex = (name.firstOrNull()?.code ?: 0) % avatarColors.size
    val bgColor = avatarColors[colorIndex]
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontSize = (size.value * 0.4f).sp
            )
        }
        if (online) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(OnlineGreen)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

// ─── Bottom Navigation ─────────────────────────────────────────────────────────

sealed class NavTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Contacts : NavTab("contacts", "contacts", Icons.Default.Person)
    object Calls    : NavTab("calls",    "calls",    Icons.Default.Phone)
    object Chats    : NavTab("chats",    "chats",    Icons.AutoMirrored.Filled.Message)
    object Settings : NavTab("settings", "settings", Icons.Default.Settings)
}

val bottomTabs = listOf(NavTab.Contacts, NavTab.Calls, NavTab.Chats, NavTab.Settings)

@Composable
fun BottomNav(navController: NavController, totalUnread: Int = 0) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = PrimaryBlue) {
        bottomTabs.forEach { tab ->
            val isActive = currentRoute == tab.route ||
                    (tab.route == "chats" && currentRoute?.startsWith("chat/") == true)

            NavigationBarItem(
                selected = isActive,
                onClick  = {
                    navController.navigate(tab.route) {
                        popUpTo("chats") { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon = {
                    BadgedBox(
                        badge = {
                            if (tab.route == "chats" && totalUnread > 0) {
                                Badge(containerColor = FailedRed) {
                                    Text(if (totalUnread > 99) "99+" else totalUnread.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (isActive) Color.White else Color.White.copy(alpha = 0.6f)
                        )
                    }
                },
                label = {
                    Text(
                        text  = tab.label,
                        color = if (isActive) Color.White else Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.White.copy(alpha = 0.2f)
                )
            )
        }
    }
}

// ─── Top App Bar ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(text = title, color = Color.White)
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
    )
}

// ─── Status Label ──────────────────────────────────────────────────────────────

fun contactStatusLabel(status: ContactStatus, lastSeen: String): String =
    when (status) {
        ContactStatus.ONLINE  -> "online"
        ContactStatus.OFFLINE -> lastSeen.ifBlank { "offline" }
        ContactStatus.DND     -> "do not disturb"
    }
