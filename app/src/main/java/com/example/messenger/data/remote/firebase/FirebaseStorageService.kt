package com.example.messenger.data.remote.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles Firebase Storage operations, like uploading profile pictures.
 */
@Singleton
class FirebaseStorageService @Inject constructor(
    private val storage: FirebaseStorage
) {

    private val profileImagesRef = storage.reference.child("profileImages")

    /**
     * Uploads a user's profile image and returns the download URL.
     *
     * @param uid The user's auth UID to associate the image with.
     * @param imageUri The local Uri of the image file (from gallery or camera).
     * @return A Result containing the public download URL string.
     */
    suspend fun uploadProfileImage(uid: String, imageUri: Uri): Result<String> {
        return try {
            // Create a unique file name, e.g., "uid/random_uuid.jpg"
            // This also makes it easy to find in the storage bucket.
            val fileRef = profileImagesRef.child(uid).child("${UUID.randomUUID()}.jpg")

            // Upload the file
            val uploadTask = fileRef.putFile(imageUri).await()

            // Get the download URL
            val downloadUrl = fileRef.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a user's profile image folder.
     * Note: This deletes ALL profile images for a user.
     * You might want to store a direct reference to the file path to delete a single one.
     *
     * @param uid The user's auth UID.
     */
    suspend fun deleteProfileImages(uid: String): Result<Unit> {
        return try {
            // This reference points to the user's folder (e.g., "profileImages/uid")
            val userFolderRef = profileImagesRef.child(uid)

            // List all items (images) in that folder and delete them
            val listResult = userFolderRef.listAll().await()
            listResult.items.forEach { it.delete().await() }

            Result.success(Unit)
        } catch (e: Exception)
        {
            Result.failure(e)
        }
    }
}