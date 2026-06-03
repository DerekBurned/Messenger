package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.example.messenger.domain.model.Conversation
import com.example.messenger.presentation.base.ObserveAsEvents
import com.example.messenger.presentation.effect.EditChatEffect
import com.example.messenger.presentation.screens.ui.theme.LightGray
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.state.EditChatUiState
import com.example.messenger.presentation.viewmodel.EditChatViewModel

@Composable
fun EditChatScreen(
    viewModel: EditChatViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            EditChatEffect.Done -> onBackClick()
        }
    }
    EditChatScreenContent(
        state = state,
        onBackClick = onBackClick,
        onToggle = viewModel::toggle,
        onDelete = { viewModel.deleteSelected() },
        onMarkAllRead = { viewModel.markAllRead() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditChatScreenContent(
    state: EditChatUiState,
    onBackClick: () -> Unit = {},
    onToggle: (String) -> Unit = {},
    onDelete: () -> Unit = {},
    onMarkAllRead: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(elevation = 4.dp),
                title = { Text("Select chat", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = PrimaryBlue),
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            ) {
                Button(
                    onClick = onMarkAllRead,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f),
                ) { Text("Read all", fontSize = 14.sp) }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f),
                    enabled = state.selectedIds.isNotEmpty(),
                ) { Text("Delete", fontSize = 14.sp) }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color.White),
        ) {
            items(state.conversations, key = { it.id }) { conversation ->
                val selected = conversation.id in state.selectedIds
                EditChatRow(
                    conversation = conversation,
                    selected = selected,
                    onClick = { onToggle(conversation.id) },
                )
                HorizontalDivider(color = LightGray, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun EditChatRow(conversation: Conversation, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(CircleShape).background(LightGray),
            contentAlignment = Alignment.Center,
        ) {
            val initial = conversation.participantNames.firstOrNull()?.take(1)?.uppercase().orEmpty()
            if (initial.isNotBlank()) {
                Text(initial, color = PrimaryBlue, fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = conversation.participantNames.firstOrNull() ?: "Unknown",
            color = Color.Black,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (selected) PrimaryBlue else Color.Transparent)
                .border(2.dp, if (selected) PrimaryBlue else LightGray, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditChatScreenPreview() {
    MessengerTheme {
        EditChatScreenContent(
            state = EditChatUiState(
                conversations = listOf(
                    Conversation(id = "1", participantNames = listOf("Alice")),
                    Conversation(id = "2", participantNames = listOf("Bob")),
                    Conversation(id = "3", participantNames = listOf("Carol")),
                ),
                selectedIds = setOf("1", "3"),
            ),
        )
    }
}
