package com.example.messenger.data.repository

import android.util.Log
import com.example.messenger.data.local.dao.MessageDao
import com.example.messenger.data.local.dao.SyncQueueDao
import com.example.messenger.data.local.entity.SyncQueueEntity
import com.example.messenger.data.mapper.toDomain
import com.example.messenger.data.remote.firebase.FirestoreService
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.model.SyncStatus
import com.example.messenger.domain.model.sync.SyncAction
import com.example.messenger.domain.model.sync.SyncEntityType
import com.example.messenger.domain.repository.IConversationRepository
import com.example.messenger.domain.repository.ISyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class SyncRepositoryImpl @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val messageDao: MessageDao,
    private val firestoreService: FirestoreService,
    private val conversationRepository: IConversationRepository,
) : ISyncRepository {

    private companion object {
        const val TAG = "SyncRepository"
        const val MAX_RETRIES = 5
    }

    private val _syncStatus = MutableStateFlow(SyncStatus.SYNCED)

    override suspend fun syncAllData(): Result<Unit> {
        return try {
            _syncStatus.value = SyncStatus.SYNCING

            runCatching { conversationRepository.syncConversations() }
                .onFailure { Log.w(TAG, "Conversation backfill failed", it) }
            processSyncQueue()
            _syncStatus.value = SyncStatus.SYNCED
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.FAILED
            Result.failure(e)
        }
    }

    override suspend fun processSyncQueue(): Result<Unit> {
        return try {
            val pending = syncQueueDao.getAllPending()
            for (item in pending) {
                when (item.entityType) {
                    SyncEntityType.MESSAGE -> handleMessageItem(item)
                    else -> {
                        Log.w(TAG, "Dropping unknown sync entity type: ${item.entityType}")
                        syncQueueDao.delete(item)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun handleMessageItem(item: SyncQueueEntity) {
        when (item.action) {
            SyncAction.SEND -> retrySend(item)
            else -> {
                Log.w(TAG, "Dropping message item with unknown action: ${item.action}")
                syncQueueDao.delete(item)
            }
        }
    }

    private suspend fun retrySend(item: SyncQueueEntity) {
        val entity = messageDao.getMessageById(item.entityId)
        if (entity == null) {
            
            syncQueueDao.delete(item)
            return
        }
        val message = entity.toDomain().copy(status = MessageStatus.SENT)
        val result = firestoreService.sendMessage(message)
        if (result.isSuccess) {
            messageDao.updateMessageStatus(item.entityId, MessageStatus.SENT)
            syncQueueDao.delete(item)
        } else {
            if (item.retryCount + 1 >= MAX_RETRIES) {
                Log.w(TAG, "Dropping message ${item.entityId} after $MAX_RETRIES retries")
                syncQueueDao.delete(item)
            } else {
                syncQueueDao.incrementRetryCount(item.id)
            }
        }
    }

    override fun observeSyncStatus(): Flow<SyncStatus> = _syncStatus

    override suspend fun getSyncQueueCount(): Int {
        return syncQueueDao.getCount()
    }
}
