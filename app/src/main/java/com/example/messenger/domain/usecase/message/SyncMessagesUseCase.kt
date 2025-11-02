package com.example.messenger.domain.usecase.message

import com.example.messenger.domain.repository.IMessageRepository
import com.example.messenger.util.Resource // 1. Імпортуємо Resource
import kotlinx.coroutines.flow.Flow // 2. Імпортуємо Flow
import kotlinx.coroutines.flow.flow // 3. Імпортуємо flow-білдер
import javax.inject.Inject

class SyncMessagesUseCase @Inject constructor(
    private val messageRepository: IMessageRepository
) {
    operator fun invoke(conversationId: String): Flow<Resource<Unit>> = flow {
        try {
            // 5. Валідація
            if (conversationId.isBlank()) {
                emit(Resource.Error("Invalid conversation ID"))
                return@flow
            }

            // 6. Повідомляємо UI про початок
            emit(Resource.Loading)


            val result = messageRepository.observeRemoteMessages(conversationId)

            // 8. Конвертуємо Result -> Resource
            result.fold(
                onSuccess = {
                    // Синхронізація пройшла успішно
                    emit(Resource.Success(Unit))
                },
                onFailure = { exception ->
                    // Помилка під час синхронізації
                    emit(Resource.Error(exception.message ?: "Sync failed"))
                }
            )

        } catch (e: Exception) {
            // 9. Ловимо будь-які несподівані помилки
            emit(Resource.Error(e.message ?: "An unexpected error occurred"))
        }
    }
}