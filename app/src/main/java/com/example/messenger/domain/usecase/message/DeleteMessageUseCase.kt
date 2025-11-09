package com.example.messenger.domain.usecase.message

import com.example.messenger.domain.repository.IMessageRepository
import javax.inject.Inject

class DeleteMessageUseCase @Inject constructor(
    private val messageRepository: IMessageRepository
) {
    suspend operator fun invoke(messageId: String): Result<Unit> {
        if (messageId.isBlank()) {
            return Result.failure(Exception("Invalid message ID"))
        }
        return messageRepository.deleteMessage(messageId)
    }
}