package com.example.messenger.presentation.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.content.getSystemService

object NotificationChannels {

    const val CHAT_MESSAGES = "chat_messages_channel"
    const val INCOMING_CALL = "call_channel"

    fun ensureCreated(context: Context) {
        val nm = context.getSystemService<NotificationManager>() ?: return

        if (nm.getNotificationChannel(CHAT_MESSAGES) == null) {
            val chat = NotificationChannel(
                CHAT_MESSAGES,
                "Chat messages",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "New chat messages"
                enableLights(true)
                enableVibration(true)
            }
            nm.createNotificationChannel(chat)
        }

        if (nm.getNotificationChannel(INCOMING_CALL) == null) {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val audioAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val call = NotificationChannel(
                INCOMING_CALL,
                "Incoming calls",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Ringing for incoming voice calls"
                setBypassDnd(true)
                enableLights(true)
                enableVibration(true)
                setSound(ringtoneUri, audioAttrs)
            }
            nm.createNotificationChannel(call)
        }
    }
}
