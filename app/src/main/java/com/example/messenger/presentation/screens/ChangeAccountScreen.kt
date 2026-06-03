package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.presentation.screens.ui.theme.LightGray
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.state.AccountSummary
import com.example.messenger.presentation.state.ChangeAccountUiState
import com.example.messenger.presentation.viewmodel.ChangeAccountViewModel

@Composable
fun ChangeAccountScreen(
    viewModel: ChangeAccountViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onAddAccount: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ChangeAccountScreenContent(
        state = state,
        onBackClick = onBackClick,
        onSelect = viewModel::selectAccount,
        onAddAccount = onAddAccount,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeAccountScreenContent(
    state: ChangeAccountUiState,
    onBackClick: () -> Unit = {},
    onSelect: (String) -> Unit = {},
    onAddAccount: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(elevation = 4.dp),
                title = { Text("Change account", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = PrimaryBlue),
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White)) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.accounts, key = { it.id }) { account ->
                    AccountRow(
                        account = account,
                        isCurrent = account.id == state.currentAccountId,
                        onClick = { onSelect(account.id) },
                    )
                    HorizontalDivider(color = LightGray, thickness = 0.5.dp)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryBlue)
                    .clickable(onClick = onAddAccount)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(0.2f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Default.Add, contentDescription = "Add account", tint = Color.White) }
                Spacer(Modifier.width(12.dp))
                Text("Add account", color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White.copy(0.7f))
            }
        }
    }
}

@Composable
private fun AccountRow(account: AccountSummary, isCurrent: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isCurrent) PrimaryBlue.copy(0.05f) else Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(CircleShape).background(LightGray),
            contentAlignment = Alignment.Center,
        ) {
            val initial = account.name.take(1).uppercase()
            if (initial.isNotBlank()) {
                Text(initial, color = PrimaryBlue, fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(account.name, color = Color.Black, fontSize = 15.sp)
            Text(account.phone, color = Color.Gray, fontSize = 12.sp)
        }
        if (isCurrent) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PrimaryBlue))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChangeAccountScreenPreview() {
    MessengerTheme {
        ChangeAccountScreenContent(
            state = ChangeAccountUiState(
                accounts = listOf(
                    AccountSummary("a1", "Alice", "+1 555 0100"),
                    AccountSummary("a2", "Bob Work", "+1 555 0200"),
                ),
                currentAccountId = "a1",
            ),
        )
    }
}
