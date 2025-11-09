package com.example.messenger.domain.usecase.sync

import com.example.messenger.data.sync.NetworkObserver
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNetworkUseCase @Inject constructor(
    private val networkObserver: NetworkObserver
) {
    operator fun invoke(): Flow<Boolean> {
        return networkObserver.observeConnectivity()
    }
}