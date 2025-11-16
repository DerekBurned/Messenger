package com.example.messenger.di

import android.content.Context
import androidx.room.Room
import com.example.messenger.data.local.dao.ConversationDao
import com.example.messenger.data.local.dao.MessageDao
import com.example.messenger.data.local.dao.SyncQueueDao
import com.example.messenger.data.local.dao.UserDao
import com.example.messenger.data.local.database.MessengerDatabase  // CHANGED: removed 'R'
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMessengerDatabase(
        @ApplicationContext context: Context
    ): MessengerDatabase {  // CHANGED: removed 'R'
        return Room.databaseBuilder(
            context,
            MessengerDatabase::class.java,  // CHANGED: removed 'R'
            "messenger_database.db"
        )
            .build()
    }

    @Provides
    @Singleton
    fun provideMessageDao(db: MessengerDatabase): MessageDao {  // CHANGED: removed 'R'
        return db.messageDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: MessengerDatabase): UserDao {  // CHANGED: removed 'R'
        return db.userDao()
    }

    @Provides
    @Singleton
    fun provideConversationDao(db: MessengerDatabase): ConversationDao {  // CHANGED: removed 'R'
        return db.conversationDao()
    }

    @Provides
    @Singleton
    fun provideSyncQueueDao(db: MessengerDatabase): SyncQueueDao {  // CHANGED: removed 'R'
        return db.syncQueueDao()
    }
}
