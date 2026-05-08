package com.example.messenger.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.messenger.presentation.state.FilePickerUiState
import com.example.messenger.presentation.state.FileItem
import com.example.messenger.presentation.state.FileTab
import com.example.messenger.presentation.state.PhotoItem
import com.example.messenger.presentation.state.VideoItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class FilePickerViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(
        FilePickerUiState(
            photos = stubPhotos,
            videos = stubVideos,
            files = stubFiles,
        )
    )
    val uiState: StateFlow<FilePickerUiState> = _uiState.asStateFlow()

    fun onTabChange(tab: FileTab) = _uiState.update { it.copy(activeTab = tab) }

    private companion object {
        val stubPhotos = listOf(
            PhotoItem(1, Color(0xFFA8D8EA)),
            PhotoItem(2, Color(0xFFB8E0D2)),
            PhotoItem(3, Color(0xFFFFD3B6)),
            PhotoItem(4, Color(0xFFD6E4F0)),
            PhotoItem(5, Color(0xFFF7D6E0)),
            PhotoItem(6, Color(0xFFE8D5C4)),
        )
        val stubVideos = listOf(
            VideoItem(1, Color(0xFF2C3E50), "0:32"),
            VideoItem(2, Color(0xFF16213E), "1:14"),
            VideoItem(3, Color(0xFF0F3460), "2:05"),
        )
        val stubFiles = listOf(
            FileItem(1, "document.pdf", "2.4 MB"),
            FileItem(2, "notes.txt", "12 KB"),
            FileItem(3, "report.docx", "1.2 MB"),
        )
    }
}
