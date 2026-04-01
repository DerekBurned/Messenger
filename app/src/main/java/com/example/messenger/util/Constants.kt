package com.example.messenger.util

object Constants {
    // Firestore collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_CONVERSATIONS = "conversations"
    const val COLLECTION_MESSAGES = "messages"

    // Room database
    const val DATABASE_NAME = "messenger_database.db"

    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "chat_messages_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Chat Messages"

    // WorkManager
    const val SYNC_WORK_NAME = "messenger_sync_work"
    const val SYNC_WORK_TAG = "sync"
}
