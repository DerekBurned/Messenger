package com.example.messenger.data.remote.call

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.messenger.data.remote.auth.FirebaseAuthService
import com.example.messenger.domain.service.ICallSignalingService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomingCallCoordinator @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val signalingService: ICallSignalingService,
    private val firebaseAuthService: FirebaseAuthService,
    private val firebaseAuth: FirebaseAuth,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var observerJob: Job? = null
    private var lastSeenCallId: String? = null

    fun start() {
        
        firebaseAuth.addAuthStateListener { auth ->
            attachListener(auth.currentUser?.uid)
        }
        attachListener(firebaseAuthService.getCurrentUserId())
    }

    private fun attachListener(uid: String?) {
        observerJob?.cancel()
        lastSeenCallId = null
        if (uid.isNullOrBlank()) return
        observerJob = scope.launch {
            signalingService.observeIncomingCall(uid).collectLatest { signal ->
                if (signal == null) return@collectLatest
                if (signal.callId == lastSeenCallId) return@collectLatest
                
                if (ActiveCallHolder.snapshot()?.callId == signal.callId) return@collectLatest
                lastSeenCallId = signal.callId
                val intent = CallForegroundService.incomingIntent(
                    ctx = context,
                    callId = signal.callId,
                    callerId = signal.callerId,
                    calleeId = signal.calleeId,
                    channelName = signal.channelName,

                    partnerName = signal.callerId,
                    partnerPhone = "",
                )
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }
}
