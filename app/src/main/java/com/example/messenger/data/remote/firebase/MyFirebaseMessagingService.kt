package com.example.messenger.data.remote.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.messenger.presentation.screens.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * This class handles receiving push notifications (FCM).
 * It needs to be registered in your AndroidManifest.xml.
 *
 * <service
 * android:name=".data.remote.firebase.MyFirebaseMessagingService"
 * android:exported="false">
 * <intent-filter>
 * <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 * </service>
 *
 * (Note: The class name in the manifest must match this class name)
 */
class MyFirebaseMessagingService @Inject constructor(

): FirebaseMessagingService() {

    // Note: @Inject doesn't work directly in a Service constructor
    // You would typically use @AndroidEntryPoint if using Hilt for this.
    // For this example, we'll keep it simple.

    private val TAG = "MyFirebaseMsgService"

    /**
     * Called when a new FCM registration token is generated.
     * This token is the "address" for sending a notification to this specific device.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

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

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            // Handle data (e.g., update local database without showing notification)
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

