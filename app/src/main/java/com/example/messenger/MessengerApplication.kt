package com.example.messenger

import android.app.Application
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import com.example.messenger.data.presence.AppLifecycleObserver
import com.example.messenger.data.remote.firebase.FcmTokenSyncer
import com.example.messenger.data.sync.SyncCoordinator
import com.example.messenger.data.sync.SyncManager
import com.example.messenger.presentation.notification.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MessengerApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var appLifecycleObserver: AppLifecycleObserver

    @Inject
    lateinit var syncCoordinator: SyncCoordinator

    @Inject
    lateinit var syncManager: SyncManager

    @Inject
    lateinit var fcmTokenSyncer: FcmTokenSyncer

    override fun onCreate() {
        super.onCreate()

        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("APP_CRASH", "Uncaught exception on thread '${thread.name}'", throwable)
            previous?.uncaughtException(thread, throwable)
        }

        runCatching { deleteDatabase("messenger_database.db") }
        NotificationChannels.ensureCreated(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        syncCoordinator.start()
        syncManager.schedulePeriodicSync()
        fcmTokenSyncer.start()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
