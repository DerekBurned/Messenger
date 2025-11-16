package com.example.messenger.presentation.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.messenger.presentation.screens.ui.theme.LightGray
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material.icons.filled.Email

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MessengerTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}

// Главный экран со списком чатов
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithNav(onChatClick: () -> Unit = {}) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBarContentM3() },
        bottomBar = { BottomNavBarM3() }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            items(8) {
                ChatListItemM3(onClick = onChatClick)
            }
        }
    }
}

// Верхняя панель
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarContentM3() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Chats",
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = { /* Handle edit click */ }) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    tint = Color.White,
                    contentDescription = "Edit"
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Handle search click */ }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = PrimaryBlue,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

// Элемент списка чата
@Composable
fun ChatListItemM3(onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onClick() } // При клике вызываем переход
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(LightGray)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(LightGray)
        )
    }
    HorizontalDivider(color = LightGray, thickness = 1.dp, modifier = Modifier.padding(start = 80.dp))}

// Нижняя навигация
@Composable
fun BottomNavBarM3() {
    var selectedItem by remember { mutableStateOf(1) }

    NavigationBar(
        containerColor = PrimaryBlue,
        contentColor = Color.White
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            selectedTextColor = Color.White,
            unselectedIconColor = Color.White.copy(alpha = 0.6f),
            unselectedTextColor = Color.White.copy(alpha = 0.6f),
            indicatorColor = PrimaryBlue
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Call, contentDescription = "Calls") },
            label = { Text("calls") },
            selected = selectedItem == 0,
            onClick = { selectedItem = 0 },
            colors = itemColors
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Email, contentDescription = "Chats") },
            label = { Text("chats") },
            selected = selectedItem == 1,
            onClick = { selectedItem = 1 },
            colors = itemColors
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("settings") },
            selected = selectedItem == 2,
            onClick = { selectedItem = 2 },
            colors = itemColors
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MessengerTheme {
        MainScreenWithNav()
    }
}