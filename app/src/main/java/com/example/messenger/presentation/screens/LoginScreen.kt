package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.presentation.components.AuthMethod
import com.example.messenger.presentation.components.AuthMethodToggle
import com.example.messenger.presentation.components.AuthInputTextField
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit = {}
) {
    var authMethod by remember { mutableStateOf(AuthMethod.EMAIL) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF5B8DEE)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            // Переключатель Email / Phone (Используется из AuthComponents.kt)
            AuthMethodToggle(
                selectedMethod = authMethod,
                onMethodSelected = { authMethod = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Поля ввода
            when (authMethod) {
                AuthMethod.EMAIL -> {
                    AuthInputTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "enter email",
                        keyboardType = KeyboardType.Email,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthInputTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "enter your password",
                        visualTransformation = PasswordVisualTransformation(),
                    )
                }
                AuthMethod.PHONE -> {
                    AuthInputTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        placeholder = "enter phone number",
                        keyboardType = KeyboardType.Phone,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка входа
            Button(
                onClick = { /* ... */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = true
            ) {
                Text(
                    text = if (authMethod == AuthMethod.EMAIL) "Login with Email" else "Login with Phone",
                    color = Color(0xFF5B8DEE),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка Google Sign In
            OutlinedButton(
                onClick = { /* ... */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color.White),
                    width = 2.dp
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = true
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Google",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sign in with Google",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = "Don't have an account? Register",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    MessengerTheme {
        LoginScreen()
    }
}