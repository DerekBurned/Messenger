package com.example.messenger.domain.usecase.auth

import com.example.messenger.data.local.repository.ILocalRepository
import com.example.messenger.domain.repository.IAuthRepository
import com.example.messenger.domain.repository.IUserRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: IAuthRepository,
    private val userRepository: IUserRepository,
    private val localRepository: ILocalRepository,
) {
    
    suspend operator fun invoke() {
        userRepository.updateUserStatus(isOnline = false)
        authRepository.logout()
        localRepository.resetDB()
    }
}
