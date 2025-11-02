package com.example.messenger.data.Repository

import com.example.messenger.domain.model.SyncStatus
import com.example.messenger.domain.repository.ISyncRepository
import kotlinx.coroutines.flow.Flow

class SyncRepositoryImpl: ISyncRepository {
    override suspend fun syncAllData(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun processSyncQueue(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun observeSyncStatus(): Flow<SyncStatus> {
        TODO("Not yet implemented")
    }

    override suspend fun getSyncQueueCount(): Int {
        TODO("Not yet implemented")
    }
    private fun syncMessages() {
        TODO("Not yet implemented")
    }

    private fun syncConversations() {
        TODO("Not yet implemented")

    }
    private fun syncUsers(){
        TODO("Not yet implemented")
    }
}