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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.messenger.presentation.screens.ui.theme.LightGray
import com.example.messenger.presentation.screens.ui.theme.MessengerTheme
import com.example.messenger.presentation.screens.ui.theme.PrimaryBlue
import com.example.messenger.presentation.state.FileItem
import com.example.messenger.presentation.state.FilePickerUiState
import com.example.messenger.presentation.state.FileTab
import com.example.messenger.presentation.state.PhotoItem
import com.example.messenger.presentation.state.VideoItem
import com.example.messenger.presentation.viewmodel.FilePickerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePickerBottomSheet(
    onDismiss: () -> Unit,
    onSelectFile: (type: String, name: String) -> Unit,
    viewModel: FilePickerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FilePickerBottomSheetContent(
        state = state,
        onDismiss = onDismiss,
        onTabChange = viewModel::onTabChange,
        onSelectFile = onSelectFile,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilePickerBottomSheetContent(
    state: FilePickerUiState,
    onDismiss: () -> Unit,
    onTabChange: (FileTab) -> Unit,
    onSelectFile: (type: String, name: String) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FileTab.entries.forEach { tab ->
                        val isActive = state.activeTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isActive) PrimaryBlue else LightGray)
                                .clickable { onTabChange(tab) }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = tab.name.lowercase(),
                                color = if (isActive) Color.White else Color.Gray,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            HorizontalDivider(color = LightGray)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
            ) {
                when (state.activeTab) {
                    FileTab.PHOTO -> PhotoGrid(
                        photos = state.photos,
                        onCameraClick = { onSelectFile("camera", "camera"); onDismiss() },
                        onPhotoClick = { onSelectFile("photo", "photo_${it.id}"); onDismiss() },
                    )
                    FileTab.VIDEO -> VideoGrid(
                        videos = state.videos,
                        onCameraClick = { onSelectFile("camera", "camera"); onDismiss() },
                        onVideoClick = { onSelectFile("video", "video_${it.id}"); onDismiss() },
                    )
                    FileTab.FILE -> FileList(
                        files = state.files,
                        onFileClick = { onSelectFile("file", it.name); onDismiss() },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PhotoGrid(
    photos: List<PhotoItem>,
    onCameraClick: () -> Unit,
    onPhotoClick: (PhotoItem) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(LightGray)
                    .clickable(onClick = onCameraClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.Gray, modifier = Modifier.size(32.dp))
            }
        }
        items(photos, key = { it.id }) { photo ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(photo.color)
                    .clickable { onPhotoClick(photo) },
            )
        }
    }
}

@Composable
private fun VideoGrid(
    videos: List<VideoItem>,
    onCameraClick: () -> Unit,
    onVideoClick: (VideoItem) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(LightGray)
                    .clickable(onClick = onCameraClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Videocam, contentDescription = "Camera", tint = Color.Gray, modifier = Modifier.size(32.dp))
            }
        }
        items(videos, key = { it.id }) { video ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(video.color)
                    .clickable { onVideoClick(video) },
            ) {
                Text(
                    text = video.duration,
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(0.5f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 3.dp, vertical = 1.dp),
                )
            }
        }
    }
}

@Composable
private fun FileList(
    files: List<FileItem>,
    onFileClick: (FileItem) -> Unit,
) {
    Column {
        files.forEach { file ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFileClick(file) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = PrimaryBlue)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(file.name, color = Color.Black, fontSize = 14.sp)
                    Text(file.size, color = Color.Gray, fontSize = 12.sp)
                }
            }
            HorizontalDivider(color = LightGray, thickness = 0.5.dp)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilePickerBottomSheetPreview() {
    MessengerTheme {
        
        Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
            Text("FilePickerBottomSheet (preview stub)", modifier = Modifier.padding(16.dp))
        }
    }
}
