package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.domain.model.User
import com.example.messenger.presentation.screens.ui.theme.LightGray
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.OnSurface
import com.example.messenger.presentation.screens.ui.theme.OnSurfaceMuted
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.viewmodel.SearchUsersViewModel

@Composable
fun ContactsScreenContent(
    modifier: Modifier = Modifier,
    viewModel: SearchUsersViewModel = hiltViewModel(),
    onContactClick: (User) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    ContactsScreenContentInner(
        modifier = modifier,
        users = uiState.users,
        isLoading = uiState.isLoading,
        query = query,
        onQueryChange = {
            query = it
            viewModel.searchUsers(it)
        },
        onContactClick = onContactClick,
    )
}

@Composable
private fun ContactsScreenContentInner(
    modifier: Modifier = Modifier,
    users: List<User>,
    isLoading: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onContactClick: (User) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        val shape = RoundedCornerShape(12.dp)
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .shadow(elevation = 2.dp, shape = shape, clip = false),
            placeholder = { Text("Search contacts", color = OnSurfaceMuted) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = OnSurfaceMuted) },
            singleLine = true,
            shape = shape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = OnSurface,
                unfocusedTextColor = OnSurface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = PrimaryBlue,
            ),
        )

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            users.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (query.isBlank()) {
                            "Start typing to find contacts"
                        } else {
                            "No contacts found"
                        },
                        color = Color.Gray,
                    )
                }
            }
            else -> {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(users) { user ->
                        ContactRow(user = user, onClick = { onContactClick(user) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(LightGray),
            contentAlignment = Alignment.Center,
        ) {
            val initial = user.username?.take(1)?.uppercase()
            if (!initial.isNullOrBlank()) {
                Text(initial, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            } else {
                Icon(Icons.Filled.Person, contentDescription = null, tint = PrimaryBlue)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = user.username ?: "Unknown",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
            Text(
                text = user.email ?: user.phoneNumber?.getFullNumber() ?: "",
                color = Color.Gray,
                fontSize = 12.sp,
            )
        }
    }
    HorizontalDivider(color = LightGray, modifier = Modifier.padding(start = 72.dp))
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ContactsScreenContentPreview() {
    MessengerTheme {
        ContactsScreenContentInner(
            users = listOf(
                User(id = "1", username = "Alice", email = "alice@example.com"),
                User(id = "2", username = "Bob", email = "bob@example.com"),
                User(id = "3", username = "Carol", email = "carol@example.com"),
            ),
            isLoading = false,
            query = "",
            onQueryChange = {},
            onContactClick = {},
        )
    }
}
