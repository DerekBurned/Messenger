package com.example.messenger.data.remote.firebase

import com.example.messenger.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import com.example.messenger.data.local.dao.UserDao
import com.example.messenger.data.remote.firebase.FirebaseAuthService
import com.example.messenger.domain.repository.IAuthRepository

class FirebaseAuthService @Inject constructor() {
    private val auth: FirebaseAuth  = FirebaseAuth.getInstance()
    suspend fun login(email:String? = null, pass: String, number: String? = null): Result<User> {
        return Result.failure(Exception("Not implemented"))
    }


}