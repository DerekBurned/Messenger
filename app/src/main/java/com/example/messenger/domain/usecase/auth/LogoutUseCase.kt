package com.example.messenger.domain.usecase.auth

import com.example.messenger.domain.repository.IAuthRepository
import com.example.messenger.domain.repository.IUserRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: IAuthRepository,
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke() {
        // Update user status to offline before logout
        userRepository.updateUserStatus(isOnline = false)
        return authRepository.logout()
    }
}