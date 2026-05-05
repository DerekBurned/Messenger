package com.example.messenger.domain.repository

import com.example.messenger.domain.model.User
import com.example.messenger.util.Resource
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    suspend fun linkPhoneToAccount(credential: PhoneAuthCredential): Flow<Resource<Unit>>

    suspend fun loginWithPhone(credential: PhoneAuthCredential, username: String? = null): Resource<User>

    suspend fun logout()
    fun getCurrentUser(): User?
    fun observeAuthState(): Flow<FirebaseUser?>
    suspend fun updateUserProfile(username: String, avatarUrl: String?): Result<Unit>
}
