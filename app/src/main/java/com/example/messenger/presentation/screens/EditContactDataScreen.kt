package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.messenger.data.Contact
import com.example.messenger.presentation.components.Avatar
import com.example.messenger.presentation.screens.ui.theme.*

/**
 * data-figma-name: edit_contact_data
 * Редактирование имени контакта и удаление контакта.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactDataScreen(
    contact: Contact,
    onBackClick: () -> Unit,
    onSave: (newName: String) -> Unit,
    onDeleteContact: () -> Unit
) {
    var name               by remember { mutableStateOf(contact.name) }
    var showDeleteConfirm  by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Contact", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // Аватар
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Avatar(name = contact.name, size = 84.dp)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { /* смена фото */ }) {
                    Text("change photo", color = BluePrimary, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Поле изменения имени
            TextField(
                value         = name,
                onValueChange = { name = it },
                placeholder   = { Text("change profile name", color = Color.White.copy(0.6f)) },
                modifier      = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor        = Color.White,
                    unfocusedTextColor      = Color.White,
                    focusedContainerColor   = BluePrimary,
                    unfocusedContainerColor = BluePrimary,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor             = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            Spacer(Modifier.height(24.dp))

            // Кнопка подтвердить
            Button(
                onClick = { onSave(name.trim()) },
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(containerColor = BluePrimary.copy(alpha = 0.6f)),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Text("confirm changes", fontSize = 14.sp)
            }

            Spacer(Modifier.height(8.dp))

            // Кнопка удалить контакт
            Button(
                onClick  = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Text("delete contact", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }
        }
    }

    // Диалог подтверждения удаления
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete ${contact.name}?") },
            text  = { Text("This will also delete the chat.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDeleteContact()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
