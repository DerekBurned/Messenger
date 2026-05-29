package com.example.messenger.data.repository

import android.util.Log
import com.example.messenger.data.local.dao.ConversationDao
import com.example.messenger.data.mapper.toDomain
import com.example.messenger.data.mapper.toEntity
import com.example.messenger.data.remote.auth.FirebaseAuthService
import com.example.messenger.data.remote.firebase.FirestoreService
import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.Profile
import com.example.messenger.domain.repository.IConversationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService,
    private val dao: ConversationDao,
    private val authService: FirebaseAuthService,
) : IConversationRepository {

    override fun getAllConversations(): Flow<List<Conversation>> = channelFlow {
        
        val remoteJob = launch {
            val userId = authService.getCurrentUserId()
            if (userId.isNullOrBlank()) {
                Log.w(TAG, "getAllConversations: no signed-in user; skipping remote listener")
                return@launch
            }
            Log.d(TAG, "getAllConversations: starting remote listener")
            try {
                firestoreService.getAllConversations(userId).collect { conversations ->
                    Log.d(TAG, "Remote listener emitted ${conversations.size} conversations")
                    conversations.forEach { dao.insertConversation(it.toEntity()) }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Remote conversation listener errored", e)
            }
        }
        
        val localJob = launch {
            dao.getAllConversations()
                .map { summaries ->
                    summaries.map { summary ->
                        val conv = summary.conversation.toDomain()
                        val previewText = summary.latestMessageText
                            ?: conv.lastMessage?.takeIf { it.isNotBlank() }
                        val previewTs = summary.latestMessageTimestamp?.takeIf { it > 0L }
                            ?: conv.lastMessageTimestamp
                        conv.copy(
                            lastMessage = previewText,
                            lastMessageTimestamp = previewTs,
                        )
                    }
                }
                .collect { send(it) }
        }
        awaitClose {
            Log.d(TAG, "getAllConversations: subscriber gone, tearing down")
            remoteJob.cancel()
            localJob.cancel()
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
            
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createConversation(participantIds: List<String>): Result<Conversation> {
        return try {
            
            val existingResult = firestoreService.findExistingConversation(participantIds)
            existingResult.getOrNull()?.let { existing ->
                dao.insertConversation(existing.toEntity())
                return Result.success(existing)
            }

            val names = mutableListOf<String>()
            val avatars = mutableListOf<String?>()
            for (id in participantIds) {
                val userResult = firestoreService.getUserProfile(id)
                val user = userResult.getOrNull()
                names.add(user?.username ?: "Unknown")
                avatars.add(user?.avatarUrl)
            }
            val conversation = Conversation(
                id = "",
                participantIds = participantIds,
                participantNames = names,
                participantAvatars = avatars,
                lastMessage = null,
                lastMessageTimestamp = System.currentTimeMillis()
            )

            val createResult = firestoreService.createConversation(conversation)
            val firestoreId = createResult.getOrThrow()
            val savedConversation = conversation.copy(id = firestoreId)

            dao.insertConversation(savedConversation.toEntity())
            Result.success(savedConversation)
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
            .onEach { conversations ->
                conversations.forEach { dao.insertConversation(it.toEntity()) }
            }
    }

    override suspend fun syncConversations() {
        try {
            val userId = authService.getCurrentUserId()
            if (userId.isNullOrBlank()) {
                Log.w(TAG, "syncConversations: no signed-in user")
                return
            }
            val remote = firestoreService.fetchAllConversationsOnce(userId).getOrNull()
            if (remote == null) {
                Log.w(TAG, "syncConversations: remote fetch returned null")
                return
            }
            Log.d(TAG, "syncConversations: fetched ${remote.size} from Firestore")
            remote.forEach { dao.insertConversation(it.toEntity()) }
        } catch (e: Exception) {
            Log.w(TAG, "Conversation sync failed (SyncWorker will retry)", e)
        }
    }

    private companion object {
        const val TAG = "ConversationRepo"
    }
}
