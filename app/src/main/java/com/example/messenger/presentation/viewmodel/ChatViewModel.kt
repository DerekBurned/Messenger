package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.messenger.data.Repository.MessageRepositoryImpl
import com.example.messenger.domain.usecase.message.GetMessagesUseCase
import com.example.messenger.domain.usecase.message.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
private val sendMessageUseCase: SendMessageUseCase,
    private val getMessagesUseCase: GetMessagesUseCase
): ViewModel() {
}