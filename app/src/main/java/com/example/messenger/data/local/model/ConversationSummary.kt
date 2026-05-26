package com.example.messenger.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.example.messenger.data.local.entity.ConversationEntity

data class ConversationSummary(
    @Embedded val conversation: ConversationEntity,
    @ColumnInfo(name = "latestMessageText") val latestMessageText: String?,
    @ColumnInfo(name = "latestMessageTimestamp") val latestMessageTimestamp: Long?,
)
