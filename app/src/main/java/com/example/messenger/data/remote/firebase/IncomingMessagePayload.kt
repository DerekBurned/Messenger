package com.example.messenger.data.remote.firebase

import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.MessageStatus

fun parseIncomingMessage(data: Map<String, String>): Message.Text? {
    val conversationId = data["conversationId"].orEmpty()
    val messageId = data["messageId"].orEmpty()
    if (conversationId.isBlank() || messageId.isBlank()) return null
    return Message.Text(
        id = messageId,
        conversationId = conversationId,
        senderId = data["senderId"].orEmpty(),
        text = data["preview"].orEmpty(),
        timestamp = data["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis(),
        status = MessageStatus.SENT,
        isRead = false,
    )
}
