package com.example.messenger.domain.usecase.message

import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.repository.IMessageRepository
import com.example.messenger.domain.usecase.auth.GetCurrentUserUseCase
import java.util.UUID
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val messageRepository: IMessageRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {
    suspend operator fun invoke(conversationId: String, text: String): Result<Unit> {
        // Validation
        if (text.isBlank()) {
            return Result.failure(Exception("Message cannot be empty"))
        }

        if (text.length > 5000) {
            return Result.failure(Exception("Message is too long"))
        }

        // Get current user
        val currentUser = getCurrentUserUseCase()
            ?: return Result.failure(Exception("User not logged in"))

        // Create message
        val message = Message(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = currentUser.id,
            text = text.trim(),
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENDING
        )

        return messageRepository.sendMessage(message)
    }
}