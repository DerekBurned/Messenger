package com.example.messenger.domain.usecase.message

import com.example.messenger.domain.repository.IMessageRepository
import com.example.messenger.util.Resource 
import kotlinx.coroutines.flow.Flow 
import kotlinx.coroutines.flow.flow 
import javax.inject.Inject

class SyncMessagesUseCase @Inject constructor(
    private val messageRepository: IMessageRepository
) {
    operator fun invoke(conversationId: String): Flow<Resource<Unit>> = flow {
        try {
            
            if (conversationId.isBlank()) {
                emit(Resource.Error("Invalid conversation ID"))
                return@flow
            }

            emit(Resource.Loading)

            val result = messageRepository.observeRemoteMessages(conversationId)

            result.fold(
                onSuccess = {
                    
                    emit(Resource.Success(Unit))
                },
                onFailure = { exception ->
                    
                    emit(Resource.Error(exception.message ?: "Sync failed"))
                }
            )

        } catch (e: Exception) {
            
            emit(Resource.Error(e.message ?: "An unexpected error occurred"))
        }
    }
}