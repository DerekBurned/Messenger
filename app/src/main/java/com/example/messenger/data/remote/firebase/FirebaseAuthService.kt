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

/**
 * Handles Firebase Authentication operations
 * - User registration with email/password
 * - User login
 * - Password reset
 * - Auth state observation
 * - User logout
 * - Phone number authentication and linking
 */
@Singleton
class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth
) {

    /**
     * Register a new user with email and password
     */
    suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User creation failed")

            // Update display name
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
     * Login user with email and password
     */
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
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

    // --- Phone Authentication Functions ---

    /**
     * Starts the phone number verification process.
     * This function should be called from your UI (Activity/Fragment).
     *
     * @param activity The activity to bind the verification callbacks to.
     * @param phoneNumber The full phone number (e.g., "+16505551234").
     * @param callbacks The listener for verification state changes.
     */
    fun sendVerificationCode(
        activity: Activity,
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)             // Activity (for callback binding)
            .setCallbacks(callbacks)           // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Sign in (or register) a user using a PhoneAuthCredential.
     * You get this credential from the onVerificationCompleted callback or
     * by creating one from the verificationId and the user's OTP code.
     */
    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Phone sign-in failed")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Account Linking Functions ---

    /**
     * Links a PhoneAuthCredential to an existing, already signed-in user account.
     * Call this after a user signs in with email/password and then wants to add their phone number.
     */
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

    /**
     * Links an Email/Password credential to an existing, already signed-in user account.
     * Call this after a user signs in with their phone and then wants to add an email/password.
     */
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

    // --- Existing Helper Functions ---

    /**
     * Get current authenticated user
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Observe authentication state changes
     */
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    /**
     * Logout current user
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean = auth.currentUser != null

    /**
     * Update user profile
     */
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

    /**
     * Delete user account
     */
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