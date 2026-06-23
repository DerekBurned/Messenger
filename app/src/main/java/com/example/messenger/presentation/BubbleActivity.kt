package com.example.messenger.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.messenger.presentation.navigation.Screens
import com.example.messenger.presentation.screens.ChatScreenWithNav
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BubbleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val conversationId = intent.getStringExtra(MainActivity.EXTRA_OPEN_CONVERSATION_ID).orEmpty()
        val partnerId = intent.getStringExtra(MainActivity.EXTRA_PARTNER_ID).orEmpty()
        val partnerName = intent.getStringExtra(MainActivity.EXTRA_PARTNER_NAME).orEmpty()
        if (conversationId.isBlank()) {
            finish()
            return
        }
        setContent {
            MessengerTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screens.ChatScreen.route,
                ) {
                    composable(
                        route = Screens.ChatScreen.route,
                        arguments = listOf(
                            navArgument("conversationId") { type = NavType.StringType; defaultValue = conversationId },
                            navArgument("partnerId") { type = NavType.StringType; defaultValue = partnerId },
                            navArgument("partnerName") { type = NavType.StringType; defaultValue = partnerName },
                        ),
                    ) {
                        ChatScreenWithNav(onBackClick = { finish() })
                    }
                }
            }
        }
    }
}
