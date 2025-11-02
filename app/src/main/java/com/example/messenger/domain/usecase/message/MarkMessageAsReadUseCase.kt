package com.example.messenger.domain.usecase.message

import com.example.messenger.domain.repository.IMessageRepository
import com.example.messenger.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MarkMessageAsReadUseCase @Inject constructor(
    private val messageRepository: IMessageRepository
) {
    suspend operator fun invoke(messageId: String): Flow<Resource<Unit>> = flow {
       try {
           emit(Resource.Loading)
           if(messageId.isBlank()){
               emit(Resource.Error("Invalid Message ID"))
               return@flow
           }
           val result = messageRepository.markMessageAsRead(messageId)
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