package com.example.messenger.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.messenger.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Query ("SELECT * FROM users_table")
    fun getAllUsers(): List<UserEntity>
}