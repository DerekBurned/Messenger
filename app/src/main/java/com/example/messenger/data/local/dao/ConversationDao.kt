package com.example.messenger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.messenger.data.local.entity.ConversationEntity
import com.example.messenger.data.local.model.ConversationSummary
import com.example.messenger.data.local.model.ConversationWithMessages
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Transaction
    @Query("SELECT * FROM conversations_table WHERE id = :conversationId")
    fun getConversationWithMessages(conversationId: String): Flow<ConversationWithMessages>

    @Transaction
    @Query("SELECT * FROM conversations_table ORDER BY lastMessageTimestamp DESC")
    fun getAllConversations(): Flow<List<ConversationSummary>>

    @Query("SELECT * FROM conversations_table WHERE id = :conversationId")
    suspend fun getConversationById(conversationId: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations_table WHERE id = :conversationId")
    suspend fun deleteConversationById(conversationId: String)
}
