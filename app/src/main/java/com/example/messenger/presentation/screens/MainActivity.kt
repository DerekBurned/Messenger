package com.example.messenger.presentation.screens
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.messenger.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.messenger.presentation.screens.ui.theme.LightGray
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MessengerTheme {
                // The main screen content is now just Screen()
                // Screen() itself is a Scaffold, so we don't wrap it in another one.
                Screen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBarContentM3() },
        bottomBar = { BottomNavBarM3() }
    ) { innerPadding ->
        // LazyColumn for the scrollable list of chats
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply the padding from the Scaffold
                .background(Color.White)
        ) {
            // Display 8 placeholder items, just like in the image
            items(8) {
                ChatListItemM3()
            }
        }
    }
}

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

@Composable
fun ChatListItemM3() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular placeholder for the avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(LightGray)
        )
        Spacer(modifier = Modifier.width(16.dp))
        // Placeholder for chat name/message
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(LightGray)
        )
    }
    // Divider line
    Divider(color = LightGray, thickness = 1.dp, modifier = Modifier.padding(start = 80.dp))
}

@Composable
fun BottomNavBarM3() {
    // This state would be hosted higher up (in a ViewModel) in a real app
    var selectedItem by remember { mutableStateOf(1) } // 1 = "chats"

    NavigationBar(
        containerColor = PrimaryBlue,
        contentColor = Color.White
    ) {
        // Define the colors for the items
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            selectedTextColor = Color.White,
            unselectedIconColor = Color.White.copy(alpha = 0.6f),
            unselectedTextColor = Color.White.copy(alpha = 0.6f),
            indicatorColor = PrimaryBlue // Set to background color to "hide" the pill
        )

        // Item 1: Calls
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Call, contentDescription = "Calls") },
            label = { Text("calls") },
            selected = selectedItem == 0,
            onClick = { selectedItem = 0 },
            colors = itemColors
        )
        // Item 2: Chats
        NavigationBarItem(
            icon = { Image(
                painter = painterResource(id = R.drawable.cloud_shape),
                contentDescription = "image description",
                contentScale = ContentScale.None
            ) },
            label = { Text("chats") },
            selected = selectedItem == 1,
            onClick = { selectedItem = 1 },
            colors = itemColors
        )
        // Item 3: Settings
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
fun ScreenPreview() {
    MessengerTheme {
        Screen()
    }
}