package com.example.messenger.domain.usecase.user

import com.example.messenger.domain.model.User
import com.example.messenger.domain.repository.IUserRepository
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(query: String): Result<List<User>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }

        if (query.length < 2) {
            return Result.failure(Exception("Search query too short"))
        }

        return userRepository.searchUsers(query.trim())
    }
}