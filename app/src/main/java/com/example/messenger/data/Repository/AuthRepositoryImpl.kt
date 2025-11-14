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
            val authResult = auth.login(email, password)
            val firebaseUser = authResult.getOrNull()
            val profileResult = firebaseUser?.let { firestore.getUserProfile(it.uid) }
            val user = profileResult?.getOrThrow()

        }catch (e: Exception){
            emit(Resource.Error("${e.message}, Internal error occurred"))
        }
    }

    override suspend fun loginWithPhone(
        credential: PhoneAuthCredential
    ): Flow<Resource<List<User>>> = flow {
        try {
            emit(Resource.Loading)
            val result = auth.signInWithPhoneCredential(credential)
            val firebaseUser =result.getOrNull()
            val profileResult = firebaseUser?.let { firestore.getUserProfile(it.uid) }
            val user  = profileResult?.getOrThrow()
            emit(Resource.Success(user))

        }catch (e: Exception){
            emit(Resource.Error("${e.message}, Internal error occurred"))
        }
    }

    override suspend fun linkPhoneToAccount(credential: PhoneAuthCredential): Flow<Resource<Unit>> =flow {
            try {
                emit(Resource.Loading)
                auth.linkPhoneCredential(credential).getOrThrow()
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

            // Step 1: Create the authentication user in Firebase Auth
            val authResult = auth.register(email, password, username)
            val firebaseUser = authResult.getOrThrow() // This is your 'onSuccess'

            // Step 2: MAP the FirebaseUser to your domain User model
            val newUser = User(
                id = firebaseUser.uid, // This is the crucial link!
                username = username, // Use the username passed in
                email = email,
                phoneNumber = null, // Will be null until linked
                avatarUrl = firebaseUser.photoUrl?.toString(), // Use auth profile pic if any
                lastSeen = System.currentTimeMillis(),
                isOnline = false, // Or true, depending on your logic
                fcmToken = null // Token will be updated later
            )
            // Step 3: SAVE your new User model to Firestore
            firestore.createUserProfile(newUser).getOrThrow()

            // Step 4: Emit the successful User object
            emit(Resource.Success(newUser))

        } catch (e: Exception) {
            // This will catch errors from auth.register OR firestore.createUserProfile
            emit(Resource.Error(e.message ?: "An error occurred during registration"))
        }
    }

    override suspend fun logout() {
       return auth.logout()
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = auth.getCurrentUser()
        return firebaseUser?.let {
            User( id = it.uid, // This is the crucial link!
                username = firebaseUser.displayName, // Use the username passed in
                email = firebaseUser.email,
                phoneNumber = null, // Will be null until linked
                avatarUrl = firebaseUser.photoUrl?.toString(), // Use auth profile pic if any
                lastSeen = System.currentTimeMillis(),
                isOnline = false, // Or true, depending on your logic
                fcmToken = null // Token will be updated later
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