package com.example.messenger.data.Repository

import androidx.compose.foundation.isSystemInDarkTheme
import com.example.messenger.data.remote.firebase.FirebaseAuthService
import com.example.messenger.data.remote.firebase.FirestoreService
import com.example.messenger.domain.model.User
import com.example.messenger.domain.repository.IAuthRepository
import com.example.messenger.util.Resource
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuthService,
    private val firestore: FirestoreService

): IAuthRepository {
    override suspend fun loginWithEmail(
        email: String,
        password: String
    ): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading)
            val authResult = auth.loginWithEmail(email, password)
            val firebaseUser = authResult.getOrNull()
            val profileResult = firebaseUser?.let { firestore.getUserProfile(it.uid) }
            val user = profileResult?.getOrThrow()

        }catch (e: Exception){
            emit(Resource.Error("${e.message}, Internal error occurred"))
        }
    }

    override suspend fun loginWithPhone(
        credential: PhoneAuthCredential
    ): Resource<User> {
        return try {
            val result = auth.signInWithPhone(credential)
            val firebaseUser =result.getOrNull()
            val profileResult = firebaseUser?.let { firestore.getUserProfile(it.uid) }
            val user  = profileResult?.getOrThrow()
            val currentUser = User(
                id = firebaseUser!!.uid, 
                username = firebaseUser.displayName, 
                email = firebaseUser.email,
                phoneNumber = null, 
                avatarUrl = firebaseUser.photoUrl?.toString(), 
                lastSeen = System.currentTimeMillis(),
                isOnline = false, 
                fcmToken = null 
            )
            Resource.Success(currentUser)
        }catch (e: Exception){
            Resource.Error("${e.message}, Internal error occurred")
        }
    }

    override suspend fun linkPhoneToAccount(credential: PhoneAuthCredential): Flow<Resource<Unit>> =flow {
            try {
                emit(Resource.Loading)
                auth.linkPhone(credential).getOrThrow()
                val uid = auth.getCurrentUserId()
                val phone = credential.signInMethod
                emit(Resource.Success(Unit))
            }catch (e: Exception){
                emit(Resource.Error("${e.message}, Internal error occurred"))
            }
    }

    override suspend fun register(
        email: String,
        password: String,
        username: String
    ): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading)

            val authResult = auth.registerWithEmail(email, password, username)
            val firebaseUser = authResult.getOrThrow() 

            val newUser = User(
                id = firebaseUser.uid, 
                username = username, 
                email = email,
                phoneNumber = null, 
                avatarUrl = firebaseUser.photoUrl?.toString(), 
                lastSeen = System.currentTimeMillis(),
                isOnline = false, 
                fcmToken = null 
            )
            
            firestore.createUserProfile(newUser).getOrThrow()

            emit(Resource.Success(newUser))

        } catch (e: Exception) {
            
            emit(Resource.Error(e.message ?: "An error occurred during registration"))
        }
    }

    override suspend fun logout() {
       return auth.logout()
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = auth.getCurrentUser()
        return firebaseUser?.let {
            User( id = it.uid, 
                username = firebaseUser.displayName, 
                email = firebaseUser.email,
                phoneNumber = null, 
                avatarUrl = firebaseUser.photoUrl?.toString(), 
                lastSeen = System.currentTimeMillis(),
                isOnline = false, 
                fcmToken = null 
            )
        }
    }

    override fun observeAuthState(): Flow<FirebaseUser?> {
        return auth.observeAuthState()
    }

    override suspend fun updateUserProfile(
        username: String,
        avatarUrl: String?
    ): Result<Unit> {
        return auth.updateProfile(username, avatarUrl)

    }
}