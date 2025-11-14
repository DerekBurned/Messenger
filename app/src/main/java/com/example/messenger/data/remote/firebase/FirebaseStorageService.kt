package com.example.messenger.data.remote.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageService @Inject constructor(
    private val storage: FirebaseStorage
) {

    private val profileImagesRef = storage.reference.child("profileImages")

    suspend fun uploadProfileImage(uid: String, imageUri: Uri): Result<String> {
        return try {

            val fileRef = profileImagesRef.child(uid).child("${UUID.randomUUID()}.jpg")

            val uploadTask = fileRef.putFile(imageUri).await()

            val downloadUrl = fileRef.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProfileImages(uid: String): Result<Unit> {
        return try {
            
            val userFolderRef = profileImagesRef.child(uid)

            val listResult = userFolderRef.listAll().await()
            listResult.items.forEach { it.delete().await() }

            Result.success(Unit)
        } catch (e: Exception)
        {
            Result.failure(e)
        }
    }
}