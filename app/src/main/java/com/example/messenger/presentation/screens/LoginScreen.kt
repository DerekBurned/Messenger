package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.viewmodel.AuthViewModel
import com.example.messenger.util.Resource

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginState by viewModel.loginState.observeAsState()
    val context = LocalContext.current

    // Следим за состоянием логина
    LaunchedEffect(loginState) {
        when (loginState) {
            is Resource.Success -> {
                onLoginSuccess()
            }
            else -> {}
        }
    }

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
            // Круглый аватар
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFFD3D3D3), CircleShape)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Поле для email
            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        "enter email or phone number",
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(4.dp),
                singleLine = true,
                enabled = loginState !is Resource.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле для пароля
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        "enter your password",
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(4.dp),
                singleLine = true,
                enabled = loginState !is Resource.Loading
            )



            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка Sign up (логин через email)
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        viewModel.loginWithEmail(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = loginState !is Resource.Loading
            ) {
                if (loginState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF5B8DEE)
                    )
                } else {
                    Text(
                        "Sign up",
                        color = Color(0xFF5B8DEE),
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка Google Sign In (пока заглушка)
            OutlinedButton(
                onClick = {
                    // TODO: Добавить Google Sign In позже
                },
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
                enabled = loginState !is Resource.Loading
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