package com.example.messenger.data.sync

import androidx.compose.foundation.layout.FlowRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NetworkObserver {

    fun observeConnectivity(): Flow<Boolean>{
         flow {
        emit(true)
        }
        return TODO("Provide the return value")
    }
}