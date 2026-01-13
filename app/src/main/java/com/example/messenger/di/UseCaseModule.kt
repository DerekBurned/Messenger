package com.example.messenger.di

import com.example.messenger.data.sync.NetworkObserver
import com.example.messenger.domain.repository.IAuthRepository
import com.example.messenger.domain.repository.IConversationRepository
import com.example.messenger.domain.repository.IMessageRepository
import com.example.messenger.domain.repository.ISyncRepository
import com.example.messenger.domain.repository.IUserRepository
import com.example.messenger.domain.usecase.auth.GetCurrentUserUseCase
import com.example.messenger.domain.usecase.auth.LinkPhoneUseCase
import com.example.messenger.domain.usecase.auth.LoginWithEmailUseCase
import com.example.messenger.domain.usecase.auth.LoginWithPhoneNumberUseCase
import com.example.messenger.domain.usecase.auth.LogoutUseCase
import com.example.messenger.domain.usecase.auth.ObserveAuthStateUseCase
import com.example.messenger.domain.usecase.auth.RegisterUseCase
import com.example.messenger.domain.usecase.conversation.CreateConversationUseCase
import com.example.messenger.domain.usecase.conversation.DeleteConversationUseCase
import com.example.messenger.domain.usecase.conversation.GetConversationsUseCase
import com.example.messenger.domain.usecase.conversation.SyncConversationsUseCase
import com.example.messenger.domain.usecase.message.DeleteMessageUseCase
import com.example.messenger.domain.usecase.message.GetMessagesUseCase
import com.example.messenger.domain.usecase.message.MarkMessageAsReadUseCase
import com.example.messenger.domain.usecase.message.SendMessageUseCase
import com.example.messenger.domain.usecase.message.SyncMessagesUseCase
import com.example.messenger.domain.usecase.sync.ObserveNetworkUseCase
import com.example.messenger.domain.usecase.sync.ProcessSyncQueueUseCase
import com.example.messenger.domain.usecase.sync.SyncAllDataUseCase
import com.example.messenger.domain.usecase.user.GetUserByIdUseCase
import com.example.messenger.domain.usecase.user.SearchUsersUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideGetCurrentUserUseCase(
        repository: IAuthRepository
    ): GetCurrentUserUseCase {
        return GetCurrentUserUseCase(repository)
    }
    @Provides
    fun provideLoginWithEmailUseCase(
        repository: IAuthRepository
    ): LoginWithEmailUseCase {
        return LoginWithEmailUseCase(repository)
    }
    @Provides
    fun provideLoginWithPhoneNumberUseCase(
        repository: IAuthRepository
    ): LoginWithPhoneNumberUseCase{
        return LoginWithPhoneNumberUseCase(repository)
    }
    @Provides
    fun provideLogoutUseCase(
        authRepository: IAuthRepository,
        userRepository: IUserRepository
    ): LogoutUseCase {
        return LogoutUseCase(authRepository, userRepository)
    }

    @Provides
    fun provideLinkPhoneUseCase(
        authRepository: IAuthRepository
    ): LinkPhoneUseCase {
        return LinkPhoneUseCase(authRepository)
    }

    @Provides
    fun provideObserveAuthStateUseCase(
        authRepository: IAuthRepository
    ): ObserveAuthStateUseCase {
        return ObserveAuthStateUseCase(authRepository)
    }

    @Provides
    fun provideRegisterUseCase(
        authRepository: IAuthRepository
    ): RegisterUseCase {
        return RegisterUseCase(authRepository)
    }

    // ===========================
    // Conversation Use Cases
    // ===========================

    @Provides
    fun provideGetConversationsUseCase(
        conversationRepository: IConversationRepository
    ): GetConversationsUseCase {
        return GetConversationsUseCase(conversationRepository)
    }

    @Provides
    fun provideDeleteConversationUseCase(
        conversationRepository: IConversationRepository
    ): DeleteConversationUseCase {
        return DeleteConversationUseCase(conversationRepository)
    }

    @Provides
    fun provideSyncConversationsUseCase(
        conversationRepository: IConversationRepository
    ): SyncConversationsUseCase {
        return SyncConversationsUseCase(conversationRepository)
    }

    @Provides
    fun provideCreateConversationUseCase(
        conversationRepository: IConversationRepository,
        // Note: This use case depends on another use case!
        getCurrentUserUseCase: GetCurrentUserUseCase
    ): CreateConversationUseCase {
        return CreateConversationUseCase(conversationRepository, getCurrentUserUseCase)
    }

    // ===========================
    // Message Use Cases
    // ===========================

    @Provides
    fun provideDeleteMessageUseCase(
        messageRepository: IMessageRepository
    ): DeleteMessageUseCase {
        return DeleteMessageUseCase(messageRepository)
    }

    @Provides
    fun provideGetMessagesUseCase(
        messageRepository: IMessageRepository
    ): GetMessagesUseCase {
        return GetMessagesUseCase(messageRepository)
    }
    @Provides
    fun provideSendMessageUseCase(
        messageRepository: IMessageRepository,
        getCurrentUserUseCase: GetCurrentUserUseCase
    ): SendMessageUseCase {
        return SendMessageUseCase(messageRepository, getCurrentUserUseCase)
    }

    @Provides
    fun provideMarkMessageAsReadUseCase(
        messageRepository: IMessageRepository
    ): MarkMessageAsReadUseCase {
        return MarkMessageAsReadUseCase(messageRepository)
    }

    @Provides
    fun provideSyncMessagesUseCase(
        messageRepository: IMessageRepository
    ): SyncMessagesUseCase {
        return SyncMessagesUseCase(messageRepository)
    }

    // ===========================
    // User Use Cases
    // ===========================

    @Provides
    fun provideGetUserByIdUseCase(
        userRepository: IUserRepository
    ): GetUserByIdUseCase {
        return GetUserByIdUseCase(userRepository)
    }

    @Provides
    fun provideSearchUsersUseCase(
        userRepository: IUserRepository
    ): SearchUsersUseCase {
        return SearchUsersUseCase(userRepository)
    }

    // ===========================
    // Sync Use Cases
    // ===========================

    @Provides
    fun provideProcessSyncQueueUseCase(
        syncRepository: ISyncRepository
    ): ProcessSyncQueueUseCase {
        return ProcessSyncQueueUseCase(syncRepository)
    }

    @Provides
    fun provideSyncAllDataUseCase(
        syncRepository: ISyncRepository
    ): SyncAllDataUseCase {
        return SyncAllDataUseCase(syncRepository)
    }

    @Provides
    fun provideObserveNetworkUseCase(
        networkObserver: NetworkObserver
    ): ObserveNetworkUseCase {
        return ObserveNetworkUseCase(networkObserver)
    }

}