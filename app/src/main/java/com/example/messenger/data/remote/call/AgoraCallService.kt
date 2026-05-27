package com.example.messenger.data.remote.call

import android.content.Context
import com.example.messenger.BuildConfig
import com.example.messenger.domain.service.CallEventListener
import com.example.messenger.domain.service.ICallService
import dagger.hilt.android.qualifiers.ApplicationContext
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import javax.inject.Inject

class AgoraCallService @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ICallService {

    private var engine: RtcEngine? = null
    private var listener: CallEventListener? = null

    private val handler = object : IRtcEngineEventHandler() {
        fun onUserJoined(connection: RtcConnection?, remoteUid: Int, elapsed: Int) {
            listener?.onRemoteUserJoined(remoteUid)
        }

        fun onUserOffline(connection: RtcConnection?, remoteUid: Int, reason: Int) {
            listener?.onRemoteUserLeft(remoteUid)
        }

        override fun onError(err: Int) {
            listener?.onError(err)
        }
    }

    init {
        require(BuildConfig.AGORA_APP_ID.isNotBlank()) {
            "BuildConfig.AGORA_APP_ID is missing. Set AGORA_APP_ID in gradle.properties / local.properties."
        }

        try {
            val config = RtcEngineConfig().apply {
                mAppId = BuildConfig.AGORA_APP_ID
                mContext = context
                mEventHandler = handler
            }
            engine = RtcEngine.create(config)
            engine?.enableAudio()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun joinChannel(channelName: String, uid: Int) {
        val options = ChannelMediaOptions().apply {
            channelProfile = io.agora.rtc2.Constants.CHANNEL_PROFILE_COMMUNICATION
            clientRoleType = io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER
        }
        engine?.joinChannel("", channelName, uid, options)
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
}