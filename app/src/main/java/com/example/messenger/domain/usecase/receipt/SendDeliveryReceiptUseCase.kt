package com.example.messenger.domain.usecase.receipt

import com.example.messenger.domain.service.IReceiptService
import javax.inject.Inject

class SendDeliveryReceiptUseCase @Inject constructor(
    private val receiptService: IReceiptService
) {
    suspend operator fun invoke(conversationId: String, messageTimestamp: Long) {
        receiptService.sendDeliveryReceipt(conversationId, messageTimestamp)
    }
}
