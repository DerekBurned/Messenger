package com.example.messenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.data.repository.CallHistoryRepository
import com.example.messenger.domain.model.CallHistoryEntry
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CallsViewModel @Inject constructor(
    callHistoryRepository: CallHistoryRepository,
    firebaseAuth: FirebaseAuth,
) : ViewModel() {

    val calls: StateFlow<List<CallHistoryEntry>> =
        callHistoryRepository.getCallHistory(firebaseAuth.currentUser?.uid.orEmpty())
            .catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
