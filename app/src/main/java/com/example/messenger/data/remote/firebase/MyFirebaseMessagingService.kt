package com.example.messenger.data.remote.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.messenger.presentation.screens.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.BuildConfig
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

        // Get the current User ID (Assuming you use Firebase Auth)
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

        // If user is logged in, update Firestore
        if (currentUid != null) {
            // You might need to use a CoroutineScope here if not using a WorkManager
            CoroutineScope(Dispatchers.IO).launch {
                // Instantiate FirestoreService or get from Hilt entry point
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                firestore.collection("users").document(currentUid)
                    .update("fcmToken", token)
            }
        }
    }

    /**
     * Called when a message is received while the app is in the foreground.
     * Notifications received in the background are (by default) handled by
     * the system tray and not this function.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Handle data payload — send delivery receipt for new messages
        if (remoteMessage.data.isNotEmpty()) {
            val conversationId = remoteMessage.data["conversationId"]
            val timestamp = remoteMessage.data["timestamp"]?.toLongOrNull()
            if (conversationId != null && timestamp != null) {
                val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (currentUid != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val database = com.google.firebase.database.FirebaseDatabase.getInstance()
                            database.getReference("receipts")
                                .child(conversationId)
                                .child(currentUid)
                                .child("lastDeliveredTimestamp")
                                .setValue(timestamp)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to send delivery receipt", e)
                        }
                    }
                }
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            sendNotification(it.title, it.body)
        }
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val channelId = "chat_messages_channel"

        // Create an Intent to open the chat when clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // You can pass the conversation ID here to open the specific chat
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_email) // Replace with your app icon
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Chat Messages",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}

