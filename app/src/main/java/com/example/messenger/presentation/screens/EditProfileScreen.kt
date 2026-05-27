package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.presentation.screens.ui.theme.DangerRed
import com.example.messenger.presentation.screens.ui.theme.LightGray
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.state.EditProfileUiState
import com.example.messenger.presentation.viewmodel.EditProfileViewModel

@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSaved: () -> Unit = {},
    onLogout: () -> Unit = {},
    onChangeAccount: () -> Unit = {},
    onChangePhone: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.onSaveNavigated()
            onSaved()
        }
    }

    EditProfileScreenContent(
        state = state,
        onBackClick = onBackClick,
        onNameChange = viewModel::onNameChange,
        onUsernameChange = viewModel::onUsernameChange,
        onDobChange = viewModel::onDobChange,
        onSaveClick = viewModel::save,
        onLogoutClick = onLogout,
        onChangeAccountClick = onChangeAccount,
        onChangePhoneClick = onChangePhone,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileScreenContent(
    state: EditProfileUiState,
    onBackClick: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onUsernameChange: (String) -> Unit = {},
    onDobChange: (String) -> Unit = {},
    onSaveClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onChangeAccountClick: () -> Unit = {},
    onChangePhoneClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.shadow(elevation = 4.dp),
                title = { Text("Edit profile", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = PrimaryBlue),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(LightGray),
                    contentAlignment = Alignment.Center,
                ) {
                    val initial = state.name.take(1).uppercase()
                    if (initial.isNotBlank()) {
                        Text(initial, color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 36.sp)
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(48.dp))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = {}, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Change photo", color = Color.Gray, fontSize = 13.sp)
            }
            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BlueTextField(state.name, onNameChange, "Profile name")
                ReadOnlyPhoneField(
                    phone = state.phone,
                    onChangeClick = onChangePhoneClick,
                )
                BlueTextField(state.username, onUsernameChange, "Username")
                BlueTextField(state.dob, onDobChange, "Date of birth")

                Button(
                    onClick = onChangeAccountClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Change account", color = Color.White)
                        Text("+", color = Color.White.copy(0.7f), fontSize = 20.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (state.error != null) {
                Text(state.error, color = DangerRed, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                enabled = !state.isSaving,
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Confirm changes", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DangerRed),
            ) {
                Text("Log out", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ReadOnlyPhoneField(
    phone: String,
    onChangeClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryBlue, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Phone number",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
            )
            Text(
                text = phone.ifBlank { "(not set)" },
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        TextButton(onClick = onChangeClick) {
            Text("Change", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun BlueTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryBlue, RoundedCornerShape(12.dp))
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.White.copy(0.6f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White,
            ),
            singleLine = true,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditProfileScreenPreview() {
    MessengerTheme {
        EditProfileScreenContent(
            state = EditProfileUiState(
                name = "Alice",
                phone = "+1 555 0100",
                username = "alice",
                dob = "1995-04-15",
            ),
        )
    }
}
