package com.example.messenger.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.messenger.data.local.database.Converters
import com.example.messenger.domain.model.MessageStatus

@Entity(
    tableName = "messages_table",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["sender_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["conversation_id"]),
        Index(value = ["sender_id"])
    ]
)
@TypeConverters(Converters::class)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "conversation_id")
    val conversationId: String,
    @ColumnInfo(name = "sender_id")
    val senderId: String,
    val text: String,
    val timestamp: Long,
    val status: MessageStatus,
    val isRead: Boolean = false
)