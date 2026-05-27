package com.example.messenger.data.repository

import com.example.messenger.data.local.dao.ConversationDao
import com.example.messenger.data.local.dao.MessageDao
import com.example.messenger.data.local.dao.SyncQueueDao
import com.example.messenger.data.local.entity.SyncQueueEntity
import com.example.messenger.data.mapper.toDomain
import com.example.messenger.data.mapper.toEntity
import com.example.messenger.data.remote.firebase.FirestoreService
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.model.sync.SyncAction
import com.example.messenger.domain.model.sync.SyncEntityType
import com.example.messenger.domain.repository.IMessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val messageService: FirestoreService,
    private val conversationDao: ConversationDao,
    private val syncQueueDao: SyncQueueDao,
) : IMessageRepository {

    override fun getMessagesStream(conversationId: String): Flow<List<Message>> {
        return messageDao.getMessagesWithSendersDesc(conversationId)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun sendMessage(message: Message): Result<Unit> {
        return try {
            
            messageDao.insertMessage(message.toEntity())
            conversationDao.updateLastMessage(
                conversationId = message.conversationId,
                lastMessage = message.text,
                lastMessageTimestamp = message.timestamp,
            )

            val result = messageService.sendMessage(message.copy(status = MessageStatus.SENT))
            if (result.isSuccess) {
                messageDao.updateMessageStatus(message.id, MessageStatus.SENT)
            } else {
                messageDao.updateMessageStatus(message.id, MessageStatus.FAILED)
                enqueueRetry(message.id)
            }
            result
        } catch (e: Exception) {
            runCatching { messageDao.updateMessageStatus(message.id, MessageStatus.FAILED) }
            runCatching { enqueueRetry(message.id) }
            Result.failure(e)
        }
    }

    private suspend fun enqueueRetry(messageId: String) {
        syncQueueDao.insert(
            SyncQueueEntity(
                entityType = SyncEntityType.MESSAGE,
                entityId = messageId,
                action = SyncAction.SEND,
                timestamp = System.currentTimeMillis(),
            ),
        )
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
