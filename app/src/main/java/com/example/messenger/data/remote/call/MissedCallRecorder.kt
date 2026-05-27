package com.example.messenger.data.remote.call

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.example.messenger.R
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.repository.IConversationRepository
import com.example.messenger.domain.repository.IMessageRepository
import com.example.messenger.presentation.MainActivity
import com.example.messenger.presentation.notification.NotificationChannels
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissedCallRecorder @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val conversationRepository: IConversationRepository,
    private val messageRepository: IMessageRepository,
) {

    suspend fun record(callerId: String, calleeId: String, callerName: String) {
        if (callerId.isBlank() || calleeId.isBlank()) {
            Log.w(TAG, "record skipped: blank ids caller=$callerId callee=$calleeId")
            return
        }
        val displayName = callerName.ifBlank { "Unknown" }

        val conversationResult = conversationRepository.createConversation(
            listOf(callerId, calleeId),
        )
        val conversation = conversationResult.getOrNull()
        if (conversation == null) {
            Log.w(
                TAG,
                "record: failed to resolve conversation",
                conversationResult.exceptionOrNull(),
            )
            return
        }

        val now = System.currentTimeMillis()
        val message = Message(
            id = UUID.randomUUID().toString(),
            conversationId = conversation.id,
            senderId = callerId,
            text = MISSED_CALL_TEXT,
            timestamp = now,
        )
        runCatching { messageRepository.sendMessage(message) }
            .onFailure { Log.w(TAG, "record: failed to write missed-call message", it) }

        postMissedCallNotification(
            conversationId = conversation.id,
            callerId = callerId,
            callerName = displayName,
        )
    }

    private fun postMissedCallNotification(
        conversationId: String,
        callerId: String,
        callerName: String,
    ) {
        val nm = context.getSystemService<NotificationManager>() ?: return
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(MainActivity.EXTRA_OPEN_CONVERSATION_ID, conversationId)
            putExtra(MainActivity.EXTRA_PARTNER_ID, callerId)
            putExtra(MainActivity.EXTRA_PARTNER_NAME, callerName)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            conversationId.hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, NotificationChannels.CHAT_MESSAGES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Missed call")
            .setContentText("Missed call from $callerName")
            .setCategory(NotificationCompat.CATEGORY_MISSED_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        nm.notify(MISSED_CALL_NOTIFICATION_ID_PREFIX xor conversationId.hashCode(), notification)
    }

    private companion object {
        const val TAG = "MissedCallRecorder"
        const val MISSED_CALL_TEXT = "📞 Missed call"
        const val MISSED_CALL_NOTIFICATION_ID_PREFIX = 0x6D15500
    }
}
