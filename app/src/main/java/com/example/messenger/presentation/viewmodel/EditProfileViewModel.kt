package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.data.remote.auth.FirebaseAuthService
import com.example.messenger.domain.repository.IUserRepository
import com.example.messenger.presentation.state.EditProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    private val firebaseAuthService: FirebaseAuthService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val uid = firebaseAuthService.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = userRepository.getUserById(uid)
            val user = result.getOrNull()
            val phoneFromAuth = firebaseAuthService.getUserPhoneNumber().orEmpty()
            val phoneFromProfile = user?.phoneNumber?.getFullNumber().orEmpty()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = user?.username.orEmpty(),
                    username = user?.username.orEmpty(),
                    phone = phoneFromProfile.ifBlank { phoneFromAuth },
                )
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value) }
    fun onUsernameChange(value: String) = _uiState.update { it.copy(username = value) }
    fun onDobChange(value: String) = _uiState.update { it.copy(dob = value) }

    fun save() {
        val state = _uiState.value
        if (state.username.isBlank()) {
            _uiState.update { it.copy(error = "Username cannot be empty") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val updates = mutableMapOf<String, Any>("username" to state.username)
            val result = userRepository.updateUserProfile(updates)
            result.fold(
                onSuccess = {
                    
                    firebaseAuthService.updateProfile(displayName = state.username, photoUrl = null)
                    _uiState.update {
                        it.copy(isSaving = false, saveSuccess = true, name = state.username)
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message ?: "Save failed") }
                },
            )
        }
    }

    fun onSaveNavigated() = _uiState.update { it.copy(saveSuccess = false) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}
