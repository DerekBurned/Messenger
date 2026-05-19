package com.example.messenger.data.repository

import com.example.messenger.data.remote.auth.FirebaseAuthService
import com.example.messenger.data.remote.firebase.FirestoreService
import com.example.messenger.domain.model.User
import com.example.messenger.domain.repository.IAuthRepository
import com.example.messenger.util.Resource
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuthService,
    private val firestore: FirestoreService,
) : IAuthRepository {

    override suspend fun loginWithPhone(
        credential: PhoneAuthCredential,
        username: String?,
    ): Resource<User> {
        return try {
            val firebaseUser = auth.signInWithPhone(credential).getOrThrow()
            val existing = firestore.getUserProfile(firebaseUser.uid).getOrNull()
            val user = existing ?: User(
                id = firebaseUser.uid,
                username = username?.takeIf { it.isNotBlank() } ?: firebaseUser.displayName,
                email = null,
                phoneNumber = null,
                avatarUrl = firebaseUser.photoUrl?.toString(),
                lastSeen = System.currentTimeMillis(),
                isOnline = true,
                fcmToken = null,
            ).also { firestore.createUserProfile(it).getOrThrow() }
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error("${e.message}, Phone sign-in failed")
        }
    }

    override suspend fun linkPhoneToAccount(credential: PhoneAuthCredential): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading)
            auth.linkPhone(credential).getOrThrow()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error("${e.message}, Internal error occurred"))
        }
    }

    override suspend fun logout() {
        return auth.logout()
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = auth.getCurrentUser()
        return firebaseUser?.let {
            User(
                id = it.uid,
                username = firebaseUser.displayName,
                email = null,
                phoneNumber = null,
                avatarUrl = firebaseUser.photoUrl?.toString(),
                lastSeen = System.currentTimeMillis(),
                isOnline = false,
                fcmToken = null,
            )
        }
    }

    override fun observeAuthState(): Flow<FirebaseUser?> {
        return auth.observeAuthState()
    }

    override suspend fun updateUserProfile(
        username: String,
        avatarUrl: String?,
    ): Result<Unit> {
        return auth.updateProfile(username, avatarUrl)
    }
}
