package com.example.messenger.data.repository

import com.example.messenger.data.local.dao.UserDao
import com.example.messenger.data.mapper.toDomain
import com.example.messenger.data.mapper.toEntity
import com.example.messenger.data.remote.firebase.FirebaseAuthService
import com.example.messenger.data.remote.firebase.FirestoreService
import com.example.messenger.domain.model.User
import com.example.messenger.domain.repository.IUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val firestoreService: FirestoreService,
    private val authService: FirebaseAuthService
) : IUserRepository {

    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            // Try local first
            val localUser = userDao.getUserById(userId)
            if (localUser != null) {
                return Result.success(localUser.toDomain())
            }
            // Fall back to remote
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
            // Search locally by username
            val localUser = userDao.getUserByUsername(query)
            if (localUser != null) {
                Result.success(listOf(localUser.toDomain()))
            } else {
                Result.success(emptyList())
            }
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
}
