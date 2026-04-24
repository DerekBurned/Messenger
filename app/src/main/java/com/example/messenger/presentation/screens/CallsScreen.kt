package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.PhoneMissed
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.messenger.data.*
import com.example.messenger.presentation.components.Avatar
import com.example.messenger.presentation.components.BottomNav
import com.example.messenger.presentation.screens.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * data-figma-name: calls_all  /  calls_missed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(
    calls: List<Call>,
    contacts: List<Contact>,
    totalUnread: Int,
    onCallContact: (contactId: String) -> Unit,
    navController: NavController
) {
    var tab by remember { mutableStateOf(CallTab.All) }

    val filtered = when (tab) {
        CallTab.All    -> calls
        CallTab.Missed -> calls.filter { it.type == CallType.MISSED }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Tab switcher centered
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(modifier = Modifier.padding(4.dp)) {
                                CallTab.entries.forEach { t ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (tab == t) Color.White else Color.Transparent)
                                            .clickable { tab = t }
                                            .padding(horizontal = 20.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text     = t.label,
                                            color    = if (tab == t) BluePrimary else Color.White.copy(0.7f),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        },
        bottomBar = { BottomNav(navController = navController, totalUnread = totalUnread) }
    ) { paddingValues ->
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.AutoMirrored.Filled.PhoneMissed, contentDescription = null,
                        modifier = Modifier.size(48.dp), tint = TextSecondary.copy(0.3f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text     = if (tab == CallTab.Missed) "No missed calls" else "No calls",
                        color    = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                items(filtered, key = { call: Call -> call.id }) { call ->
                    val contact = contacts.find { it.id == call.contactId } ?: return@items
                    CallItem(
                        call    = call,
                        contact = contact,
                        onClick = { onCallContact(contact.id) }
                    )
                    HorizontalDivider(color = GrayDivider, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun CallItem(call: Call, contact: Contact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(name = contact.name, size = 46.dp, online = contact.status == ContactStatus.ONLINE)
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = contact.name,
                color    = if (call.type == CallType.MISSED) MaterialTheme.colorScheme.error else TextPrimary,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = callIcon(call.type),
                    contentDescription = null,
                    tint     = callIconTint(call.type),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text     = call.type.name.lowercase() + formatDuration(call.duration),
                    color    = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text     = formatCallTime(call.timestamp),
            color    = TextSecondary,
            fontSize = 12.sp
        )
    }
}

private fun callIcon(type: CallType): ImageVector = when (type) {
    CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
    CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
    CallType.MISSED   -> Icons.AutoMirrored.Filled.PhoneMissed
}

@Composable
private fun callIconTint(type: CallType): Color = when (type) {
    CallType.INCOMING -> Color(0xFF34C759)
    CallType.OUTGOING -> BluePrimary
    CallType.MISSED   -> MaterialTheme.colorScheme.error
}

private fun formatDuration(seconds: Int?): String {
    if (seconds == null || seconds == 0) return ""
    val m = seconds / 60
    val s = seconds % 60
    return " · $m:${s.toString().padStart(2, '0')}"
}

private fun formatCallTime(date: Date): String {
    val now  = Date()
    val diff = now.time - date.time
    return when {
        diff < 86_400_000  -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        diff < 172_800_000 -> "Yesterday"
        else               -> SimpleDateFormat("d MMM", Locale.getDefault()).format(date)
    }
}

enum class CallTab(val label: String) { All("all"), Missed("missed") }
