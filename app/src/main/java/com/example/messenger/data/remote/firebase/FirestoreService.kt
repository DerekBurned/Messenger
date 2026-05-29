package com.example.messenger.data.remote.firebase

import android.util.Log
import com.example.messenger.domain.model.Conversation
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.model.User 
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")
    private val conversationsCollection = firestore.collection("conversations")

    suspend fun createUserProfile(user: User): Result<Unit> {
        return try {
            
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            val user = snapshot.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            usersCollection.document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(uid: String, token: String): Result<Unit> {
        return try {
            
            val updates = mapOf("fcmToken" to token)
            usersCollection.document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun sendMessage(message: Message): Result<Unit> {
        return try {

            val remoteMessage = if (message.status == MessageStatus.SENDING) {
                message.copy(status = MessageStatus.SENT)
            } else {
                message
            }
            val conversationRef = conversationsCollection.document(message.conversationId)
            conversationRef
                .collection("messages")
                .document(remoteMessage.id)
                .set(remoteMessage)
                .await()
            conversationRef
                .update(
                    mapOf(
                        "lastMessage" to message.text,
                        "lastMessageTimestamp" to message.timestamp,
                    ),
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error sending message", e)
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(message: Message): Result<Unit>{
        return try {
            conversationsCollection
                .document(message.conversationId)
                .collection("messages")
                .document(message.id).delete()
            Result.success(Unit)
        }catch (e: Exception)
        {
            Log.e("FirestoreService", "Error deleting message", e)
            Result.failure(e)
        }
    }
    suspend fun markMessageAsRead(message: Message): Result<Unit>{
        return try{
            conversationsCollection
                .document(message.conversationId)
                .collection("messages")
                .document(message.id)
                .update("status", MessageStatus.READ.name)
                .await()
            Log.d("FirestoreService", "Message marked as read")
            Result.success(Unit)
        }catch (e: Exception){
            Log.e("FirestoreService", "Error marking message as read", e)
            Result.failure(e)
        }
    }

    fun getMessagesStream(conversationId: String): Flow<List<Message>> = callbackFlow {
        val messagesRef = conversationsCollection
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING) 

        val listener = messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("FirestoreService", "Listen failed.", error)
                close(error) 
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val messages = snapshot.toObjects(Message::class.java).mapIndexed { index, message ->
                    message.copy(id = snapshot.documents[index].id)
                }
                trySend(messages) 
            }
        }

        awaitClose { listener.remove() }
    }
    suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val snapshot = usersCollection
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .get()
                .await()
            val users = snapshot.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error searching users", e)
            Result.failure(e)
        }
    }

    suspend fun findExistingConversation(participantIds: List<String>): Result<Conversation?> {
        return try {
            val snapshot = conversationsCollection
                .whereArrayContains("participantIds", participantIds[0])
                .get()
                .await()
            val conversations = snapshot.toObjects(Conversation::class.java).mapIndexed { index, conversation ->
                conversation.copy(id = snapshot.documents[index].id)
            }
            val existing = conversations.find { conv ->
                conv.participantIds.size == 2 && conv.participantIds.containsAll(participantIds)
            }
            Result.success(existing)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error finding existing conversation", e)
            Result.failure(e)
        }
    }

    suspend fun createConversation(conversation: Conversation): Result<String> {
        return try {
            val docRef = conversationsCollection.add(conversation).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error creating conversation", e)
            Result.failure(e)
        }
    }

    suspend fun fetchAllConversationsOnce(userId: String): Result<List<Conversation>> {
        return try {
            val snapshot = conversationsCollection
                .whereArrayContains("participantIds", userId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val conversations = snapshot.toObjects(Conversation::class.java)
                .mapIndexed { index, conversation ->
                    conversation.copy(id = snapshot.documents[index].id)
                }
            Result.success(conversations)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error fetching conversations once", e)
            Result.failure(e)
        }
    }

    fun getAllConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        val conversationsRef = conversationsCollection
            .whereArrayContains("participantIds", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

        val listener = conversationsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("FirestoreService", "Listen failed.", error)
                close(error) 
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val  conversations =
                    snapshot.toObjects(Conversation::class.java).mapIndexed { index, conversation ->
                        
                        conversation.copy(id = snapshot.documents[index].id)
                    }
                trySend(conversations) 
            }
        }
        awaitClose { listener.remove() }
    }

}