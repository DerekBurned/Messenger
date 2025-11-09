package com.example.messenger.domain.usecase.user

import com.example.messenger.domain.model.User
import com.example.messenger.domain.repository.IUserRepository
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(userId: String): Result<User?> {
        if (userId.isBlank()) {
            return Result.failure(Exception("Invalid user ID"))
        }
        return userRepository.getUserById(userId)
    }
}