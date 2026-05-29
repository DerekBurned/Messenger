package com.example.messenger.data.mapper

import com.example.messenger.data.local.entity.ConversationEntity
import com.example.messenger.data.local.entity.MessageEntity
import com.example.messenger.data.local.entity.UserEntity
import com.example.messenger.data.local.model.MessageWithSender
import com.example.messenger.data.remote.dto.ConversationDTO
import com.example.messenger.data.remote.dto.MessageDTO
import com.example.messenger.data.remote.dto.UserDTO
import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.model.PhoneVisibility
import com.example.messenger.domain.model.User
import java.util.Date

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        username = username,
        email = email,
        phoneNumber = phoneNumber,
        avatarUrl = avatarUrl,
        lastSeen = lastSeen,
        isOnline = isOnline,
        
        phoneVisibility = PhoneVisibility.HIDDEN,
        fcmToken = null
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        username = username ?: "Unknown", 
        email = email,
        phoneNumber = phoneNumber,
        avatarUrl = avatarUrl,
        lastSeen = lastSeen,
        isOnline = isOnline
    )
}

fun List<UserEntity>.toUserDomainList(): List<User> = map { it.toDomain() }
fun List<User>.toUserEntityList(): List<UserEntity> = map { it.toEntity() }

fun UserDTO.toDomain(): User {
    return User(
        id = id,
        username = username ?: "Unknown",
        email = email,
        phoneNumber = phoneNumber,
        avatarUrl = avatarUrl,
        lastSeen = lastSeen,
        isOnline = isOnline,
        phoneVisibility = try {
            PhoneVisibility.valueOf(phoneVisibility)
        } catch (e: IllegalArgumentException) {
            PhoneVisibility.HIDDEN
        },
        fcmToken = fcmToken
    )
}

fun User.toDTO(): UserDTO {
    return UserDTO(
        id = id,
        username = username,
        email = email,
        phoneNumber = phoneNumber,
        avatarUrl = avatarUrl,
        lastSeen = lastSeen,
        isOnline = isOnline,
        phoneVisibility = phoneVisibility.name,
        fcmToken = fcmToken
    )
}

fun List<UserDTO>.toUserDomainListFromDTO(): List<User> = map { it.toDomain() }
fun List<User>.toUserDTOList(): List<UserDTO> = map { it.toDTO() }

fun MessageEntity.toDomain(): Message {
    return Message(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        text = text,
        timestamp = timestamp,
        status = status,
        isRead = isRead
    )
}

fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        text = text,
        timestamp = timestamp,
        status = status,
        isRead = isRead
    )
}

fun List<MessageEntity>.toMessageDomainList(): List<Message> = map { it.toDomain() }
fun List<Message>.toMessageEntityList(): List<MessageEntity> = map { it.toEntity() }

fun MessageDTO.toDomain(): Message {
    val parsedStatus = try {
        MessageStatus.valueOf(status)
    } catch (e: IllegalArgumentException) {
        MessageStatus.SENT
    }

    val serverStatus = if (parsedStatus == MessageStatus.SENDING) MessageStatus.SENT else parsedStatus
    return Message(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        text = text,
        timestamp = timestamp,
        status = serverStatus,
        isRead = isRead
    )
}

fun Message.toDTO(): MessageDTO {
    return MessageDTO(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        text = text,
        
        timestamp = timestamp,
        status = status.name,
        isRead = isRead
    )
}

fun List<MessageDTO>.toMessageDomainListFromDTO(): List<Message> = map { it.toDomain() }
fun List<Message>.toMessageDTOList(): List<MessageDTO> = map { it.toDTO() }

fun ConversationEntity.toDomain(): Conversation {
    return Conversation(
        id = id,
        participantIds = participantIds,
        participantNames = participantNames,
        participantAvatars = participantAvatars,
        lastMessage = lastMessage,
        lastMessageTimestamp = lastMessageTimestamp,
        unreadCount = unreadCount
    )
}

fun Conversation.toEntity(): ConversationEntity {
    return ConversationEntity(
        id = id,
        participantIds = participantIds,
        participantNames = participantNames,
        participantAvatars = participantAvatars,
        lastMessage = lastMessage,
        lastMessageTimestamp = lastMessageTimestamp,
        unreadCount = unreadCount
    )
}

fun List<ConversationEntity>.toConversationDomainList(): List<Conversation> = map { it.toDomain() }
fun List<Conversation>.toConversationEntityList(): List<ConversationEntity> = map { it.toEntity() }

fun ConversationDTO.toDomain(): Conversation {
    return Conversation(
        id = id,
        participantIds = participantIds,
        participantNames = participantNames,
        participantAvatars = participantAvatars,
        lastMessage = lastMessage,
        
        lastMessageTimestamp = lastMessageTimestamp,
        unreadCount = unreadCount
    )
}

fun Conversation.toDTO(): ConversationDTO {
    return ConversationDTO(
        id = id,
        participantIds = participantIds,
        participantNames = participantNames,
        participantAvatars = participantAvatars,
        lastMessage = lastMessage,
        
        lastMessageTimestamp = lastMessageTimestamp,
        unreadCount = unreadCount
    )
}

fun List<ConversationDTO>.toConversationDomainListFromDTO(): List<Conversation> = map { it.toDomain() }
fun List<Conversation>.toConversationDTOList(): List<ConversationDTO> = map { it.toDTO() }

fun MessageWithSender.toDomain(): Message = Message(
    id = message.id,
    conversationId = message.conversationId,
    senderId = message.senderId,
    text = message.text,
    timestamp = message.timestamp,
    status = message.status,
    isRead = message.isRead
)
