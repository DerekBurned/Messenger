package com.example.messenger.presentation.state

import androidx.compose.ui.graphics.Color

enum class FileTab { PHOTO, VIDEO, FILE }

data class PhotoItem(val id: Int, val color: Color)
data class VideoItem(val id: Int, val color: Color, val duration: String)
data class FileItem(val id: Int, val name: String, val size: String)

data class FilePickerUiState(
    val activeTab: FileTab = FileTab.PHOTO,
    val photos: List<PhotoItem> = emptyList(),
    val videos: List<VideoItem> = emptyList(),
    val files: List<FileItem> = emptyList(),
)
