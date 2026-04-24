package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.data.UserProfile
import com.example.messenger.presentation.components.Avatar
import com.example.messenger.presentation.components.BottomNav
import com.example.messenger.presentation.screens.ui.theme.*

/**
 * data-figma-name: settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    user: UserProfile,
    darkMode: Boolean,
    totalUnread: Int,
    onToggleDarkMode: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChangeAccount: () -> Unit,
    navController: androidx.navigation.NavController
) {
    var showLanguage by remember { mutableStateOf(false) }
    var showDevices  by remember { mutableStateOf(false) }
    var selectedLang by remember { mutableStateOf("English") }

    val languages = listOf("English","Russian","Spanish","French","German","Chinese","Arabic","Polish")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        },
        bottomBar = { BottomNav(navController = navController, totalUnread = totalUnread) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(GrayBg)
                .verticalScroll(rememberScrollState())
        ) {
            // ── User info ──────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    Avatar(name = user.name, size = 70.dp)
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(BluePrimary)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Change photo",
                            tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(text = user.name, color = TextPrimary, fontSize = 17.sp)
                Text(text = user.phone, color = TextSecondary, fontSize = 14.sp)
            }

            Spacer(Modifier.height(12.dp))

            // ── Group 1: Photo + Profile ───────────────────────────────────────
            SettingsGroup {
                SettingsItem(
                    icon  = Icons.Default.AccountCircle,
                    label = "Change Photo",
                    onClick = {}
                )
                SettingsItem(
                    icon  = Icons.Default.Person,
                    label = "My Profile",
                    onClick = onNavigateToProfile
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Group 2: Account, Theme, Language, Devices ────────────────────
            SettingsGroup {
                SettingsItem(
                    icon  = Icons.Default.SupervisedUserCircle,
                    label = "Change Account",
                    onClick = onNavigateToChangeAccount
                )
                SettingsItem(
                    icon  = Icons.Default.WbSunny,
                    label = "Switch Theme",
                    onClick = onToggleDarkMode,
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WbSunny, null,
                                tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Switch(
                                checked = darkMode,
                                onCheckedChange = { onToggleDarkMode() },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White,
                                    checkedTrackColor = BluePrimary)
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.NightlightRound, null,
                                tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                )
                SettingsItem(
                    icon  = Icons.Default.Language,
                    label = "Language",
                    onClick = { showLanguage = true },
                    trailing = { Text(selectedLang, color = TextSecondary, fontSize = 14.sp) }
                )
                SettingsItem(
                    icon  = Icons.Default.Devices,
                    label = "Devices",
                    onClick = { showDevices = true }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Language modal ─────────────────────────────────────────────────────────
    if (showLanguage) {
        ModalBottomSheetPicker(
            title    = "Language",
            onClose  = { showLanguage = false }
        ) {
            languages.forEach { lang ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedLang = lang; showLanguage = false }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(lang, color = TextPrimary)
                    if (selectedLang == lang) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = BluePrimary)
                    }
                }
                HorizontalDivider(color = GrayDivider, thickness = 0.5.dp)
            }
        }
    }

    // ── Devices modal ──────────────────────────────────────────────────────────
    if (showDevices) {
        val devices = listOf(
            Triple("iPhone 14",    true,  "Active now"),
            Triple("MacBook Pro",  false, "2 days ago"),
            Triple("iPad Air",     false, "1 week ago"),
        )
        ModalBottomSheetPicker(
            title   = "Devices",
            onClose = { showDevices = false }
        ) {
            devices.forEach { (name, active, time) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Smartphone, contentDescription = null, tint = BluePrimary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(name, color = TextPrimary)
                        Text(time, color = if (active) Color(0xFF34C759) else TextSecondary,
                            fontSize = 12.sp)
                    }
                }
                HorizontalDivider(color = GrayDivider, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White),
        content = content
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = BluePrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text = label, color = TextPrimary, modifier = Modifier.weight(1f))
        trailing?.invoke()
            ?: Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
    }
    HorizontalDivider(color = GrayDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 48.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModalBottomSheetPicker(
    title: String,
    onClose: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(onDismissRequest = onClose) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = TextPrimary, fontSize = 17.sp)
                TextButton(onClick = onClose) { Text("Done", color = BluePrimary) }
            }
            HorizontalDivider(color = GrayDivider)
            content()
            Spacer(Modifier.height(24.dp))
        }
    }
}

// Extension for clip modifier (already imported from foundation)
private fun Modifier.clip(shape: androidx.compose.ui.graphics.Shape) =
    this.clip(shape)
