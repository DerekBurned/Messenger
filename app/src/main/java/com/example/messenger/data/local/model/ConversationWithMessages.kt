package com.example.messenger.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.messenger.data.local.entity.ConversationEntity
import com.example.messenger.data.local.entity.MessageEntity

data class ConversationWithMessages(
    @Embedded // Tells Room to treat 'conversation's fields as if they were in this class
    val conversation: ConversationEntity, // The Parent

    @Relation(
        parentColumn = "id",
        entityColumn = "conversation_id"
    )
    val messages: List<MessageEntity> // The Children
)