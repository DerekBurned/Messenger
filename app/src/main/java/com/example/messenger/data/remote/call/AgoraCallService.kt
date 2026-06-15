package com.example.messenger.data.remote.call

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.messenger.BuildConfig
import com.example.messenger.domain.service.CallConnectionState
import com.example.messenger.domain.service.CallEventListener
import com.example.messenger.domain.service.ICallService
import dagger.hilt.android.qualifiers.ApplicationContext
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgoraCallService @Inject constructor(
    @ApplicationContext private val contextMY: Context
) : ICallService {
    private var engine: RtcEngine? = null
    private var listener: CallEventListener? = null

    private val handler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.e(TAG, "onJoinChannelSuccess channel=$channel uid=$uid")
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.e(TAG, "onUserJoined uid=$uid")
            listener?.onRemoteUserJoined(uid)
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.e(TAG, "onUserOffline uid=$uid reason=$reason")
            if (reason == io.agora.rtc2.Constants.USER_OFFLINE_QUIT) {
                listener?.onRemoteUserLeft(uid)
            }
        }

        override fun onError(err: Int) {
            Log.e(TAG, "onError code=$err")
            listener?.onError(err)
        }

        override fun onConnectionStateChanged(state: Int, reason: Int) {
            Log.e(TAG, "onConnectionStateChanged state=$state reason=$reason")
            listener?.onConnectionStateChanged(state.toCallConnectionState())
        }
    }

    init {
        require(BuildConfig.AGORA_APP_ID.isNotBlank()) {
            "BuildConfig.AGORA_APP_ID is missing. Set AGORA_APP_ID in gradle.properties / local.properties."
        }

        try {
            val config = RtcEngineConfig().apply {
                mContext = contextMY
                mAppId = BuildConfig.AGORA_APP_ID
                mEventHandler = handler
            }
            val created = RtcEngine.create(config)
            if (created == null) {
                Log.e(TAG, "RtcEngine.create returned null — process ABIs: ${Build.SUPPORTED_ABIS.joinToString()}")
            } else {
                created.enableAudio()
            }
            engine = created
        } catch (t: Throwable) {

            Log.e(TAG, "Failed to create Agora RtcEngine", t)
        }
    }

    override fun joinChannel(channelName: String, uid: Int) {
        val engine = engine
        if (engine == null) {

            Log.e(TAG, "joinChannel called but engine is unavailable")
            listener?.onError(ERR_ENGINE_UNAVAILABLE)
            return
        }
        val options = ChannelMediaOptions().apply {
            channelProfile = io.agora.rtc2.Constants.CHANNEL_PROFILE_COMMUNICATION
            clientRoleType = io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER
            publishMicrophoneTrack = true
        }
        Log.e(TAG, "joinChannel channel=$channelName uid=$uid")
        engine.joinChannel("", channelName, uid, options)
    }

    override fun leaveChannel() {
        engine?.leaveChannel()
    }

    override fun muteLocalAudio(mute: Boolean) {
        engine?.muteLocalAudioStream(mute)
    }

    override fun setSpeakerphone(enable: Boolean) {
        engine?.setEnableSpeakerphone(enable)
    }

    override fun setEventListener(listener: CallEventListener) {
        this.listener = listener
    }

    override fun clearEventListener(listener: CallEventListener) {
        if (this.listener === listener) this.listener = null
    }

    private fun Int.toCallConnectionState(): CallConnectionState = when (this) {
        io.agora.rtc2.Constants.CONNECTION_STATE_CONNECTING -> CallConnectionState.CONNECTING
        io.agora.rtc2.Constants.CONNECTION_STATE_CONNECTED -> CallConnectionState.CONNECTED
        io.agora.rtc2.Constants.CONNECTION_STATE_RECONNECTING -> CallConnectionState.RECONNECTING
        io.agora.rtc2.Constants.CONNECTION_STATE_FAILED -> CallConnectionState.FAILED
        io.agora.rtc2.Constants.CONNECTION_STATE_DISCONNECTED -> CallConnectionState.DISCONNECTED
        else -> CallConnectionState.DISCONNECTED
    }

    private companion object {
        const val TAG = "AgoraCallService"
        
        const val ERR_ENGINE_UNAVAILABLE = -1
    }
}
