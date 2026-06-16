package com.example.messenger.data.media

import android.net.Uri
import com.example.messenger.domain.model.MediaTransfer
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TransferCancelledException : Exception()

@Singleton
class MediaTransferManager @Inject constructor(
    private val storage: FirebaseStorage,
) {
    private val _transfers = MutableStateFlow<Map<String, MediaTransfer>>(emptyMap())
    val transfers: StateFlow<Map<String, MediaTransfer>> = _transfers.asStateFlow()

    private val activeTasks = ConcurrentHashMap<String, StorageTask<*>>()

    suspend fun upload(itemId: String, file: File, storagePath: String): Result<String> {
        return try {
            val ref = storage.reference.child(storagePath)
            val task = ref.putFile(Uri.fromFile(file))
            activeTasks[itemId] = task
            update(itemId, MediaTransfer.Uploading(0f))
            task.addOnProgressListener { snapshot ->
                val total = snapshot.totalByteCount
                if (total > 0) {
                    update(itemId, MediaTransfer.Uploading(snapshot.bytesTransferred.toFloat() / total))
                }
            }
            awaitStorage(task)
            val url = ref.downloadUrl.await()
            activeTasks.remove(itemId)
            update(itemId, MediaTransfer.Completed)
            Result.success(url.toString())
        } catch (e: TransferCancelledException) {
            activeTasks.remove(itemId)
            update(itemId, MediaTransfer.Cancelled)
            Result.failure(e)
        } catch (e: Exception) {
            activeTasks.remove(itemId)
            update(itemId, MediaTransfer.Failed)
            Result.failure(e)
        }
    }

    suspend fun download(itemId: String, url: String, dest: File): Result<Unit> {
        return try {
            dest.parentFile?.mkdirs()
            val ref = storage.getReferenceFromUrl(url)
            val task = ref.getFile(dest)
            activeTasks[itemId] = task
            update(itemId, MediaTransfer.Downloading(0f))
            task.addOnProgressListener { snapshot ->
                val total = snapshot.totalByteCount
                if (total > 0) {
                    update(itemId, MediaTransfer.Downloading(snapshot.bytesTransferred.toFloat() / total))
                }
            }
            awaitStorage(task)
            activeTasks.remove(itemId)
            update(itemId, MediaTransfer.Completed)
            Result.success(Unit)
        } catch (e: TransferCancelledException) {
            activeTasks.remove(itemId)
            dest.delete()
            update(itemId, MediaTransfer.Cancelled)
            Result.failure(e)
        } catch (e: Exception) {
            activeTasks.remove(itemId)
            dest.delete()
            update(itemId, MediaTransfer.Failed)
            Result.failure(e)
        }
    }

    fun cancel(itemId: String) {
        activeTasks.remove(itemId)?.cancel()
    }

    fun clear(itemId: String) {
        _transfers.update { it - itemId }
    }

    private fun update(itemId: String, transfer: MediaTransfer) {
        _transfers.update { it + (itemId to transfer) }
    }

    private suspend fun <T : Any> awaitStorage(task: StorageTask<T>): T =
        suspendCancellableCoroutine { cont ->
            task.addOnSuccessListener { result -> cont.resume(result) }
            task.addOnFailureListener { error -> cont.resumeWithException(error) }
            task.addOnCanceledListener { cont.resumeWithException(TransferCancelledException()) }
            cont.invokeOnCancellation { runCatching { task.cancel() } }
        }
}
