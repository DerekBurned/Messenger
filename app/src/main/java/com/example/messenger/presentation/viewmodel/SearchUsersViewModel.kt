package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.domain.model.User
import com.example.messenger.domain.usecase.conversation.CreateConversationUseCase
import com.example.messenger.domain.usecase.user.SearchUsersUseCase
import com.example.messenger.presentation.state.SearchUsersUiState
import com.example.messenger.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchUsersViewModel @Inject constructor(
    private val searchUsersUseCase: SearchUsersUseCase,
    private val createConversationUseCase: CreateConversationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUsersUiState())
    val uiState: StateFlow<SearchUsersUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun searchUsers(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(users = emptyList(), isLoading = false, error = null) }
            return
        }
        searchJob = viewModelScope.launch {
            searchUsersUseCase(query).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> {
                        @Suppress("UNCHECKED_CAST")
                        val users = resource.data as? List<User> ?: emptyList()
                        _uiState.update { it.copy(isLoading = false, users = users, error = null) }
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = resource.message) }
                    is Resource.Failure -> _uiState.update { it.copy(isLoading = false, error = resource.exception.message) }
                }
            }
        }
    }

    fun createConversationWithUser(user: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingConversation = true) }
            val result = createConversationUseCase(user.id)
            result.fold(
                onSuccess = { conversation ->
                    _uiState.update {
                        it.copy(
                            isCreatingConversation = false,
                            createdConversation = conversation
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isCreatingConversation = false,
                            error = e.message ?: "Failed to create conversation"
                        )
                    }
                }
            )
        }
    }

    fun onConversationNavigated() {
        _uiState.update { it.copy(createdConversation = null) }
    }
}
