package com.example.messenger.domain.model

data class Message(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val status: MessageStatus = MessageStatus.SENT,
    val isRead: Boolean = false,
    val deleted: Boolean = false,
    
    val type: String = TYPE_TEXT,
) {
    companion object {
        const val TYPE_TEXT = "TEXT"
        const val TYPE_MISSED_CALL = "MISSED_CALL"
    }
}