package com.example.messenger.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.messenger.data.local.model.ConversationWithMessages
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Transaction
    @Query("SELECT * FROM conversations_table WHERE id = :conversationId")
    fun getConversationWithMessages(conversationId : String): Flow<ConversationWithMessages>
}