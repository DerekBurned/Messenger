package com.example.messenger.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.messenger.data.local.entity.MessageEntity
import com.example.messenger.data.local.entity.UserEntity

data class MessageWithSender(
    @Embedded
    val message: MessageEntity, // The Parent (in this relationship)

    @Relation(
        parentColumn = "sender_id", // The key from the parent (MessageEntity)
        entityColumn = "id"         // The key from the child (UserEntity)
    )
    val sender: UserEntity? // The Child (the User we are fetching)
)