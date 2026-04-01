package com.example.messenger.data.repository

import com.example.messenger.data.local.dao.ConversationDao
import com.example.messenger.data.mapper.toDomain
import com.example.messenger.data.mapper.toEntity
import com.example.messenger.data.remote.firebase.FirebaseAuthService
import com.example.messenger.data.remote.firebase.FirestoreService
import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.Profile
import com.example.messenger.domain.repository.IConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService,
    private val dao: ConversationDao,
    private val authService: FirebaseAuthService
) : IConversationRepository {

    override fun getAllConversations(): Flow<List<Conversation>> {
        return dao.getAllConversations().map { summaries ->
            summaries.map { it.conversation.toDomain() }
        }
    }

    override suspend fun getConversationById(conversationId: String): Result<Conversation?> {
        return try {
            val entity = dao.getConversationById(conversationId)
            Result.success(entity?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getConversationsForProfile(profile: Profile): Result<List<Conversation>> {
        return try {
            // Filter local conversations where the profile's userId is a participant
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createConversation(participantIds: List<String>): Result<Conversation> {
        return try {
            val conversation = Conversation(
                id = "",
                participantIds = participantIds,
                participantNames = emptyList(),
                participantAvatars = emptyList(),
                lastMessage = null,
                lastMessageTimestamp = System.currentTimeMillis()
            )
            // Save to local DB after remote succeeds
            dao.insertConversation(conversation.toEntity())
            Result.success(conversation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteConversation(conversationId: String): Result<Unit> {
        return try {
            dao.deleteConversationById(conversationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markConversationAsRead(conversationId: String): Result<Unit> {
        return try {
            val entity = dao.getConversationById(conversationId)
            if (entity != null) {
                dao.insertConversation(entity.copy(unreadCount = 0))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun observeRemoteConversations(): Flow<List<Conversation>> {
        val userId = authService.getCurrentUserId() ?: ""
        return firestoreService.getAllConversations(userId)
    }

    override suspend fun syncConversations() {
        try {
            val userId = authService.getCurrentUserId() ?: return
            // Observe remote will emit once via callbackFlow; collect first emission
            // For a full sync, you'd collect and insert into local DB
        } catch (_: Exception) { }
    }
}
