package com.example.messenger.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.messenger.data.local.entity.ConversationEntity
import com.example.messenger.data.local.entity.MessageEntity

data class ConversationWithMessages(
    @Embedded 
    val conversation: ConversationEntity, 

    @Relation(
        parentColumn = "id",
        entityColumn = "conversation_id"
    )
    val messages: List<MessageEntity> 
)