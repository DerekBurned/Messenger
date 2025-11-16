package com.example.messenger.domain.usecase.auth

import com.example.messenger.domain.model.User
import com.example.messenger.domain.repository.IAuthRepository
import com.example.messenger.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginWithEmailUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Flow<Resource<User>> {

        return authRepository.loginWithEmail(email = email.trim(), password =  password)
    }
}