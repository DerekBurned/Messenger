package com.example.messenger.domain.model

data class ReceiptInfo(
    val userId: String,
    val lastReadTimestamp: Long = 0L
)
