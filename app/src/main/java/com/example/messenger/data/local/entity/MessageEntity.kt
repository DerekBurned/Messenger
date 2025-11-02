package com.example.messenger.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.model.PhoneNumber

@Entity(tableName = "messages_table",
    foreignKeys = [
        ForeignKey(
        entity = ConversationEntity::class,
        parentColumns = ["id"],
        childColumns = ["conversationId"],
        onDelete = ForeignKey.CASCADE),
        ForeignKey(
            entity = UserEntity::class, // <-- Parent table is 'users'
            parentColumns = ["id"], // <-- Parent's Primary Key is 'id'
            childColumns = ["sender_id"], // <-- Our Foreign Key is 'sender_id'
            onDelete = ForeignKey.SET_NULL // (Optional: If a user is deleted, don't delete the message, just set sender to null)
        )
    ])
data class MessageEntity (
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "conversation_id", index = true)
    val conversationId: String,
    @ColumnInfo(name = "sender_id", index = true)
    val senderId: String,
    val text: String,
    val timestamp: Long,
    val status: MessageStatus,
    val isRead: Boolean = false
)