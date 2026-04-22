package com.example.messenger.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.data.presence.PresenceManager
import com.example.messenger.data.remote.firebase.FirebaseAuthService
import com.example.messenger.domain.usecase.auth.LinkPhoneUseCase
import com.example.messenger.domain.usecase.auth.LoginWithEmailUseCase
import com.example.messenger.domain.usecase.auth.LoginWithPhoneNumberUseCase
import com.example.messenger.domain.usecase.auth.LogoutUseCase
import com.example.messenger.domain.usecase.auth.ObserveAuthStateUseCase
import com.example.messenger.domain.usecase.auth.RegisterUseCase
import com.example.messenger.presentation.state.AuthUiState
import com.example.messenger.util.Resource
import com.example.messenger.util.VerificationResult
import com.google.firebase.auth.PhoneAuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
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
    private val firebaseAuthService: FirebaseAuthService,
    private val presenceManager: PresenceManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var verificationId: String? = null

    init {
        observeAuthStatus()
    }

    private fun observeAuthStatus() {
        viewModelScope.launch {
            observeAuthStateUseCase().collectLatest { firebaseUser ->
                _uiState.update { it.copy(isAuthenticated = firebaseUser != null) }
            }
        }
    }

    fun loginWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            loginWithEmailUseCase(email, pass).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, currentUser = resource.data, loginSuccess = true, error = null)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, error = resource.message)
                    }
                    is Resource.Failure -> _uiState.update {
                        it.copy(isLoading = false, error = resource.exception.message)
                    }
                }
            }
        }
    }

    fun register(email: String, pass: String, username: String) {
        viewModelScope.launch {
            registerUseCase(email, pass, username).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, currentUser = resource.data, registerSuccess = true, error = null)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, error = resource.message)
                    }
                    is Resource.Failure -> _uiState.update {
                        it.copy(isLoading = false, error = resource.exception.message)
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            presenceManager.disconnect(viewModelScope)
            logoutUseCase()
            _uiState.update { AuthUiState() }
        }
    }

    fun sendVerificationCode(activity: Activity, phoneNumber: String) {
        _uiState.update { it.copy(isLoading = true, error = null, codeSent = false) }

        viewModelScope.launch {
            val result = firebaseAuthService.sendVerificationCode(phoneNumber, activity)

            if (result.isSuccess) {
                when (val verifyResult = result.getOrThrow()) {
                    is VerificationResult.CodeSent -> {
                        verificationId = verifyResult.verificationId
                        _uiState.update { it.copy(isLoading = false, codeSent = true) }
                    }
                    is VerificationResult.AutoVerified -> {
                        signInWithPhone(verifyResult.credential)
                    }
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Verification Failed"
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    fun verifyOtpAndLogin(otpCode: String) {
        viewModelScope.launch {
            val credentialResult = firebaseAuthService.verifyCode(otpCode)
            if (credentialResult.isSuccess) {
                signInWithPhone(credentialResult.getOrThrow())
            } else {
                _uiState.update { it.copy(error = "Invalid Code") }
            }
        }
    }

    fun verifyOtpAndLink(otpCode: String) {
        viewModelScope.launch {
            val credentialResult = firebaseAuthService.verifyCode(otpCode)
            if (credentialResult.isSuccess) {
                linkPhone(credentialResult.getOrThrow())
            } else {
                _uiState.update { it.copy(error = "Invalid Code") }
            }
        }
    }

    private fun signInWithPhone(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = loginWithPhoneNumberUseCase(credential)
            when (result) {
                is Resource.Success -> _uiState.update {
                    it.copy(isLoading = false, currentUser = result.data, loginSuccess = true, error = null)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Resource.Failure -> _uiState.update {
                    it.copy(isLoading = false, error = result.exception.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun linkPhone(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            linkPhoneUseCase(credential).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, error = null) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = resource.message) }
                    is Resource.Failure -> _uiState.update { it.copy(isLoading = false, error = resource.exception.message) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onLoginNavigated() {
        _uiState.update { it.copy(loginSuccess = false) }
    }

    fun onRegisterNavigated() {
        _uiState.update { it.copy(registerSuccess = false) }
    }
}
