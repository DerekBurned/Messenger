package com.example.messenger.domain.usecase.message

import com.example.messenger.domain.repository.IMessageRepository
import com.example.messenger.util.Resource
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class SyncMessagesUseCase @Inject constructor(
    private val messageRepository: IMessageRepository
) {
    suspend operator fun invoke(conversationId: String): Flow<Resource<Unit>>  {

            val result = messageRepository.observeRemoteMessages(conversationId).collect {
                result ->
                try {


                result.fold(
                    onSuccess = {},
                    onFailure = {}
                )}catch (e: Exception){

                }
            }



        return TODO("Provide the return value")
    }
}