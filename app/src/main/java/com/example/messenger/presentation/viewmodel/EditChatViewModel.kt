package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.domain.usecase.conversation.DeleteConversationUseCase
import com.example.messenger.domain.usecase.conversation.ObserveConversationsUseCase
import com.example.messenger.presentation.state.EditChatUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditChatViewModel @Inject constructor(
    private val observeConversationsUseCase: ObserveConversationsUseCase,
    private val deleteConversationUseCase: DeleteConversationUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditChatUiState())
    val uiState: StateFlow<EditChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeConversationsUseCase()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { list ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            conversations = list,
                            
                            selectedIds = if (it.selectedIds.isEmpty()) list.map { c -> c.id }.toSet() else it.selectedIds,
                        )
                    }
                }
        }
    }

    fun toggle(id: String) {
        _uiState.update {
            val next = it.selectedIds.toMutableSet().apply {
                if (contains(id)) remove(id) else add(id)
            }
            it.copy(selectedIds = next)
        }
    }

    fun deleteSelected(onComplete: () -> Unit = {}) {
        val ids = _uiState.value.selectedIds.toList()
        viewModelScope.launch {
            ids.forEach { deleteConversationUseCase(it) }
            onComplete()
        }
    }

    fun markAllRead(onComplete: () -> Unit = {}) {
        
        onComplete()
    }
}
