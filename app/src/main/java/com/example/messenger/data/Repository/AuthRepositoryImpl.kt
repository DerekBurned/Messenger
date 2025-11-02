package com.example.messenger.data.Repository

import com.example.messenger.domain.model.User
import com.example.messenger.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl: IAuthRepository {
    override suspend fun login(
        email: String?,
        number: String?,
        password: String
    ): Result<User> {
        TODO("Not yet implemented")
    }

    override suspend fun register(
        email: String,
        password: String,
        username: String
    ): Result<User> {
        TODO("Not yet implemented")
    }

    override suspend fun logout(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun getCurrentUser(): User? {
        TODO("Not yet implemented")
    }

    override fun observeAuthState(): Flow<User?> {
        TODO("Not yet implemented")
    }

    override suspend fun updateUserProfile(
        username: String,
        avatarUrl: String?
    ): Result<Unit> {
        TODO("Not yet implemented")
    }
}