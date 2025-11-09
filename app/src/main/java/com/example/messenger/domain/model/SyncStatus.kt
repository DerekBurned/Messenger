package com.example.messenger.domain.model

enum class SyncStatus {
    SYNCED,      // Successfully synced with Firebase
    PENDING,     // Waiting to be synced
    SYNCING,     // Currently syncing
    FAILED       // Sync failed
}