package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.data.Contact
import com.example.messenger.data.ContactStatus
import com.example.messenger.presentation.components.Avatar
import com.example.messenger.presentation.screens.ui.theme.*

/**
 * data-figma-name: forward_modal
 * Нижний лист пересылки сообщения — выбор контакта.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForwardMessageBottomSheet(
    contacts: List<Contact>,
    onDismiss: () -> Unit,
    onForward: (contactId: String) -> Unit
) {
    var searchActive by remember { mutableStateOf(false) }
    var query        by remember { mutableStateOf("") }

    val filtered = if (query.isNotBlank())
        contacts.filter { it.name.contains(query, ignoreCase = true) }
    else contacts

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Заголовок
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BluePrimary)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (searchActive) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        TextField(
                            value         = query,
                            onValueChange = { query = it },
                            placeholder   = { Text("Search...", color = Color.White.copy(0.7f)) },
                            modifier      = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor        = Color.White,
                                unfocusedTextColor      = Color.White,
                                focusedContainerColor   = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor   = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor             = Color.White
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                        )
                        IconButton(onClick = { searchActive = false; query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Close search", tint = Color.White)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("send to", color = Color.White, fontSize = 16.sp)
                        IconButton(onClick = { searchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                        }
                    }
                }
            }

            // Список контактов
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
            ) {
                if (filtered.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No contacts found", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(filtered, key = { it.id }) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onForward(contact.id)
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Avatar(
                                name   = contact.name,
                                size   = 46.dp,
                                online = contact.status == ContactStatus.ONLINE
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(contact.name,        color = TextPrimary,   fontSize = 14.sp)
                                Text("@${contact.username}", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                        HorizontalDivider(color = GrayDivider, thickness = 0.5.dp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
