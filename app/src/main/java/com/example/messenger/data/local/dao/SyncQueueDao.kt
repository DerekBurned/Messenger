package com.example.messenger.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.messenger.data.local.entity.SyncQueueEntity

@Dao
interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue_table ORDER BY timestamp ASC")
    suspend fun getAllPending(): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue_table")
    suspend fun getCount(): Int

    @Delete
    suspend fun delete(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue_table")
    suspend fun deleteAll()

    @Query("UPDATE sync_queue_table SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetryCount(id: Long)
}
