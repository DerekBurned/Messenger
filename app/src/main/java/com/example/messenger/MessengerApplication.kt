package com.example.messenger

import android.app.Application
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory // <-- 1. Import the correct Hilt factory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MessengerApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}