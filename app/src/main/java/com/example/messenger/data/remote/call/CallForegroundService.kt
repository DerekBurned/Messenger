package com.example.messenger.data.remote.call

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.getSystemService
import com.example.messenger.R
import com.example.messenger.domain.model.CallSignal
import com.example.messenger.domain.model.CallStatus
import com.example.messenger.domain.service.CallConnectionState
import com.example.messenger.domain.service.CallEventListener
import com.example.messenger.domain.service.ICallService
import com.example.messenger.domain.service.ICallSignalingService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.messenger.presentation.IncomingCallActivity
import com.example.messenger.presentation.MainActivity
import com.example.messenger.presentation.notification.NotificationChannels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.zip.CRC32
import javax.inject.Inject

@AndroidEntryPoint
class CallForegroundService : Service() {

    @Inject lateinit var callService: ICallService
    @Inject lateinit var signalingService: ICallSignalingService
    @Inject lateinit var missedCallRecorder: MissedCallRecorder

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var tickerJob: Job? = null
    private var statusObserverJob: Job? = null
    private var ringTimeoutJob: Job? = null
    private var requestingTimeoutJob: Job? = null
    private var outgoingTimeoutJob: Job? = null
    private var ringingAckJob: Job? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus: Boolean = false
    private var ringbackTone: ToneGenerator? = null
    
    private var ringingStarted: Boolean = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        callService.setEventListener(eventListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(TAG, "onStartCommand action=$action")
        if (action == null) return START_NOT_STICKY
        when (action) {
            ACTION_START_OUTGOING -> handleStartOutgoing(intent)
            ACTION_START_INCOMING -> handleStartIncoming(intent)
            ACTION_ACCEPT -> handleAccept()
            ACTION_DECLINE -> handleDecline()
            ACTION_END -> handleEnd()
            ACTION_TOGGLE_MUTE -> handleToggleMute()
            ACTION_TOGGLE_SPEAKER -> handleToggleSpeaker()
            else -> Unit
        }
        return START_NOT_STICKY
    }

    private fun handleStartOutgoing(intent: Intent) {
        ringingStarted = false
        val call = ActiveCallHolder.ActiveCall(
            callId = intent.getStringExtra(EXTRA_CALL_ID).orEmpty(),
            callerId = intent.getStringExtra(EXTRA_CALLER_ID).orEmpty(),
            calleeId = intent.getStringExtra(EXTRA_CALLEE_ID).orEmpty(),
            channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME).orEmpty(),
            partnerName = intent.getStringExtra(EXTRA_PARTNER_NAME).orEmpty(),
            partnerPhone = intent.getStringExtra(EXTRA_PARTNER_PHONE).orEmpty(),
            isIncoming = false,
        )
        ActiveCallHolder.set(call)
        if (!startForegroundCompat(buildOngoingNotification(call))) return

        scope.launch {
            runCatching {
                signalingService.sendCallSignal(
                    CallSignal(
                        callId = call.callId,
                        callerId = call.callerId,
                        calleeId = call.calleeId,
                        channelName = call.channelName,
                        status = CallStatus.RINGING,
                        timestamp = System.currentTimeMillis(),
                    ),
                )
            }
        }
        acquireAudioFocus()
        callService.joinChannel(call.channelName, uidFromUserId(call.callerId))
        observeRemoteStatus(call.calleeId, call.callId, isCaller = true)
        observeRingingAck(call.callerId, call.callId)
        startRequestingTimeout()
    }

    private fun handleStartIncoming(intent: Intent) {
        val newCallId = intent.getStringExtra(EXTRA_CALL_ID).orEmpty()
        val existing = ActiveCallHolder.snapshot()
        if (existing != null && existing.callId != newCallId) {

            Log.d(TAG, "busy: declining concurrent call $newCallId while on ${existing.callId}")
            val existingNotification =
                if (existing.isActive || !existing.isIncoming) buildOngoingNotification(existing)
                else buildRingingNotification(existing)
            startForegroundCompat(existingNotification)
            val newCalleeId = intent.getStringExtra(EXTRA_CALLEE_ID).orEmpty()
            scope.launch {
                runCatching {
                    signalingService.updateCallStatus(newCalleeId, newCallId, CallStatus.DECLINED)
                }
            }
            return
        }
        val call = ActiveCallHolder.ActiveCall(
            callId = intent.getStringExtra(EXTRA_CALL_ID).orEmpty(),
            callerId = intent.getStringExtra(EXTRA_CALLER_ID).orEmpty(),
            calleeId = intent.getStringExtra(EXTRA_CALLEE_ID).orEmpty(),
            channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME).orEmpty(),
            partnerName = intent.getStringExtra(EXTRA_PARTNER_NAME).orEmpty(),
            partnerPhone = intent.getStringExtra(EXTRA_PARTNER_PHONE).orEmpty(),
            isIncoming = true,
        )
        ActiveCallHolder.set(call)

        val notification =
            if (isAppInForeground()) buildSilentIncomingNotification(call)
            else buildRingingNotification(call)
        if (!startForegroundCompat(notification)) return

        scope.launch {
            runCatching {
                signalingService.ackRinging(call.callerId, call.callId, call.calleeId)
            }.onFailure { Log.w(TAG, "Failed to write ringing ack", it) }
        }
        observeRemoteStatus(call.calleeId, call.callId, isCaller = false)
        startRingTimeout(call)
    }

    private fun handleAccept() {
        val call = ActiveCallHolder.snapshot() ?: return
        ringTimeoutJob?.cancel()
        ringTimeoutJob = null
        ActiveCallHolder.update { it.copy(isIncoming = false, isActive = true, seconds = 0) }
        scope.launch {
            runCatching {
                signalingService.updateCallStatus(call.calleeId, call.callId, CallStatus.ACTIVE)
            }
        }
        acquireAudioFocus()
        callService.joinChannel(call.channelName, uidFromUserId(call.calleeId))
        if (!startForegroundCompat(buildOngoingNotification(ActiveCallHolder.snapshot() ?: call))) return
        startTicker()
    }

    private fun handleDecline() {
        terminate(CallStatus.DECLINED)
    }

    private fun handleEnd() {
        terminate(CallStatus.ENDED)
    }

    private fun terminate(status: CallStatus) {
        val call = ActiveCallHolder.snapshot() ?: run { stopSelfClean(); return }

        stopForegroundOnly()
        scope.launch {
            withTimeoutOrNull(STATUS_WRITE_TIMEOUT_MS) {
                runCatching {
                    signalingService.updateCallStatus(call.calleeId, call.callId, status)
                }.onFailure { Log.w(TAG, "Failed to write terminal status=$status", it) }

                runCatching {
                    signalingService.clearCall(call.calleeId, call.callId)
                }.onFailure { Log.w(TAG, "Failed to clear call node", it) }
                
                runCatching {
                    signalingService.clearRingingAck(call.callerId, call.callId)
                }
            } ?: Log.w(TAG, "Terminal status write timed out (status=$status)")
            stopSelfClean()
        }
    }

    private fun stopForegroundOnly() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun handleToggleMute() {
        val call = ActiveCallHolder.snapshot() ?: return
        val muted = !call.muted
        callService.muteLocalAudio(muted)
        ActiveCallHolder.update { it.copy(muted = muted) }
        refreshOngoingNotification()
    }

    private fun handleToggleSpeaker() {
        val call = ActiveCallHolder.snapshot() ?: return
        val speakerOn = !call.speakerOn
        callService.setSpeakerphone(speakerOn)
        ActiveCallHolder.update { it.copy(speakerOn = speakerOn) }
        refreshOngoingNotification()
    }

    private fun observeRemoteStatus(calleeId: String, callId: String, isCaller: Boolean) {
        statusObserverJob?.cancel()
        statusObserverJob = scope.launch {
            signalingService.observeCallStatus(calleeId, callId).collectLatest { status ->
                Log.d(TAG, "remote status=$status isCaller=$isCaller")
                when (status) {
                    CallStatus.ACTIVE -> {
                        if (isCaller && ActiveCallHolder.snapshot()?.isActive != true) {
                            ActiveCallHolder.update { it.copy(isActive = true) }
                            startTicker()
                            refreshOngoingNotification()
                        }
                    }
                    CallStatus.DECLINED, CallStatus.ENDED -> stopSelfClean()
                    CallStatus.RINGING, null -> Unit
                }
            }
        }
    }

    private fun startRingTimeout(call: ActiveCallHolder.ActiveCall) {
        ringTimeoutJob?.cancel()
        ringTimeoutJob = scope.launch {
            delay(RING_TIMEOUT_MS)
            if (ActiveCallHolder.snapshot()?.isActive == true) return@launch
            Log.d(TAG, "ring timeout fired — recording missed call")
            runCatching {
                missedCallRecorder.record(
                    callerId = call.callerId,
                    calleeId = call.calleeId,
                    callerName = call.partnerName,
                )
            }.onFailure { Log.w(TAG, "missed-call record failed", it) }
            runCatching {
                signalingService.updateCallStatus(call.calleeId, call.callId, CallStatus.ENDED)
            }
            runCatching {
                signalingService.clearCall(call.calleeId, call.callId)
            }
            runCatching {
                signalingService.clearRingingAck(call.callerId, call.callId)
            }
            stopSelfClean()
        }
    }

    private fun observeRingingAck(callerId: String, callId: String) {
        ringingAckJob?.cancel()
        ringingAckJob = scope.launch {
            signalingService.observeRingingAck(callerId, callId).collectLatest { ringing ->
                if (!ringing || ringingStarted) return@collectLatest
                if (ActiveCallHolder.snapshot()?.isActive == true) return@collectLatest
                ringingStarted = true
                Log.d(TAG, "callee confirmed ringing — entering Ringing state")
                requestingTimeoutJob?.cancel()
                requestingTimeoutJob = null
                ActiveCallHolder.update { it.copy(remoteRinging = true) }
                startRingback()
                startRingingTimeout()
            }
        }
    }

    private fun startRequestingTimeout() {
        requestingTimeoutJob?.cancel()
        requestingTimeoutJob = scope.launch {
            delay(REQUESTING_TIMEOUT_MS)
            if (ActiveCallHolder.snapshot()?.isActive == true) return@launch
            Log.d(TAG, "requesting timeout fired — callee never confirmed ringing, ending call")
            terminate(CallStatus.ENDED)
        }
    }

    private fun startRingingTimeout() {
        outgoingTimeoutJob?.cancel()
        outgoingTimeoutJob = scope.launch {
            delay(OUTGOING_TIMEOUT_MS)
            if (ActiveCallHolder.snapshot()?.isActive == true) return@launch
            Log.d(TAG, "ringing timeout fired — no answer, ending call")
            terminate(CallStatus.ENDED)
        }
    }

    private fun startRingback() {
        if (ringbackTone != null) return
        ringbackTone = runCatching {
            ToneGenerator(AudioManager.STREAM_VOICE_CALL, RINGBACK_VOLUME)
                .also { it.startTone(ToneGenerator.TONE_SUP_RINGTONE) }
        }.onFailure { Log.w(TAG, "ringback tone unavailable", it) }.getOrNull()
    }

    private fun stopRingback() {
        ringbackTone?.let {
            runCatching {
                it.stopTone()
                it.release()
            }
        }
        ringbackTone = null
    }

    private fun stopSelfClean() {
        stopTicker()
        stopRingback()
        statusObserverJob?.cancel()
        statusObserverJob = null
        ringTimeoutJob?.cancel()
        ringTimeoutJob = null
        requestingTimeoutJob?.cancel()
        requestingTimeoutJob = null
        outgoingTimeoutJob?.cancel()
        outgoingTimeoutJob = null
        ringingAckJob?.cancel()
        ringingAckJob = null
        ringingStarted = false
        callService.leaveChannel()
        releaseAudioFocus()
        ActiveCallHolder.clear()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun startTicker() {

        stopRingback()
        requestingTimeoutJob?.cancel()
        requestingTimeoutJob = null
        outgoingTimeoutJob?.cancel()
        outgoingTimeoutJob = null
        stopTicker()
        tickerJob = scope.launch {
            while (true) {
                delay(1000)
                ActiveCallHolder.update { it.copy(seconds = it.seconds + 1) }
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private val eventListener = object : CallEventListener {
        override fun onRemoteUserJoined(uid: Int) {
            ActiveCallHolder.update { it.copy(isActive = true, isIncoming = false) }
            startTicker()
            refreshOngoingNotification()
        }

        override fun onRemoteUserLeft(uid: Int) {

            stopSelfClean()
        }

        override fun onError(code: Int) {
            ActiveCallHolder.update { it.copy(error = "Call error: $code") }
        }

        override fun onConnectionStateChanged(state: CallConnectionState) {
            ActiveCallHolder.update { it.copy(connectionState = state) }
        }
    }

    private fun acquireAudioFocus() {
        if (hasAudioFocus) return
        val am = getSystemService<AudioManager>() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(attrs)
                .build()
            audioFocusRequest = req
            hasAudioFocus = am.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            hasAudioFocus = am.requestAudioFocus(
                null,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE,
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun releaseAudioFocus() {
        if (!hasAudioFocus) return
        val am = getSystemService<AudioManager>() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(null)
        }
        audioFocusRequest = null
        hasAudioFocus = false
    }

    private fun startForegroundCompat(notification: Notification): Boolean {
        return try {
            Log.d(TAG, "startForeground (sdk=${Build.VERSION.SDK_INT})")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID_CALL,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL,
                )
            } else {
                startForeground(NOTIFICATION_ID_CALL, notification)
            }
            true
        } catch (t: Throwable) {

            Log.e(TAG, "startForeground rejected our notification", t)
            stopSelfClean()
            false
        }
    }

    private fun refreshOngoingNotification() {
        val call = ActiveCallHolder.snapshot() ?: return
        val nm = getSystemService<android.app.NotificationManager>() ?: return
        nm.notify(NOTIFICATION_ID_CALL, buildOngoingNotification(call))
    }

    private fun buildOngoingNotification(call: ActiveCallHolder.ActiveCall): Notification {
        val openAppPi = openAppPendingIntent()
        val endPi = actionPendingIntent(ACTION_END, REQ_END)
        return NotificationCompat.Builder(this, NotificationChannels.ONGOING_CALL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentTitle(call.partnerName.ifBlank { "Voice call" })
            .setContentText(if (call.isActive) "On call" else "Calling…")
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppPi)
            .setShowWhen(true)
            .addAction(0, "End call", endPi)
            .build()
    }

    private fun buildRingingNotification(call: ActiveCallHolder.ActiveCall): Notification {

        val acceptPi = incomingCallActivityPendingIntent(accept = true)
        val declinePi = actionPendingIntent(ACTION_DECLINE, REQ_DECLINE)
        val openPi = incomingCallActivityPendingIntent(accept = false)
        val builder = NotificationCompat.Builder(this, NotificationChannels.INCOMING_CALL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentTitle(call.partnerName.ifBlank { "Incoming call" })
            .setContentText("Incoming voice call")
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(openPi, true)
            .setContentIntent(openPi)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val person = Person.Builder().setName(call.partnerName.ifBlank { "Caller" }).build()
            builder.setStyle(
                NotificationCompat.CallStyle.forIncomingCall(person, declinePi, acceptPi),
            )
        } else {
            builder.addAction(0, "Decline", declinePi)
            builder.addAction(0, "Accept", acceptPi)
        }
        return builder.build()
    }

    private fun buildSilentIncomingNotification(call: ActiveCallHolder.ActiveCall): Notification {
        return NotificationCompat.Builder(this, NotificationChannels.ONGOING_CALL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentTitle(call.partnerName.ifBlank { "Incoming call" })
            .setContentText("Incoming voice call")
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(incomingCallActivityPendingIntent(accept = false))
            .build()
    }

    private fun isAppInForeground(): Boolean =
        ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)

    private fun actionPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, CallForegroundService::class.java).setAction(action)
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun openAppPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        return PendingIntent.getActivity(
            this,
            REQ_OPEN_APP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun incomingCallActivityPendingIntent(accept: Boolean): PendingIntent {
        val intent = Intent(this, IncomingCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (accept) putExtra(MainActivity.EXTRA_ACCEPT_CALL, true)
        }
        return PendingIntent.getActivity(
            this,
            if (accept) REQ_ACCEPT else REQ_OPEN_APP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun uidFromUserId(userId: String): Int {
        if (userId.isEmpty()) return 0
        val crc = CRC32()
        crc.update(userId.toByteArray(Charsets.UTF_8))
        return (crc.value and 0x7FFFFFFFL).toInt()
    }

    override fun onDestroy() {
        stopTicker()
        stopRingback()
        releaseAudioFocus()

        callService.clearEventListener(eventListener)
        scope.coroutineContext[Job]?.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "CallForegroundService"
        const val ACTION_START_OUTGOING = "com.example.messenger.call.START_OUTGOING"
        const val ACTION_START_INCOMING = "com.example.messenger.call.START_INCOMING"
        const val ACTION_ACCEPT = "com.example.messenger.call.ACCEPT"
        const val ACTION_DECLINE = "com.example.messenger.call.DECLINE"
        const val ACTION_END = "com.example.messenger.call.END"
        const val ACTION_TOGGLE_MUTE = "com.example.messenger.call.TOGGLE_MUTE"
        const val ACTION_TOGGLE_SPEAKER = "com.example.messenger.call.TOGGLE_SPEAKER"

        const val EXTRA_CALL_ID = "extra_call_id"
        const val EXTRA_CALLER_ID = "extra_caller_id"
        const val EXTRA_CALLEE_ID = "extra_callee_id"
        const val EXTRA_CHANNEL_NAME = "extra_channel_name"
        const val EXTRA_PARTNER_NAME = "extra_partner_name"
        const val EXTRA_PARTNER_PHONE = "extra_partner_phone"

        private const val NOTIFICATION_ID_CALL = 0x3CA11
        private const val REQ_OPEN_APP = 1
        private const val REQ_ACCEPT = 2
        private const val REQ_DECLINE = 3
        private const val REQ_END = 4
        private const val STATUS_WRITE_TIMEOUT_MS = 4_000L
        private const val RING_TIMEOUT_MS = 60_000L

        private const val OUTGOING_TIMEOUT_MS = 65_000L

        private const val REQUESTING_TIMEOUT_MS = 30_000L
        
        private const val RINGBACK_VOLUME = 80

        fun outgoingIntent(
            ctx: Context,
            callId: String,
            callerId: String,
            calleeId: String,
            channelName: String,
            partnerName: String,
            partnerPhone: String,
        ): Intent = Intent(ctx, CallForegroundService::class.java)
            .setAction(ACTION_START_OUTGOING)
            .putExtra(EXTRA_CALL_ID, callId)
            .putExtra(EXTRA_CALLER_ID, callerId)
            .putExtra(EXTRA_CALLEE_ID, calleeId)
            .putExtra(EXTRA_CHANNEL_NAME, channelName)
            .putExtra(EXTRA_PARTNER_NAME, partnerName)
            .putExtra(EXTRA_PARTNER_PHONE, partnerPhone)

        fun incomingIntent(
            ctx: Context,
            callId: String,
            callerId: String,
            calleeId: String,
            channelName: String,
            partnerName: String,
            partnerPhone: String,
        ): Intent = Intent(ctx, CallForegroundService::class.java)
            .setAction(ACTION_START_INCOMING)
            .putExtra(EXTRA_CALL_ID, callId)
            .putExtra(EXTRA_CALLER_ID, callerId)
            .putExtra(EXTRA_CALLEE_ID, calleeId)
            .putExtra(EXTRA_CHANNEL_NAME, channelName)
            .putExtra(EXTRA_PARTNER_NAME, partnerName)
            .putExtra(EXTRA_PARTNER_PHONE, partnerPhone)
    }
}
