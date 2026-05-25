package com.example.messenger.data.remote.call

import com.example.messenger.data.remote.dto.CallSignalDto
import com.example.messenger.data.remote.dto.toCallSignal
import com.example.messenger.data.remote.dto.toDto
import com.example.messenger.domain.model.CallSignal
import com.example.messenger.domain.model.CallStatus
import com.example.messenger.domain.service.ICallSignalingService
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseCallSignalingService @Inject constructor(
    private val db: FirebaseDatabase
) : ICallSignalingService {

    private val callsRef get() = db.reference.child("calls")

    override suspend fun sendCallSignal(signal: CallSignal) {
        callsRef.child(signal.callId).setValue(signal.toDto()).await()
    }

    override fun observeIncomingCall(myUserId: String): Flow<CallSignal?> = callbackFlow {
        val query = callsRef.orderByChild("calleeId").equalTo(myUserId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val signal = snapshot.children
                    .mapNotNull { it.getValue(CallSignalDto::class.java)?.toCallSignal() }
                    .firstOrNull { it.status == CallStatus.RINGING }
                trySend(signal)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    override suspend fun updateCallStatus(callId: String, status: CallStatus) {
        callsRef.child(callId).child("status").setValue(status.name).await()
    }
}
