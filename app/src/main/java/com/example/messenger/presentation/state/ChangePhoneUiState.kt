package com.example.messenger.presentation.state

import com.example.messenger.presentation.components.Countries
import com.example.messenger.presentation.components.Country

enum class ChangePhoneStep {
    
    CONFIRM_CURRENT,

    VERIFY_CURRENT,

    ENTER_NEW,

    VERIFY_NEW,

    DONE,
}

data class ChangePhoneUiState(
    val step: ChangePhoneStep = ChangePhoneStep.CONFIRM_CURRENT,
    val currentPhone: String = "",
    val newCountry: Country = Countries.default,
    val newNationalNumber: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)
