package com.example.messenger.domain.usecase.message

import com.example.messenger.domain.repository.IMessageRepository
import javax.inject.Inject

class SyncMessagesUseCase @Inject constructor(
    private val messageRepository: IMessageRepository
) {
    suspend operator fun invoke(conversationId: String) {
        messageRepository.observeRemoteMessages(conversationId)
    }
}