package com.example.messenger.data.repository

import com.example.messenger.data.local.obx.ObxConversation
import com.example.messenger.data.local.obx.ObxUser
import com.example.messenger.data.local.obx.ObxUser_
import com.example.messenger.data.local.obx.asFlow
import com.example.messenger.data.local.obx.toDomain
import com.example.messenger.data.local.obx.toObx
import com.example.messenger.data.remote.auth.FirebaseAuthService
import com.example.messenger.data.remote.firebase.FirestoreService
import com.example.messenger.domain.model.PhoneNumber
import com.example.messenger.domain.model.User
import com.example.messenger.domain.repository.IUserRepository
import io.objectbox.Box
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userBox: Box<ObxUser>,
    private val conversationBox: Box<ObxConversation>,
    private val firestoreService: FirestoreService,
    private val authService: FirebaseAuthService,
) : IUserRepository {

    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            findByUid(userId)?.let { return Result.success(it.toDomain()) }
            val remoteResult = firestoreService.getUserProfile(userId)
            remoteResult.onSuccess { user -> upsert(user.toObx()) }
            Result.success(remoteResult.getOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val currentUserId = authService.getCurrentUserId()
            val local = userBox.query(ObxUser_.username.equal(query)).build().use { it.findFirst() }
            if (local != null && local.uid != currentUserId) {
                return Result.success(listOf(local.toDomain()))
            }
            firestoreService.searchUsers(query).fold(
                onSuccess = { users ->
                    val filtered = users.filter { it.id != currentUserId }
                    if (filtered.isNotEmpty()) {
                        userBox.put(filtered.map { it.toObx() })
                    }
                    Result.success(filtered)
                },
                onFailure = { e -> Result.failure(e) },
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeUser(userId: String): Flow<User?> {
        return userBox.query(ObxUser_.uid.equal(userId)).build()
            .asFlow()
            .map { rows -> rows.firstOrNull()?.toDomain() }
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
                findByUid(uid)?.let { local ->
                    updates.forEach { (key, value) ->
                        when (key) {
                            "username" -> local.username = value as String
                            "email" -> local.email = value as? String
                            "avatarUrl" -> local.avatarUrl = value as? String
                            "phoneNumber" -> local.phoneNumber = value as? PhoneNumber
                        }
                    }
                    userBox.put(local)
                }
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateContactName(contactId: String, newName: String): Result<Unit> = try {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            Result.failure(IllegalArgumentException("Name cannot be empty"))
        } else {
            val local = findByUid(contactId)
            if (local == null) {
                val remote = firestoreService.getUserProfile(contactId).getOrNull()
                val seed = remote?.copy(username = trimmed)?.toObx()
                    ?: User(id = contactId, username = trimmed).toObx()
                upsert(seed)
            } else {
                local.username = trimmed
                userBox.put(local)
            }
            propagateRenameToConversations(contactId, trimmed)
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun propagateRenameToConversations(contactId: String, newName: String) {
        conversationBox.all.forEach { conv ->
            val idx = conv.participantIds.indexOf(contactId)
            if (idx < 0 || idx >= conv.participantNames.size) return@forEach
            if (conv.participantNames[idx] == newName) return@forEach
            conv.participantNames = conv.participantNames.toMutableList().apply { this[idx] = newName }
            conversationBox.put(conv)
        }
    }

    private fun findByUid(uid: String): ObxUser? =
        userBox.query(ObxUser_.uid.equal(uid)).build().use { it.findFirst() }

    private fun upsert(user: ObxUser) {
        findByUid(user.uid)?.let { user.boxId = it.boxId }
        userBox.put(user)
    }
}
