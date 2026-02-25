package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.messenger.data.repository.ConversationRepositoryImpl
import com.example.messenger.domain.usecase.conversation.CreateConversationUseCase
import com.example.messenger.domain.usecase.conversation.DeleteConversationUseCase
import com.example.messenger.domain.usecase.conversation.GetConversationsUseCase
import com.example.messenger.domain.usecase.conversation.ObserveConversationsUseCase
import com.example.messenger.domain.usecase.conversation.SyncConversationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val deleteConversationUseCase: DeleteConversationUseCase,
    private val createConversationUseCase: CreateConversationUseCase,
    private val syncConversationsUseCase: SyncConversationsUseCase,
    private val observeConversationsUseCase: ObserveConversationsUseCase
    ): ViewModel() {

}