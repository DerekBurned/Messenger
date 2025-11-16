package com.example.messenger.data.remote.firebase

import android.annotation.SuppressLint
import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.core.net.toUri
import com.example.messenger.util.VerificationResult

/**
 * Unified Authentication Service
 * Supports Phone AND Email authentication with account linking
 *
 * User Journey Options:
 * 1. Sign up with phone → optionally add email later
 * 2. Sign up with email → optionally add phone later
 * 3. Login with phone OR email (if both are linked)
 */
@Singleton
class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth
) {

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    // ==========================================================================
    // PHONE AUTHENTICATION (Primary Method for Messenger)
    // ==========================================================================

    /**
     * Step 1: Send SMS verification code to phone number
     */
    suspend fun sendVerificationCode(
        phoneNumber: String,
        activity: Activity
    ): Result<VerificationResult> = suspendCancellableCoroutine { continuation ->

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification successful (instant or SMS auto-read)
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

    /**
     * Step 2: Verify SMS code
     */
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

    /**
     * Step 3: Sign in with phone credential
     * Creates new account if doesn't exist, or logs into existing account
     */
    suspend fun signInWithPhone(credential: PhoneAuthCredential): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Sign in failed")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Complete flow: Verify code and sign in
     */
    suspend fun verifyCodeAndSignIn(code: String): Result<FirebaseUser> {
        return try {
            val credentialResult = verifyCode(code)
            if (credentialResult.isFailure) {
                return Result.failure(credentialResult.exceptionOrNull()!!)
            }

            val credential = credentialResult.getOrNull()!!
            signInWithPhone(credential)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================================================
    // EMAIL/PASSWORD AUTHENTICATION
    // ==========================================================================

    /**
     * Register with email/password
     */
    suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Registration failed")

            // Set display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login with email/password
     */
    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Login failed")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send password reset email
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================================================
    // ACCOUNT LINKING (The Important Part!)
    // ==========================================================================

    /**
     * Link email/password to current phone account
     * Call this AFTER user signs in with phone
     *
     * Use case: User signed up with phone, now wants to add email for backup
     */
    suspend fun linkEmail(email: String, password: String): Result<Unit> {
        return try {
            val user = getCurrentUser()
                ?: return Result.failure(Exception("No user signed in"))

            // Check if email is already linked
            if (isEmailLinked()) {
                return Result.failure(Exception("Email already linked to this account"))
            }

            // Create email credential
            val credential = EmailAuthProvider.getCredential(email, password)

            // Link to current account
            user.linkWithCredential(credential).await()

            Result.success(Unit)
        } catch (e: FirebaseAuthUserCollisionException) {
            // This email is already used by another account
            Result.failure(Exception("This email is already registered to another account"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Link phone number to current email account
     * Call this AFTER user signs in with email
     *
     * Use case: User signed up with email, now wants to add phone for easier login
     */
    suspend fun linkPhone(credential: PhoneAuthCredential): Result<Unit> {
        return try {
            val user = getCurrentUser()
                ?: return Result.failure(Exception("No user signed in"))

            // Check if phone is already linked
            if (isPhoneLinked()) {
                return Result.failure(Exception("Phone already linked to this account"))
            }

            // Link to current account
            user.linkWithCredential(credential).await()

            Result.success(Unit)
        } catch (e: FirebaseAuthUserCollisionException) {
            // This phone is already used by another account
            Result.failure(Exception("This phone number is already registered to another account"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Complete flow: Send SMS to link phone number
     */
    suspend fun sendVerificationCodeToLink(
        phoneNumber: String,
        activity: Activity
    ): Result<VerificationResult> {
        // Same as sendVerificationCode but for linking
        return sendVerificationCode(phoneNumber, activity)
    }

    /**
     * Complete flow: Verify code and link phone
     */
    suspend fun verifyCodeAndLinkPhone(code: String): Result<Unit> {
        return try {
            val credentialResult = verifyCode(code)
            if (credentialResult.isFailure) {
                return Result.failure(credentialResult.exceptionOrNull()!!)
            }

            val credential = credentialResult.getOrNull()!!
            linkPhone(credential)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Unlink email from account
     */
    suspend fun unlinkEmail(): Result<Unit> {
        return try {
            val user = getCurrentUser() ?: throw Exception("No user signed in")

            if (!isEmailLinked()) {
                return Result.failure(Exception("No email linked to unlink"))
            }

            user.unlink(EmailAuthProvider.PROVIDER_ID).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Unlink phone from account
     */
    suspend fun unlinkPhone(): Result<Unit> {
        return try {
            val user = getCurrentUser() ?: throw Exception("No user signed in")

            if (!isPhoneLinked()) {
                return Result.failure(Exception("No phone linked to unlink"))
            }

            user.unlink(PhoneAuthProvider.PROVIDER_ID).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================================================
    // ACCOUNT INFO
    // ==========================================================================

    /**
     * Check if email is linked to current account
     */
    fun isEmailLinked(): Boolean {
        val user = getCurrentUser() ?: return false
        return user.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }
    }

    /**
     * Check if phone is linked to current account
     */
    fun isPhoneLinked(): Boolean {
        val user = getCurrentUser() ?: return false
        return user.providerData.any { it.providerId == PhoneAuthProvider.PROVIDER_ID }
    }

    /**
     * Get linked providers (e.g., ["phone", "password"])
     */
    fun getLinkedProviders(): List<String> {
        val user = getCurrentUser() ?: return emptyList()
        return user.providerData.map { it.providerId }
    }

    /**
     * Get user's phone number (if linked)
     */
    fun getUserPhoneNumber(): String? = getCurrentUser()?.phoneNumber

    /**
     * Get user's email (if linked)
     */
    fun getUserEmail(): String? = getCurrentUser()?.email

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

    /**
     * Resend verification code
     */
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

/**
 * Verification result sealed class
 */
