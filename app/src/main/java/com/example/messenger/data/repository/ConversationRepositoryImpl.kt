package com.example.messenger.data.repository

import androidx.compose.foundation.layout.FlowRow
import com.example.messenger.data.sync.NetworkObserver
import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.Profile
import com.example.messenger.domain.repository.IConversationRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold

class ConversationRepositoryImpl @Inject constructor(
    private val networkObserver: NetworkObserver
): IConversationRepository {
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

    override suspend fun observeRemoteConversations(): Flow<List<Conversation>> {
        TODO("Not yet implemented")
    }

    override suspend fun syncConversations() {
        TODO("Not yet implemented")
    }
}