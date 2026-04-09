package com.example.messenger.data.service

import com.example.messenger.data.remote.firebase.RealtimeDbService
import com.example.messenger.domain.model.ReceiptInfo
import com.example.messenger.domain.service.IReceiptService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseReceiptService @Inject constructor(
    private val realtimeDbService: RealtimeDbService,
    private val firebaseAuth: FirebaseAuth
) : IReceiptService {

    override suspend fun sendDeliveryReceipt(conversationId: String, messageTimestamp: Long) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        realtimeDbService.sendDeliveryReceipt(conversationId, userId, messageTimestamp)
    }

    override suspend fun sendReadReceipt(conversationId: String, messageTimestamp: Long) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        realtimeDbService.sendReadReceipt(conversationId, userId, messageTimestamp)
    }

    override fun observeReceipts(conversationId: String): Flow<Map<String, ReceiptInfo>> {
        return realtimeDbService.observeReceipts(conversationId).map { data ->
            data.mapValues { (userId, timestamps) ->
                ReceiptInfo(
                    userId = userId,
                    lastDeliveredTimestamp = timestamps["lastDeliveredTimestamp"] ?: 0L,
                    lastReadTimestamp = timestamps["lastReadTimestamp"] ?: 0L
                )
            }
        }
    }
}
