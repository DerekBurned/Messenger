package com.example.messenger.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.messenger.presentation.navigation.AppNavigation
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import dagger.hilt.android.AndroidEntryPoint

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
