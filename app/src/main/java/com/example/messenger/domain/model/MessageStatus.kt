package com.example.messenger.domain.model

enum class MessageStatus {
    SENDING,    // Being sent
    SENT,       // Sent to server
    DELIVERED,  // Delivered to recipient
    READ,       // Read by recipient
    FAILED      // Failed to send
}