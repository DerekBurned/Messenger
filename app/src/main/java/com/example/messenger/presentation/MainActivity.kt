package com.example.messenger.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.messenger.data.remote.call.ActiveCallHolder
import com.example.messenger.presentation.navigation.AppNavigation
import com.example.messenger.presentation.navigation.Screens
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {  }

    private val pendingIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestNotificationPermission()
        pendingIntent.value = intent
        setContent {
            MessengerTheme {
                val navController = rememberNavController()
                val deepLink = remember { pendingIntent }
                AppNavigation(navController = navController)
                LaunchedEffect(deepLink.value) {
                    deepLink.value?.let { handleDeepLink(it) {
                        navController.navigate(it)
                    } }
                    deepLink.value = null
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingIntent.value = intent
    }

    private fun handleDeepLink(intent: Intent, navigate: (String) -> Unit) {
        when {
            intent.getBooleanExtra(EXTRA_OPEN_INCOMING_CALL, false) -> {
                val call = ActiveCallHolder.snapshot() ?: return
                intent.removeExtra(EXTRA_OPEN_INCOMING_CALL)
                navigate(
                    Screens.CallScreen.createRoute(
                        partnerId = call.callerId,
                        partnerName = call.partnerName,
                        partnerPhone = call.partnerPhone,
                    ),
                )
            }
            intent.hasExtra(EXTRA_OPEN_CONVERSATION_ID) -> {
                val conversationId = intent.getStringExtra(EXTRA_OPEN_CONVERSATION_ID).orEmpty()
                val partnerId = intent.getStringExtra(EXTRA_PARTNER_ID).orEmpty()
                val partnerName = intent.getStringExtra(EXTRA_PARTNER_NAME).orEmpty()
                intent.removeExtra(EXTRA_OPEN_CONVERSATION_ID)
                intent.removeExtra(EXTRA_PARTNER_ID)
                intent.removeExtra(EXTRA_PARTNER_NAME)
                if (conversationId.isNotBlank()) {
                    navigate(Screens.ChatScreen.createRoute(conversationId, partnerId, partnerName))
                }
            }
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        const val EXTRA_OPEN_INCOMING_CALL = "extra_open_incoming_call"
        const val EXTRA_OPEN_CONVERSATION_ID = "extra_open_conversation_id"
        const val EXTRA_PARTNER_ID = "extra_partner_id"
        const val EXTRA_PARTNER_NAME = "extra_partner_name"
    }
}
