package com.example.messenger.data.Repository

import com.example.messenger.data.local.dao.MessageDao
import com.example.messenger.data.local.model.MessageWithSender
import com.example.messenger.data.remote.firebase.FirebaseMessagingManager
import com.example.messenger.data.remote.firebase.FirestoreService
import com.example.messenger.data.remote.firebase.MyFirebaseMessagingService
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.repository.IMessageRepository
import com.google.firebase.messaging.FirebaseMessagingService
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class MessageRepositoryImpl @Inject constructor(
    val messageDao: MessageDao,
    val messageService: FirestoreService
): IMessageRepository{
    override fun getMessagesStream(conversationId: String): Flow<List<MessageWithSender>> {
        return messageDao.getMessagesWithSenders(conversationId)
            .distinctUntilChanged()
    }

    override suspend fun sendMessage(message: Message): Result<Unit> {
        return messageService.sendMessage(message)
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