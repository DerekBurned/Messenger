package com.example.messenger.data.presence

import android.util.Log
import com.example.messenger.domain.model.PresenceState
import com.example.messenger.domain.service.IPresenceService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceManager @Inject constructor(
    private val presenceService: IPresenceService
) {
    companion object {
        private const val TAG = "PresenceManager"
        private const val AWAY_TO_OFFLINE_DELAY_MS = 5 * 60 * 1000L 
    }

    private val transitions = Channel<PresenceState>(Channel.CONFLATED)
    private var collector: Job? = null
    private var offlineTimerJob: Job? = null

    fun goOnline(scope: CoroutineScope) {
        ensureCollector(scope)
        offlineTimerJob?.cancel()
        offlineTimerJob = null
        transitions.trySend(PresenceState.ONLINE)
    }

    fun goAway(scope: CoroutineScope) {
        ensureCollector(scope)
        transitions.trySend(PresenceState.AWAY)
        offlineTimerJob?.cancel()
        offlineTimerJob = scope.launch {
            delay(AWAY_TO_OFFLINE_DELAY_MS)
            transitions.trySend(PresenceState.OFFLINE)
        }
    }

    fun goOffline(scope: CoroutineScope) {
        ensureCollector(scope)
        offlineTimerJob?.cancel()
        offlineTimerJob = null
        transitions.trySend(PresenceState.OFFLINE)
    }

    fun disconnect(scope: CoroutineScope) {
        offlineTimerJob?.cancel()
        offlineTimerJob = null
        scope.launch {
            try {
                presenceService.removePresence()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove presence", e)
            }
        }
    }

    private fun ensureCollector(scope: CoroutineScope) {
        if (collector?.isActive == true) return
        collector = scope.launch {
            transitions.consumeAsFlow()
                .distinctUntilChanged()
                .collect { state ->
                    try {
                        presenceService.setMyPresence(state)
                        if (state == PresenceState.ONLINE) {
                            presenceService.setupOnDisconnect()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to set $state presence", e)
                    }
                }
        }
    }
}
