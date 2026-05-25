package com.example.messenger.presentation.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.presentation.components.AuthInputTextField
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(uiState.registerSuccess) {
        if (uiState.registerSuccess) {
            viewModel.onRegisterNavigated()
            onRegisterSuccess()
        }
    }

    RegisterScreenContent(
        username = username,
        phoneNumber = phoneNumber,
        otp = otp,
        isLoading = uiState.isLoading,
        error = uiState.error,
        codeSent = uiState.codeSent,
        onUsernameChange = { username = it },
        onPhoneChange = { phoneNumber = it },
        onOtpChange = { otp = it },
        onSendOtp = { activity?.let { viewModel.sendVerificationCode(it, phoneNumber, username) } },
        onVerifyOtp = { viewModel.verifyOtpAndLogin(otp) },
        onNavigateToLogin = onNavigateToLogin,
    )
}

@Composable
private fun RegisterScreenContent(
    username: String,
    phoneNumber: String,
    otp: String,
    isLoading: Boolean,
    error: String?,
    codeSent: Boolean,
    onUsernameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF5B8DEE))
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
            ) {
                Text(
                    text = "Create account",
                    color = Color.White,
                    fontSize = 24.sp,
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (!codeSent) {
                    AuthInputTextField(
                        value = username,
                        onValueChange = onUsernameChange,
                        placeholder = "enter your username",
                        keyboardType = KeyboardType.Text,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthInputTextField(
                        value = phoneNumber,
                        onValueChange = onPhoneChange,
                        placeholder = "enter phone number (e.g. +1234567890)",
                        keyboardType = KeyboardType.Phone,
                    )
                } else {
                    AuthInputTextField(
                        value = otp,
                        onValueChange = onOtpChange,
                        placeholder = "enter SMS code",
                        keyboardType = KeyboardType.Number,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (error != null) {
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { if (!codeSent) onSendOtp() else onVerifyOtp() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF5B8DEE),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = if (codeSent) "Verify code" else "Send code",
                            color = Color(0xFF5B8DEE),
                            fontSize = 16.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Already have an account? Log in",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenPreview() {
    MessengerTheme {
        RegisterScreenContent(
            username = "",
            phoneNumber = "",
            otp = "",
            isLoading = false,
            error = null,
            codeSent = false,
            onUsernameChange = {},
            onPhoneChange = {},
            onOtpChange = {},
            onSendOtp = {},
            onVerifyOtp = {},
            onNavigateToLogin = {},
        )
    }
}
