package com.example.messenger.data.local.obx

import io.objectbox.query.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun <T> Query<T>.asFlow(): Flow<List<T>> = callbackFlow {
    val subscription = subscribe().observer { data -> trySend(data) }
    awaitClose {
        subscription.cancel()
        close()
    }
}
