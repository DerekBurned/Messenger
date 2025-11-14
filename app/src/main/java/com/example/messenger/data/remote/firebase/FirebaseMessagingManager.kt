package com.example.messenger.data.remote.firebase

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This helper class can be injected into your repositories/viewmodels
 * to get the current FCM token on demand.
 */
@Singleton
class FirebaseMessagingManager @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging
) {
    /**
     * Gets the current FCM registration token for this device.
     */
    suspend fun getFcmToken(): Result<String> {
        return try {
            val token = firebaseMessaging.token.await()
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}