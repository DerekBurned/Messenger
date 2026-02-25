package com.example.messenger.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.messenger.data.local.model.MessageWithSender
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Transaction
    @Query("SELECT * FROM messages_table WHERE conversation_id = :conversationId ORDER BY timestamp DESC")
    fun getMessagesWithSendersDesc(conversationId: String): Flow<List<MessageWithSender>>
}