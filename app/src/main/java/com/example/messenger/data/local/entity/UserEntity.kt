package com.example.messenger.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.messenger.domain.model.PhoneNumber

@Entity(tableName = "users_table")
data class UserEntity (
    @PrimaryKey
    val id:Integer,
    val username: String,
    val email: String? = null,
    val phoneNumber: PhoneNumber? = null,
    val avatarUrl: String? = null,
    val lastSeen: Long = 0L,
    val isOnline: Boolean = false
    )
