package com.example.messenger.domain.service

interface ICallService {
    fun joinChannel(channelName: String, uid: Int)
    fun leaveChannel()
    fun muteLocalAudio(mute: Boolean)
    fun setSpeakerphone(enable: Boolean)
    fun setEventListener(listener: CallEventListener)
}

interface CallEventListener {
    fun onRemoteUserJoined(uid: Int)
    fun onRemoteUserLeft(uid: Int)
    fun onError(code: Int)
}
