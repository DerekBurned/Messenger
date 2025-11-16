package com.example.messenger.domain.usecase.auth

import androidx.credentials.Credential
import com.example.messenger.domain.model.User
import com.example.messenger.domain.repository.IAuthRepository
import com.example.messenger.util.Resource
import com.google.firebase.auth.PhoneAuthCredential
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginWithPhoneNumberUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(credential: PhoneAuthCredential): Resource<User> {

        return authRepository.loginWithPhone(credential)
    }
}