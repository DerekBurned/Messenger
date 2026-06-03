package com.example.messenger.presentation.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.presentation.base.ObserveAsEvents
import com.example.messenger.presentation.components.AuthInputTextField
import com.example.messenger.presentation.components.CountryCodePicker
import com.example.messenger.presentation.effect.ChangePhoneEffect
import com.example.messenger.presentation.intent.ChangePhoneIntent
import com.example.messenger.presentation.screens.ui.theme.OnSurface
import com.example.messenger.presentation.screens.ui.theme.OnSurfaceMuted
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.state.ChangePhoneStep
import com.example.messenger.presentation.viewmodel.ChangePhoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePhoneScreen(
    viewModel: ChangePhoneViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onDone: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            ChangePhoneEffect.Done -> onDone()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(elevation = 4.dp),
                title = {
                    Text(
                        text = when (state.step) {
                            ChangePhoneStep.CONFIRM_CURRENT,
                            ChangePhoneStep.VERIFY_CURRENT,
                            -> "Verify current number"
                            ChangePhoneStep.ENTER_NEW,
                            ChangePhoneStep.VERIFY_NEW,
                            -> "Set new number"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.step == ChangePhoneStep.CONFIRM_CURRENT) onBackClick()
                        else viewModel.dispatch(ChangePhoneIntent.GoBack)
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = PrimaryBlue),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            StepIndicator(step = state.step)
            Spacer(modifier = Modifier.height(20.dp))

            when (state.step) {
                ChangePhoneStep.CONFIRM_CURRENT -> ConfirmCurrentStep(
                    currentPhone = state.currentPhone,
                    isLoading = state.isLoading,
                    onSendCode = {
                        activity?.let { viewModel.dispatch(ChangePhoneIntent.SendCodeToCurrentPhone(it)) }
                    },
                )
                ChangePhoneStep.VERIFY_CURRENT -> OtpStep(
                    label = "Enter the code we sent to ${state.currentPhone}",
                    otp = state.otp,
                    onOtpChange = { viewModel.dispatch(ChangePhoneIntent.OtpChange(it)) },
                    isLoading = state.isLoading,
                    onVerify = { viewModel.dispatch(ChangePhoneIntent.VerifyCurrentOtp) },
                )
                ChangePhoneStep.ENTER_NEW -> EnterNewStep(
                    state = state,
                    onCountrySelected = { viewModel.dispatch(ChangePhoneIntent.NewCountrySelected(it)) },
                    onNumberChange = { viewModel.dispatch(ChangePhoneIntent.NewNumberChange(it)) },
                    onSendCode = {
                        activity?.let { viewModel.dispatch(ChangePhoneIntent.SendCodeToNewPhone(it)) }
                    },
                )
                ChangePhoneStep.VERIFY_NEW -> OtpStep(
                    label = "Enter the code we sent to " +
                        "${state.newCountry.dialCode} ${state.newNationalNumber}",
                    otp = state.otp,
                    onOtpChange = { viewModel.dispatch(ChangePhoneIntent.OtpChange(it)) },
                    isLoading = state.isLoading,
                    onVerify = { viewModel.dispatch(ChangePhoneIntent.VerifyNewOtp) },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            state.error?.let { err ->
                Text(err.asString(), color = Color.Red, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun StepIndicator(step: ChangePhoneStep) {
    val currentIndex = when (step) {
        ChangePhoneStep.CONFIRM_CURRENT, ChangePhoneStep.VERIFY_CURRENT -> 0
        ChangePhoneStep.ENTER_NEW -> 1
        ChangePhoneStep.VERIFY_NEW -> 2
    }
    val labels = listOf("Verify current", "New number", "Verify new")
    Column {
        Text(
            text = "Step ${currentIndex + 1} of 3 · ${labels[currentIndex]}",
            color = OnSurfaceMuted,
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(3) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = if (i <= currentIndex) PrimaryBlue else PrimaryBlue.copy(alpha = 0.15f),
                            shape = CircleShape,
                        ),
                )
            }
        }
    }
}

@Composable
private fun ConfirmCurrentStep(
    currentPhone: String,
    isLoading: Boolean,
    onSendCode: () -> Unit,
) {
    Text(
        text = "To change your phone number we first need to verify it's still you.",
        color = OnSurfaceMuted,
        fontSize = 14.sp,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Current number",
        color = OnSurfaceMuted,
        fontSize = 12.sp,
    )
    Text(
        text = currentPhone.ifBlank { "(none on file)" },
        color = OnSurface,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(modifier = Modifier.height(24.dp))
    PrimaryActionButton(
        text = "Send verification code",
        enabled = !isLoading && currentPhone.isNotBlank(),
        isLoading = isLoading,
        onClick = onSendCode,
    )
}

@Composable
private fun EnterNewStep(
    state: com.example.messenger.presentation.state.ChangePhoneUiState,
    onCountrySelected: (com.example.messenger.presentation.components.Country) -> Unit,
    onNumberChange: (String) -> Unit,
    onSendCode: () -> Unit,
) {
    Text(
        text = "Enter your new phone number. We'll send a code to confirm it.",
        color = OnSurfaceMuted,
        fontSize = 14.sp,
    )
    Spacer(modifier = Modifier.height(20.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CountryCodePicker(
            selected = state.newCountry,
            onCountrySelected = onCountrySelected,
        )
        Box(modifier = Modifier.weight(1f)) {
            AuthInputTextField(
                value = state.newNationalNumber,
                onValueChange = onNumberChange,
                placeholder = "phone number",
                keyboardType = KeyboardType.Phone,
            )
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
    PrimaryActionButton(
        text = "Send code",
        enabled = !state.isLoading && state.newNationalNumber.isNotBlank(),
        isLoading = state.isLoading,
        onClick = onSendCode,
    )
}

@Composable
private fun OtpStep(
    label: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    isLoading: Boolean,
    onVerify: () -> Unit,
) {
    Text(label, color = OnSurfaceMuted, fontSize = 14.sp)
    Spacer(modifier = Modifier.height(20.dp))
    AuthInputTextField(
        value = otp,
        onValueChange = onOtpChange,
        placeholder = "SMS code",
        keyboardType = KeyboardType.Number,
    )
    Spacer(modifier = Modifier.height(24.dp))
    PrimaryActionButton(
        text = "Verify",
        enabled = !isLoading && otp.isNotBlank(),
        isLoading = isLoading,
        onClick = onVerify,
    )
}

@Composable
private fun PrimaryActionButton(
    text: String,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
        enabled = enabled,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}
