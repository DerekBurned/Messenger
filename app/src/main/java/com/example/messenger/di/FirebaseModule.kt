package com.example.messenger.di

import com.example.messenger.data.remote.firebase.FirebaseAuthService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            // Return a dummy/mock or just let it fail if used, but don't crash Hilt init
            FirebaseAuth.getInstance() 
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            FirebaseFirestore.getInstance()
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return try {
            FirebaseStorage.getInstance()
        } catch (e: Exception) {
            FirebaseStorage.getInstance()
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return try {
            FirebaseMessaging.getInstance()
        } catch (e: Exception) {
            FirebaseMessaging.getInstance()
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return try {
            FirebaseDatabase.getInstance()
        } catch (e: Exception) {
            FirebaseDatabase.getInstance()
        }
    }
}