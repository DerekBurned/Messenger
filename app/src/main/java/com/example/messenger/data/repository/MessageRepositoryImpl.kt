package com.example.messenger.data.repository

import com.example.messenger.data.local.dao.MessageDao
import com.example.messenger.data.local.model.MessageWithSender
import com.example.messenger.data.mapper.toEntity
import com.example.messenger.data.remote.firebase.FirestoreService
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.repository.IMessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val messageService: FirestoreService
) : IMessageRepository {

    override fun getMessagesStream(conversationId: String): Flow<List<MessageWithSender>> {
        return messageDao.getMessagesWithSendersDesc(conversationId)
    }

    override suspend fun sendMessage(message: Message): Result<Unit> {
        return try {
            // Save locally first with SENDING status
            messageDao.insertMessage(message.toEntity())
            // Send to remote
            val result = messageService.sendMessage(message)
            if (result.isSuccess) {
                messageDao.updateMessageStatus(message.id, MessageStatus.SENT)
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMessage(message: Message): Result<Unit> {
        return try {
            val result = messageService.deleteMessage(message)
            if (result.isSuccess) {
                messageDao.deleteMessage(message.toEntity())
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markMessageAsRead(message: Message): Result<Unit> {
        return try {
            val result = messageService.markMessageAsRead(message)
            if (result.isSuccess) {
                messageDao.markAsRead(message.id)
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markMessagesAsDelivered(conversationId: String, message: Message): Result<Unit> {
        return try {
            messageDao.updateMessageStatus(message.id, MessageStatus.DELIVERED)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun observeRemoteMessages(conversationId: String): Flow<Result<Message>> = flow {
        try {
            messageService.getMessagesStream(conversationId).collect { messages ->
                messages.forEach { message ->
                    messageDao.insertMessage(message.toEntity())
                    emit(Result.success(message))
                }
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
