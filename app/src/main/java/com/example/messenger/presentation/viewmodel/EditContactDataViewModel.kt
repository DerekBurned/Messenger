package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.domain.usecase.user.GetUserByIdUseCase
import com.example.messenger.presentation.state.EditContactDataUiState
import com.example.messenger.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditContactDataViewModel @Inject constructor(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditContactDataUiState())
    val uiState: StateFlow<EditContactDataUiState> = _uiState.asStateFlow()

    init {
        val contactId: String? = savedStateHandle["contactId"]
        if (!contactId.isNullOrBlank()) load(contactId)
    }

    private fun load(contactId: String) {
        viewModelScope.launch {
            getUserByIdUseCase(contactId).collect { resource ->
                if (resource is Resource.Success) {
                    val name = resource.data?.username.orEmpty()
                    _uiState.update {
                        it.copy(contactId = contactId, name = name, initialName = name)
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun showDeleteConfirm() = _uiState.update { it.copy(showDeleteConfirm = true) }
    fun dismissDeleteConfirm() = _uiState.update { it.copy(showDeleteConfirm = false) }

    fun save() {
        
        _uiState.update { it.copy(saveSuccess = true) }
    }

    fun delete() {
        _uiState.update { it.copy(showDeleteConfirm = false, deleted = true) }
    }
}
