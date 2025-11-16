package com.example.messenger.domain.usecase.message

import com.example.messenger.domain.model.Message
import com.example.messenger.domain.repository.IMessageRepository
import com.example.messenger.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MarkMessageAsReadUseCase @Inject constructor(
    private val messageRepository: IMessageRepository
) {
    suspend operator fun invoke(message: Message): Flow<Resource<Unit>> = flow {
       try {
           emit(Resource.Loading)
           val result = messageRepository.markMessageAsRead(message)
           result.fold(
               onSuccess = {
                   emit(Resource.Success(Unit))
               },
               onFailure = { e ->
                   emit(Resource.Error(e.message?: "Failed to mark as read"))

               }
           )

       }catch (e: Exception){
           emit(Resource.Error(e.message?: "An internal error occurred"))
       }
    }
}