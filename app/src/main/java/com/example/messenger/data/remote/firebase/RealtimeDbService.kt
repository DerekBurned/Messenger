package com.example.messenger.data.remote.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeDbService @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val presenceRef = database.getReference("presence")
    private val typingRef = database.getReference("typing")
    private val receiptsRef = database.getReference("receipts")
    private val connectedRef = database.getReference(".info/connected")

    // ── Presence ──────────────────────────────────────────────────

    fun observeConnectionState(): Flow<Boolean> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Boolean::class.java) ?: false)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        connectedRef.addValueEventListener(listener)
        awaitClose { connectedRef.removeEventListener(listener) }
    }

    suspend fun setPresence(userId: String, state: String, lastSeen: Long) {
        val data = mapOf(
            "state" to state,
            "lastSeen" to lastSeen,
            "lastChanged" to ServerValue.TIMESTAMP
        )
        presenceRef.child(userId).setValue(data).await()
    }

    fun setupOnDisconnect(userId: String) {
        val offlineData = mapOf(
            "state" to "offline",
            "lastSeen" to ServerValue.TIMESTAMP,
            "lastChanged" to ServerValue.TIMESTAMP
        )
        presenceRef.child(userId).onDisconnect().setValue(offlineData)
    }

    fun cancelOnDisconnect(userId: String) {
        presenceRef.child(userId).onDisconnect().cancel()
    }

    fun observePresence(userId: String): Flow<Map<String, Any?>> = callbackFlow {
        val ref = presenceRef.child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as? Map<String, Any?> ?: emptyMap()
                trySend(data)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun removePresence(userId: String) {
        presenceRef.child(userId).removeValue()
    }

    // ── Typing ────────────────────────────────────────────────────

    suspend fun setTyping(conversationId: String, userId: String) {
        val data = mapOf("timestamp" to ServerValue.TIMESTAMP)
        typingRef.child(conversationId).child(userId).setValue(data).await()
    }

    suspend fun clearTyping(conversationId: String, userId: String) {
        typingRef.child(conversationId).child(userId).removeValue().await()
    }

    fun observeTyping(conversationId: String): Flow<Map<String, Any?>> = callbackFlow {
        val ref = typingRef.child(conversationId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val typingUsers = mutableMapOf<String, Any?>()
                for (child in snapshot.children) {
                    child.key?.let { typingUsers[it] = child.value }
                }
                trySend(typingUsers)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // ── Receipts ──────────────────────────────────────────────────

    suspend fun sendDeliveryReceipt(conversationId: String, userId: String, timestamp: Long) {
        receiptsRef.child(conversationId).child(userId)
            .child("lastDeliveredTimestamp").setValue(timestamp).await()
    }

    suspend fun sendReadReceipt(conversationId: String, userId: String, timestamp: Long) {
        receiptsRef.child(conversationId).child(userId)
            .child("lastReadTimestamp").setValue(timestamp).await()
    }

    fun observeReceipts(conversationId: String): Flow<Map<String, Map<String, Long>>> = callbackFlow {
        val ref = receiptsRef.child(conversationId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val receipts = mutableMapOf<String, Map<String, Long>>()
                for (child in snapshot.children) {
                    val userId = child.key ?: continue
                    val delivered = child.child("lastDeliveredTimestamp")
                        .getValue(Long::class.java) ?: 0L
                    val read = child.child("lastReadTimestamp")
                        .getValue(Long::class.java) ?: 0L
                    receipts[userId] = mapOf(
                        "lastDeliveredTimestamp" to delivered,
                        "lastReadTimestamp" to read
                    )
                }
                trySend(receipts)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
