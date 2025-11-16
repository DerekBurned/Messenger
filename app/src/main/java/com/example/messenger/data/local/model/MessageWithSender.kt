package com.example.messenger.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.messenger.data.local.entity.MessageEntity
import com.example.messenger.data.local.entity.UserEntity

data class MessageWithSender(
    @Embedded
    val message: MessageEntity, 

    @Relation(
        parentColumn = "sender_id", 
        entityColumn = "id"         
    )
    val sender: UserEntity? 
)