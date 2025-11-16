package com.example.messenger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.messenger.data.local.database.Converters
import com.example.messenger.domain.model.PhoneNumber

@Entity(tableName = "users_table")
@TypeConverters(Converters::class)
data class UserEntity(
    @PrimaryKey
    val id: String,  // Changed from Integer to String
    val username: String,
    val email: String? = null,
    val phoneNumber: PhoneNumber? = null,
    val avatarUrl: String? = null,
    val lastSeen: Long = 0L,
    val isOnline: Boolean = false
)