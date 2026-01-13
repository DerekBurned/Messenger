package com.example.messenger.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.messenger.domain.usecase.sync.SyncAllDataUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val parameters: WorkerParameters,
    // INJECT YOUR USE CASE HERE
    private val syncAllDataUseCase: SyncAllDataUseCase
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        // Execute the sync logic
        val result = syncAllDataUseCase()

        return if (result.isSuccess) {
            Result.success()
        } else {
            // If sync fails, tell WorkManager to try again later
            Result.retry()
        }
    }
}