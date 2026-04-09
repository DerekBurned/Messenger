package com.example.messenger.domain.usecase.receipt

import com.example.messenger.domain.model.ReceiptInfo
import com.example.messenger.domain.service.IReceiptService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveReceiptsUseCase @Inject constructor(
    private val receiptService: IReceiptService
) {
    operator fun invoke(conversationId: String): Flow<Map<String, ReceiptInfo>> {
        return receiptService.observeReceipts(conversationId)
    }
}
