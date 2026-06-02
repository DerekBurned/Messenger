package com.example.messenger.data.local.obx

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class MessageObxEntity(
    @Id var boxId: Long = 0,
    @Index var uid: String = "",
    var conversationId: String = "",
    var text: String = "",
    var timestamp: Long = 0,
    var deleted: Boolean = false,
)
