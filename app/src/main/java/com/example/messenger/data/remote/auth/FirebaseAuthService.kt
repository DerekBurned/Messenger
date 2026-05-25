package com.example.messenger.data.remote.auth

import android.app.Activity
import android.util.Log
import androidx.core.net.toUri
import com.example.messenger.data.local.database.MessengerDatabase
import com.example.messenger.data.local.repository.ILocalRepository
import com.example.messenger.util.VerificationResult
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth,
    private val repository: ILocalRepository
    ) {

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    suspend fun sendVerificationCode(
        phoneNumber: String,
        activity: Activity
    ): Result<VerificationResult> = suspendCancellableCoroutine { continuation ->

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                if (continuation.isActive) {
                    continuation.resume(
                        Result.success(VerificationResult.AutoVerified(credential))
                    )
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                if (continuation.isActive) {
                    continuation.resumeWithException(e)
                }
            }

            override fun onCodeSent(
                verId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = verId
                resendToken = token
                Log.d("FirebaseAuthService", "onCodeSent: $verId")
                if (continuation.isActive) {
                    continuation.resume(
                        Result.success(VerificationResult.CodeSent(verId))
                    )
                }
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyCode(code: String): Result<PhoneAuthCredential> {
        return try {
            val verId = verificationId
                ?: return Result.failure(Exception("No verification ID found"))

            val credential = PhoneAuthProvider.getCredential(verId, code)
            Result.success(credential)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithPhone(credential: PhoneAuthCredential): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Sign in failed")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyCodeAndSignIn(code: String): Result<FirebaseUser> {
        return try {
            val credentialResult = verifyCode(code)
            if (credentialResult.isFailure) {
                return Result.failure(credentialResult.exceptionOrNull()!!)
            }
            signInWithPhone(credentialResult.getOrNull()!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun linkPhone(credential: PhoneAuthCredential): Result<Unit> {
        return try {
            val user = getCurrentUser()
                ?: return Result.failure(Exception("No user signed in"))
            if (isPhoneLinked()) {
                return Result.failure(Exception("Phone already linked to this account"))
            }
            user.linkWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("This phone number is already registered to another account"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isPhoneLinked(): Boolean {
        val user = getCurrentUser() ?: return false
        return user.providerData.any { it.providerId == PhoneAuthProvider.PROVIDER_ID }
    }

    fun getUserPhoneNumber(): String? = getCurrentUser()?.phoneNumber

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

    suspend fun logout() {
        repository.resetDB()
        auth.signOut()
        verificationId = null
        resendToken = null
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

    suspend fun resendVerificationCode(
        phoneNumber: String,
        activity: Activity
    ): Result<VerificationResult> = suspendCancellableCoroutine { continuation ->

        val token = resendToken
            ?: return@suspendCancellableCoroutine continuation.resume(
                Result.failure(Exception("No resend token available"))
            )

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                if (continuation.isActive) {
                    continuation.resume(
                        Result.success(VerificationResult.AutoVerified(credential))
                    )
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                if (continuation.isActive) {
                    continuation.resumeWithException(e)
                }
            }

            override fun onCodeSent(
                verId: String,
                newToken: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = verId
                resendToken = newToken

                if (continuation.isActive) {
                    continuation.resume(
                        Result.success(VerificationResult.CodeSent(verId))
                    )
                }
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}