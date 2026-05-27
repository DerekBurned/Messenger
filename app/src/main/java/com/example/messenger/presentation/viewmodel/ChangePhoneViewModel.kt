package com.example.messenger.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.data.remote.auth.FirebaseAuthService
import com.example.messenger.domain.model.PhoneNumber
import com.example.messenger.domain.repository.IUserRepository
import com.example.messenger.presentation.components.Countries
import com.example.messenger.presentation.components.Country
import com.example.messenger.presentation.state.ChangePhoneStep
import com.example.messenger.presentation.state.ChangePhoneUiState
import com.example.messenger.util.ValidationUtils
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
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private companion object {
        const val KEY_STEP = "change_phone_step"
        const val KEY_CURRENT_PHONE = "change_phone_current"
        const val KEY_NEW_ISO = "change_phone_new_iso"
        const val KEY_NEW_NUMBER = "change_phone_new_number"
        const val KEY_REAUTH_AT = "change_phone_reauth_at"

        const val REAUTH_VALIDITY_MS = 3 * 60 * 1000L
    }

    private val _uiState: MutableStateFlow<ChangePhoneUiState>
    val uiState: StateFlow<ChangePhoneUiState>

    private var reauthSucceededAt: Long
        get() = savedStateHandle[KEY_REAUTH_AT] ?: 0L
        set(value) { savedStateHandle[KEY_REAUTH_AT] = value }

    init {
        val persistedStep = savedStateHandle.get<String>(KEY_STEP)
            ?.let { runCatching { ChangePhoneStep.valueOf(it) }.getOrNull() }
            ?: ChangePhoneStep.CONFIRM_CURRENT
        val persistedCurrent = savedStateHandle.get<String>(KEY_CURRENT_PHONE).orEmpty()
        val persistedIso = savedStateHandle.get<String>(KEY_NEW_ISO).orEmpty()
        val persistedNumber = savedStateHandle.get<String>(KEY_NEW_NUMBER).orEmpty()
        val country = Countries.all.firstOrNull { it.isoCode == persistedIso }
            ?: Countries.default

        val currentPhone = persistedCurrent.ifBlank {
            firebaseAuthService.getUserPhoneNumber().orEmpty()
        }

        _uiState = MutableStateFlow(
            ChangePhoneUiState(
                step = persistedStep,
                currentPhone = currentPhone,
                newCountry = country,
                newNationalNumber = persistedNumber,
            ),
        )
        uiState = _uiState.asStateFlow()
        savedStateHandle[KEY_CURRENT_PHONE] = currentPhone
    }

    fun onNewCountrySelected(country: Country) {
        savedStateHandle[KEY_NEW_ISO] = country.isoCode
        _uiState.update { it.copy(newCountry = country) }
    }

    fun onNewNumberChange(value: String) {
        val digits = value.filter { ch -> ch.isDigit() }
        savedStateHandle[KEY_NEW_NUMBER] = digits
        _uiState.update { it.copy(newNationalNumber = digits) }
    }

    fun onOtpChange(value: String) {
        _uiState.update { it.copy(otp = value) }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun sendCodeToCurrentPhone(activity: Activity) {
        val phone = _uiState.value.currentPhone
        if (phone.isBlank()) {
            _uiState.update { it.copy(error = "No phone number on file") }
            return
        }
        sendOtp(activity, phone, onSent = {
            updateStep(ChangePhoneStep.VERIFY_CURRENT)
            _uiState.update { it.copy(otp = "", isLoading = false) }
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
                    reauthSucceededAt = System.currentTimeMillis()
                    updateStep(ChangePhoneStep.ENTER_NEW)
                    _uiState.update {
                        it.copy(otp = "", isLoading = false, error = null)
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
        if (!ValidationUtils.isValidPhoneNumber(fullNewPhone)) {
            _uiState.update { it.copy(error = "That doesn't look like a valid phone number") }
            return
        }
        if (!hasFreshReauth()) {
            failWithStaleReauth()
            return
        }
        sendOtp(activity, fullNewPhone, onSent = {
            updateStep(ChangePhoneStep.VERIFY_NEW)
            _uiState.update { it.copy(otp = "", isLoading = false) }
        }, onAutoVerified = { credential ->
            updatePhoneAndFinish(credential)
        })
    }

    fun verifyNewOtp() {
        if (!hasFreshReauth()) {
            failWithStaleReauth()
            return
        }
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
            clearPersistedFormState()
            updateStep(ChangePhoneStep.DONE)
            _uiState.update { it.copy(isLoading = false) }
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
        updateStep(previous)
        _uiState.update { it.copy(otp = "", error = null, isLoading = false) }
    }

    private fun hasFreshReauth(): Boolean {
        val at = reauthSucceededAt
        if (at == 0L) return false
        return System.currentTimeMillis() - at <= REAUTH_VALIDITY_MS
    }

    private fun failWithStaleReauth() {
        reauthSucceededAt = 0L
        updateStep(ChangePhoneStep.CONFIRM_CURRENT)
        _uiState.update {
            it.copy(
                otp = "",
                isLoading = false,
                error = "Verification expired — please confirm your current number again.",
            )
        }
    }

    private fun updateStep(step: ChangePhoneStep) {
        savedStateHandle[KEY_STEP] = step.name
        _uiState.update { it.copy(step = step) }
    }

    private fun clearPersistedFormState() {
        savedStateHandle[KEY_NEW_NUMBER] = ""
        savedStateHandle[KEY_NEW_ISO] = ""
        savedStateHandle[KEY_REAUTH_AT] = 0L
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
