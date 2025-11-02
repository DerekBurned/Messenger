package com.example.messenger.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.messenger.data.local.dao.ConversationDao
import com.example.messenger.data.local.dao.MessageDao
import com.example.messenger.data.local.dao.SyncQueueDao
import com.example.messenger.data.local.dao.UserDao
import com.example.messenger.data.local.entity.ConversationEntity
import com.example.messenger.data.local.entity.MessageEntity
import com.example.messenger.data.local.entity.SyncQueueEntity
import com.example.messenger.data.local.entity.UserEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        SyncQueueEntity::class,
        UserEntity::class
        // (Примітка: ConversationWithMessages - це, ймовірно, @Relation, тому її тут НЕ потрібно)
    ],
    version = 1
    // exportSchema = false // (Гарна практика - додати це, щоб уникнути попередження)
)
@TypeConverters(Converters::class)
abstract class MessengerDatabaseR : RoomDatabase() {


    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun syncQueueDao(): SyncQueueDao
}