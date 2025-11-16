package com.example.messenger.data.mapper

import com.example.messenger.data.local.entity.ConversationEntity
import com.example.messenger.data.local.entity.MessageEntity
import com.example.messenger.data.local.entity.UserEntity
import com.example.messenger.data.remote.dto.ConversationDTO
import com.example.messenger.data.remote.dto.MessageDTO
import com.example.messenger.data.remote.dto.UserDTO
import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.model.PhoneVisibiliity
import com.example.messenger.domain.model.User
import java.util.Date

// =============================================================================
// USER MAPPERS
// =============================================================================


fun UserEntity.toDomain(): User {
    return User(
        id = id,
        username = username,
        email = email,
        phoneNumber = phoneNumber,
        avatarUrl = avatarUrl,
        lastSeen = lastSeen,
        isOnline = isOnline,
        // Defaults for fields missing in Entity but present in Domain
        phoneVisibility = PhoneVisibiliity.HIDDEN,
        fcmToken = null
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        username = username ?: "Unknown", // Handle nullable username for DB
        email = email,
        phoneNumber = phoneNumber,
        avatarUrl = avatarUrl,
        lastSeen = lastSeen,
        isOnline = isOnline
    )
}

fun List<UserEntity>.toUserDomainList(): List<User> = map { it.toDomain() }
fun List<User>.toUserEntityList(): List<UserEntity> = map { it.toEntity() }


// --- DTO <-> Domain ---

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
            PhoneVisibiliity.valueOf(phoneVisibility)
        } catch (e: IllegalArgumentException) {
            PhoneVisibiliity.HIDDEN
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


// =============================================================================
// MESSAGE MAPPERS
// =============================================================================

// --- Entity <-> Domain ---

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


// --- DTO <-> Domain ---

fun MessageDTO.toDomain(): Message {
    return Message(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        text = text,
        // FIXED: timestamp is already a Long in DTO. No .time needed.
        timestamp = timestamp,
        status = try {
            MessageStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            MessageStatus.SENT // Default fallback
        },
        isRead = isRead
    )
}

fun Message.toDTO(): MessageDTO {
    return MessageDTO(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        text = text,
        // FIXED: timestamp is Long in Domain and Long in DTO. No conversion needed.
        timestamp = timestamp,
        status = status.name,
        isRead = isRead
    )
}

fun List<MessageDTO>.toMessageDomainListFromDTO(): List<Message> = map { it.toDomain() }
fun List<Message>.toMessageDTOList(): List<MessageDTO> = map { it.toDTO() }


// =============================================================================
// CONVERSATION MAPPERS
// =============================================================================

// --- Entity <-> Domain ---

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


// --- DTO <-> Domain ---

fun ConversationDTO.toDomain(): Conversation {
    return Conversation(
        id = id,
        participantIds = participantIds,
        participantNames = participantNames,
        participantAvatars = participantAvatars,
        lastMessage = lastMessage,
        // FIXED: lastMessageTimestamp is Long in DTO. No .time needed.
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
        // FIXED: lastMessageTimestamp is Long in Domain and DTO. No conversion needed.
        lastMessageTimestamp = lastMessageTimestamp,
        unreadCount = unreadCount
    )
}

fun List<ConversationDTO>.toConversationDomainListFromDTO(): List<Conversation> = map { it.toDomain() }
fun List<Conversation>.toConversationDTOList(): List<ConversationDTO> = map { it.toDTO() }