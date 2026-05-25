package com.example.messenger.data.local.repository

import com.example.messenger.data.local.dao.ConversationDao
import com.example.messenger.data.local.dao.MessageDao
import com.example.messenger.data.local.dao.UserDao
import com.example.messenger.data.local.database.MessengerDatabase
import com.example.messenger.data.local.model.ConversationSummary
import com.example.messenger.data.local.model.MessageWithSender
import com.example.messenger.data.mapper.toDomain
import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.User
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class LocalRepositoryImpl @Inject constructor(
    private val db: MessengerDatabase
) : ILocalRepository {
    override fun getAllConversations(): Flow<List<ConversationSummary>> {
        return db.conversationDao().getAllConversations()
    }

    override fun getAllMessages(conversationId: String): Flow<List<MessageWithSender>> {
        return db.messageDao().getMessagesWithSendersDesc(conversationId)
    }

    override fun getAllUsers(): Flow<List<User>> {
        return db.userDao().getAllUsers()
            .map { userEntities ->
                userEntities
                    .map { entity ->
                        entity.toDomain()
                    }
            }
    }

    override suspend fun resetDB() = withContext(Dispatchers.IO){
        db.runInTransaction {
            db.clearAllTables()
            db.databaseDao().clearPrimaryKeyIndex()
        }
    }
}