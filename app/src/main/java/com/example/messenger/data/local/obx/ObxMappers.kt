package com.example.messenger.data.local.obx

import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.model.PhoneVisibility
import com.example.messenger.domain.model.User

fun ObxMessage.toDomain(): Message = Message(
    id = uid,
    conversationId = conversationId,
    senderId = senderId,
    text = text,
    timestamp = timestamp,
    status = parseStatus(status),
    isRead = isRead,
    deleted = deleted,
)

fun Message.toObx(): ObxMessage = ObxMessage(
    uid = id,
    conversationId = conversationId,
    senderId = senderId,
    text = text,
    timestamp = timestamp,
    status = status.name,
    isRead = isRead,
    deleted = deleted,
)

private fun parseStatus(value: String): MessageStatus {

    if (value == "DELIVERED" || value == MessageStatus.SENDING.name) return MessageStatus.SENT
    return runCatching { MessageStatus.valueOf(value) }.getOrDefault(MessageStatus.SENT)
}

fun ObxUser.toDomain(): User = User(
    id = uid,
    username = username,
    email = email,
    phoneNumber = phoneNumber,
    avatarUrl = avatarUrl,
    lastSeen = lastSeen,
    isOnline = isOnline,
    phoneVisibility = PhoneVisibility.HIDDEN,
    fcmToken = null,
)

fun User.toObx(): ObxUser = ObxUser(
    uid = id,
    username = username ?: "Unknown",
    email = email,
    phoneNumber = phoneNumber,
    avatarUrl = avatarUrl,
    lastSeen = lastSeen,
    isOnline = isOnline,
)

fun ObxConversation.toDomain(): Conversation = Conversation(
    id = uid,
    participantIds = participantIds,
    participantNames = participantNames,
    participantAvatars = participantAvatars,
    lastMessage = latestMessageText ?: lastMessage,
    lastMessageTimestamp = if (latestMessageTimestamp > 0L) latestMessageTimestamp else lastMessageTimestamp,
    unreadCount = unreadCount,
)

fun Conversation.toObx(): ObxConversation = ObxConversation(
    uid = id,
    participantIds = participantIds,
    participantNames = participantNames,
    participantAvatars = participantAvatars,
    lastMessage = lastMessage,
    lastMessageTimestamp = lastMessageTimestamp,
    unreadCount = unreadCount,
)
