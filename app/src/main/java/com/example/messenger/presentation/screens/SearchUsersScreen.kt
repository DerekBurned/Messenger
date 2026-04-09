package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.domain.model.User
import com.example.messenger.presentation.screens.ui.theme.LightGray
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.viewmodel.SearchUsersViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUsersScreen(
    viewModel: SearchUsersViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onConversationCreated: (conversationId: String, partnerId: String, partnerName: String) -> Unit = { _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Auto-focus search field
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    // Debounce search
    LaunchedEffect(searchQuery) {
        delay(300)
        viewModel.searchUsers(searchQuery)
    }

    // Navigate when conversation is created
    LaunchedEffect(uiState.createdConversation) {
        uiState.createdConversation?.let { conversation ->
            val partnerId = conversation.participantIds.firstOrNull { id ->
                id != com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            } ?: ""
            val partnerName = conversation.participantNames.firstOrNull() ?: "Unknown"
            viewModel.onConversationNavigated()
            onConversationCreated(conversation.id, partnerId, partnerName)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search users...", color = Color.White.copy(alpha = 0.7f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PrimaryBlue
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            when {
                uiState.isCreatingConversation -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryBlue
                    )
                }
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryBlue
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                searchQuery.isNotBlank() && uiState.users.isEmpty() && !uiState.isLoading -> {
                    Text(
                        text = "No users found",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                searchQuery.isBlank() -> {
                    Text(
                        text = "Type a username to search",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.users) { user ->
                            SearchUserItem(
                                user = user,
                                onClick = { viewModel.createConversationWithUser(user) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchUserItem(
    user: User,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.username?.take(1)?.uppercase() ?: "?",
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = user.username ?: "Unknown",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = user.email ?: "",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
    HorizontalDivider(color = LightGray, thickness = 1.dp, modifier = Modifier.padding(start = 80.dp))
}
