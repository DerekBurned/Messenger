package com.example.messenger.data.mapper

import com.example.messenger.data.local.model.MessageWithSender
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.MessageStatus

fun MessageWithSender.toDomain(): Message = Message(
    id = message.id,
    conversationId = message.conversationId,
    senderId = message.senderId,
    text = message.text,
    timestamp = message.timestamp,
    status = message.status,
    isRead = message.isRead
)

