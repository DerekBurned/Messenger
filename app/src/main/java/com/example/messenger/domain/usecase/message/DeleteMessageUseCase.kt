package com.example.messenger.domain.usecase.message

import com.example.messenger.domain.repository.IMessageRepository
import com.example.messenger.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DeleteMessageUseCase @Inject constructor(
    private val messageRepository: IMessageRepository
) {
    suspend operator fun invoke(messageId: String): Flow<Resource<Unit>> = flow  {
       try {
           emit(Resource.Loading)
            val result = messageRepository.deleteMessage(messageId)
            if (messageId.isBlank()) {
                Resource.Error("Invalid message ID")
            }
            result.fold(
                onSuccess = {
                    emit(Resource.Success(Unit))
                },
                onFailure = { e ->
                    emit(Resource.Error(e.message ?: "Failed to delete the message"))
                }
            )
        }catch (
            e: Exception
        ){
            emit(Resource.Error("Internal error occurred"))
        }
    }
}