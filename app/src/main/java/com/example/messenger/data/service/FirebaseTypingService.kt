package com.example.messenger.data.service

import com.example.messenger.data.remote.firebase.RealtimeDbService
import com.example.messenger.domain.service.ITypingService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTypingService @Inject constructor(
    private val realtimeDbService: RealtimeDbService,
    private val firebaseAuth: FirebaseAuth
) : ITypingService {

    override fun observeTyping(conversationId: String): Flow<Set<String>> {
        return realtimeDbService.observeTyping(conversationId).map { data ->
            val currentUserId = firebaseAuth.currentUser?.uid
            data.keys.filter { it != currentUserId }.toSet()
        }
    }

    override suspend fun setTyping(conversationId: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        realtimeDbService.setTyping(conversationId, userId)
    }

    override suspend fun clearTyping(conversationId: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        realtimeDbService.clearTyping(conversationId, userId)
    }
}
