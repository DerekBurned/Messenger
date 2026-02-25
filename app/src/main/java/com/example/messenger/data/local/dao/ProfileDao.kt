package com.example.messenger.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.messenger.data.local.entity.ProfileEntity
import com.example.messenger.domain.model.Profile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE user_id = :userId")
    fun getProfileByUserId(userId: String): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE user_id = :userId")
    suspend fun getProfileByUserIdOnce(userId: String): ProfileEntity?
}