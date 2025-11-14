package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.messenger.domain.model.User
import com.example.messenger.domain.repository.IAuthRepository
import com.example.messenger.util.Resource
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {
    // 1. LiveData for login state
    private val _loginState = MutableLiveData<Resource<User>>()
    val loginState: LiveData<Resource<User>> = _loginState

    // 2. LiveData for phone linking state
    private val _linkState = MutableLiveData<Resource<Unit>>()
    val linkState: LiveData<Resource<Unit>> = _linkState

    // 3. Stored credential info (must be managed here)
    var verificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null
}