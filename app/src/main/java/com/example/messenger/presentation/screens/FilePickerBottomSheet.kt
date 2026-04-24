package com.example.messenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messenger.presentation.screens.ui.theme.*

/**
 * data-figma-name: file_picker_modal
 * Нижний лист выбора медиа / файлов для отправки в чате.
 */

private data class MockPhoto(val id: Int, val color: Color)
private data class MockVideo(val id: Int, val color: Color, val duration: String)
private data class MockFile(val id: Int, val name: String, val size: String)

private val mockPhotos = listOf(
    MockPhoto(1, Color(0xFFa8d8ea)), MockPhoto(2, Color(0xFFb8e0d2)),
    MockPhoto(3, Color(0xFFffd3b6)), MockPhoto(4, Color(0xFFd6e4f0)),
    MockPhoto(5, Color(0xFFf7d6e0)), MockPhoto(6, Color(0xFFe8d5c4)),
    MockPhoto(7, Color(0xFFc9e4ca)), MockPhoto(8, Color(0xFFe0c3fc))
)

private val mockVideos = listOf(
    MockVideo(1, Color(0xFF2c3e50), "0:32"), MockVideo(2, Color(0xFF16213e), "1:14"),
    MockVideo(3, Color(0xFF0f3460), "2:05"), MockVideo(4, Color(0xFF1a1a2e), "0:45"),
    MockVideo(5, Color(0xFF2c2c54), "3:22"), MockVideo(6, Color(0xFF141414), "0:18"),
    MockVideo(7, Color(0xFF2d4059), "1:55"), MockVideo(8, Color(0xFF393e46), "4:00")
)

private val mockFiles = listOf(
    MockFile(1, "document.pdf",       "2.4 MB"),
    MockFile(2, "presentation.pptx",  "5.1 MB"),
    MockFile(3, "notes.txt",          "12 KB"),
    MockFile(4, "audio.mp3",          "3.8 MB"),
    MockFile(5, "report.docx",        "1.2 MB"),
    MockFile(6, "spreadsheet.xlsx",   "800 KB")
)

private enum class FileTab { PHOTO, VIDEO, FILE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePickerBottomSheet(
    onDismiss: () -> Unit,
    onSelectFile: (type: String, name: String) -> Unit
) {
    var activeTab by remember { mutableStateOf(FileTab.PHOTO) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Заголовок с вкладками
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FileTab.values().forEach { tab ->
                        val isActive = activeTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isActive) BluePrimary else GrayBg)
                                .clickable { activeTab = tab }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text     = tab.name.lowercase(),
                                color    = if (isActive) Color.White else TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            HorizontalDivider(color = GrayDivider)

            // Контент вкладок
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                when (activeTab) {
                    FileTab.PHOTO -> PhotoGrid(
                        photos = mockPhotos,
                        onCameraClick = { onSelectFile("camera", "camera"); onDismiss() },
                        onPhotoClick  = { photo -> onSelectFile("photo", "photo_${photo.id}"); onDismiss() }
                    )
                    FileTab.VIDEO -> VideoGrid(
                        videos = mockVideos,
                        onCameraClick = { onSelectFile("camera", "camera"); onDismiss() },
                        onVideoClick  = { video -> onSelectFile("video", "video_${video.id}"); onDismiss() }
                    )
                    FileTab.FILE -> FileList(
                        files = mockFiles,
                        onFileClick = { file -> onSelectFile("file", file.name); onDismiss() }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PhotoGrid(
    photos: List<MockPhoto>,
    onCameraClick: () -> Unit,
    onPhotoClick: (MockPhoto) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(2.dp),
        verticalArrangement   = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Камера
        item {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(GrayBg)
                    .clickable(onClick = onCameraClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = TextSecondary, modifier = Modifier.size(32.dp))
            }
        }
        items(photos, key = { it.id }) { photo ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(photo.color)
                    .clickable { onPhotoClick(photo) }
            )
        }
    }
}

@Composable
private fun VideoGrid(
    videos: List<MockVideo>,
    onCameraClick: () -> Unit,
    onVideoClick: (MockVideo) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(2.dp),
        verticalArrangement   = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(GrayBg)
                    .clickable(onClick = onCameraClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Videocam, contentDescription = "Camera", tint = TextSecondary, modifier = Modifier.size(32.dp))
            }
        }
        items(videos, key = { it.id }) { video ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(video.color)
                    .clickable { onVideoClick(video) }
            ) {
                Text(
                    text     = video.duration,
                    color    = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(0.5f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun FileList(
    files: List<MockFile>,
    onFileClick: (MockFile) -> Unit
) {
    Column {
        files.forEach { file ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFileClick(file) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BluePrimary.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = BluePrimary)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(file.name,  color = TextPrimary,   fontSize = 14.sp)
                    Text(file.size,  color = TextSecondary, fontSize = 12.sp)
                }
            }
            HorizontalDivider(color = GrayDivider, thickness = 0.5.dp)
        }
    }
}
