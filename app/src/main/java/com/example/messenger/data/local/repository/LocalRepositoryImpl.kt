package com.example.messenger.data.local.repository

import com.example.messenger.data.local.dao.ConversationDao
import com.example.messenger.data.local.dao.MessageDao
import com.example.messenger.data.local.dao.UserDao
import com.example.messenger.data.local.model.ConversationSummary
import com.example.messenger.data.local.model.MessageWithSender
import com.example.messenger.data.mapper.toDomain
import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.User
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class LocalRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val userDao: UserDao
) : ILocalRepository {
    override fun getAllConversations(): Flow<List<ConversationSummary>> {
        return conversationDao.getAllConversations()
    }

    override fun getAllMessages(conversationId: String): Flow<List<MessageWithSender>> {
        return messageDao.getMessagesWithSendersDesc(conversationId)
    }

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
            .map { userEntities ->
                userEntities
                    .map { entity ->
                        entity.toDomain()
                    }
            }
    }
}