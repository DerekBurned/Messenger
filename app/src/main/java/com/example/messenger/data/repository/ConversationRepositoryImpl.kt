package com.example.messenger.data.repository

import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.Profile
import com.example.messenger.domain.repository.IConversationRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ConversationRepositoryImpl @Inject constructor(): IConversationRepository {
    override fun getAllConversations(): Flow<List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun getConversationById(conversationId: String): Result<Conversation?> {
        TODO("Not yet implemented")
    }

    override suspend fun getConversationsForProfile(profile: Profile): Result<List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun createConversation(participantIds: List<String>): Result<Conversation> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteConversation(conversationId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun markConversationAsRead(conversationId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun observeRemoteConversations() {
        TODO("Not yet implemented")
    }
}