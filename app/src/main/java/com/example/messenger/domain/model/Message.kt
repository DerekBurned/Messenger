package com.example.messenger.domain.model

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val text: String,
    val timestamp: Long,
    val status: MessageStatus,
    val isRead: Boolean = false
)