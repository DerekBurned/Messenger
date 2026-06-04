package com.example.messenger.data.repository

import android.util.Log
import com.example.messenger.data.local.obx.ObxConversation
import com.example.messenger.data.local.obx.ObxConversation_
import com.example.messenger.data.local.obx.asFlow
import com.example.messenger.data.local.obx.toDomain
import com.example.messenger.data.local.obx.toObx
import com.example.messenger.data.remote.auth.FirebaseAuthService
import com.example.messenger.data.remote.firebase.FirestoreService
import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.Profile
import com.example.messenger.domain.repository.IConversationRepository
import io.objectbox.Box
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService,
    private val conversationBox: Box<ObxConversation>,
    private val authService: FirebaseAuthService,
) : IConversationRepository {

    override fun getAllConversations(): Flow<List<Conversation>> = channelFlow {
        val remoteJob = launch {
            val userId = authService.getCurrentUserId()
            if (userId.isNullOrBlank()) {
                Log.w(TAG, "getAllConversations: no signed-in user; skipping remote listener")
                return@launch
            }
            try {
                firestoreService.getAllConversations(userId).collect { conversations ->
                    conversations.forEach { upsert(it) }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Remote conversation listener errored", e)
            }
        }
        val localJob = launch {
            conversationBox.query()
                .orderDesc(ObxConversation_.lastMessageTimestamp)
                .build()
                .asFlow()
                .map { rows -> rows.map { it.toDomain() } }
                .collect { send(it) }
        }
        awaitClose {
            remoteJob.cancel()
            localJob.cancel()
        }
    }

    override suspend fun getConversationById(conversationId: String): Result<Conversation?> {
        return try {
            Result.success(findByUid(conversationId)?.toDomain())
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
            val currentUserId = authService.getCurrentUserId()
            if (currentUserId.isNullOrBlank()) {
                return Result.failure(
                    IllegalStateException("Cannot create conversation: no signed-in user"),
                )
            }

            val existing = firestoreService
                .findExistingConversation(participantIds, currentUserId)
                .getOrElse { return Result.failure(it) }
            if (existing != null) {
                upsert(existing)
                return Result.success(existing)
            }

            val names = mutableListOf<String>()
            val avatars = mutableListOf<String?>()
            for (id in participantIds) {
                val user = firestoreService.getUserProfile(id).getOrNull()
                names.add(user?.username ?: "Unknown")
                avatars.add(user?.avatarUrl)
            }
            val conversation = Conversation(
                id = "",
                participantIds = participantIds,
                participantNames = names,
                participantAvatars = avatars,
                lastMessage = null,
                lastMessageTimestamp = System.currentTimeMillis(),
            )

            val firestoreId = firestoreService.createConversation(conversation).getOrThrow()
            val savedConversation = conversation.copy(id = firestoreId)
            upsert(savedConversation)
            Result.success(savedConversation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteConversation(conversationId: String): Result<Unit> {
        return try {
            conversationBox.query(ObxConversation_.uid.equal(conversationId)).build()
                .use { it.remove() }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markConversationAsRead(conversationId: String): Result<Unit> {
        return try {
            findByUid(conversationId)?.let { conv ->
                conv.unreadCount = 0
                conversationBox.put(conv)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun observeRemoteConversations(): Flow<List<Conversation>> {
        val userId = authService.getCurrentUserId() ?: ""
        return firestoreService.getAllConversations(userId)
            .onEach { conversations -> conversations.forEach { upsert(it) } }
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
            remote.forEach { upsert(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Conversation sync failed (SyncWorker will retry)", e)
        }
    }

    private fun findByUid(uid: String): ObxConversation? =
        conversationBox.query(ObxConversation_.uid.equal(uid)).build().use { it.findFirst() }

    private fun upsert(conversation: Conversation) {
        val incoming = conversation.toObx()
        findByUid(conversation.id)?.let { existing ->
            incoming.boxId = existing.boxId
            incoming.latestMessageText = existing.latestMessageText
            incoming.latestMessageTimestamp = existing.latestMessageTimestamp
        }
        conversationBox.put(incoming)
    }

    private companion object {
        const val TAG = "ConversationRepo"
    }
}
