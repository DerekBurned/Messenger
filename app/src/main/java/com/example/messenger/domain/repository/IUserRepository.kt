package com.example.messenger.domain.repository

import com.example.messenger.domain.model.User
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    suspend fun getUserById(userId: String): Result<User?>
    suspend fun searchUsers(query: String): Result<List<User>>
    fun observeUser(userId: String): Flow<User?>
    suspend fun updateUserStatus(isOnline: Boolean): Result<Unit>
    suspend fun updateLastSeen(): Result<Unit>
}