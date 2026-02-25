package com.example.messenger.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.messenger.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Query ("SELECT * FROM users_table")
    fun getAllUsers(): List<UserEntity>
    @Query ("SELECT * FROM users_table WHERE id = :id")
    fun getUserById(id: Int): UserEntity
    @Query ("SELECT * FROM users_table WHERE phoneNumber = :phone")
    fun getUserByPhone(phone: String): UserEntity
    @Query ("SELECT * FROM users_table WHERE email = :email")
    fun getUserByEmail(email: String): UserEntity
    @Query ("SELECT * FROM users_table WHERE username = :username")
    fun getUserByUsername(username: String): UserEntity


}