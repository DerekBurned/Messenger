package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.data.UserProfile
import com.example.messenger.presentation.components.Avatar
import com.example.messenger.presentation.screens.ui.theme.*

/**
 * data-figma-name: edit_profile
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: UserProfile,
    onSave: (name: String, phone: String, username: String, dob: String) -> Unit,
    onLogout: () -> Unit,
    onBackClick: () -> Unit
) {
    var name     by remember { mutableStateOf(user.name) }
    var phone    by remember { mutableStateOf(user.phone) }
    var username by remember { mutableStateOf(user.username) }
    var dob      by remember { mutableStateOf(user.dob) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("edit profile", color = Color.White) },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // ── Avatar ─────────────────────────────────────────────────────────
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                ) {
                    Avatar(name = name, size = 80.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("change", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = {},
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("change photo", color = TextSecondary, fontSize = 13.sp)
            }

            Spacer(Modifier.height(16.dp))

            // ── Input Fields ───────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BlueTextField(
                    value       = name,
                    onValueChange = { name = it },
                    placeholder = "profile name"
                )
                BlueTextField(
                    value       = phone,
                    onValueChange = { phone = it },
                    placeholder = "change phone number"
                )
                BlueTextField(
                    value       = username,
                    onValueChange = { username = it },
                    placeholder = "change user name"
                )
                BlueTextField(
                    value       = dob,
                    onValueChange = { dob = it },
                    placeholder = "change date of birth"
                )

                // Change account button
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("change account", color = Color.White)
                        Text("+", color = Color.White.copy(0.7f), fontSize = 20.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Actions ────────────────────────────────────────────────────────
            Button(
                onClick  = { onSave(name, phone, username, dob) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary.copy(alpha = 0.6f)
                )
            ) {
                Text("confirm changes", color = Color.White)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick  = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("log out", color = Color.Red.copy(0.8f))
            }
        }
    }
}

@Composable
private fun BlueTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BluePrimary, RoundedCornerShape(12.dp))
    ) {
        TextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = { Text(placeholder, color = Color.White.copy(0.6f)) },
            modifier      = Modifier.fillMaxWidth(),
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
    }
}
