package com.example.messenger.data.remote.firebase

import android.R
import android.util.Log
import com.example.messenger.domain.model.Message
import com.example.messenger.domain.model.User // Import your new domain model
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles Firestore database operations, like managing user profiles.
 * This service now works with your 'User' domain model.
 */
@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")
    private val conversationsCollection = firestore.collection("conversations")

    /**
     * Creates a new user document in Firestore.
     * This is typically called right after registration.
     * @param user The custom User object to save.
     */
    suspend fun createUserProfile(user: User): Result<Unit> {
        return try {
            // Use the user's auth ID as the document ID
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches a user's profile from Firestore.
     * @param uid The user's auth UID.
     */
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

    /**
     * Updates specific fields in a user's profile.
     * @param uid The user's auth UID.
     * @param updates A map of fields to update (e.g., "bio" to "New bio").
     */
    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            usersCollection.document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates just the FCM token for a user.
     * @param uid The user's auth UID.
     * @param token The new FCM registration token.
     */
    suspend fun updateFcmToken(uid: String, token: String): Result<Unit> {
        return try {
            // This now correctly maps to the 'fcmToken' field in your User model
            val updates = mapOf("fcmToken" to token)
            usersCollection.document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun sendMessage(message: Message): Result<Unit> {
        return try {
            conversationsCollection
                .document(message.conversationId)
                .collection("messages")
                .add(message) // Use add() to auto-generate a document ID
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error sending message", e)
            Result.failure(e)
        }
    }


    /*suspend fun deleteMessage(messageId:String): Result<Unit>{
        return try {
            conversationsCollection
                .document(conversationId)
                .collection("messages")
                .document(messageId).delete()
            Result.success(Unit)
        }catch (e: Exception)
        {
            Log.e("FirestoreService", "Error deleting message", e)
            Result.failure(e)
        }
    }
    suspend fun markMessageAsRead(messageId: String, newStatus: String): Result<Unit>{
        conversationsCollection
            .document(conversationId)
            .collection("messages")
            .document(messageId)
            // 2. Call update() with the field name and new value
            .update("status", newStatus)
            .await()
    }*/
/**
 * Listens for real-time updates to messages in a conversation.
 * This is the core of your real-time chat.
 */
fun getMessagesStream(conversationId: String): Flow<List<Message>> = callbackFlow {
        val messagesRef = conversationsCollection
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING) // Show oldest first

        val listener = messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("FirestoreService", "Listen failed.", error)
                close(error) // Close the flow on error
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val messages = snapshot.toObjects(Message::class.java).mapIndexed { index, message ->
                    // Manually add the document ID to our model
                    message.copy(id = snapshot.documents[index].id)
                }
                trySend(messages) // Send the new list to the flow
            }
        }

        // This block is called when the flow is cancelled
        awaitClose { listener.remove() }
    }
}