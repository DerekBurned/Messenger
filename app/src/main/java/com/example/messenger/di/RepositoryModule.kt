package com.example.messenger.di

import com.example.messenger.data.repository.AuthRepositoryImpl
import com.example.messenger.data.repository.ConversationRepositoryImpl
import com.example.messenger.data.repository.MessageRepositoryImpl
import com.example.messenger.data.repository.SyncRepositoryImpl
import com.example.messenger.data.repository.UserRepositoryImpl
import com.example.messenger.domain.repository.IAuthRepository
import com.example.messenger.domain.repository.IConversationRepository
import com.example.messenger.domain.repository.IMessageRepository
import com.example.messenger.domain.repository.ISyncRepository
import com.example.messenger.domain.repository.IUserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl : AuthRepositoryImpl
    ): IAuthRepository

    @Binds
    @Singleton
    abstract fun bindConversationRepository(
        impl: ConversationRepositoryImpl
    ): IConversationRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        impl: MessageRepositoryImpl
    ): IMessageRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        impl: SyncRepositoryImpl
    ): ISyncRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): IUserRepository

}