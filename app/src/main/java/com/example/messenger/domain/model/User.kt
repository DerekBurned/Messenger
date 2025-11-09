package com.example.messenger.domain.model


data class User(
    val id: String,
    val email: String,
    val username: String,
    val avatarUrl: String? = null,
    val lastSeen: Long = 0L,
    val isOnline: Boolean = false
)