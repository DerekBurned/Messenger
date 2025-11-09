package com.example.messenger.domain.usecase.message

import com.example.messenger.domain.repository.IMessageRepository
import javax.inject.Inject

class MarkMessageAsReadUseCase @Inject constructor(
    private val messageRepository: IMessageRepository
) {
    suspend operator fun invoke(messageId: String): Result<Unit> {
        return messageRepository.markMessageAsRead(messageId)
    }
}