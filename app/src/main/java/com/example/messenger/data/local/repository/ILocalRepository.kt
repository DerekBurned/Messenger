package com.example.messenger.data.local.repository

import com.example.messenger.data.local.model.ConversationSummary
import com.example.messenger.data.local.model.MessageWithSender
import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ILocalRepository {
    fun getAllConversations(): Flow<List<ConversationSummary>>
    fun getAllMessages(conversationId: String): Flow<List<MessageWithSender>>
    fun getAllUsers(): Flow<List<User>>
}


