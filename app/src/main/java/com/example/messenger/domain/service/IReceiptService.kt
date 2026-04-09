package com.example.messenger.domain.service

import com.example.messenger.domain.model.ReceiptInfo
import kotlinx.coroutines.flow.Flow

interface IReceiptService {
    suspend fun sendDeliveryReceipt(conversationId: String, messageTimestamp: Long)
    suspend fun sendReadReceipt(conversationId: String, messageTimestamp: Long)
    fun observeReceipts(conversationId: String): Flow<Map<String, ReceiptInfo>>
}
