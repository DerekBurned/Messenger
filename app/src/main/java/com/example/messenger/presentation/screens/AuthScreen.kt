package com.example.messenger.presentation.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.presentation.base.ObserveAsEvents
import com.example.messenger.presentation.components.AuthInputTextField
import com.example.messenger.presentation.components.AuthMode
import com.example.messenger.presentation.components.AuthModeToggle
import com.example.messenger.presentation.components.Countries
import com.example.messenger.presentation.components.Country
import com.example.messenger.presentation.components.CountryCodePicker
import com.example.messenger.presentation.effect.AuthEffect
import com.example.messenger.presentation.intent.AuthIntent
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.viewmodel.AuthViewModel

private val ScreenBackground = Color(0xFF5B8DEE)

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit = {},
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var selectedCountry by remember { mutableStateOf(Countries.default) }
    var nationalNumber by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activity = context as? Activity

    ObserveAsEvents(viewModel.effect) { effect ->
        android.util.Log.d("AUTHFLOW_UI", "effect: $effect")
        when (effect) {
            AuthEffect.AuthSucceeded -> {
                android.util.Log.d("AUTHFLOW_UI", "AuthSucceeded -> onAuthSuccess() (navigate to Main)")
                onAuthSuccess()
            }
            AuthEffect.PhoneLinked -> Unit
        }
    }

    AuthScreenContent(
        mode = mode,
        selectedCountry = selectedCountry,
        nationalNumber = nationalNumber,
        username = username,
        otp = otp,
        isLoading = uiState.isLoading,
        error = uiState.error?.asString(),
        codeSent = uiState.codeSent,
        onModeChange = { newMode ->
            if (newMode != mode) {
                mode = newMode
                if (newMode == AuthMode.LOGIN) username = ""
                viewModel.dispatch(AuthIntent.ClearError)
            }
        },
        onCountryChange = { selectedCountry = it },
        onNationalNumberChange = { nationalNumber = it.filter { ch -> ch.isDigit() } },
        onUsernameChange = { username = it },
        onOtpChange = { otp = it },
        onSendOtp = {
            val fullPhone = selectedCountry.dialCode + nationalNumber
            val isRegister = mode == AuthMode.REGISTER
            val nameForVm = if (isRegister) username else null
            activity?.let {
                viewModel.dispatch(AuthIntent.SendVerificationCode(it, fullPhone, nameForVm, isRegister))
            }
        },
        onVerifyOtp = { viewModel.dispatch(AuthIntent.VerifyOtpAndLogin(otp)) },
        onEditPhoneNumber = {
            otp = ""
            viewModel.dispatch(AuthIntent.EditPhoneNumber)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthScreenContent(
    mode: AuthMode,
    selectedCountry: Country,
    nationalNumber: String,
    username: String,
    otp: String,
    isLoading: Boolean,
    error: String?,
    codeSent: Boolean,
    onModeChange: (AuthMode) -> Unit,
    onCountryChange: (Country) -> Unit,
    onNationalNumberChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
    onEditPhoneNumber: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ScreenBackground,
        topBar = {
            if (codeSent) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onEditPhoneNumber) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Edit phone number",
                                tint = Color.White,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ScreenBackground,
                    ),
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
            ) {
                if (!codeSent) {
                    AuthModeToggle(mode = mode, onModeChange = onModeChange)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = when {
                        codeSent -> "Verify code"
                        mode == AuthMode.LOGIN -> "Log in"
                        else -> "Create account"
                    },
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (!codeSent) {
                    PhoneEntryFields(
                        mode = mode,
                        selectedCountry = selectedCountry,
                        nationalNumber = nationalNumber,
                        username = username,
                        onCountryChange = onCountryChange,
                        onNationalNumberChange = onNationalNumberChange,
                        onUsernameChange = onUsernameChange,
                    )
                } else {
                    Text(
                        text = "Code sent to ${selectedCountry.dialCode} $nationalNumber",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthInputTextField(
                        value = otp,
                        onValueChange = onOtpChange,
                        placeholder = "enter SMS code",
                        keyboardType = KeyboardType.Number,
                    )
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 14.sp,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                val sendEnabled = !isLoading && nationalNumber.isNotBlank() &&
                        (mode == AuthMode.LOGIN || username.isNotBlank())
                val buttonEnabled = if (codeSent) {
                    !isLoading && otp.isNotBlank()
                } else {
                    sendEnabled
                }

                Button(
                    onClick = { if (codeSent) onVerifyOtp() else onSendOtp() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    enabled = buttonEnabled,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .height(20.dp),
                            color = ScreenBackground,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = if (codeSent) "Verify code" else "Send code",
                            color = ScreenBackground,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                if (!codeSent) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            onModeChange(
                                if (mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN,
                            )
                        },
                    ) {
                        Text(
                            text = if (mode == AuthMode.LOGIN) {
                                "Don't have an account? Register"
                            } else {
                                "Already have an account? Log in"
                            },
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneEntryFields(
    mode: AuthMode,
    selectedCountry: Country,
    nationalNumber: String,
    username: String,
    onCountryChange: (Country) -> Unit,
    onNationalNumberChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
) {
    if (mode == AuthMode.REGISTER) {
        AuthInputTextField(
            value = username,
            onValueChange = onUsernameChange,
            placeholder = "enter your username",
            keyboardType = KeyboardType.Text,
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CountryCodePicker(
            selected = selectedCountry,
            onCountrySelected = onCountryChange,
        )
        Box(modifier = Modifier.weight(1f)) {
            AuthInputTextField(
                value = nationalNumber,
                onValueChange = onNationalNumberChange,
                placeholder = "phone number",
                keyboardType = KeyboardType.Phone,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AuthScreenLoginPreview() {
    MessengerTheme {
        AuthScreenContent(
            mode = AuthMode.LOGIN,
            selectedCountry = Countries.all.first { it.isoCode == "US" },
            nationalNumber = "",
            username = "",
            otp = "",
            isLoading = false,
            error = null,
            codeSent = false,
            onModeChange = {},
            onCountryChange = {},
            onNationalNumberChange = {},
            onUsernameChange = {},
            onOtpChange = {},
            onSendOtp = {},
            onVerifyOtp = {},
            onEditPhoneNumber = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AuthScreenRegisterPreview() {
    MessengerTheme {
        AuthScreenContent(
            mode = AuthMode.REGISTER,
            selectedCountry = Countries.all.first { it.isoCode == "PL" },
            nationalNumber = "123456789",
            username = "derek",
            otp = "",
            isLoading = false,
            error = null,
            codeSent = false,
            onModeChange = {},
            onCountryChange = {},
            onNationalNumberChange = {},
            onUsernameChange = {},
            onOtpChange = {},
            onSendOtp = {},
            onVerifyOtp = {},
            onEditPhoneNumber = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AuthScreenOtpPreview() {
    MessengerTheme {
        AuthScreenContent(
            mode = AuthMode.LOGIN,
            selectedCountry = Countries.all.first { it.isoCode == "PL" },
            nationalNumber = "123456789",
            username = "",
            otp = "",
            isLoading = false,
            error = null,
            codeSent = true,
            onModeChange = {},
            onCountryChange = {},
            onNationalNumberChange = {},
            onUsernameChange = {},
            onOtpChange = {},
            onSendOtp = {},
            onVerifyOtp = {},
            onEditPhoneNumber = {},
        )
    }
}
