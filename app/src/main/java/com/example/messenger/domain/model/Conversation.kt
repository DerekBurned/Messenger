package com.example.messenger.domain.model

data class Conversation(
    val id: String,
    val participantIds: List<String>,
    val participantNames: List<String>,
    val participantAvatars: List<String?>,
    val lastMessage: String?,
    val lastMessageTimestamp: Long,
    val unreadCount: Int = 0
)