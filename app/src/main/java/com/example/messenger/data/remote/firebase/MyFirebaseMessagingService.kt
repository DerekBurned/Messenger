package com.example.messenger.data.remote.firebase

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.example.messenger.BuildConfig
import com.example.messenger.R
import com.example.messenger.data.remote.call.ActiveCallHolder
import com.example.messenger.data.remote.call.CallForegroundService
import com.example.messenger.presentation.MainActivity
import com.example.messenger.presentation.notification.CurrentConversationHolder
import com.example.messenger.presentation.notification.NotificationChannels
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Refreshed token: $token")
        }
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            firestore.collection("users").document(currentUid)
                .update("fcmToken", token)
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
                remoteMessage.notification?.let { sendChatNotification(it.title, it.body, conversationId = null, partnerId = "", partnerName = "") }
            }
        }
    }

    private fun handleMessagePayload(data: Map<String, String>) {
        val conversationId = data["conversationId"].orEmpty()
        val senderId = data["senderId"].orEmpty()
        val senderName = data["senderName"].orEmpty().ifBlank { "New message" }
        val preview = data["preview"].orEmpty()

        if (conversationId.isNotBlank() && CurrentConversationHolder.isOpen(conversationId)) {
            Log.d(TAG, "handleMessagePayload: conversation $conversationId already open, suppressing notification")
            return
        }
        Log.d(TAG, "handleMessagePayload: posting notification for conv=$conversationId sender=$senderName")

        sendChatNotification(
            title = senderName,
            body = preview,
            conversationId = conversationId.takeIf { it.isNotBlank() },
            partnerId = senderId,
            partnerName = senderName,
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
    }

    private fun sendChatNotification(
        title: String?,
        body: String?,
        conversationId: String?,
        partnerId: String,
        partnerName: String,
    ) {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (conversationId != null) {
                putExtra(MainActivity.EXTRA_OPEN_CONVERSATION_ID, conversationId)
                putExtra(MainActivity.EXTRA_PARTNER_ID, partnerId)
                putExtra(MainActivity.EXTRA_PARTNER_NAME, partnerName)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            (conversationId ?: "").hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = NotificationCompat.Builder(this, NotificationChannels.CHAT_MESSAGES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body.orEmpty()))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        if (conversationId != null) {
            builder.setGroup(GROUP_PREFIX + conversationId)
        }
        val nm = getSystemService<NotificationManager>() ?: return

        val notificationId = (conversationId ?: System.currentTimeMillis().toString()).hashCode()
        nm.notify(notificationId, builder.build())
    }

    companion object {
        private const val TYPE_MESSAGE = "message"
        private const val TYPE_CALL = "call"
        private const val GROUP_PREFIX = "chat:"
    }
}
