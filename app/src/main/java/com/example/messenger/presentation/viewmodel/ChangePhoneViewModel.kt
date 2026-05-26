package com.example.messenger.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.data.remote.auth.FirebaseAuthService
import com.example.messenger.domain.model.PhoneNumber
import com.example.messenger.domain.repository.IUserRepository
import com.example.messenger.presentation.components.Country
import com.example.messenger.presentation.state.ChangePhoneStep
import com.example.messenger.presentation.state.ChangePhoneUiState
import com.example.messenger.util.VerificationResult
import com.google.firebase.auth.PhoneAuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePhoneViewModel @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService,
    private val userRepository: IUserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePhoneUiState())
    val uiState: StateFlow<ChangePhoneUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(currentPhone = firebaseAuthService.getUserPhoneNumber().orEmpty())
        }
    }

    fun onNewCountrySelected(country: Country) =
        _uiState.update { it.copy(newCountry = country) }

    fun onNewNumberChange(value: String) =
        _uiState.update { it.copy(newNationalNumber = value.filter { ch -> ch.isDigit() }) }

    fun onOtpChange(value: String) =
        _uiState.update { it.copy(otp = value) }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun sendCodeToCurrentPhone(activity: Activity) {
        val phone = _uiState.value.currentPhone
        if (phone.isBlank()) {
            _uiState.update { it.copy(error = "No phone number on file") }
            return
        }
        sendOtp(activity, phone, onSent = {
            _uiState.update {
                it.copy(step = ChangePhoneStep.VERIFY_CURRENT, otp = "", isLoading = false)
            }
        }, onAutoVerified = { credential ->
            reauthenticateAndAdvance(credential)
        })
    }

    fun verifyCurrentOtp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val credentialResult = firebaseAuthService.verifyCode(_uiState.value.otp)
            credentialResult.fold(
                onSuccess = { reauthenticateAndAdvance(it) },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Invalid code") }
                },
            )
        }
    }

    private fun reauthenticateAndAdvance(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            val result = firebaseAuthService.reauthenticateWithPhone(credential)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            step = ChangePhoneStep.ENTER_NEW,
                            otp = "",
                            isLoading = false,
                            error = null,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Could not verify current phone")
                    }
                },
            )
        }
    }

    fun sendCodeToNewPhone(activity: Activity) {
        val s = _uiState.value
        val fullNewPhone = s.newCountry.dialCode + s.newNationalNumber
        if (s.newNationalNumber.isBlank()) {
            _uiState.update { it.copy(error = "Enter a new phone number") }
            return
        }
        sendOtp(activity, fullNewPhone, onSent = {
            _uiState.update {
                it.copy(step = ChangePhoneStep.VERIFY_NEW, otp = "", isLoading = false)
            }
        }, onAutoVerified = { credential ->
            updatePhoneAndFinish(credential)
        })
    }

    fun verifyNewOtp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val credentialResult = firebaseAuthService.verifyCode(_uiState.value.otp)
            credentialResult.fold(
                onSuccess = { updatePhoneAndFinish(it) },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Invalid code")
                    }
                },
            )
        }
    }

    private fun updatePhoneAndFinish(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            val updateResult = firebaseAuthService.updatePhoneNumber(credential)
            if (updateResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = updateResult.exceptionOrNull()?.message ?: "Phone update failed",
                    )
                }
                return@launch
            }
            val s = _uiState.value
            val newPhoneNumber = PhoneNumber(
                countryCode = s.newCountry.dialCode,
                number = s.newNationalNumber,
            )
            userRepository.updateUserProfile(mapOf("phoneNumber" to newPhoneNumber))
            _uiState.update { it.copy(isLoading = false, step = ChangePhoneStep.DONE) }
        }
    }

    fun goBack() {
        val current = _uiState.value.step
        val previous = when (current) {
            ChangePhoneStep.VERIFY_CURRENT -> ChangePhoneStep.CONFIRM_CURRENT
            ChangePhoneStep.ENTER_NEW -> ChangePhoneStep.CONFIRM_CURRENT
            ChangePhoneStep.VERIFY_NEW -> ChangePhoneStep.ENTER_NEW
            ChangePhoneStep.CONFIRM_CURRENT, ChangePhoneStep.DONE -> current
        }
        _uiState.update {
            it.copy(step = previous, otp = "", error = null, isLoading = false)
        }
    }

    private fun sendOtp(
        activity: Activity,
        phoneNumber: String,
        onSent: () -> Unit,
        onAutoVerified: (PhoneAuthCredential) -> Unit,
    ) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = firebaseAuthService.sendVerificationCode(phoneNumber, activity)
            result.fold(
                onSuccess = { verification ->
                    when (verification) {
                        is VerificationResult.CodeSent -> onSent()
                        is VerificationResult.AutoVerified -> onAutoVerified(verification.credential)
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Verification failed")
                    }
                },
            )
        }
    }
}
