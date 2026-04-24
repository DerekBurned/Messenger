package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.data.Contact
import com.example.messenger.data.ContactStatus
import com.example.messenger.presentation.components.Avatar
import com.example.messenger.presentation.components.BottomNav
import com.example.messenger.presentation.screens.ui.theme.*

/**
 * data-figma-name: contacts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    contacts: List<Contact>,
    totalUnread: Int,
    onContactClick: (contactId: String) -> Unit,
    navController: androidx.navigation.NavController
) {
    var searchOpen  by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = if (searchQuery.isNotBlank()) {
        contacts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.username.contains(searchQuery, ignoreCase = true)
        }
    } else contacts

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (searchOpen) {
                        TextField(
                            value         = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder   = { Text("Search contacts...", color = Color.White.copy(0.7f)) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor        = Color.White,
                                unfocusedTextColor      = Color.White,
                                focusedContainerColor   = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor   = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor             = Color.White
                            ),
                            singleLine = true
                        )
                    } else {
                        Text("contacts", color = Color.White)
                    }
                },
                actions = {
                    if (searchOpen) {
                        IconButton(onClick = { searchOpen = false; searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    } else {
                        IconButton(onClick = { searchOpen = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
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
                    Icon(Icons.Default.Search, contentDescription = null,
                        modifier = Modifier.size(48.dp), tint = TextSecondary.copy(0.3f))
                    Spacer(Modifier.height(8.dp))
                    Text("No contacts found", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                items(filtered, key = { it.id }) { contact ->
                    ContactItem(
                        contact = contact,
                        onClick = { onContactClick(contact.id) }
                    )
                    HorizontalDivider(color = GrayDivider, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun ContactItem(contact: Contact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(name = contact.name, size = 46.dp, online = contact.status == ContactStatus.ONLINE)
        Spacer(Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text     = contact.name,
                    color    = TextPrimary,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (contact.status == ContactStatus.DND) {
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.NightlightRound, contentDescription = "DND",
                        modifier = Modifier.size(13.dp), tint = TextSecondary)
                }
            }
            Text(
                text     = when (contact.status) {
                    ContactStatus.ONLINE  -> "online"
                    ContactStatus.OFFLINE -> contact.lastSeen.ifBlank { "offline" }
                    ContactStatus.DND     -> "do not disturb"
                },
                color    = if (contact.status == ContactStatus.ONLINE) Color(0xFF34C759) else TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
