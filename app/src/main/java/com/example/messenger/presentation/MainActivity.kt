package com.example.messenger.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.example.messenger.data.local.prefs.ThemePreferenceStore
import com.example.messenger.data.presence.PresenceManager
import com.example.messenger.data.remote.call.ActiveCallHolder
import com.example.messenger.data.remote.call.CallForegroundService
import com.example.messenger.presentation.navigation.AppNavigation
import com.example.messenger.presentation.navigation.CallRoute
import com.example.messenger.presentation.navigation.ChatRoute
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var presenceManager: PresenceManager

    @Inject
    lateinit var themePreferenceStore: ThemePreferenceStore

    private val idleScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var idleJob: Job? = null
    private var isIdle = false

    private val pendingIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingIntent.value = intent
        setContent {
            val themeMode by themePreferenceStore.themeMode
                .collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            MessengerTheme(themeMode = themeMode) {
                val deepLink = remember { pendingIntent }
                val pendingRoute = remember { mutableStateOf<NavKey?>(null) }
                LaunchedEffect(deepLink.value) {
                    deepLink.value?.let { pendingRoute.value = resolveDeepLinkRoute(it) }
                    deepLink.value = null
                }
                AppNavigation(
                    pendingRoute = pendingRoute.value,
                    onRouteConsumed = { pendingRoute.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingIntent.value = intent
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        if (isIdle) {
            isIdle = false
            presenceManager.goOnline(idleScope)
        }
        scheduleIdleTimer()
    }

    private fun scheduleIdleTimer() {
        idleJob?.cancel()
        idleJob = idleScope.launch {
            delay(IDLE_TIMEOUT_MS)
            isIdle = true
            presenceManager.goIdleAway(idleScope)
        }
    }

    override fun onResume() {
        super.onResume()
        
        isIdle = false
        scheduleIdleTimer()
    }

    override fun onPause() {
        super.onPause()

        idleJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        idleScope.cancel()
    }

    private fun resolveDeepLinkRoute(intent: Intent): NavKey? = when {
        intent.getBooleanExtra(EXTRA_OPEN_INCOMING_CALL, false) -> {
            val call = ActiveCallHolder.snapshot()
            if (call == null) {
                null
            } else {
                val accept = intent.getBooleanExtra(EXTRA_ACCEPT_CALL, false)
                intent.removeExtra(EXTRA_OPEN_INCOMING_CALL)
                intent.removeExtra(EXTRA_ACCEPT_CALL)
                if (accept) {
                    runCatching {
                        startService(
                            Intent(this, CallForegroundService::class.java)
                                .setAction(CallForegroundService.ACTION_ACCEPT),
                        )
                    }
                }
                CallRoute(
                    partnerId = call.callerId,
                    partnerName = call.partnerName,
                    partnerPhone = call.partnerPhone,
                )
            }
        }
        intent.hasExtra(EXTRA_OPEN_CONVERSATION_ID) -> {
            val conversationId = intent.getStringExtra(EXTRA_OPEN_CONVERSATION_ID).orEmpty()
            val partnerId = intent.getStringExtra(EXTRA_PARTNER_ID).orEmpty()
            val partnerName = intent.getStringExtra(EXTRA_PARTNER_NAME).orEmpty()
            intent.removeExtra(EXTRA_OPEN_CONVERSATION_ID)
            intent.removeExtra(EXTRA_PARTNER_ID)
            intent.removeExtra(EXTRA_PARTNER_NAME)
            if (conversationId.isNotBlank()) {
                ChatRoute(conversationId, partnerId, partnerName)
            } else {
                null
            }
        }
        else -> null
    }

    companion object {
        const val EXTRA_OPEN_INCOMING_CALL = "extra_open_incoming_call"
        const val EXTRA_ACCEPT_CALL = "extra_accept_call"
        const val EXTRA_OPEN_CONVERSATION_ID = "extra_open_conversation_id"
        const val EXTRA_PARTNER_ID = "extra_partner_id"
        const val EXTRA_PARTNER_NAME = "extra_partner_name"
        private const val IDLE_TIMEOUT_MS = 45_000L
    }
}
