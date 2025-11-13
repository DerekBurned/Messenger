package com.example.messenger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.messenger.data.local.database.Converters

@Entity(tableName = "conversations_table")
@TypeConverters(Converters::class)
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val participantIds: List<String>,
    val participantNames: List<String>,
    val participantAvatars: List<String?>,
    val lastMessage: String?,
    val lastMessageTimestamp: Long,
    val unreadCount: Int = 0
)