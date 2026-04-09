package com.example.messenger.data.presence

import android.util.Log
import com.example.messenger.domain.model.PresenceState
import com.example.messenger.domain.service.IPresenceService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceManager @Inject constructor(
    private val presenceService: IPresenceService
) {
    companion object {
        private const val TAG = "PresenceManager"
        private const val AWAY_TO_OFFLINE_DELAY_MS = 5 * 60 * 1000L // 5 minutes
    }

    private var offlineTimerJob: Job? = null
    private var isActive = false

    fun goOnline(scope: CoroutineScope) {
        if (isActive) return
        isActive = true
        offlineTimerJob?.cancel()
        scope.launch {
            try {
                presenceService.setMyPresence(PresenceState.ONLINE)
                presenceService.setupOnDisconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set online presence", e)
            }
        }
    }

    fun goAway(scope: CoroutineScope) {
        isActive = false
        offlineTimerJob?.cancel()
        scope.launch {
            try {
                presenceService.setMyPresence(PresenceState.AWAY)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set away presence", e)
            }
        }
        offlineTimerJob = scope.launch {
            delay(AWAY_TO_OFFLINE_DELAY_MS)
            try {
                presenceService.setMyPresence(PresenceState.OFFLINE)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set offline presence", e)
            }
        }
    }

    fun goOffline(scope: CoroutineScope) {
        isActive = false
        offlineTimerJob?.cancel()
        scope.launch {
            try {
                presenceService.setMyPresence(PresenceState.OFFLINE)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set offline presence", e)
            }
        }
    }

    fun disconnect(scope: CoroutineScope) {
        isActive = false
        offlineTimerJob?.cancel()
        scope.launch {
            try {
                presenceService.removePresence()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove presence", e)
            }
        }
    }
}
