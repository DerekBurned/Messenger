package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.data.presence.PresenceManager
import com.example.messenger.domain.repository.IUserRepository
import com.example.messenger.domain.usecase.auth.GetCurrentUserUseCase
import com.example.messenger.domain.usecase.auth.LogoutUseCase
import com.example.messenger.domain.usecase.user.GetUserByIdUseCase
import com.example.messenger.presentation.state.ProfileUiState
import com.example.messenger.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val presenceManager: PresenceManager,
    private val userRepository: IUserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val authUser = getCurrentUserUseCase()
        if (authUser == null) {
            _uiState.update { it.copy(isLoading = false, user = null) }
            return
        }
        
        _uiState.update { it.copy(isLoading = true, user = authUser) }
        viewModelScope.launch {
            getUserByIdUseCase(authUser.id).collect { resource ->
                when (resource) {
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, user = resource.data ?: authUser)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, error = resource.message)
                    }
                    is Resource.Failure -> _uiState.update {
                        it.copy(isLoading = false, error = resource.exception.message)
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            presenceManager.disconnect(viewModelScope)
            logoutUseCase()
            _uiState.update { ProfileUiState() }
        }
    }

    fun startEditing() {
        _uiState.update { it.copy(isEditing = true) }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(isEditing = false) }
    }

    fun updateProfile(username: String) {
        if (username.isBlank()) {
            _uiState.update { it.copy(error = "Username cannot be empty") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = userRepository.updateUserProfile(mapOf("username" to username))
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditing = false,
                            updateSuccess = true,
                            user = it.user?.copy(username = username)
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Update failed")
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
