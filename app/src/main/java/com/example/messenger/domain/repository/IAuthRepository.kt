package com.example.messenger.domain.repository
import com.example.messenger.domain.model.User
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    suspend fun login(email: String? = null,number: String? =null, password: String): Result<User>
    suspend fun register(email: String, password: String, username: String): Result<User>
    suspend fun logout(): Result<Unit>
    fun getCurrentUser(): User?
    fun observeAuthState(): Flow<User?>
    suspend fun updateUserProfile(username: String, avatarUrl: String?): Result<Unit>
}