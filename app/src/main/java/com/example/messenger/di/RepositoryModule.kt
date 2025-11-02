package com.example.messenger.di

import com.example.messenger.data.Repository.AuthRepositoryImpl
import com.example.messenger.data.Repository.ConversationRepositoryImpl
import com.example.messenger.data.Repository.MessageRepositoryImpl
import com.example.messenger.data.Repository.SyncRepositoryImpl
import com.example.messenger.data.Repository.UserRepositoryImpl
import com.example.messenger.domain.repository.IAuthRepository
import com.example.messenger.domain.repository.IConversationRepository
import com.example.messenger.domain.repository.IMessageRepository
import com.example.messenger.domain.repository.ISyncRepository
import com.example.messenger.domain.repository.IUserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import javax.inject.Singleton

@Module
@InstallIn(Singleton::class)
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
    abstract fun bindConversationRepository(
        impl: SyncRepositoryImpl
    ): ISyncRepository

    @Binds
    @Singleton
    abstract fun bindConversationRepository(
        impl: UserRepositoryImpl
    ): IUserRepository

}