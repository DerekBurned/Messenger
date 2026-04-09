package com.example.messenger.domain.model

data class ReceiptInfo(
    val userId: String,
    val lastDeliveredTimestamp: Long = 0L,
    val lastReadTimestamp: Long = 0L
)
