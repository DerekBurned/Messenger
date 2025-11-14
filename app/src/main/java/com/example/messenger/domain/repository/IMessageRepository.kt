package com.example.messenger.domain.repository

import com.example.messenger.data.local.model.MessageWithSender
import com.example.messenger.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface IMessageRepository {
    fun getMessagesStream(conversationId: String): Flow<List<MessageWithSender>>
    suspend fun sendMessage(message: Message): Result<Unit>
    suspend fun deleteMessage(messageId: String): Result<Unit>
    suspend fun markMessageAsRead(messageId: String): Result<Unit>
    suspend fun markMessagesAsDelivered(conversationId: String): Result<Unit>
    suspend fun observeRemoteMessages(conversationId: String): Result<Unit>
}