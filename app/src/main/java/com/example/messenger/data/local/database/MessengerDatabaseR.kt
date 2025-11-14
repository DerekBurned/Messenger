package com.example.messenger.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.messenger.data.local.dao.*
import com.example.messenger.data.local.entity.*
import javax.inject.Singleton


@Database(
    entities = [
        UserEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = false  // Set to false to avoid schema export warning
)
@TypeConverters(Converters::class)  // Register type converters at database level
abstract class MessengerDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun syncQueueDao(): SyncQueueDao
}