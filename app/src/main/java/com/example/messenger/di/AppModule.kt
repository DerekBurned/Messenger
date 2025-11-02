package com.example.messenger.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(Singleton::class)
object AppModule {
    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }
    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    // @Provides
    // @Singleton
    // fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
    //     return context.getSharedPreferences("messenger_prefs", Context.MODE_PRIVATE)
    // }
}