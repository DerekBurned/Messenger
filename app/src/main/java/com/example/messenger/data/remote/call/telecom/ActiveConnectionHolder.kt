package com.example.messenger.data.remote.call.telecom

import java.util.concurrent.ConcurrentHashMap

object ActiveConnectionHolder {

    private val connections = ConcurrentHashMap<String, MessengerConnection>()

    fun put(callId: String, connection: MessengerConnection) {
        if (callId.isBlank()) return
        connections[callId] = connection
    }

    fun get(callId: String): MessengerConnection? = connections[callId]

    fun remove(callId: String) {
        connections.remove(callId)
    }
}
