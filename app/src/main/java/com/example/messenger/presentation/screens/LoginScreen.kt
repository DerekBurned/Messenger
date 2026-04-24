package com.example.messenger.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.presentation.screens.ui.theme.BluePrimary

/**
 * data-figma-name: log_phone
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var phoneOrEmail by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BluePrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // ── Title ──────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "💬", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text      = "Login by phone number",
                    color     = Color.White.copy(alpha = 0.8f),
                    fontSize  = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            // ── Input Fields ───────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = phoneOrEmail,
                    onValueChange = { phoneOrEmail = it },
                    placeholder   = { Text("enter phone number or email", color = Color.White.copy(0.7f)) },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = outlinedFieldColors(),
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction    = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value                  = password,
                    onValueChange          = { password = it },
                    placeholder            = { Text("enter your password", color = Color.White.copy(0.7f)) },
                    modifier               = Modifier.fillMaxWidth(),
                    shape                  = RoundedCornerShape(12.dp),
                    colors                 = outlinedFieldColors(),
                    singleLine             = true,
                    visualTransformation   = PasswordVisualTransformation(),
                    keyboardOptions        = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions        = KeyboardActions(onDone = { onLoginSuccess() })
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Buttons ────────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick  = { if (phoneOrEmail.isNotBlank()) onLoginSuccess() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.25f)
                    )
                ) {
                    Text("Log in with Phone", color = Color.White)
                }

                OutlinedButton(
                    onClick  = { if (phoneOrEmail.isNotBlank()) onLoginSuccess() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape  = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(0.6f))
                ) {
                    Text("G  Log in with Google", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Register Link ──────────────────────────────────────────────────
            TextButton(
                onClick  = onNavigateToRegister,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text  = "Don't have an account? Register",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor      = Color.White,
    unfocusedTextColor    = Color.White,
    focusedBorderColor    = Color.White,
    unfocusedBorderColor  = Color.White.copy(alpha = 0.4f),
    cursorColor           = Color.White,
    focusedContainerColor = Color.White.copy(alpha = 0.2f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.2f)
)
