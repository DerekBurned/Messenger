package com.example.messenger.data.Repository

import com.example.messenger.domain.model.User
import com.example.messenger.domain.repository.IUserRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl @Inject constructor(): IUserRepository {
    override suspend fun getUserById(userId: String): Result<User?> {
        TODO("Not yet implemented")
    }

    override suspend fun searchUsers(query: String): Result<List<User>> {
        TODO("Not yet implemented")
    }

    override fun observeUser(userId: String): Flow<User?> {
        TODO("Not yet implemented")
    }

    override suspend fun updateUserStatus(isOnline: Boolean): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun updateLastSeen(): Result<Unit> {
        TODO("Not yet implemented")
    }
}