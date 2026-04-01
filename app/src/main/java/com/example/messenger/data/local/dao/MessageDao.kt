package com.example.messenger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.messenger.data.local.entity.MessageEntity
import com.example.messenger.data.local.model.MessageWithSender
import com.example.messenger.domain.model.MessageStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Transaction
    @Query("SELECT * FROM messages_table WHERE conversation_id = :conversationId ORDER BY timestamp DESC")
    fun getMessagesWithSendersDesc(conversationId: String): Flow<List<MessageWithSender>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("UPDATE messages_table SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)

    @Query("UPDATE messages_table SET isRead = 1 WHERE id = :messageId")
    suspend fun markAsRead(messageId: String)
}
