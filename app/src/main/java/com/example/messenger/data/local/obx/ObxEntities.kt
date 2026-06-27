package com.example.messenger.data.local.obx

import com.example.messenger.domain.model.MediaItem
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.model.PhoneNumber
import com.example.messenger.domain.model.PhoneVisibility
import io.objectbox.annotation.ConflictStrategy
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique

@Entity
data class ObxMessage(
    @Id var boxId: Long = 0,
    @Unique(onConflict = ConflictStrategy.REPLACE) var uid: String = "",
    @Index var conversationId: String = "",
    var senderId: String = "",
    var text: String = "",
    var timestamp: Long = 0,
    var status: String = MessageStatus.SENT.name,
    var isRead: Boolean = false,
    var deleted: Boolean = false,

    var type: String = com.example.messenger.domain.model.Message.TYPE_TEXT,

    var replyToMessageId: String? = null,
    var replyToText: String? = null,
    var replyToSenderId: String? = null,
    var callDurationSeconds: Int = 0,

    @Convert(converter = MediaItemListConverter::class, dbType = String::class)
    var mediaItemsJson: List<MediaItem> = emptyList(),
)

@Entity
data class ObxUser(
    @Id var boxId: Long = 0,
    @Unique(onConflict = ConflictStrategy.REPLACE) var uid: String = "",
    @Index var username: String = "",
    @Index var email: String? = null,
    @Convert(converter = PhoneNumberConverter::class, dbType = String::class)
    var phoneNumber: PhoneNumber? = null,
    var avatarUrl: String? = null,
    var lastSeen: Long = 0,
    var isOnline: Boolean = false,
)

@Entity
data class ObxConversation(
    @Id var boxId: Long = 0,
    @Unique(onConflict = ConflictStrategy.REPLACE) var uid: String = "",
    @Convert(converter = StringListConverter::class, dbType = String::class)
    var participantIds: List<String> = emptyList(),
    @Convert(converter = StringListConverter::class, dbType = String::class)
    var participantNames: List<String> = emptyList(),
    @Convert(converter = NullableStringListConverter::class, dbType = String::class)
    var participantAvatars: List<String?> = emptyList(),
    var lastMessage: String? = null,
    var lastMessageSenderId: String = "",
    var lastMessageTimestamp: Long = 0,
    var unreadCount: Int = 0,

    var latestMessageText: String? = null,
    var latestMessageSenderId: String = "",
    var latestMessageTimestamp: Long = 0,
   var clearedAtForMe: Long = 0,
)

@Entity
data class ObxSyncQueueItem(
    @Id var boxId: Long = 0,
    var entityType: String = "",
    var entityId: String = "",
    var action: String = "",
    var timestamp: Long = 0,
    var retryCount: Int = 0,
)

@Entity
data class ObxProfile(
    @Id var boxId: Long = 0,
    @Unique(onConflict = ConflictStrategy.REPLACE) var uid: String = "",
    var username: String? = null,
    var email: String? = null,
    @Index var userId: String = "",
    var avatarUrl: String? = null,
    var lastSeen: Long = 0,
    var isOnline: Boolean = false,
    @Convert(converter = PhoneVisibilityConverter::class, dbType = String::class)
    var phoneVisibility: PhoneVisibility = PhoneVisibility.HIDDEN,
    var fcmToken: String? = null,
)
