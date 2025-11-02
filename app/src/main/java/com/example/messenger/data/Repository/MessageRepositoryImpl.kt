package com.example.messenger.data.Repository

import com.example.messenger.domain.model.Message
import com.example.messenger.domain.repository.IMessageRepository
import kotlinx.coroutines.flow.Flow

class MessageRepositoryImpl : IMessageRepository{
    override fun getMessagesStream(conversationId: String): Flow<List<Message>> {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(message: Message): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun markMessageAsRead(messageId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun markMessagesAsDelivered(conversationId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun observeRemoteMessages(conversationId: String): Result<Unit> {
        TODO("Not yet implemented")
    }
}