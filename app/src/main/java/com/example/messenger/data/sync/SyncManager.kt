package com.example.messenger.data.sync

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.work.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncManager @Inject constructor(
    private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun schedulePeriodicSync() {
        // Define constraints: Only run if we have internet
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES // Run every 15 minutes
        )
            .setConstraints(constraints)
            .build()

        // Enqueue unique work prevents duplicate workers
        workManager.enqueueUniquePeriodicWork(
            "MessengerSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    fun triggerOneTimeSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueue(request)
    }
}