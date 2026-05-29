package com.example.messenger.presentation.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.messenger.data.presence.PresenceManager
import com.example.messenger.data.remote.auth.FirebaseAuthService
import com.example.messenger.data.remote.firebase.FirebaseMessagingManager
import com.example.messenger.data.remote.firebase.FirestoreService
import com.example.messenger.domain.usecase.auth.LinkPhoneUseCase
import com.example.messenger.domain.usecase.auth.LoginWithPhoneNumberUseCase
import com.example.messenger.domain.usecase.auth.LogoutUseCase
import com.example.messenger.domain.usecase.auth.ObserveAuthStateUseCase
import com.example.messenger.R
import com.example.messenger.presentation.base.MviViewModel
import com.example.messenger.presentation.base.UiText
import com.example.messenger.presentation.base.toUiText
import com.example.messenger.presentation.effect.AuthEffect
import com.example.messenger.presentation.intent.AuthIntent
import com.example.messenger.presentation.state.AuthUiState
import com.example.messenger.util.Resource
import com.example.messenger.util.VerificationResult
import com.google.firebase.auth.PhoneAuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginWithPhoneNumberUseCase: LoginWithPhoneNumberUseCase,
    private val linkPhoneUseCase: LinkPhoneUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val firebaseAuthService: FirebaseAuthService,
    private val presenceManager: PresenceManager,
    private val firebaseMessagingManager: FirebaseMessagingManager,
    private val firestoreService: FirestoreService,
) : MviViewModel<AuthUiState, AuthIntent, AuthEffect>(
    initialState = AuthUiState(isAuthenticated = firebaseAuthService.isAuthenticated()),
) {
    
    private var pendingUsername: String? = null

    init {
        observeAuthStatus()
    }

    override fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.SendVerificationCode ->
                sendVerificationCode(intent.activity, intent.phoneNumber, intent.username)
            is AuthIntent.VerifyOtpAndLogin -> verifyOtpAndLogin(intent.otpCode)
            is AuthIntent.VerifyOtpAndLink -> verifyOtpAndLink(intent.otpCode)
            AuthIntent.Logout -> logout()
            AuthIntent.ClearError -> setState { copy(error = null) }
            AuthIntent.EditPhoneNumber -> setState { copy(codeSent = false, error = null) }
        }
    }

    private fun observeAuthStatus() {
        viewModelScope.launch {
            observeAuthStateUseCase().collectLatest { user ->
                setState { copy(isAuthenticated = user != null) }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            presenceManager.disconnect(viewModelScope)
            logoutUseCase()
            setState { AuthUiState() }
        }
    }

    private fun sendVerificationCode(activity: Activity, phoneNumber: String, username: String?) {
        pendingUsername = username
        setState { copy(isLoading = true, error = null, codeSent = false) }

        viewModelScope.launch {
            val result = firebaseAuthService.sendVerificationCode(phoneNumber, activity)

            if (result.isSuccess) {
                when (val verifyResult = result.getOrThrow()) {
                    is VerificationResult.CodeSent -> {
                        setState { copy(isLoading = false, codeSent = true) }
                    }
                    is VerificationResult.AutoVerified -> {
                        signInWithPhone(verifyResult.credential)
                    }
                }
            } else {
                val errorMsg: UiText = result.exceptionOrNull()?.message?.toUiText()
                    ?: UiText.StringResource(R.string.auth_error_verification_failed)
                setState { copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    private fun verifyOtpAndLogin(otpCode: String) {
        viewModelScope.launch {
            val credentialResult = firebaseAuthService.verifyCode(otpCode)
            if (credentialResult.isSuccess) {
                signInWithPhone(credentialResult.getOrThrow())
            } else {
                setState { copy(error = UiText.StringResource(R.string.auth_error_invalid_code)) }
            }
        }
    }

    private fun verifyOtpAndLink(otpCode: String) {
        viewModelScope.launch {
            val credentialResult = firebaseAuthService.verifyCode(otpCode)
            if (credentialResult.isSuccess) {
                linkPhone(credentialResult.getOrThrow())
            } else {
                setState { copy(error = UiText.StringResource(R.string.auth_error_invalid_code)) }
            }
        }
    }

    private fun signInWithPhone(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = loginWithPhoneNumberUseCase(credential, pendingUsername)
            pendingUsername = null
            when (result) {
                is Resource.Success -> {
                    syncFcmTokenForCurrentUser()
                    setState {
                        copy(
                            isLoading = false,
                            currentUser = result.data,
                            error = null,
                        )
                    }
                    emitEffect(AuthEffect.AuthSucceeded)
                }
                is Resource.Error -> setState {
                    copy(isLoading = false, error = result.message.toUiText())
                }
                is Resource.Failure -> setState {
                    copy(isLoading = false, error = result.exception.message?.toUiText())
                }
                is Resource.Loading -> {}
            }
        }
    }

    private suspend fun syncFcmTokenForCurrentUser() {
        val uid = firebaseAuthService.getCurrentUserId() ?: return
        firebaseMessagingManager.getFcmToken()
            .onSuccess { token ->
                firestoreService.updateFcmToken(uid, token)
            }
            .onFailure { e ->
                Log.w("AuthViewModel", "FCM token sync skipped: ${e.message}")
            }
    }

    private fun linkPhone(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            linkPhoneUseCase(credential).collect { resource ->
                when (resource) {
                    is Resource.Loading -> setState { copy(isLoading = true, error = null) }
                    is Resource.Success -> {
                        setState { copy(isLoading = false, error = null) }
                        emitEffect(AuthEffect.PhoneLinked)
                    }
                    is Resource.Error -> setState { copy(isLoading = false, error = resource.message.toUiText()) }
                    is Resource.Failure -> setState { copy(isLoading = false, error = resource.exception.message?.toUiText()) }
                }
            }
        }
    }
}
