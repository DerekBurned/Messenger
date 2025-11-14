package com.example.messenger.data.remote.firebase

import android.app.Activity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri

@Singleton
class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth
) {

    suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User creation failed")

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Login failed")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun sendVerificationCode(
        activity: Activity,
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       
            .setTimeout(60L, TimeUnit.SECONDS) 
            .setActivity(activity)             
            .setCallbacks(callbacks)           
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Phone sign-in failed")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun linkPhoneCredential(credential: PhoneAuthCredential): Result<FirebaseUser> {
        return try {
            val user = getCurrentUser() ?: throw Exception("No user is currently signed in")
            val result = user.linkWithCredential(credential).await()
            val linkedUser = result.user ?: throw Exception("Failed to link phone credential")
            Result.success(linkedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun linkEmailCredential(email: String, password: String): Result<FirebaseUser> {
        return try {
            val user = getCurrentUser() ?: throw Exception("No user is currently signed in")
            val credential = EmailAuthProvider.getCredential(email, password)
            val result = user.linkWithCredential(credential).await()
            val linkedUser = result.user ?: throw Exception("Failed to link email credential")
            Result.success(linkedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun isAuthenticated(): Boolean = auth.currentUser != null

    suspend fun updateProfile(displayName: String?, photoUrl: String?): Result<Unit> {
        return try {
            val user = getCurrentUser() ?: throw Exception("No authenticated user")
            val profileUpdates = UserProfileChangeRequest.Builder().apply {
                displayName?.let { setDisplayName(it) }
                photoUrl?.let { photoUri = it.toUri() }
            }.build()

            user.updateProfile(profileUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = getCurrentUser() ?: throw Exception("No authenticated user")
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}