package com.example.messenger.domain.usecase.auth

import com.example.messenger.domain.model.User
import com.example.messenger.domain.repository.IAuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAuthStateUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    operator fun invoke(): Flow<FirebaseUser?> {
        return authRepository.observeAuthState()
    }
}