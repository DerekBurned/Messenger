package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.domain.usecase.conversation.ClearConversationForMeUseCase
import com.example.messenger.domain.usecase.conversation.CreateConversationUseCase
import com.example.messenger.domain.usecase.conversation.DeleteConversationForEveryoneUseCase
import com.example.messenger.domain.usecase.conversation.DeleteConversationUseCase
import com.example.messenger.domain.usecase.conversation.GetConversationsUseCase
import com.example.messenger.domain.usecase.conversation.SyncConversationsUseCase
import com.example.messenger.domain.usecase.presence.ObserveUserPresenceUseCase
import com.example.messenger.domain.repository.IUserRepository
import com.example.messenger.presentation.base.toUiText
import com.example.messenger.presentation.state.ConversationsUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val deleteConversationUseCase: DeleteConversationUseCase,
    private val deleteConversationForEveryoneUseCase: DeleteConversationForEveryoneUseCase,
    private val clearConversationForMeUseCase: ClearConversationForMeUseCase,
    private val createConversationUseCase: CreateConversationUseCase,
    private val syncConversationsUseCase: SyncConversationsUseCase,
    private val observeUserPresenceUseCase: ObserveUserPresenceUseCase,
    private val userRepository: IUserRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ConversationsUiState(currentUserId = firebaseAuth.currentUser?.uid.orEmpty()),
    )
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    private var presenceJob: Job? = null

    init {
        loadConversations()
        observeAliases()
    }

    private fun observeAliases() {
        viewModelScope.launch {
            runCatching { userRepository.refreshContactAliases() }
            userRepository.observeContactAliases().collect { aliases ->
                _uiState.update { it.copy(aliases = aliases) }
            }
        }
    }

    private fun loadConversations() {
        viewModelScope.launch {
            getConversationsUseCase()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message?.toUiText()) } }
                .collect { conversations ->
                    _uiState.update {
                        it.copy(isLoading = false, conversations = conversations, error = null)
                    }
                    observeParticipantPresence(conversations)
                }
        }
    }

    private fun observeParticipantPresence(conversations: List<com.example.messenger.domain.model.Conversation>) {
        presenceJob?.cancel()
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        val otherUserIds = conversations
            .flatMap { it.participantIds }
            .filter { it != currentUserId }
            .distinct()

        if (otherUserIds.isEmpty()) return

        presenceJob = viewModelScope.launch {
            observeUserPresenceUseCase.observeMultiple(otherUserIds)
                .catch {  }
                .collect { presenceMap ->
                    _uiState.update { it.copy(presenceMap = presenceMap) }
                }
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = deleteConversationUseCase(conversationId)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message?.toUiText()) } }
            )
        }
    }

    fun deleteForMe(conversationId: String) {
        viewModelScope.launch {
            val result = clearConversationForMeUseCase(conversationId)
            result.onFailure { e -> _uiState.update { it.copy(error = e.message?.toUiText()) } }
        }
    }

    fun deleteForEveryone(conversationId: String) {
        viewModelScope.launch {
            val result = deleteConversationForEveryoneUseCase(conversationId)
            result.onFailure { e -> _uiState.update { it.copy(error = e.message?.toUiText()) } }
        }
    }

    fun createConversation(participantId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = createConversationUseCase(participantId)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message?.toUiText()) } }
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            syncConversationsUseCase()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
