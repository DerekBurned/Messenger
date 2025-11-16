package com.example.messenger.data.remote.dto

data class MessageDTO(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val status: String = "SENT", // Stored as String: "SENT", "READ", etc.
    val isRead: Boolean = false
)