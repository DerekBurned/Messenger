package com.example.messenger.data.remote.firebase

import android.util.Log
import androidx.core.content.ContextCompat
import com.example.messenger.BuildConfig
import com.example.messenger.R
import com.example.messenger.data.remote.call.ActiveCallHolder
import com.example.messenger.data.remote.call.CallForegroundService
import com.example.messenger.data.remote.call.telecom.TelecomCallManager
import com.example.messenger.data.remote.call.telecom.TelecomCallMeta
import com.example.messenger.presentation.notification.ChatNotifier
import com.example.messenger.presentation.notification.CurrentConversationHolder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FcmEntryPoint {
    fun firestoreService(): FirestoreService
    fun telecomCallManager(): TelecomCallManager
}

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Refreshed token: $token")
        }
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            ?: return
        val firestoreService = EntryPointAccessors.fromApplication(
            applicationContext,
            FcmEntryPoint::class.java,
        ).firestoreService()
        CoroutineScope(Dispatchers.IO).launch {
            firestoreService.updateFcmToken(currentUid, token)
                .onFailure { Log.w(TAG, "onNewToken: token sync failed", it) }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        Log.d(TAG, "onMessageReceived: from=${remoteMessage.from} type=${data["type"]} data=$data")

        if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null) {
            Log.d(TAG, "onMessageReceived: no authenticated user, ignoring")
            return
        }

        when (data["type"]) {
            TYPE_MESSAGE -> handleMessagePayload(data)
            TYPE_CALL -> handleCallPayload(data)
            else -> {
                Log.w(TAG, "onMessageReceived: unknown type, notification=${remoteMessage.notification != null}")
                remoteMessage.notification?.let { ChatNotifier.notifyFallback(this, it.title, it.body) }
            }
        }
    }

    private fun handleMessagePayload(data: Map<String, String>) {
        val conversationId = data["conversationId"].orEmpty()
        val senderId = data["senderId"].orEmpty()
        val senderName = data["senderName"].orEmpty().ifBlank { getString(R.string.notif_message_fallback_sender) }
        val preview = data["preview"].orEmpty()
        val senderAvatar = data["senderAvatar"].orEmpty()
        val timestamp = data["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()

        if (conversationId.isBlank()) {
            ChatNotifier.notifyFallback(this, senderName, preview)
            return
        }
        if (CurrentConversationHolder.isOpen(conversationId)) {
            Log.d(TAG, "handleMessagePayload: conversation $conversationId already open, suppressing notification")
            return
        }
        Log.d(TAG, "handleMessagePayload: posting notification for conv=$conversationId sender=$senderName")

        ChatNotifier.notifyIncoming(
            context = this,
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            text = preview,
            timestamp = timestamp,
            senderAvatar = senderAvatar,
        )
    }

    private fun handleCallPayload(data: Map<String, String>) {
        val callId = data["callId"].orEmpty()
        val callerId = data["callerId"].orEmpty()
        val callerName = data["callerName"].orEmpty().ifBlank { callerId }
        val channelName = data["channelName"].orEmpty()
        val calleeId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        if (callId.isBlank() || channelName.isBlank() || calleeId.isBlank()) return

        if (ActiveCallHolder.snapshot()?.callId == callId) return

        val intent = CallForegroundService.incomingIntent(
            ctx = this,
            callId = callId,
            callerId = callerId,
            calleeId = calleeId,
            channelName = channelName,
            partnerName = callerName,
            partnerPhone = "",
        )
        ContextCompat.startForegroundService(this, intent)

        val meta = TelecomCallMeta(
            callId = callId,
            callerId = callerId,
            calleeId = calleeId,
            channelName = channelName,
            partnerName = callerName,
            partnerPhone = "",
            isIncoming = true,
        )
        runCatching {
            EntryPointAccessors.fromApplication(applicationContext, FcmEntryPoint::class.java)
                .telecomCallManager()
                .addIncoming(meta)
        }
    }

    companion object {
        private const val TYPE_MESSAGE = "message"
        private const val TYPE_CALL = "call"
    }
}
