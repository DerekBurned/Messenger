package com.example.messenger.data.repository

import com.example.messenger.data.local.obx.ObxConversation
import com.example.messenger.data.local.obx.ObxConversation_
import com.example.messenger.data.local.obx.ObxMessage
import com.example.messenger.data.local.obx.ObxMessage_
import com.example.messenger.data.local.obx.ObxSyncQueueItem
import com.example.messenger.data.local.obx.asFlow
import com.example.messenger.data.local.obx.toDomain
import com.example.messenger.data.local.obx.toObx
import com.example.messenger.data.remote.firebase.FirestoreService
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.model.sync.SyncAction
import com.example.messenger.domain.repository.IMessageRepository
import io.objectbox.Box
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageBox: Box<ObxMessage>,
    private val conversationBox: Box<ObxConversation>,
    private val syncQueueBox: Box<ObxSyncQueueItem>,
    private val firestoreService: FirestoreService,
) : IMessageRepository {

    override fun getMessagesStream(conversationId: String): Flow<List<Message>> {
        return messageBox.query(
            ObxMessage_.conversationId.equal(conversationId)
                .and(ObxMessage_.deleted.equal(false)),
        )
            .order(ObxMessage_.timestamp)
            .build()
            .asFlow()
            .map { rows -> rows.map { it.toDomain() } }
    }

    private fun cutoffFor(conversationId: String): Long =
        conversationBox.query(ObxConversation_.uid.equal(conversationId))
            .build()
            .use { it.findFirst() }
            ?.clearedAtForMe ?: 0L

    override suspend fun sendMessage(message: Message): Result<Unit> {
        return try {
            
            messageBox.put(message.toObx())
            updateConversationPreview(message)

            val result = firestoreService.sendMessage(message.copy(status = MessageStatus.SENT))
            if (result.isSuccess) {
                updateMessage(message.id) { it.status = MessageStatus.SENT.name }
            } else {
                updateMessage(message.id) { it.status = MessageStatus.FAILED.name }
                enqueueRetry(message.id)
            }
            result
        } catch (e: Exception) {
            runCatching { updateMessage(message.id) { it.status = MessageStatus.FAILED.name } }
            runCatching { enqueueRetry(message.id) }
            Result.failure(e)
        }
    }

    private fun enqueueRetry(messageId: String) {
        syncQueueBox.put(
            ObxSyncQueueItem(
                entityType = com.example.messenger.domain.model.sync.SyncEntityType.MESSAGE,
                entityId = messageId,
                action = SyncAction.SEND,
                timestamp = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun deleteMessage(message: Message): Result<Unit> {
        return try {
            val result = firestoreService.deleteMessage(message)
            if (result.isSuccess) {
                
                updateMessage(message.id) { it.deleted = true }
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markMessageAsRead(message: Message): Result<Unit> {
        return try {
            val result = firestoreService.markMessageAsRead(message)
            if (result.isSuccess) {
                updateMessage(message.id) {
                    it.isRead = true
                    it.status = MessageStatus.READ.name
                }
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun observeRecentMessages(conversationId: String, limit: Long): Flow<Result<Unit>> = flow {
        try {
            firestoreService.getRecentMessagesStream(conversationId, limit, cutoffFor(conversationId)).collect { messages ->
                messageBox.put(messages.map { it.toObx() })
                emit(Result.success(Unit))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun loadOlderMessages(conversationId: String, limit: Long): Result<Int> {
        val oldestId = messageBox.query(ObxMessage_.conversationId.equal(conversationId))
            .order(ObxMessage_.timestamp)
            .build()
            .use { it.findFirst()?.uid }
            ?: return Result.success(0)
        return firestoreService.fetchOlderMessages(conversationId, oldestId, limit, cutoffFor(conversationId)).map { older ->
            if (older.isNotEmpty()) {
                messageBox.put(older.map { it.toObx() })
            }
            older.size
        }
    }

    private fun updateMessage(id: String, mutate: (ObxMessage) -> Unit) {
        val existing = messageBox.query(ObxMessage_.uid.equal(id)).build()
            .use { it.findFirst() } ?: return
        mutate(existing)
        messageBox.put(existing)
    }

    private fun updateConversationPreview(message: Message) {
        val conv = conversationBox.query(ObxConversation_.uid.equal(message.conversationId)).build()
            .use { it.findFirst() } ?: return
        conv.lastMessage = message.text
        conv.lastMessageTimestamp = message.timestamp
        conv.latestMessageText = message.text
        conv.latestMessageTimestamp = message.timestamp
        conversationBox.put(conv)
    }
}
