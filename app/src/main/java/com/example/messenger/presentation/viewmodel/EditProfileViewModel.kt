package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
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
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private companion object {
        const val KEY_LOADED = "edit_profile_loaded"
        const val KEY_NAME = "edit_profile_name"
        const val KEY_USERNAME = "edit_profile_username"
        const val KEY_PHONE = "edit_profile_phone"
        const val KEY_DOB = "edit_profile_dob"
    }

    private val _uiState = MutableStateFlow(
        EditProfileUiState(
            name = savedStateHandle[KEY_NAME] ?: "",
            username = savedStateHandle[KEY_USERNAME] ?: "",
            phone = savedStateHandle[KEY_PHONE] ?: "",
            dob = savedStateHandle[KEY_DOB] ?: "",
        ),
    )
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {

        if (savedStateHandle.get<Boolean>(KEY_LOADED) != true) {
            loadProfile()
        }
    }

    private fun loadProfile() {
        val uid = firebaseAuthService.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = userRepository.getUserById(uid)
            val user = result.getOrNull()
            val phoneFromAuth = firebaseAuthService.getUserPhoneNumber().orEmpty()
            val phoneFromProfile = user?.phoneNumber?.getFullNumber().orEmpty()
            val name = user?.username.orEmpty()
            val username = user?.username.orEmpty()
            val phone = phoneFromProfile.ifBlank { phoneFromAuth }

            savedStateHandle[KEY_NAME] = name
            savedStateHandle[KEY_USERNAME] = username
            savedStateHandle[KEY_PHONE] = phone
            savedStateHandle[KEY_LOADED] = true

            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = name,
                    username = username,
                    phone = phone,
                )
            }
        }
    }

    fun onNameChange(value: String) {
        savedStateHandle[KEY_NAME] = value
        _uiState.update { it.copy(name = value) }
    }

    fun onPhoneChange(value: String) {
        savedStateHandle[KEY_PHONE] = value
        _uiState.update { it.copy(phone = value) }
    }

    fun onUsernameChange(value: String) {
        savedStateHandle[KEY_USERNAME] = value
        _uiState.update { it.copy(username = value) }
    }

    fun onDobChange(value: String) {
        savedStateHandle[KEY_DOB] = value
        _uiState.update { it.copy(dob = value) }
    }

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
                    savedStateHandle[KEY_NAME] = state.username
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
