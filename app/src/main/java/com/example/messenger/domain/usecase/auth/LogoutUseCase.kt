package com.example.messenger.domain.usecase.auth

import android.util.Log
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
        Log.d(TAG, "logout: marking offline")
        userRepository.updateUserStatus(isOnline = false)
        Log.d(TAG, "logout: signing out of Firebase")
        authRepository.logout()
        Log.d(TAG, "logout: clearing local Room")
        localRepository.resetDB()
        Log.d(TAG, "logout: done")
    }

    private companion object {
        const val TAG = "LogoutUseCase"
    }
}
