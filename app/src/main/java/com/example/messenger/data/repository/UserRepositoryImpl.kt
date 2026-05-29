package com.example.messenger.data.repository

import com.example.messenger.data.local.dao.ConversationDao
import com.example.messenger.data.local.dao.UserDao
import com.example.messenger.data.mapper.toDomain
import com.example.messenger.data.mapper.toEntity
import com.example.messenger.data.remote.auth.FirebaseAuthService
import com.example.messenger.data.remote.firebase.FirestoreService
import com.example.messenger.domain.model.PhoneNumber
import com.example.messenger.domain.model.User
import com.example.messenger.domain.repository.IUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val conversationDao: ConversationDao,
    private val firestoreService: FirestoreService,
    private val authService: FirebaseAuthService,
) : IUserRepository {

    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            
            val localUser = userDao.getUserById(userId)
            if (localUser != null) {
                return Result.success(localUser.toDomain())
            }
            
            val remoteResult = firestoreService.getUserProfile(userId)
            remoteResult.onSuccess { user ->
                userDao.insertUser(user.toEntity())
            }
            Result.success(remoteResult.getOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val currentUserId = authService.getCurrentUserId()

            val localUser = userDao.getUserByUsername(query)
            if (localUser != null && localUser.id != currentUserId) {
                return Result.success(listOf(localUser.toDomain()))
            }

            val remoteResult = firestoreService.searchUsers(query)
            remoteResult.fold(
                onSuccess = { users ->
                    val filtered = users.filter { it.id != currentUserId }
                    
                    if (filtered.isNotEmpty()) {
                        userDao.insertUsers(filtered.map { it.toEntity() })
                    }
                    Result.success(filtered)
                },
                onFailure = { e -> Result.failure(e) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeUser(userId: String): Flow<User?> {
        return userDao.getAllUsers().map { users ->
            users.find { it.id == userId }?.toDomain()
        }
    }

    override suspend fun updateUserStatus(isOnline: Boolean): Result<Unit> {
        return try {
            val uid = authService.getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))
            firestoreService.updateUserProfile(uid, mapOf("isOnline" to isOnline))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLastSeen(): Result<Unit> {
        return try {
            val uid = authService.getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))
            firestoreService.updateUserProfile(uid, mapOf("lastSeen" to System.currentTimeMillis()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return try {
            val uid = authService.getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))
            val result = firestoreService.updateUserProfile(uid, updates)
            result.onSuccess {

                val localUser = userDao.getUserById(uid)
                if (localUser != null) {
                    val merged = updates.entries.fold(localUser) { acc, (key, value) ->
                        when (key) {
                            "username" -> acc.copy(username = value as String)
                            "email" -> acc.copy(email = value as? String)
                            "avatarUrl" -> acc.copy(avatarUrl = value as? String)
                            "phoneNumber" -> acc.copy(phoneNumber = value as? PhoneNumber)
                            else -> acc
                        }
                    }
                    userDao.updateUser(merged)
                }
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateContactName(
        contactId: String,
        newName: String,
    ): Result<Unit> = try {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            Result.failure(IllegalArgumentException("Name cannot be empty"))
        } else {
            val local = userDao.getUserById(contactId)
            if (local == null) {
                
                val remote = firestoreService.getUserProfile(contactId).getOrNull()
                val seed = remote?.copy(username = trimmed)?.toEntity()
                    ?: User(id = contactId, username = trimmed).toEntity()
                userDao.insertUser(seed)
            } else {
                userDao.updateUser(local.copy(username = trimmed))
            }
            propagateRenameToConversations(contactId, trimmed)
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun propagateRenameToConversations(contactId: String, newName: String) {
        val conversations = conversationDao.getAllConversationsOnce()
        conversations.forEach { conv ->
            val idx = conv.participantIds.indexOf(contactId)
            if (idx < 0 || idx >= conv.participantNames.size) return@forEach
            if (conv.participantNames[idx] == newName) return@forEach
            val updatedNames = conv.participantNames.toMutableList().apply { this[idx] = newName }
            conversationDao.insertConversation(conv.copy(participantNames = updatedNames))
        }
    }
}
