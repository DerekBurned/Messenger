package com.example.messenger.data.remote.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
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

        // TODO: Send this token to your server (e.g., update in Firestore)
        // You would typically get the current user's UID and call
        // a function (like the one in FirestoreService) to save this token.
        // This requires access to your auth/firestore services, often via a
        // coroutine launched from a repository.
    }

    /**
     * Called when a message is received while the app is in the foreground.
     * Notifications received in the background are (by default) handled by
     * the system tray and not this function.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            // TODO: Handle data payload (e.g., for silent updates)
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            // TODO: Show a custom foreground notification
        }
    }
}

