package com.example.messenger.domain.service

interface ICallService {
    fun joinChannel(channelName: String, uid: Int)
    fun leaveChannel()
    fun muteLocalAudio(mute: Boolean)
    fun setSpeakerphone(enable: Boolean)
    fun setEventListener(listener: CallEventListener)

    fun clearEventListener(listener: CallEventListener)
}

enum class CallConnectionState { CONNECTING, CONNECTED, RECONNECTING, FAILED, DISCONNECTED }

interface CallEventListener {
    fun onRemoteUserJoined(uid: Int)
    fun onRemoteUserLeft(uid: Int)
    fun onRemoteConnectionLost(uid: Int) {}
    fun onRemoteConnectionRestored(uid: Int) {}
    fun onError(code: Int)

    fun onConnectionStateChanged(state: CallConnectionState) {}
}
