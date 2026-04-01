package com.example.messenger.data.repository

import com.example.messenger.data.local.dao.SyncQueueDao
import com.example.messenger.domain.model.SyncStatus
import com.example.messenger.domain.repository.ISyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class SyncRepositoryImpl @Inject constructor(
    private val syncQueueDao: SyncQueueDao
) : ISyncRepository {

    private val _syncStatus = MutableStateFlow(SyncStatus.SYNCED)

    override suspend fun syncAllData(): Result<Unit> {
        return try {
            _syncStatus.value = SyncStatus.SYNCING
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
            val pendingItems = syncQueueDao.getAllPending()
            for (item in pendingItems) {
                // Process each item based on entityType and action
                syncQueueDao.delete(item)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeSyncStatus(): Flow<SyncStatus> = _syncStatus

    override suspend fun getSyncQueueCount(): Int {
        return syncQueueDao.getCount()
    }
}
