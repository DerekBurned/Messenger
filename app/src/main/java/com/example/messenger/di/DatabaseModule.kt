package com.example.messenger.di

import android.content.Context
import androidx.room.Room
import com.example.messenger.data.local.dao.ConversationDao
import com.example.messenger.data.local.dao.MessageDao
import com.example.messenger.data.local.dao.SyncQueueDao
import com.example.messenger.data.local.dao.UserDao
import com.example.messenger.data.local.database.MessengerDatabaseR
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(Singleton::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMessengerDatabase(
        @ApplicationContext context: Context // Hilt надасть нам 'ApplicationContext'
    ): MessengerDatabaseR {
        return Room.databaseBuilder(
            context,
            MessengerDatabaseR::class.java,
            "messenger_database.db" // Назва файлу вашої бази даних
        )
            // .addMigrations(...) // Додайте сюди міграції, коли вони знадобляться
            .build()
    }
    @Provides
    @Singleton // DAO також мають бути синглтонами
    fun provideMessageDao(db: MessengerDatabaseR): MessageDao {
        return db.messageDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: MessengerDatabaseR): UserDao {
        return db.userDao()
    }

    @Provides
    @Singleton
    fun provideConversationDao(db: MessengerDatabaseR): ConversationDao {
        return db.conversationDao()
    }

    @Provides
    @Singleton
    fun provideSyncQueueDao(db: MessengerDatabaseR): SyncQueueDao {
        return db.syncQueueDao()
    }
}