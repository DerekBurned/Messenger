package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.data.Account
import com.example.messenger.presentation.components.Avatar
import com.example.messenger.presentation.screens.ui.theme.*

/**
 * data-figma-name: change_account
 * Экран смены аккаунта / добавления нового.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeAccountScreen(
    accounts: List<Account>,
    currentAccountId: String,
    onBackClick: () -> Unit,
    onSelectAccount: (accountId: String) -> Unit,
    onAddAccount: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("change account", color = Color.White) },
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
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(accounts, key = { it.id }) { account ->
                    val isCurrent = account.id == currentAccountId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAccount(account.id) }
                            .background(if (isCurrent) BluePrimary.copy(0.05f) else Color.White)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(name = account.name, size = 46.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(account.name,  color = TextPrimary,   fontSize = 15.sp)
                            Text(account.phone, color = TextSecondary, fontSize = 12.sp)
                        }
                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(BluePrimary)
                            )
                        }
                    }
                    HorizontalDivider(color = GrayDivider, thickness = 0.5.dp)
                }
            }

            // Кнопка добавить аккаунт — закреплена снизу
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BluePrimary)
                    .clickable(onClick = onAddAccount)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add account", tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Text("Add account", color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White.copy(0.7f))
            }
        }
    }
}

// ─── Bottom Sheet вариант ─────────────────────────────────────────────────────
// Используется если нужно открыть смену аккаунта как модалку поверх Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeAccountBottomSheet(
    accounts: List<Account>,
    currentAccountId: String,
    onDismiss: () -> Unit,
    onSelectAccount: (accountId: String) -> Unit,
    onAddAccount: () -> Unit
) {
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
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("change account", color = Color.White, fontSize = 16.sp)
            }

            // Список аккаунтов
            Column {
                accounts.forEach { account ->
                    val isCurrent = account.id == currentAccountId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAccount(account.id); onDismiss() }
                            .background(if (isCurrent) BluePrimary.copy(0.05f) else Color.White)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(name = account.name, size = 46.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(account.name,  color = TextPrimary,   fontSize = 15.sp)
                            Text(account.phone, color = TextSecondary, fontSize = 12.sp)
                        }
                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(BluePrimary)
                            )
                        }
                    }
                    HorizontalDivider(color = GrayDivider, thickness = 0.5.dp)
                }
            }

            // Добавить аккаунт
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BluePrimary)
                    .clickable { onAddAccount(); onDismiss() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Text("Add account", color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White.copy(0.7f))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
