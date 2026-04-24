package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.data.Contact
import com.example.messenger.data.ContactStatus
import com.example.messenger.presentation.components.Avatar
import com.example.messenger.presentation.screens.ui.theme.*

/**
 * data-figma-name: chat_user_profile
 * Профиль контакта, открывается из внутреннего экрана чата.
 */

// Мок-данные для фото/видео/файлов
private val mockMediaColors = listOf(
    Color(0xFFa8d8ea), Color(0xFFb8e0d2), Color(0xFFffd3b6),
    Color(0xFFd6e4f0), Color(0xFFf7d6e0), Color(0xFFe8d5c4)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatUserProfileScreen(
    contact: Contact,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onCallClick: () -> Unit
) {
    var mediaTab by remember { mutableStateOf(MediaTab.PHOTO) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Аватар и имя
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Avatar(
                        name   = contact.name,
                        size   = 84.dp,
                        online = contact.status == ContactStatus.ONLINE
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(contact.name, color = TextPrimary, fontSize = 20.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (contact.status == ContactStatus.ONLINE) "online"
                        else contact.lastSeen.ifBlank { "offline" },
                        color    = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    // Кнопка Call
                    Button(
                        onClick = onCallClick,
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape  = RoundedCornerShape(50)
                    ) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "Call",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Call", fontSize = 14.sp)
                    }
                }
            }

            // Информационные поля
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BluePrimary)
                ) {
                    ContactInfoRow(label = "Phone",         value = contact.phone,           divider = true)
                    ContactInfoRow(label = "Username",      value = "@${contact.username}",  divider = true)
                    ContactInfoRow(label = "Date of Birth", value = contact.dob.ifBlank { "—" }, divider = false)
                }
                Spacer(Modifier.height(16.dp))
            }

            // Вкладки медиа
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MediaTab.values().forEach { tab ->
                        val isActive = mediaTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isActive) BluePrimary else GrayBg)
                                .clickable { mediaTab = tab }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text  = tab.name.lowercase(),
                                color = if (isActive) Color.White else TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // Сетка медиа (мок)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    val rows = mockMediaColors.chunked(3)
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            row.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(color)
                                )
                            }
                            // Заполнить пустые ячейки если ряд неполный
                            repeat(3 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ContactInfoRow(label: String, value: String, divider: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = label, color = Color.White.copy(0.7f), fontSize = 11.sp)
        Spacer(Modifier.height(2.dp))
        Text(text = value, color = Color.White, fontSize = 14.sp)
    }
    if (divider) {
        HorizontalDivider(color = Color.White.copy(0.15f), thickness = 0.5.dp)
    }
}

private enum class MediaTab { PHOTO, VIDEO, FILE }
