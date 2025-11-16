package com.example.messenger.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.data.remote.firebase.FirebaseAuthService
import com.example.messenger.domain.model.User
import com.example.messenger.domain.usecase.auth.LinkPhoneUseCase
import com.example.messenger.domain.usecase.auth.LoginWithEmailUseCase
import com.example.messenger.domain.usecase.auth.LoginWithPhoneNumberUseCase
import com.example.messenger.domain.usecase.auth.LogoutUseCase
import com.example.messenger.domain.usecase.auth.ObserveAuthStateUseCase
import com.example.messenger.domain.usecase.auth.RegisterUseCase
import com.example.messenger.util.Resource
import com.example.messenger.util.VerificationResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val registerUseCase: RegisterUseCase,
    private val loginWithPhoneNumberUseCase: LoginWithPhoneNumberUseCase,
    private val linkPhoneUseCase: LinkPhoneUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val firebaseAuthService: FirebaseAuthService
) : ViewModel() {

    // State
    private val _loginState = MutableLiveData<Resource<User>>()
    val loginState: LiveData<Resource<User>> = _loginState

    private val _registerState = MutableLiveData<Resource<User>>()
    val registerState: LiveData<Resource<User>> = _registerState

    private val _linkState = MutableLiveData<Resource<Unit>>()
    val linkState: LiveData<Resource<Unit>> = _linkState

    private val _authState = MutableLiveData<FirebaseUser?>()
    val authState: LiveData<FirebaseUser?> = _authState

    // Phone Auth Data
    var verificationId: String? = null

    init {
        observeAuthStatus()
    }

    private fun observeAuthStatus() {
        viewModelScope.launch {
            observeAuthStateUseCase().collectLatest { user ->
                _authState.value = user
            }
        }
    }

    fun loginWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            loginWithEmailUseCase(email, pass).collect { _loginState.value = it }
        }
    }

    fun register(email: String, pass: String, username: String) {
        viewModelScope.launch {
            registerUseCase(email, pass, username).collect { _registerState.value = it }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }



    fun sendVerificationCode(activity: Activity, phoneNumber: String) {
        _loginState.value = Resource.Loading

        viewModelScope.launch {
            // Call the suspend function directly! No callbacks needed here.
            val result = firebaseAuthService.sendVerificationCode(phoneNumber, activity)

            if (result.isSuccess) {
                val verifyResult = result.getOrThrow()
                when (verifyResult) {
                    is VerificationResult.CodeSent -> {
                        // SMS Sent. Store ID and tell UI to show OTP input
                        verificationId = verifyResult.verificationId
                        // Ideally, emit a "CodeSent" state here if you have one
                        // For now, keeping it Loading implies waiting for user input
                    }
                    is VerificationResult.AutoVerified -> {
                        // Instant login (e.g. device recognized)
                        signInWithPhone(verifyResult.credential)
                    }
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Verification Failed"
                _loginState.value = Resource.Error(errorMsg)
            }
        }
    }

    fun verifyOtpAndLogin(otpCode: String) {
        viewModelScope.launch {
            // Use the new service helper to get the credential from code
            val credentialResult = firebaseAuthService.verifyCode(otpCode)

            if (credentialResult.isSuccess) {
                val credential = credentialResult.getOrThrow()
                signInWithPhone(credential)
            } else {
                _loginState.value = Resource.Error("Invalid Code")
            }
        }
    }

    fun verifyOtpAndLink(otpCode: String) {
        viewModelScope.launch {
            val credentialResult = firebaseAuthService.verifyCode(otpCode)
            if (credentialResult.isSuccess) {
                val credential = credentialResult.getOrThrow()
                linkPhone(credential)
            } else {
                _linkState.value = Resource.Error("Invalid Code")
            }
        }
    }

    private fun signInWithPhone(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            // This UseCase calls the Repository, which calls Service.signInWithPhone
            val result = loginWithPhoneNumberUseCase(credential)
            _loginState.value = result
        }
    }

    private fun linkPhone(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            linkPhoneUseCase(credential).collect { _linkState.value = it }
        }
    }
}