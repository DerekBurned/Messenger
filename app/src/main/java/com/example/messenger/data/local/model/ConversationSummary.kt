package com.example.messenger.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.messenger.data.local.entity.ConversationEntity
import com.example.messenger.data.local.entity.MessageEntity

data class ConversationSummary(
    @Embedded val conversation: ConversationEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "conversation_id"
    )
    val lastMessage: MessageEntity?
)